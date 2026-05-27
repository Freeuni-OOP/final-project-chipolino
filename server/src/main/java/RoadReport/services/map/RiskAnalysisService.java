package RoadReport.services.map;

import RoadReport.entities.Report;
import RoadReport.enums.ReportStatus;
import RoadReport.enums.ReportType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class RiskAnalysisService {
    public static final int CREDIBILITY_THRESHOLD = 5;
    private static final Map<ReportType, Double> BASE_MAP = Map.of(
            ReportType.ROAD_CLOSURE,  0.0,
            ReportType.ACCIDENT,      0.1,
            ReportType.HEAVY_TRAFFIC, 0.4,
            ReportType.POTHOLE,       0.9
    );

    /**
     * Determines if a report is credible based on status or community votes.
     */
    private boolean reportIsCredible(Report report) {
        if (report.getStatus() == ReportStatus.PERMANENT) return true;
        int up = report.getUpvotes();
        int down = report.getDownvotes();
        return (up - down) >= CREDIBILITY_THRESHOLD;
    }


    /**
     * Calculates the speed penalty multiplier for a single report.
     */
    private double multiplierForSingleReport(Report report) {
        double base =  BASE_MAP.getOrDefault(report.getType(), 1.0);
        if(base == 0.0) return 0.0;

        double weight = Math.max(1, report.getWeight());

        if (reportIsCredible(report)) {
            return base + (1.0 - base) / weight;
        } else {
            return base + (1.0 - base) / (2.0 * weight);
        }
    }



    /**
     * Filters a list of reports to only include types defined in the system.
     *
     * @param reports The list of reports to filter.
     * @return A list containing only valid reports.
     */
    public List<Report> filterReports(List<Report> reports) {
        if(reports == null || reports.isEmpty()) return List.of();
        List<Report> filtered = new ArrayList<>();
        for(Report r : reports) {
            if(BASE_MAP.containsKey(r.getType())  && r.getStatus() != ReportStatus.REMOVED) {
                filtered.add(r);
            }
        }
        return filtered;
    }



    /**
     * Transforms reports into weighted reports with calculated speed multipliers.
     *
     * @param filteredReports The valid reports to process.
     * @return A list of WeightedReport objects.
     */
    public List<WeightedReport> adjustReportWeights(List<Report> filteredReports) {
        List<WeightedReport> weightedReports = new ArrayList<>(filteredReports.size());

        for (Report r : filteredReports) {
            double multiplier = multiplierForSingleReport(r);
            weightedReports.add(new WeightedReport(r, multiplier));
        }

        return weightedReports;
    }


    /**
     * Data carrier bundling a report with its calculated multiplier.
     */
    public record WeightedReport(Report report, double multiplier) {}
}
