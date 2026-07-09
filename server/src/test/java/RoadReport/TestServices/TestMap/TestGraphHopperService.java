package RoadReport.TestServices.TestMap;

import RoadReport.entities.Report;
import RoadReport.services.map.GraphHopperService;
import RoadReport.services.map.RiskAnalysisService;
import RoadReport.services.map.RiskAnalysisService.WeightedReport;
import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.ResponsePath;
import com.graphhopper.util.PointList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TestGraphHopperService {

    @Mock
    private RiskAnalysisService riskAnalysisService;

    @Mock
    private GraphHopper hopper;

    @InjectMocks
    private GraphHopperService graphHopperService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(graphHopperService, "hopper", hopper);
    }

    @Test
    public void testRouteSuccess() {
        Report dummyReport = new Report();
        dummyReport.setLatitude(41.71);
        dummyReport.setLongitude(44.82);

        WeightedReport weightedReport = new WeightedReport(dummyReport, 0.5);
        List<Report> reports = List.of(dummyReport);

        when(riskAnalysisService.filterReports(reports)).thenReturn(reports);
        when(riskAnalysisService.adjustReportWeights(reports)).thenReturn(List.of(weightedReport));

        GHResponse mockResponse = mock(GHResponse.class);
        ResponsePath mockPath = mock(ResponsePath.class);
        PointList mockPointList = mock(PointList.class);

        when(hopper.route(any(GHRequest.class))).thenReturn(mockResponse);
        when(mockResponse.hasErrors()).thenReturn(false);
        when(mockResponse.getBest()).thenReturn(mockPath);
        when(mockPath.getDistance()).thenReturn(1500.0);
        when(mockPath.getTime()).thenReturn(300000L);
        when(mockPath.getPoints()).thenReturn(mockPointList);
        when(mockPointList.size()).thenReturn(2);
        when(mockPointList.getLat(0)).thenReturn(41.71);
        when(mockPointList.getLon(0)).thenReturn(44.82);
        when(mockPointList.getLat(1)).thenReturn(41.72);
        when(mockPointList.getLon(1)).thenReturn(44.83);

        GraphHopperService.RouteResult result = graphHopperService.getRoute(41.70, 44.80, 41.72, 44.83, reports);

        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(1500.0, result.distanceMeters()),
                () -> assertEquals(300000L, result.timeMillis()),
                () -> assertEquals(2, result.points().size()),
                () -> assertEquals(41.71, result.points().get(0)[0])
        );

        verify(hopper, times(1)).route(any(GHRequest.class));
    }

    @Test
    public void testRouteFails() {
        GHResponse mockResponse = mock(GHResponse.class);
        when(hopper.route(any(GHRequest.class))).thenReturn(mockResponse);
        when(mockResponse.hasErrors()).thenReturn(true);
        when(mockResponse.getErrors()).thenReturn(new ArrayList<>());

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            graphHopperService.getRoute(41.70, 44.80, 41.72, 44.83, new ArrayList<>());
        });

        assertTrue(exception.getMessage().contains("GHResponse errors"));
    }

    @Test
    public void testWaypointsSuccess() {
        List<double[]> waypoints = List.of(
                new double[]{41.70, 44.80},
                new double[]{41.71, 44.81},
                new double[]{41.72, 44.82}
        );

        GHResponse mockResponse = mock(GHResponse.class);
        ResponsePath mockPath = mock(ResponsePath.class);
        PointList mockPointList = mock(PointList.class);

        when(hopper.route(any(GHRequest.class))).thenReturn(mockResponse);
        when(mockResponse.hasErrors()).thenReturn(false);
        when(mockResponse.getBest()).thenReturn(mockPath);
        when(mockPath.getDistance()).thenReturn(1000.0);
        when(mockPath.getTime()).thenReturn(200000L);
        when(mockPath.getPoints()).thenReturn(mockPointList);
        when(mockPointList.size()).thenReturn(2);
        when(mockPointList.getLat(anyInt())).thenReturn(41.71);
        when(mockPointList.getLon(anyInt())).thenReturn(44.81);

        GraphHopperService.RouteResult result = graphHopperService.getRouteViaWaypoints(waypoints, new ArrayList<>());

        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(2000.0, result.distanceMeters()),
                () -> assertEquals(400000L, result.timeMillis()),
                () -> assertEquals(3, result.points().size())
        );

        verify(hopper, times(2)).route(any(GHRequest.class));
    }

    @Test
    public void testBadWaypoints() {
        assertThrows(IllegalArgumentException.class, () -> {
            graphHopperService.getRouteViaWaypoints(null, new ArrayList<>());
        });

        assertThrows(IllegalArgumentException.class, () -> {
            graphHopperService.getRouteViaWaypoints(List.of(new double[]{41.70, 44.80}), new ArrayList<>());
        });
    }

    @Test
    public void testWaypointsFails() {
        List<double[]> waypoints = List.of(
                new double[]{41.70, 44.80},
                new double[]{41.71, 44.81}
        );

        GHResponse mockResponse = mock(GHResponse.class);
        when(hopper.route(any(GHRequest.class))).thenReturn(mockResponse);
        when(mockResponse.hasErrors()).thenReturn(true);
        when(mockResponse.getErrors()).thenReturn(new ArrayList<>());

        assertThrows(IllegalStateException.class, () -> {
            graphHopperService.getRouteViaWaypoints(waypoints, new ArrayList<>());
        });
    }

    @Test
    public void testInitMethodCoverage() {
        ReflectionTestUtils.setField(graphHopperService, "osmFilePath", "dummy.osm.pbf");
        ReflectionTestUtils.setField(graphHopperService, "graphCachePath", "dummy-cache");

        assertThrows(RuntimeException.class, () -> {
            graphHopperService.init();
        });
    }

    @Test
    public void testRouteWithoutReports() {
        when(riskAnalysisService.filterReports(anyList())).thenReturn(new ArrayList<>());
        when(riskAnalysisService.adjustReportWeights(anyList())).thenReturn(new ArrayList<>());

        GHResponse mockResponse = mock(GHResponse.class);
        ResponsePath mockPath = mock(ResponsePath.class);
        PointList mockPointList = mock(PointList.class);

        when(hopper.route(any(GHRequest.class))).thenReturn(mockResponse);
        when(mockResponse.hasErrors()).thenReturn(false);
        when(mockResponse.getBest()).thenReturn(mockPath);
        when(mockPath.getDistance()).thenReturn(500.0);
        when(mockPath.getTime()).thenReturn(100000L);
        when(mockPath.getPoints()).thenReturn(mockPointList);
        when(mockPointList.size()).thenReturn(1);
        when(mockPointList.getLat(0)).thenReturn(41.71);
        when(mockPointList.getLon(0)).thenReturn(44.82);

        GraphHopperService.RouteResult result = graphHopperService.getRoute(41.70, 44.80, 41.72, 44.83, new ArrayList<>());

        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(500.0, result.distanceMeters()),
                () -> assertEquals(100000L, result.timeMillis()),
                () -> assertEquals(1, result.points().size())
        );

        verify(hopper, times(1)).route(any(GHRequest.class));
    }
}