package RoadReport.controllers.dto.route;

import java.util.List;

public record RouteResponseDTO(double distanceMeters,
                            long timeMillis,
                            List<double[]> points)
{}
