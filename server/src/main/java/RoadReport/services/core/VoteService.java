package RoadReport.services.core;

import RoadReport.entities.Report;
import RoadReport.entities.User;
import RoadReport.entities.Vote;
import RoadReport.enums.ReportStatus;
import RoadReport.enums.VoteType;
import RoadReport.repositories.ReportRepository;
import RoadReport.repositories.UserRepository;
import RoadReport.repositories.VoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VoteService {
    private final VoteRepository voteRepository;
    private final UserRepository userRepository;
    private final ReportRepository reportRepository;
    private final ReportService reportService;
    private final UserService userService;

    /**
     * Manages the voting process for a specific report.
     * Checks if a vote already exists and either creates a new one or updates the existing one.
     * @param userId   The ID of the user casting the vote.
     * @param reportId The ID of the report being voted on.
     * @param vt       The type of vote (UPVOTE/DOWNVOTE).
     * @throws IllegalArgumentException if the user or report does not exist.
     * @throws IllegalStateException    if the report is removed or the user is banned.
     * @throws IllegalCallerException   if the user tries to vote on their own report.
     */
    @Transactional
    public void createVote(Long userId, Long reportId, VoteType vt) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("couldn't found report " + reportId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("couldn't found user " + userId));

        validateVoteEligibility(user, report);

        Optional<Vote> vote = voteRepository.findByReportIdAndUserId(reportId, userId);
        if (vote.isEmpty() || vote.get().getType() != vt) {
            if (vote.isPresent()) {
                if (report.getStatus() != ReportStatus.PERMANENT) updateUsersScore(report, vote.get(), true);
                reportService.handleReportVotes(vote.get(), report, -1);
                voteRepository.delete(vote.get());
                voteRepository.flush();
            }
            Vote newVote = new Vote();
            newVote.setUser(user);
            newVote.setReport(report);
            newVote.setType(vt);
            voteRepository.save(newVote);

            if (report.getStatus() != ReportStatus.PERMANENT) updateUsersScore(report, newVote, false);
            reportService.addVote(report, newVote);
        }
    }

    /**
     * Finds a specific vote cast by a user on a specific report.
     * @param reportId The ID of the report.
     * @param userId   The ID of the user.
     * @return An Optional containing the Vote if found, otherwise empty.
     */
    @Transactional(readOnly = true)
    public Optional<Vote> findByReportIdAndUserId(Long reportId, Long userId) {
        return voteRepository.findByReportIdAndUserId(reportId, userId);
    }

    /**
     * Counts the total number of votes for a specific report based on the vote type.
     * Useful for calculating the upvote/downvote ratio.
     * @param reportId The ID of the report to check.
     * @param voteType The type of vote to count (POSITIVE/NEGATIVE).
     * @return The total count of votes matching the criteria.
     */
    @Transactional(readOnly = true)
    public long countByReportIdAndType(Long reportId, VoteType voteType) {
        return voteRepository.countByReportIdAndType(reportId, voteType);
    }

    /**
     * Finds all votes cast by a specific user.
     * @param userId The ID of the user.
     * @return A list of all votes associated with this user.
     */
    @Transactional(readOnly = true)
    public List<Vote> findByUserId(Long userId) {
        return voteRepository.findByUserId(userId);
    }

    /**
     * Updates the reputation score of the report's creator based on the vote received.
     * @param report   The report being voted on (used to find the creator).
     * @param vote     The vote being applied or removed.
     * @param opposite If true, reverses the effect of the vote (used when deleting/changing a vote).
     */
    private void updateUsersScore(Report report, Vote vote, boolean opposite) {
        if (vote.getType() == VoteType.NEGATIVE) {
            if (opposite) {
                userService.handleAcceptedVote(report.getUser().getId());
            } else {
                userService.handleRejectedVote(report.getUser().getId());
            }
        } else {
            if (opposite) {
                userService.handleRejectedVote(report.getUser().getId());
            } else {
                userService.handleAcceptedVote(report.getUser().getId());
            }
        }
    }

    /**
     * Performs business rule validation to ensure the vote is legal.
     * @param user   The user entity attempting to vote.
     * @param report The report entity being targeted.
     */
    private void validateVoteEligibility(User user, Report report) {
        if (report.getStatus() == ReportStatus.REMOVED) {
            throw new IllegalStateException("Removed report cannot be voted!");
        }

        if (userService.userIsBanned(user.getId())) {
            throw new IllegalStateException("Banned users cannot vote!");
        }

        if (report.getUser().getUsername().equals(user.getUsername())) {
            throw new IllegalCallerException("Users cannot vote their own reports!");
        }
    }
}