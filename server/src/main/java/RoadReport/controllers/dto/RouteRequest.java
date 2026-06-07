package RoadReport.controllers.dto;

import java.util.List;

public record RouteRequest(List<double[]> waypoints) {
}
