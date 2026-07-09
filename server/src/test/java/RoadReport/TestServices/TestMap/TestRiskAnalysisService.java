package RoadReport.TestServices.TestMap;

import RoadReport.entities.Report;
import RoadReport.enums.ReportStatus;
import RoadReport.enums.ReportType;
import RoadReport.services.map.RiskAnalysisService;
import RoadReport.services.map.RiskAnalysisService.WeightedReport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class TestRiskAnalysisService {
    @InjectMocks
    private RiskAnalysisService riskAnalysisService;

    private Report roadClosureReport;
    private Report permanentAccidentReport;
    private Report highlyVotedTrafficReport;
    private Report unverifiedPotholeReport;
    private Report removedReport;

    @BeforeEach
    void setUp() {
        initReports();
    }

    private void initReports() {
        roadClosureReport = createReport(1L, ReportType.ROAD_CLOSURE, ReportStatus.TEMPORARY, 10, 0, 0);
        permanentAccidentReport = createReport(2L, ReportType.ACCIDENT, ReportStatus.PERMANENT, 4, 0, 0);
        highlyVotedTrafficReport = createReport(3L, ReportType.HEAVY_TRAFFIC, ReportStatus.TEMPORARY, 1, 10, 2);
        unverifiedPotholeReport = createReport(4L, ReportType.POTHOLE, ReportStatus.TEMPORARY, 4, 1, 0);
        removedReport = createReport(5L, ReportType.ACCIDENT, ReportStatus.REMOVED, 1, 0, 0);
    }

    private Report createReport(Long id, ReportType type, ReportStatus status, int weight, int upvotes, int downvotes) {
        return Report.builder()
                .id(id)
                .type(type)
                .status(status)
                .weight(weight)
                .upvotes(upvotes)
                .downvotes(downvotes)
                .build();
    }

    @Test
    public void testFilterReportsNullAndEmpty() {
        assertTrue(riskAnalysisService.filterReports(null).isEmpty());
        assertTrue(riskAnalysisService.filterReports(List.of()).isEmpty());
    }

    @Test
    public void testFilterReportsSuccess() {
        List<Report> input = Arrays.asList(roadClosureReport, removedReport);
        List<Report> filtered = riskAnalysisService.filterReports(input);

        assertAll(
                () -> assertNotNull(filtered),
                () -> assertEquals(1, filtered.size()),
                () -> assertEquals(1L, filtered.get(0).getId())
        );
    }

    @Test
    public void testAdjustReportWeightsRoadClosure() {
        List<WeightedReport> result = riskAnalysisService.adjustReportWeights(List.of(roadClosureReport));

        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(1, result.size()),
                () -> assertEquals(0.0, result.get(0).multiplier())
        );
    }

    @Test
    public void testAdjustReportWeightsCredibleByStatus() {
        List<WeightedReport> result = riskAnalysisService.adjustReportWeights(List.of(permanentAccidentReport));

        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(1, result.size()),
                () -> assertEquals(0.01, result.get(0).multiplier(), 0.0001)
        );
    }

    @Test
    public void testAdjustReportWeightsCredibleByVotes() {
        List<WeightedReport> result = riskAnalysisService.adjustReportWeights(List.of(highlyVotedTrafficReport));

        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(1, result.size()),
                () -> assertEquals(0.4, result.get(0).multiplier(), 0.0001)
        );
    }

    @Test
    public void testAdjustReportWeightsNotCredible() {
        List<WeightedReport> result = riskAnalysisService.adjustReportWeights(List.of(unverifiedPotholeReport));

        double expectedMultiplier = Math.pow(0.9, Math.sqrt(2.0));

        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(1, result.size()),
                () -> assertEquals(expectedMultiplier, result.get(0).multiplier(), 0.0001)
        );
    }

    @Test
    public void testAdjustReportWeightsZeroWeight() {
        Report zeroWeightReport = createReport(7L, ReportType.ACCIDENT, ReportStatus.TEMPORARY, 0, 0, 0);
        List<WeightedReport> result = riskAnalysisService.adjustReportWeights(List.of(zeroWeightReport));
        double expectedMultiplier = Math.pow(0.1, Math.sqrt(0.5));

        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(1, result.size()),
                () -> assertEquals(expectedMultiplier, result.get(0).multiplier(), 0.0001)
        );
    }
}