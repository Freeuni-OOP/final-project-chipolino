package RoadReport.repositories;

import RoadReport.entities.Vote;
import RoadReport.enums.VoteType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VoteRepository extends JpaRepository<Vote, Long> {

    Optional<Vote> findByReportIdAndUserId(Long reportId, Long userId);

    void deleteByReportId(Long reportId);

    long countByReportIdAndType(Long reportId, VoteType voteType);

    List<Vote> findByUserId(Long userId);
}

