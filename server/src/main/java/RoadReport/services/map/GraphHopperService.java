package RoadReport.services.map;

import com.graphhopper.util.JsonFeature;
import org.jspecify.annotations.NonNull;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import RoadReport.entities.Report;
import RoadReport.services.map.RiskAnalysisService.WeightedReport;
import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.ResponsePath;
import com.graphhopper.config.Profile;
import com.graphhopper.json.Statement;
import com.graphhopper.util.CustomModel;
import com.graphhopper.util.Parameters;
import com.graphhopper.util.PointList;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

@Service
@RequiredArgsConstructor
@Slf4j
public class GraphHopperService {
    /**
     * Default routing profile. necessary for graphhopper.
     */
    private static final String PROFILE = "car";

    /**
     * The radius where speed penalty will be applied.
     * roughly 22 meters.
     */
    private static final double REPORT_LOOKUP_RADIUS = 0.00008;


    @Value("${graphhopper.osm-file}")
    private String osmFilePath;

    @Value("${graphhopper.graph-cache}")
    private String graphCachePath;

    private final RiskAnalysisService riskAnalysisService;
    private GraphHopper hopper;

    /**
     * Initializes the GraphHopper instance after the bean is constructed.
     * Sets up the OpenStreetMap file, cache location, and the vehicle profile,
     * then imports or loads the routing graph.
     */
    @PostConstruct
    public void init() {
        hopper = new GraphHopper();
        hopper.setOSMFile(osmFilePath);
        hopper.setGraphHopperLocation(graphCachePath);
        CustomModel customModel = new CustomModel();
        customModel.addToSpeed(Statement.If("true", Statement.Op.LIMIT, "45"));
        hopper.setProfiles(new Profile(PROFILE).setCustomModel(customModel));
        hopper.importOrLoad();

    }
    /**
     * Calculates the optimal route between a  start and end point,
     *
     * @param fromLat The starting latitude.
     * @param fromLon The starting longitude.
     * @param toLat   The destination latitude.
     * @param toLon   The destination longitude.
     * @param reports A list of {@link Report} entities representing hazards or incidents.
     * @return A {@link RouteResult} containing the total distance, time, and path coordinates.
     * @throws IllegalStateException if GraphHopper encounters an error during routing.
     */
    public RouteResult getRoute(double fromLat, double fromLon,
                                double toLat,   double toLon,
                                List<Report> reports){
            List<WeightedReport> weightedReports = prepareWeights(reports);
            GHResponse response = hopper.route(buildRequest(fromLat, fromLon, toLat, toLon, weightedReports));
            assertNoErrors(response);
            return toRouteResult(response);
    }
    /**
     * Calculates a multi-segment route passing through a sequence of waypoints.
     * The routing for each segment between waypoints is executed asynchronously
     * in parallel to improve performance.
     *
     * @param waypoints A list of coordinate arrays (each containing [latitude, longitude])
     * @param reports   A list of {@link Report} entities representing hazards or incidents.
     * @return A {@link RouteResult} containing the aggregated distance, time, and sequential coordinates for the entire journey.
     * @throws IllegalArgumentException if fewer than 2 waypoints are provided.
     * @throws IllegalStateException if any segment fails during asynchronous routing.
     */
    public RouteResult getRouteViaWaypoints(List<double[]> waypoints, List<Report> reports) {
        if (waypoints == null || waypoints.size() < 2) {
            throw new IllegalArgumentException("min 2 points required");
        }
        final ClassLoader springClassLoader = Thread.currentThread().getContextClassLoader();

        List<WeightedReport> weighted = prepareWeights(reports);
        List<CompletableFuture<ResponsePath>> futures = new ArrayList<>();
        for (int i = 0; i < waypoints.size() - 1; i++) {
            final int idx = i;
            double fromLat = waypoints.get(i)[0],     fromLon = waypoints.get(i)[1];
            double toLat   = waypoints.get(i + 1)[0], toLon   = waypoints.get(i + 1)[1];

            CompletableFuture<com.graphhopper.ResponsePath> future = CompletableFuture.supplyAsync(() -> {
                Thread.currentThread().setContextClassLoader(springClassLoader);
                GHResponse response = hopper.route(buildRequest(fromLat, fromLon, toLat, toLon, weighted));
                assertNoErrors(response);
                return response.getBest();
            });
            futures.add(future);
        }


        double totalDistance = 0;
        long   totalTime     = 0;
        List<double[]> fullPoints = new ArrayList<>();
        for (int i = 0; i < futures.size(); i++) {
            com.graphhopper.ResponsePath best;
            try {
                best = futures.get(i).join();
            } catch (CompletionException e) {
                throw new IllegalStateException(e.getCause().getMessage(), e.getCause());
            }

            PointList points = best.getPoints();
            totalDistance += best.getDistance();
            totalTime     += best.getTime();

            int start = (i == 0) ? 0 : 1;
            for (int j = start; j < points.size(); j++) {
                fullPoints.add(new double[]{points.getLat(j), points.getLon(j)});
            }
        }
        return new RouteResult(totalDistance, totalTime, fullPoints);
    }



