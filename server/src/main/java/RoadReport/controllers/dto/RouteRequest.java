package RoadReport.controllers.dto;

import lombok.Data;

import java.util.List;

@Data
public class RouteRequest {
    private List<double[]> waypoints;
}
