package RoadReport.TestServices.TestCore;

import RoadReport.entities.Report;
import RoadReport.entities.User;
import RoadReport.entities.Vote;
import RoadReport.enums.ReportStatus;
import RoadReport.enums.ReportType;
import RoadReport.enums.VoteType;
import RoadReport.repositories.ReportRepository;
import RoadReport.repositories.UserRepository;
import RoadReport.repositories.VoteRepository;
import RoadReport.services.core.ReportService;
import RoadReport.services.core.UserService;
import RoadReport.services.core.VoteService;
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
        verify(reportRepository).delete(report);
        verify(userService).handleRejectedReport(user.getId());
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
}
