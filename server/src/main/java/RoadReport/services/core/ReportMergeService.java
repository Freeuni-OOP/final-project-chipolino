package RoadReport.services.core;

import RoadReport.entities.Report;
import RoadReport.enums.ReportStatus;
import RoadReport.enums.VoteType;
import RoadReport.repositories.CommentRepository;
import RoadReport.repositories.ReportRepository;
import RoadReport.repositories.VoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReportMergeService {
    private final VoteRepository voteRepository;
    private final CommentRepository commentRepository;
    private final ReportRepository reportRepository;

    /**
     * Merges a duplicate report into a main report.
     * <p>
     * The operation performs the following steps atomically:
     * <ol>
     * <li>Removes duplicate votes (where the user has voted on both reports).</li>
     * <li>Migrates remaining unique votes from the duplicate to the main report.</li>
     * <li>Migrates all comments from the duplicate to the main report.</li>
     * <li>Updates the main report's weight by adding the duplicate's weight.</li>
     * <li>Sets the duplicate report status to {@link ReportStatus#REMOVED}.</li>
     * </ol>
     * @param mainReport      The target report that will receive the merged data.
     * @param duplicateReport The report identified as a duplicate to be merged and retired.
     */
    @Transactional
    public void mergeReports(Report mainReport, Report duplicateReport) {
        voteRepository.deleteDuplicateVotes(duplicateReport.getId(),
                mainReport.getId());


        voteRepository.migrateVotes(duplicateReport.getId(), mainReport.getId());
        commentRepository.migrateComments(duplicateReport.getId(), mainReport.getId());
        mainReport.setWeight(mainReport.getWeight() + duplicateReport.getWeight());

        int mainUpvotes = (int) voteRepository.countByReportIdAndType(mainReport.getId(), VoteType.POSITIVE);
        int mainDownvotes = (int) voteRepository.countByReportIdAndType(mainReport.getId(), VoteType.NEGATIVE);
        mainReport.setUpvotes(mainUpvotes);
        mainReport.setDownvotes(mainDownvotes);

        duplicateReport.setStatus(ReportStatus.REMOVED);
        duplicateReport.setUpvotes(0);
        duplicateReport.setDownvotes(0);

        reportRepository.save(mainReport);
        reportRepository.save(duplicateReport);
    }
}
