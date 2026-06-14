package RoadReport.controllers.dto.route;

import java.util.List;

public record RouteRequestDTO(List<double[]> waypoints) {
}
