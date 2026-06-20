package RoadReport.controllers;


import RoadReport.controllers.dto.report.ReportRequestDTO;
import RoadReport.controllers.dto.report.ReportResponseDTO;
import RoadReport.entities.Report;
import RoadReport.entities.Vote;
import RoadReport.enums.VoteType;
import RoadReport.security.service.RoadUserDetails;
import RoadReport.services.core.ReportService;
import RoadReport.services.core.VoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;
    private final VoteService voteService;

    private Report convertDTOToReport(ReportRequestDTO reportRequestDTO) {
        return Report.builder()
                .type(reportRequestDTO.type())
                .description(reportRequestDTO.description())
                .latitude(reportRequestDTO.latitude())
                .longitude(reportRequestDTO.longitude())
                .build();
    }

    /**
     * Creates a new road report submitted by an authenticated user.
     */
    @PostMapping
    public ResponseEntity<Void> createReport(@Valid @RequestBody ReportRequestDTO reportRequestDTO,
                                             @AuthenticationPrincipal RoadUserDetails roadUserDetails) {
        Long userId = roadUserDetails.getId();
        Report report = convertDTOToReport(reportRequestDTO);
        reportService.createReport(userId, report);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    private ReportResponseDTO convertReportToDTO(Report report, VoteType vt) {
        return new ReportResponseDTO(
                report.getId(),
                report.getUser().getId(),
                report.getUser().getUsername(),
                report.getType(),
                report.getDescription(),
                report.getLatitude(),
                report.getLongitude(),
                report.getStatus(),
                report.getUpvotes(),
                report.getDownvotes(),
                report.getCreateDate(),
                vt
        );
    }

    /**
     * Retrieves a list of reports located within a specific geographic radius.
     */
    @GetMapping
    public ResponseEntity<List<ReportResponseDTO>> findNearbyReports(@RequestParam Double latitude,
                                                                     @RequestParam Double longitude,
                                                                     @RequestParam Double radius,
                                                                     @AuthenticationPrincipal RoadUserDetails userDetails) {

        List<Report> reportsList;
        if (radius <= 0) {
            reportsList = reportService.findAllReports();
        } else {
            reportsList = reportService.findNearbyReports(latitude, longitude, radius);
        }

        Map<Long, VoteType> userVotesMap;

        if (userDetails != null) {
            userVotesMap = voteService.findByUserId(userDetails.getId()).stream()
                    .collect(Collectors.toMap(
                            v -> v.getReport().getId(),
                            Vote::getType
                    ));
        } else {
            userVotesMap = new HashMap<>();
        }

        List<ReportResponseDTO> reports = reportsList.stream()
                .map(report -> convertReportToDTO(report, userVotesMap.get(report.getId())))
                .toList();

        return ResponseEntity.ok(reports);
    }
}