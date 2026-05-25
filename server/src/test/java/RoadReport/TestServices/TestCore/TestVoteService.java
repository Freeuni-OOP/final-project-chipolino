package RoadReport.TestServices.TestCore;

import RoadReport.entities.Report;
import RoadReport.entities.User;
import RoadReport.entities.Vote;
import RoadReport.enums.ReportStatus;
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
import java.util.Optional;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class TestVoteService {
    @Mock private VoteRepository voteRepository;
    @Mock private ReportRepository reportRepository;
    @Mock private UserRepository userRepository;
    @Mock private ReportService reportService;
    @Mock private UserService userService;

    @InjectMocks private VoteService voteService;

    private User user;
    private Report report;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).username("voter").build();
        report = Report.builder().id(10L).status(ReportStatus.TEMPORARY).build();
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

        // Verify the thread-safe flush happens during a switch
        verify(voteRepository).delete(oldVote);
        verify(voteRepository).flush();
        verify(voteRepository).save(any(Vote.class));
    }
}
