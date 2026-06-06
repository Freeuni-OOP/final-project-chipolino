package RoadReport.controllers.dto;

import java.util.List;

public record RouteResponse(double distanceMeters,
                            long timeMillis,
                            List<double[]> points)
{}
