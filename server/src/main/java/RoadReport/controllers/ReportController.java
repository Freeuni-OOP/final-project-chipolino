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

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void createReport(@Valid @RequestBody ReportRequestDTO reportRequestDTO,
                             @AuthenticationPrincipal RoadUserDetails roadUserDetails) {
        Long userId = roadUserDetails.getId();
        Report report = convertDTOToReport(reportRequestDTO);
        reportService.createReport(userId, report);
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


    @GetMapping
    public List<ReportResponseDTO> findNearbyReports(@RequestParam Double latitude,
                @RequestParam Double longitude,
                @RequestParam Double radius) {
            return reportService.findNearbyReports(latitude, longitude, radius)
                    .stream()
                    .map(this::convertReportToDTO)
                    .toList();
    }

    @PostMapping("/{id}/upvote")
    public void upvoteReport(@PathVariable Long id,
                             @AuthenticationPrincipal RoadUserDetails roadUserDetails) {
        Long userId = roadUserDetails.getId();
        voteService.createVote(userId, id, VoteType.POSITIVE);
    }

    @PostMapping("/{id}/downvote")
    public void downvoteReport(@PathVariable Long id,
                               @AuthenticationPrincipal RoadUserDetails roadUserDetails) {
        Long userId = roadUserDetails.getId();
        voteService.createVote(userId, id, VoteType.NEGATIVE);
    }
}
