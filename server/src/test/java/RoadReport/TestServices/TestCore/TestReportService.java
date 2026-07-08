package RoadReport.TestServices.TestCore;

import RoadReport.entities.Report;
import RoadReport.entities.User;
import RoadReport.entities.Vote;
import RoadReport.enums.ReportStatus;
import RoadReport.enums.ReportType;
import RoadReport.enums.VoteType;
import RoadReport.exceptions.core.ReportNotFoundException;
import RoadReport.exceptions.core.UserBannedException;
import RoadReport.repositories.ReportRepository;
import RoadReport.services.core.ReportAttributesValidator;
import RoadReport.services.core.ReportService;
import RoadReport.services.core.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TestReportService {
    @Mock private ReportRepository reportRepository;
    @Mock private UserService userService;
    @Mock private ReportAttributesValidator reportAttributesValidator;
    @InjectMocks
    private ReportService reportService;

    private User user;
    private Report report;
    private Vote vote;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).username("reporter").build();
        report = Report.builder().id(10L)
                .user(user)
                .status(ReportStatus.TEMPORARY)
                .type(ReportType.SPEED_CAMERA)
                .description("New Camera!")
                .expireDate(LocalDateTime.now().plusDays(2))
                .upvotes(0)
                .downvotes(0)
                .build();
        vote = new Vote();
        vote.setType(VoteType.POSITIVE);
    }

    @Test
    void testJsoupClean() {
        String dangerousDesc = "Hello <script>alert('Hacked')</script> World";
        report.setDescription(dangerousDesc);

        when(userService.userIsBanned(1L)).thenReturn(false);
        when(userService.getUserById(1L)).thenReturn(user);

        reportService.createReport(1L, report);

        assertEquals("Hello  World", report.getDescription());
        verify(reportRepository).save(report);
    }

    @Test
    void testRemovalOfReport() {
        report.setUpvotes(2);
        report.setDownvotes(2);
        Vote newDownvote = new Vote();
        newDownvote.setType(VoteType.NEGATIVE);

        reportService.addVote(report, newDownvote);

        assertEquals(ReportStatus.REMOVED, report.getStatus());
    }

    @Test
    void testTurningReportPermanent() {
        report.setUpvotes(10);
        report.setDownvotes(0);

        reportService.addVote(report, vote);

        assertEquals(ReportStatus.PERMANENT, report.getStatus());
    }

    @Test
    void testAddVoteFailure() {
        report.setStatus(ReportStatus.REMOVED);
        report.setUpvotes(0);

        reportService.addVote(report, vote);

        assertEquals(0, report.getUpvotes());
    }

    @Test
    void testTurningReportPermanentButTypeIsNotEligible() {
        report.setType(ReportType.CUSTOM);
        report.setUpvotes(10);
        report.setDownvotes(0);

        reportService.addVote(report, vote);

        assertEquals(ReportType.CUSTOM, report.getType());
    }

    @Test
    void testIsExpired() {
        report.setExpireDate(LocalDateTime.now().plusDays(1));
        assertFalse(reportService.isExpired(report));

        report.setExpireDate(LocalDateTime.now().minusDays(1));
        assertTrue(reportService.isExpired(report));
    }

    @Test
    void testGetFindMethods() {
        List<Report> mockList = List.of(report);

        when(reportRepository.findByStatusNotAndExpireDateAfter(eq(ReportStatus.REMOVED), any(LocalDateTime.class))).thenReturn(mockList);
        when(reportRepository.findByUserId(1L)).thenReturn(mockList);

        List<Report> active = reportService.getActiveReports();
        List<Report> userReports = reportService.getReportsByUserId(1L);

        assertEquals(1, active.size());
        assertEquals(1, userReports.size());
        verify(reportRepository).findByUserId(1L);
    }

    @Test
    void testDeletionMethods() {
        reportService.deleteReport(report);
        verify(reportRepository).delete(report);

        reportService.deleteExpiredReports();
        verify(reportRepository).deleteExpiredReports();
    }

    @Test
    void testBannedUserCreatingReport() {
        when(userService.userIsBanned(3L)).thenReturn(true);

        try {
            reportService.createReport(3L, report);
            fail("Expected an IllegalStateException to be thrown, but nothing happened.");
        } catch (UserBannedException e) {
            assertEquals("Banned users cannot submit reports.", e.getMessage());
        }

        verify(reportRepository, never()).save(any());
    }

    @Test
    void testCreateReportWithNullDescription() {
        report.setDescription(null);
        when(userService.userIsBanned(1L)).thenReturn(false);
        when(userService.getUserById(1L)).thenReturn(user);

        reportService.createReport(1L, report);

        assertNull(report.getDescription());
        verify(reportRepository).save(report);
    }

    @Test
    void testHandleReportVotesNegativePoints() {
        report.setUpvotes(5);
        reportService.handleReportVotes(vote, report, -1);
        assertEquals(4, report.getUpvotes());

        Vote negativeVote = new Vote();
        negativeVote.setType(VoteType.NEGATIVE);
        report.setDownvotes(3);
        reportService.handleReportVotes(negativeVote, report, -1);
        assertEquals(2, report.getDownvotes());
    }

    @Test
    void testAddVoteToAlreadyExpiredReport() {
        report.setExpireDate(LocalDateTime.now().minusDays(1));
        reportService.addVote(report, vote);
        assertEquals(0, report.getUpvotes());
    }

    @Test
    void testAddVoteToPermanentReportShortCircuits() {
        report.setStatus(ReportStatus.PERMANENT);
        reportService.addVote(report, vote);
        assertEquals(1, report.getUpvotes());
        assertEquals(ReportStatus.PERMANENT, report.getStatus());
    }

    @Test
    void testAddVoteBelowMinimumVotesToRemove() {
        report.setUpvotes(1);
        report.setDownvotes(2);
        Vote negativeVote = new Vote();
        negativeVote.setType(VoteType.NEGATIVE);

        reportService.addVote(report, negativeVote);

        assertEquals(ReportStatus.TEMPORARY, report.getStatus());
    }

    @Test
    void testAddVoteBelowMinimumVotesToPermanent() {
        report.setUpvotes(8);
        report.setDownvotes(0);

        reportService.addVote(report, vote);

        assertEquals(ReportStatus.TEMPORARY, report.getStatus());
    }

    @Test
    void testAddVotePermanentExactBoundary() {
        report.setUpvotes(18);
        report.setDownvotes(1);

        reportService.addVote(report, vote);

        assertEquals(ReportStatus.PERMANENT, report.getStatus());
    }

    @Test
    void testAddVotePermanentBelowRatio() {
        report.setUpvotes(17);
        report.setDownvotes(2);

        reportService.addVote(report, vote);

        assertEquals(ReportStatus.TEMPORARY, report.getStatus());
    }

    @Test
    void testTurningReportPermanentButTypeIsNotEligibleVerifiesStatus() {
        report.setType(ReportType.CUSTOM);
        report.setUpvotes(10);
        report.setDownvotes(0);

        reportService.addVote(report, vote);

        assertEquals(ReportStatus.TEMPORARY, report.getStatus());
    }

    @Test
    void testGetReportByIdSuccess() {
        when(reportRepository.findById(10L)).thenReturn(Optional.of(report));
        Report result = reportService.getReportById(10L);
        assertNotNull(result);
        assertEquals(10L, result.getId());
    }

    @Test
    void testGetReportByIdNotFound() {
        when(reportRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ReportNotFoundException.class, () -> reportService.getReportById(99L));
    }

    @Test
    void testFindByStatus() {
        List<Report> mockList = List.of(report);
        when(reportRepository.findByStatus(ReportStatus.TEMPORARY)).thenReturn(mockList);
        List<Report> result = reportService.findByStatus(ReportStatus.TEMPORARY);
        assertEquals(1, result.size());
    }

    @Test
    void testFindNearbyReports() {
        List<Report> mockList = List.of(report);
        when(reportRepository.findNearbyReports(41.7, 44.8, 5.0)).thenReturn(mockList);
        List<Report> result = reportService.findNearbyReports(41.7, 44.8, 5.0);
        assertEquals(1, result.size());
    }

    @Test
    void testFindAllReports() {
        List<Report> mockList = List.of(report);
        when(reportRepository.findAll()).thenReturn(mockList);
        List<Report> result = reportService.findAllReports();
        assertEquals(1, result.size());
    }
}