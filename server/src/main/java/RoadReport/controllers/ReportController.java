package RoadReport.controllers;

import RoadReport.controllers.dto.ReportRequestDTO;
import RoadReport.controllers.dto.ReportResponseDTO;
import RoadReport.entities.Report;
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

import java.util.List;

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
     *
     * @param reportRequestDTO the details of the report to be created (must be valid)
     * @param roadUserDetails  the currently authenticated user making the request
     * @return a ResponseEntity with HTTP status 201 (Created) upon successful creation
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
     *
     * @param latitude  the central latitude coordinate
     * @param longitude the central longitude coordinate
     * @param radius    the search radius (in kilometers or miles, depending on service implementation)
     * @return a ResponseEntity containing a list of nearby ReportResponseDTOs and HTTP status 200 (OK)
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

    /**
     * Allows an authenticated user to cast a positive vote (upvote) on a specific report.
     *
     * @param id              the unique identifier of the report to upvote
     * @param roadUserDetails the currently authenticated user casting the vote
     * @return a ResponseEntity with HTTP status 200 (OK) upon successful vote
     */
    @PostMapping("/{id}/upvote")
    public ResponseEntity<Void> upvoteReport(@PathVariable Long id,
                                             @AuthenticationPrincipal RoadUserDetails roadUserDetails) {
        Long userId = roadUserDetails.getId();
        voteService.createVote(userId, id, VoteType.POSITIVE);

        return ResponseEntity.ok().build();
    }

    /**
     * Allows an authenticated user to cast a negative vote (downvote) on a specific report.
     *
     * @param id              the unique identifier of the report to downvote
     * @param roadUserDetails the currently authenticated user casting the vote
     * @return a ResponseEntity with HTTP status 200 (OK) upon successful vote
     */
    @PostMapping("/{id}/downvote")
    public ResponseEntity<Void> downvoteReport(@PathVariable Long id,
                                               @AuthenticationPrincipal RoadUserDetails roadUserDetails) {
        Long userId = roadUserDetails.getId();
        voteService.createVote(userId, id, VoteType.NEGATIVE);

        return ResponseEntity.ok().build();
    }
}
