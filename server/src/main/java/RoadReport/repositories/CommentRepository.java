package RoadReport.repositories;

import RoadReport.entities.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Pageable;
import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByReportId(Long reportId);

    List<Comment> findByUserId(Long userId);

    long countByReportId(Long reportId);

    /**
     * Migrates all comments from one report to another.
     * <p>
     * Reassigns the {@code report_id} for all comments associated with the source report
     * to the destination report.
     * @param srcReportId  The ID of the report from which comments are being moved.
     * @param destReportId The ID of the report to which comments are being transferred.
     */
    @Modifying
    @Query("UPDATE Comment AS c SET c.report.id = :destId WHERE c.report.id = :srcId" )
    void migrateComments(@Param("srcId") Long srcReportId,
                      @Param("destId") Long destReportId);
}
