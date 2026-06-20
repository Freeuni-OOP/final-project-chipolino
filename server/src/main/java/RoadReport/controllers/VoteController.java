package RoadReport.controllers;

import RoadReport.controllers.dto.vote.VoteResponseDTO;
import RoadReport.enums.VoteType;
import RoadReport.security.service.RoadUserDetails;
import RoadReport.services.core.VoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import static RoadReport.enums.VoteType.NEGATIVE;
import static RoadReport.enums.VoteType.POSITIVE;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/vote")
public class VoteController {
    private final VoteService voteService;

    @PostMapping("/{reportId}/upvote")
    public ResponseEntity<Void> upvoteReport(@PathVariable Long reportId,
                                             @AuthenticationPrincipal RoadUserDetails userDetails) {
        voteService.createVote(reportId, userDetails.getId(), POSITIVE);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{reportId}/downvote")
    public ResponseEntity<Void> downvoteReport(@PathVariable Long reportId,
                                               @AuthenticationPrincipal RoadUserDetails userDetails) {
        voteService.createVote(reportId, userDetails.getId(), NEGATIVE);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{reportId}/votes")
    public ResponseEntity<VoteResponseDTO> downvoteReport(@PathVariable Long reportId) {
        VoteResponseDTO votes = new VoteResponseDTO(
                voteService.countByReportIdAndType(reportId, POSITIVE),
                voteService.countByReportIdAndType(reportId, NEGATIVE)
        );
        return ResponseEntity.ok(votes);
    }
}