package RoadReport.controllers;

import RoadReport.controllers.dto.ReportRequestDTO;
import RoadReport.controllers.dto.ReportResponseDTO;
import RoadReport.entities.Report;
import RoadReport.security.service.RoadUserDetails;
import RoadReport.services.core.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService; // VoteService is completely removed from here!

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

    private ReportResponseDTO convertReportToDTO(Report report) {
        return new ReportResponseDTO(
                report.getId(),
                report.getType(),
                report.getDescription(),
                report.getLatitude(),
                report.getLongitude(),
                report.getStatus(),
                report.getUpvotes(),
                report.getDownvotes(),
                report.getCreateDate()
        );
    }

    /**
     * Retrieves a list of reports located within a specific geographic radius.
     */
    @GetMapping
    public ResponseEntity<List<ReportResponseDTO>> findNearbyReports(@RequestParam Double latitude,
                                                                     @RequestParam Double longitude,
                                                                     @RequestParam Double radius) {
        List<ReportResponseDTO> reports = reportService.findNearbyReports(latitude, longitude, radius)
                .stream()
                .map(this::convertReportToDTO)
                .toList();

        return ResponseEntity.ok(reports);
    }
}