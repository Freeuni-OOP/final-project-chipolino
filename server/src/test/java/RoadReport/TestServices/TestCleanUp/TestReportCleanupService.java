package RoadReport.TestServices.TestCleanUp;

import RoadReport.entities.Report;
import RoadReport.enums.ReportStatus;
import RoadReport.enums.ReportType;
import RoadReport.repositories.ReportRepository;
import RoadReport.services.cleanUp.ReportCleanupService;
import RoadReport.services.core.ReportMergeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class TestReportCleanupService {
    @Mock
    private ReportRepository reportRepository;

    @Mock
    private ReportMergeService reportMergeService;

    @InjectMocks
    private ReportCleanupService reportCleanupService;

    private Report mainReport;
    private Report duplicateReport;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        mainReport = new Report();
        mainReport.setId(100L);
        mainReport.setLatitude(41.7151);
        mainReport.setLongitude(44.8271);
        mainReport.setType(ReportType.ACCIDENT);
        mainReport.setStatus(ReportStatus.PERMANENT);
        duplicateReport = new Report();
        duplicateReport.setId(200L);
        duplicateReport.setLatitude(41.7152);
        duplicateReport.setLongitude(44.8272);
        duplicateReport.setType(ReportType.ACCIDENT);
        duplicateReport.setStatus(ReportStatus.PERMANENT);
    }

    @Test
    public void testCleanupReports() {
        reportCleanupService.cleanupReports();

        verify(reportRepository, times(1)).deleteExpiredReports();
        verifyNoMoreInteractions(reportRepository);
        verifyNoInteractions(reportMergeService);
    }

    @Test
    public void testScheduledMergeReportsSuccessfulMerge() {
        when(reportRepository.findByStatusNotAndExpireDateAfter(eq(ReportStatus.REMOVED), any(LocalDateTime.class)))
                .thenReturn(List.of(mainReport));
        when(reportRepository.findNearbyReportsByType(
                mainReport.getLatitude(), mainReport.getLongitude(), 0.05, mainReport.getType().name()
        )).thenReturn(List.of(duplicateReport));

        reportCleanupService.scheduledMergeReports();
        verify(reportMergeService, times(1)).mergeReports(mainReport, duplicateReport);
    }

    @Test
    public void testScheduledMergeReportsNoActiveReportsFound() {
        when(reportRepository.findByStatusNotAndExpireDateAfter(eq(ReportStatus.REMOVED), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        reportCleanupService.scheduledMergeReports();

        verify(reportRepository, never()).findNearbyReportsByType(anyDouble(), anyDouble(), anyDouble(), anyString());
        verifyNoInteractions(reportMergeService);
    }

    @Test
    public void testScheduledMergeReportsMainReportIsRemoved() {
        mainReport.setStatus(ReportStatus.REMOVED);
        when(reportRepository.findByStatusNotAndExpireDateAfter(eq(ReportStatus.REMOVED), any(LocalDateTime.class)))
                .thenReturn(List.of(mainReport));

        reportCleanupService.scheduledMergeReports();

        verify(reportRepository, never()).findNearbyReportsByType(anyDouble(), anyDouble(), anyDouble(), anyString());
        verifyNoInteractions(reportMergeService);
    }

    @Test
    public void testScheduledMergeReportDuplicateReportIsRemoved() {
        duplicateReport.setStatus(ReportStatus.REMOVED);

        when(reportRepository.findByStatusNotAndExpireDateAfter(eq(ReportStatus.REMOVED), any(LocalDateTime.class)))
                .thenReturn(List.of(mainReport));
        when(reportRepository.findNearbyReportsByType(
                mainReport.getLatitude(), mainReport.getLongitude(), 0.05, mainReport.getType().name()
        )).thenReturn(List.of(duplicateReport));

        reportCleanupService.scheduledMergeReports();
        verify(reportMergeService, never()).mergeReports(any(), any());
    }

    @Test
    public void testScheduledMergeReportsSelfMerge() {
        when(reportRepository.findByStatusNotAndExpireDateAfter(eq(ReportStatus.REMOVED), any(LocalDateTime.class)))
                .thenReturn(List.of(mainReport));
        when(reportRepository.findNearbyReportsByType(
                mainReport.getLatitude(), mainReport.getLongitude(), 0.05, mainReport.getType().name()
        )).thenReturn(List.of(mainReport));

        reportCleanupService.scheduledMergeReports();
        verify(reportMergeService, never()).mergeReports(any(), any());
    }
}