    /**
     * Filters the raw reports and assigns risk multipliers to them.
     *
     * @param reports The raw list of reports.
     * @return A list of {@link WeightedReport} objects containing calculated multipliers.
     */
    private List<WeightedReport> prepareWeights(List<Report> reports) {
        List<Report> filtered = riskAnalysisService.filterReports(reports);
        return riskAnalysisService.adjustReportWeights(filtered);
    }

    /**
     * Builds a standard {@link GHRequest} using the A* Bi-directional algorithm
     * and applies any custom model rules derived from risk reports.
     *
     * @param fromLat         The starting latitude.
     * @param fromLon         The starting longitude.
     * @param toLat           The destination latitude.
     * @param toLon           The destination longitude.
     * @param weightedReports The parsed reports used to apply custom routing penalties.
     * @return A configured {@link GHRequest}.
     */
    private GHRequest buildRequest(double fromLat, double fromLon,
                                   double toLat, double toLon,
                                   List<WeightedReport> weightedReports) {
        GHRequest req = new GHRequest(fromLat, fromLon, toLat, toLon);
        req.setProfile(PROFILE);
        req.setAlgorithm(Parameters.Algorithms.ASTAR_BI);
        applyRiskToRequest(req, weightedReports);
        return req;
    }

    /**
     * Injects a Custom Model into the GraphHopper request. This custom model dynamically
     * alters the routing speed based on the bounding boxes generated from the reports.
     *
     * @param request         The {@link GHRequest} to mutate.
     * @param weightedReports The reports dictating the speed multiplication rules.
     */
    private void applyRiskToRequest(GHRequest request, List<WeightedReport> weightedReports) {
        if (weightedReports.isEmpty()) return;

        CustomModel model = new CustomModel();
        model.addToSpeed(Statement.If("true", Statement.Op.LIMIT, "45"));

        GeometryFactory geometryFactory = new GeometryFactory();
        int areaCounter = 1;

        for (WeightedReport wr : weightedReports) {
            String areaId = "report_area_" + areaCounter++;

            Coordinate[] coordinates = getCoordinates(wr);

            Polygon boundingBox = geometryFactory.createPolygon(coordinates);

            JsonFeature feature = new JsonFeature();
            feature.setId(areaId);
            feature.setGeometry(boundingBox);

            model.getAreas().getFeatures().add(feature);

            String condition = "in_" + areaId;
            model.addToSpeed(Statement.If(condition, Statement.Op.MULTIPLY, String.valueOf(wr.multiplier())));
        }

        request.setCustomModel(model);
    }

    private static Coordinate @NonNull [] getCoordinates(WeightedReport wr) {
        double lat = wr.report().getLatitude();
        double lon = wr.report().getLongitude();

        double minLat = lat - REPORT_LOOKUP_RADIUS;
        double maxLat = lat + REPORT_LOOKUP_RADIUS;
        double minLon = lon - REPORT_LOOKUP_RADIUS;
        double maxLon = lon + REPORT_LOOKUP_RADIUS;

        Coordinate[] coordinates = new Coordinate[] {
                new Coordinate(minLon, minLat),
                new Coordinate(maxLon, minLat),
                new Coordinate(maxLon, maxLat),
                new Coordinate(minLon, maxLat),
                new Coordinate(minLon, minLat)
        };
        return coordinates;
    }

    /**
     * Maps the best path from a {@link GHResponse} into the application's domain {@link RouteResult}.
     *
     * @param response The successful response from GraphHopper.
     * @return A newly constructed {@link RouteResult}.
     */
    private RouteResult toRouteResult(GHResponse response) {
        com.graphhopper.ResponsePath best = response.getBest();
        PointList points = best.getPoints();
        List<double[]> coords = new ArrayList<>(points.size());
        for (int i = 0; i < points.size(); i++) {
            coords.add(new double[]{points.getLat(i), points.getLon(i)});
        }
        return new RouteResult(
                best.getDistance(),
                best.getTime(),
                coords
        );
    }
    /**
     * Validates a GraphHopper response, throwing an exception if routing failed.
     *
     * @param response The response to validate.
     * @throws IllegalStateException if the response contains errors (e.g., route not found).
     */
    private void assertNoErrors(GHResponse response) {
        if (response.hasErrors()) {
            throw new IllegalStateException("GHResponse errors: " + response.getErrors());
        }
    }


    /**
     * A record encapsulating the result of a successful route calculation.
     *
     * @param distanceMeters The total geographical distance of the route in meters.
     * @param timeMillis     The estimated total travel time in milliseconds.
     * @param points         A list of coordinate arrays [latitude, longitude] making up the route polyline.
     */

        public record RouteResult(
            double distanceMeters,
            long   timeMillis,
            List<double[]> points
    ) {}


}
