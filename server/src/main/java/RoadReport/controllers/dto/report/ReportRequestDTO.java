package RoadReport.controllers.dto.report;

import RoadReport.enums.ReportType;
import org.jetbrains.annotations.NotNull;

public record ReportRequestDTO(
        @NotNull ReportType type,
        String description,
        @NotNull Double latitude,
        @NotNull Double longitude
) {}
