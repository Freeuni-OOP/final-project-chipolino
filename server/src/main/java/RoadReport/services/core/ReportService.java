package RoadReport.services.core;

import RoadReport.entities.Report;
import RoadReport.entities.User;
import RoadReport.entities.Vote;
import RoadReport.enums.ReportStatus;
import RoadReport.enums.ReportType;
import RoadReport.enums.VoteType;
import RoadReport.repositories.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {
    private final ReportRepository reportRepository;
    private final UserService userService;

    private final double MAX_RATIO_OF_NEGATIVE_VOTES = 0.5;
    private final double MIN_RATIO_OF_POSITIVE_VOTES = 0.95;
    private final int MIN_VOTES_TO_REMOVE_STATUS = 5;
    private final int MIN_VOTES_TO_PERMANENT_STATUS = 10;


    /**
     * Creates a new report and saves it to the database.
     * Sanitizes the description to prevent XSS attacks using Jsoup.
     * @param userId     The ID of the user creating the report.
     * @param reportData The report entity containing title, description, and location.
     * @throws IllegalStateException if the user is currently banned.
     */
    @Transactional
    public void createReport(Long userId, Report reportData) {
        if (userService.userIsBanned(userId)) {
            throw new IllegalStateException("Banned users cannot submit reports.");
        }

        String cleanDescription = Jsoup.clean(reportData.getDescription(), Safelist.none());
        reportData.setDescription(cleanDescription);

        User user = userService.getUserById(userId);
        reportData.setUser(user);
        reportData.setStatus(ReportStatus.TEMPORARY);

        reportRepository.save(reportData);
    }

    /**
     * Adjusts the raw upvote or downvote counts on a report.
     * @param vote   The vote entity being processed.
     * @param report The report to be updated.
     * @param point  The value to add (1 for a new vote, -1 to retract a vote).
     */
    @Transactional
    public void handleReportVotes(Vote vote, Report report, int point) {
        if (vote.getType() == VoteType.POSITIVE) {
            report.setUpvotes(report.getUpvotes() + point);
        } else {
            report.setDownvotes(report.getDownvotes() + point);
        }
    }

    /**
     * Processes a new vote and evaluates if the report should change status.
     * A report can be REMOVED if negative votes exceed 50% (after 5 votes).
     * A report can become PERMANENT if positive votes exceed 95% (after 10 votes)
     * and its status is eligible.
     * @param report The report receiving the vote.
     * @param vote   The vote being cast.
     */
    @Transactional
    public void addVote(Report report, Vote vote) {
        if (report.getExpireDate().isBefore(LocalDateTime.now()) ||
                report.getStatus() == ReportStatus.PERMANENT ||
                report.getStatus() == ReportStatus.REMOVED) return;

        handleReportVotes(vote, report, 1);

        int allVotes = report.getUpvotes() + report.getDownvotes();

        if (allVotes >= MIN_VOTES_TO_REMOVE_STATUS &&
                1.0 * report.getDownvotes()/allVotes > MAX_RATIO_OF_NEGATIVE_VOTES) {
            report.setStatus(ReportStatus.REMOVED);
            userService.handleRejectedReport(report.getUser().getId());
            reportRepository.delete(report);
            reportRepository.flush();
        } else if (report.getStatus() != ReportStatus.PERMANENT &&
                isEligibleForPermanentStatus(report) &&
                allVotes > MIN_VOTES_TO_PERMANENT_STATUS &&
                1.0 * report.getUpvotes()/allVotes > MIN_RATIO_OF_POSITIVE_VOTES) {
            report.setStatus(ReportStatus.PERMANENT);
        }
    }

    /**
     * Checks if the report's expiration time has passed.
     * @param report The report to check.
     * @return true if the report is expired, false otherwise.
     */
    @Transactional(readOnly = true)
    public boolean isExpired(Report report) {
        return report.getExpireDate().isBefore(LocalDateTime.now());
    }

    /**
     * Finds all reports that are currently visible on the map.
     * Filters out removed reports and those that have reached their expiration date.
     * @return A list of active, non-expired reports.
     */
    @Transactional(readOnly = true)
    public List<Report> getActiveReports() {
        return reportRepository.findByStatusNotAndExpireDateAfter(ReportStatus.REMOVED, LocalDateTime.now());
    }

    /**
     * Finds all reports submitted by a specific user.
     * @param userId The unique ID of the user.
     * @return A list of reports owned by the user.
     */
    @Transactional(readOnly = true)
    public List<Report> getReportsByUserId(long userId) {
        return reportRepository.findByUserId(userId);
    }

    /**
     * Filters reports based on their current status.
     * @param status The status to filter by.
     * @return A list of reports matching the given status.
     */
    @Transactional(readOnly = true)
    public List<Report> findByStatus(ReportStatus status) {
        return reportRepository.findByStatus(status);
    }

    /**
     * Finds reports within a specific geographical radius of the user.
     * @param user_latitude  Center latitude.
     * @param user_longitude Center longitude.
     * @param user_radius    Radius in kilometers.
     * @return A list of reports within the specified distance.
     */
    @Transactional(readOnly = true)
    public List<Report> findNearbyReports(@Param("lat") Double user_latitude,
                                   @Param("lon") Double user_longitude,
                                   @Param("radius") Double user_radius) {
        return reportRepository.findNearbyReports(user_latitude, user_longitude, user_radius);
    }

    /**
     * Removes all reports from the database that have passed their expiration date.
     */
    @Transactional
    public void deleteExpiredReports() {
        reportRepository.deleteExpiredReports();
    }

    /**
     * Permanently deletes a specific report entity.
     * @param report The report entity to be removed.
     */
    @Transactional
    public void deleteReport(Report report) {
        reportRepository.delete(report);
    }

    /**
     * Determines if a report type is eligible to transition to a PERMANENT status.
     * @param report The report entity to evaluate.
     * @return true - if the report type can become permanent;
     * false - if it is a temporary incident type.
     */
    private boolean isEligibleForPermanentStatus(Report report) {
        if (report.getType() != ReportType.ACCIDENT &&
                report.getType() != ReportType.HEAVY_TRAFFIC &&
                report.getType() != ReportType.ROAD_CLOSURE &&
                report.getType() != ReportType.CUSTOM) return true;
        return false;
    }
}