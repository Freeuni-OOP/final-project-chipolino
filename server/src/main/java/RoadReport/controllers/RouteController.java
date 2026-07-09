package RoadReport.controllers;

import RoadReport.controllers.dto.route.RouteRequestDTO;
import RoadReport.controllers.dto.route.RouteResponseDTO;
import RoadReport.entities.Report;
import RoadReport.services.core.ReportService;
import RoadReport.services.map.GraphHopperService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/routes")
public class RouteController {
    private final GraphHopperService graphHopperService;
    private final ReportService reportService;

    /**
     * Calculates the optimal route based on a provided list of waypoints.
     *
     * @param request The data transfer object containing list of coordinate points.
     * @return A {@link RouteResponseDTO} object containing the total distance,
     * estimated travel time, and the list of path coordinates for the route.
     */
    @PostMapping("/calculate")
    public ResponseEntity<RouteResponseDTO> calculateOptimalRoute(@RequestBody RouteRequestDTO request) {
        List<Report> activeReports = reportService.getActiveReports();
        var result = graphHopperService.getRouteViaWaypoints(request.waypoints(), activeReports);
        RouteResponseDTO response = new RouteResponseDTO(
                result.distanceMeters(),
                result.timeMillis(),
                result.points()
        );
        return ResponseEntity.ok(response);
    }
}
