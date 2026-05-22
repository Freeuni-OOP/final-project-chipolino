package RoadReport.services.cleanUp;

import RoadReport.repositories.ReportRepository;
import jakarta.transaction.Transactional;
import lombok.Data;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Data
@Service
@EnableScheduling
public class ReportCleanupService {

    private final ReportRepository reportCleanupRepository;


    @Scheduled(fixedDelay = 10000)
    @Transactional
    public void cleanupReports() {
        reportCleanupRepository.deleteExpiredReports();
    }
}
