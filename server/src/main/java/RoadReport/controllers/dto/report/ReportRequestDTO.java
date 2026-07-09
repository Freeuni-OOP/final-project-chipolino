package RoadReport.controllers.dto.report;

import RoadReport.enums.ReportType;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public record ReportRequestDTO(
        @NotNull ReportType type,
        String description,
        @NotNull Double latitude,
        @NotNull Double longitude,
        Map<String, Object> attributes
) {}
