package RoadReport.controllers.dto.report;

import RoadReport.enums.ReportStatus;
import RoadReport.enums.ReportType;
import RoadReport.enums.VoteType;

import java.time.LocalDateTime;
import java.util.Map;

public record ReportResponseDTO(
        Long id,
        Long userId,
        String authorUsername,
        ReportType type,
        String description,
        Double latitude,
        Double longitude,
        ReportStatus status,
        Integer upvotes,
        Integer downvotes,
        LocalDateTime createDate,
        VoteType vote,
        Map<String, Object> attributes
) {}
