package RoadReport.controllers.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class RouteResponse {
    private double distanceMeters;
    private long timeMillis;
    private List<double[]> points;
}
