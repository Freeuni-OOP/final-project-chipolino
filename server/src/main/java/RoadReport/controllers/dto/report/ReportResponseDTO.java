package RoadReport.controllers.dto.report;

import RoadReport.enums.ReportStatus;
import RoadReport.enums.ReportType;

import java.time.LocalDateTime;

public record ReportResponseDTO(
        Long id,
        ReportType type,
        String description,
        Double latitude,
        Double longitude,
        ReportStatus status,
        Integer upvotes,
        Integer downvotes,
        LocalDateTime createDate
) {}
