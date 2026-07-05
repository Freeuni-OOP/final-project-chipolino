package RoadReport.services.cleanUp;

import RoadReport.entities.Report;
import RoadReport.enums.ReportStatus;
import RoadReport.repositories.ReportRepository;
import RoadReport.services.core.ReportMergeService;
import jakarta.transaction.Transactional;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@RequiredArgsConstructor
@Service
@Slf4j
public class ReportCleanupService {

    private final ReportRepository reportRepository;
    private final ReportMergeService reportMergeService;

    private final static double MERGE_RADIUS = 0.05;

    /**
     * Performs a cleanup operation to delete false reports or reports that have
     * reached their expiration date.
     * <p>
     * This method is triggered automatically at a fixed interval of 2 minutes.
     */
    @Scheduled(fixedDelay = 1000 * 60 * 2)
    @Transactional
    public void cleanupReports() {
        log.info("Starting scheduled Cleanup reports");

        List<Report> reports = reportRepository.findByExpireDateBeforeOrStatus(LocalDateTime.now(), ReportStatus.REMOVED);
        if (!reports.isEmpty()) {
            reportRepository.deleteAllInBatch(reports);
        }

        log.info("Finished scheduled Cleanup reports");
    }

    /**
     * Scans for duplicate reports within a 50-meter radius and merges them into a main report.
     * <p>
     * The process identifies active reports of the same type and migrates
     * comments, votes, and weight scores. This method runs hourly to maintain
     * map accuracy and prioritize high-weight incidents.
     */
    @Scheduled(fixedDelay = 1000 * 60 * 60)
    @Transactional
    public void scheduledMergeReports() {
        log.info("Starting scheduled Merge reports");

        List<Report> allActiveReports =
                reportRepository.findByStatusNotAndExpireDateAfter(
                        ReportStatus.REMOVED, LocalDateTime.now());

        Set<Long> processedIds = new HashSet<Long>();

        for(Report mainReport : allActiveReports){
            if(!ReportStatus.REMOVED.equals(mainReport.getStatus())
                && !processedIds.contains(mainReport.getId())) {
                List<Report> duplicates = reportRepository.findNearbyReportsByType(
                        mainReport.getLatitude(),
                        mainReport.getLongitude(),
                        MERGE_RADIUS,
                        mainReport.getType().name()
                );

                for(Report duplicateReport : duplicates) {
                    try {
                        if (!ReportStatus.REMOVED.equals(duplicateReport.getStatus())
                                && !mainReport.getId().equals(duplicateReport.getId())
                                && !processedIds.contains(duplicateReport.getId())) {
                            reportMergeService.mergeReports(mainReport, duplicateReport);
                            processedIds.add(duplicateReport.getId());
                        }
                    } catch (Exception e) {
                        log.error("Merge of {} and {} failed: {}", mainReport.getId(), duplicateReport.getId(), e.getMessage());
                    }
                }
            }
        }

        log.info("Finished scheduled Merge reports");
    }
}
