package RoadReport.repositories;

import RoadReport.entities.Vote;
import RoadReport.enums.VoteType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VoteRepository extends JpaRepository<Vote, Long> {

    Optional<Vote> findByReportIdAndUserId(Long reportId, Long userId);

    long countByReportIdAndType(Long reportId, VoteType voteType);

    List<Vote> findByUserId(Long userId);

    /**
     * Removes duplicate votes from the database.
     * <p>
     * This method targets votes associated with the duplicate report that conflict
     * with existing votes on the main report.
     * @param duplicateReportId The ID of the report being merged (the source).
     * @param mainReportId      The ID of the report receiving the data (the destination).
     */
    @Modifying
    @Query(value =  "DELETE dup FROM votes AS dup " +
                    "JOIN votes AS main ON main.user_id = dup.user_id " +
                    "WHERE main.report_id = :mainId " +
                    "AND dup.report_id = :dupId",
            nativeQuery = true)
    void deleteDuplicateVotes(@Param("dupId") Long duplicateReportId,
                              @Param("mainId") Long mainReportId);


    /**
     * Migrates votes from one report to another.
     * <p>
     * Reassigns all votes associated with the source report to the destination report.
     * This should be called after {@link #deleteDuplicateVotes} to ensure that
     * no unique constraint violations occur.
     * @param srcReportId  The ID of the report from which votes are being moved.
     * @param destReportId The ID of the report to which votes are being transferred.
     */
    @Modifying
    @Query("UPDATE Vote AS v SET v.report.id = :destId WHERE v.report.id = :srcId")
    void migrateVotes(@Param("srcId") Long srcReportId,
                      @Param("destId") Long destReportId);
}

