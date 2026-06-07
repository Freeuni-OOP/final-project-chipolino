package RoadReport.TestServices.TestCore;

import RoadReport.entities.Report;
import RoadReport.entities.User;
import RoadReport.entities.Vote;
import RoadReport.enums.ReportStatus;
import RoadReport.enums.VoteType;
import RoadReport.exceptions.core.UserBannedException;
import RoadReport.exceptions.special.ActionForbiddenException;
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
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TestVoteService {
    @Mock private VoteRepository voteRepository;
    @Mock private ReportRepository reportRepository;
    @Mock private UserRepository userRepository;
    @Mock private ReportService reportService;
    @Mock private UserService userService;

    @InjectMocks
    private VoteService voteService;

    private User user;
    private Report report;

    @BeforeEach
    void setUp() {
        User reportOwner = User.builder().id(2L).username("owner").build();
        report = Report.builder().id(10L).user(reportOwner).status(ReportStatus.TEMPORARY).build();
        user = User.builder().id(1L).username("voter").build();
    }

    @Test
    void testCreateNewVoteSuccess() {
        when(reportRepository.findById(10L)).thenReturn(Optional.of(report));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(voteRepository.findByReportIdAndUserId(10L, 1L)).thenReturn(Optional.empty());

        voteService.createVote(1L, 10L, VoteType.POSITIVE);

        verify(voteRepository).save(any(Vote.class));
        verify(reportService).addVote(eq(report), any(Vote.class));
    }

    @Test
    void testSwitchVoteSuccess() {
        Vote oldVote = new Vote();
        oldVote.setType(VoteType.NEGATIVE);

        when(reportRepository.findById(10L)).thenReturn(Optional.of(report));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(voteRepository.findByReportIdAndUserId(10L, 1L)).thenReturn(Optional.of(oldVote));

        voteService.createVote(1L, 10L, VoteType.POSITIVE);

        verify(reportService).addVote(any(Report.class), any(Vote.class));
    }

    @Test
    void testIneligibleVoter() {
        when(reportRepository.findById(10L)).thenReturn(Optional.of(report));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userService.userIsBanned(1L)).thenReturn(true);

        try {
            voteService.createVote(1L, 10L, VoteType.NEGATIVE);
            fail("Expected an IllegalStateException to be thrown, but nothing happened.");
        } catch (UserBannedException e) {
            assertEquals("Banned users cannot vote!", e.getMessage());
        }

        verify(voteRepository, never()).save(any());
    }

    @Test
    void testIneligibleReport() {
        when(reportRepository.findById(10L)).thenReturn(Optional.of(report));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        report.setStatus(ReportStatus.REMOVED);

        try {
            voteService.createVote(1L, 10L, VoteType.NEGATIVE);
            fail("Expected an IllegalStateException to be thrown, but nothing happened.");
        } catch (ActionForbiddenException e) {
            assertEquals("Removed report cannot be voted!", e.getMessage());
        }

        verify(voteRepository, never()).save(any());
    }

    @Test
    void testVoterIsOwner() {
        User voter = User.builder().id(1L).username("Luka").build();
        User owner = User.builder().id(1L).username("Luka").build();

        report.setUser(owner);

        when(reportRepository.findById(10L)).thenReturn(Optional.of(report));
        when(userRepository.findById(1L)).thenReturn(Optional.of(voter));

        try {
            voteService.createVote(1L, 10L, VoteType.NEGATIVE);
            fail("Should have thrown IllegalCallerException because usernames match");
        } catch (ActionForbiddenException e) {
            assertEquals("Users cannot vote their own reports!", e.getMessage());
        }

        verify(voteRepository, never()).save(any());
    }

    @Test
    void testFindMethods() {
        Vote v = new Vote();
        List<Vote> vList = List.of(v);
        Long userId = 1L;
        Long reportId = 10L;

        when(voteRepository.findByReportIdAndUserId(reportId, userId)).thenReturn(Optional.of(v));
        when(voteRepository.countByReportIdAndType(reportId, VoteType.POSITIVE)).thenReturn(3L);
        when(voteRepository.findByUserId(userId)).thenReturn(vList);

        Optional<Vote> findVotes = voteService.findByReportIdAndUserId(reportId, userId);
        long voteCount = voteService.countByReportIdAndType(reportId, VoteType.POSITIVE);
        List<Vote> userVotes = voteService.findByUserId(userId);

        assertTrue(findVotes.isPresent());
        assertEquals(v, findVotes.get());
        assertEquals(3L, voteCount);
        assertEquals(1, userVotes.size());
        assertEquals(v, userVotes.get(0));
    }
}