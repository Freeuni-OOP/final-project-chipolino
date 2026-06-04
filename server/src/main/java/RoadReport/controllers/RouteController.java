package RoadReport.controllers;

import RoadReport.controllers.dto.RouteRequest;
import RoadReport.controllers.dto.RouteResponse;
import RoadReport.entities.Report;
import RoadReport.services.core.ReportService;
import RoadReport.services.map.GraphHopperService;
import lombok.RequiredArgsConstructor;
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

    @PostMapping("/calculate")
    public RouteResponse calculateOptimalRoute(@RequestBody RouteRequest request) {
        List<Report> activeReports = reportService.getActiveReports();
        var result = graphHopperService.getRouteViaWaypoints(request.getWaypoints(), activeReports);
        return RouteResponse.builder()
                .distanceMeters(result.distanceMeters())
                .timeMillis(result.timeMillis())
                .points(result.points())
                .build();
    }
}
