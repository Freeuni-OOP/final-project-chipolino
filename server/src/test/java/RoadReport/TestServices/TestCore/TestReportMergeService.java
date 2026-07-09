package RoadReport.TestServices.TestCore;

import RoadReport.entities.Report;
import RoadReport.enums.ReportStatus;
import RoadReport.enums.VoteType;
import RoadReport.repositories.CommentRepository;
import RoadReport.repositories.ReportRepository;
import RoadReport.repositories.VoteRepository;
import RoadReport.services.core.ReportMergeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class TestReportMergeService {
    @Mock
    private VoteRepository voteRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private ReportRepository reportRepository;

    @InjectMocks
    private ReportMergeService reportMergeService;

    private Report mainReport;
    private Report duplicateReport;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        mainReport = new Report();
        mainReport.setId(10L);
        mainReport.setWeight(50);
        mainReport.setStatus(ReportStatus.TEMPORARY);

        duplicateReport = new Report();
        duplicateReport.setId(20L);
        duplicateReport.setWeight(30);
        duplicateReport.setStatus(ReportStatus.TEMPORARY);
    }

    @Test
    public void testMergeReportsSuccess() {
        reportMergeService.mergeReports(mainReport, duplicateReport);
        assertAll(
                () -> assertEquals(80, mainReport.getWeight()),
                () -> assertEquals(ReportStatus.REMOVED, duplicateReport.getStatus())
        );

        verify(voteRepository, times(1)).deleteDuplicateVotes(20L, 10L);
        verify(voteRepository, times(1)).migrateVotes(20L, 10L);
        verify(voteRepository, times(1)).countByReportIdAndType(10L, VoteType.POSITIVE);
        verify(voteRepository, times(1)).countByReportIdAndType(10L, VoteType.NEGATIVE);
        verify(commentRepository, times(1)).migrateComments(20L, 10L);
        verify(reportRepository, times(1)).save(mainReport);
        verify(reportRepository, times(1)).save(duplicateReport);

        verifyNoMoreInteractions(voteRepository, commentRepository, reportRepository);
    }

    @Test
    public void testMergeReportsSameReport() {
        reportMergeService.mergeReports(mainReport, mainReport);

        assertAll(
                () -> assertEquals(100, mainReport.getWeight()),
                () -> assertEquals(ReportStatus.REMOVED, mainReport.getStatus())
        );

        verify(voteRepository, times(1)).deleteDuplicateVotes(10L, 10L);
        verify(voteRepository, times(1)).migrateVotes(10L, 10L);
        verify(commentRepository, times(1)).migrateComments(10L, 10L);
        verify(reportRepository, times(2)).save(mainReport);
    }
}
