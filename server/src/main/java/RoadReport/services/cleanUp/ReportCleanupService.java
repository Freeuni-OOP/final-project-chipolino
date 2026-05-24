package RoadReport.services.cleanUp;

import RoadReport.repositories.ReportRepository;
import jakarta.transaction.Transactional;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class ReportCleanupService {

    private final ReportRepository reportRepository;


    /**
     * Performs a cleanup operation to delete false reports or reports that have
     * reached their expiration date.
     * <p>
     * This method is triggered automatically at a fixed interval of 100 seconds.
     */
    @Scheduled(fixedDelay = 100000)
    @Transactional
    public void cleanupReports() {
        reportRepository.deleteExpiredReports();
    }
}
