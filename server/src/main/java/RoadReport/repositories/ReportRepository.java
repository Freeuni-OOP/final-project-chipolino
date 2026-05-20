package RoadReport.repositories;

import RoadReport.entities.Report;
import RoadReport.enums.ReportStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {
    List<Report> findByUserId(Long user_id);

    List<Report> findByStatus(ReportStatus status);


    @Query(value = "SELECT * FROM reports as r " +
            "WHERE (6371 * acos(cos(radians(:lat)) * cos(radians(r.latitude)) *" +
            " cos(radians(r.longitude) - radians(:lon)) + sin(radians(:lat)) *" +
            " sin(radians(r.latitude)))) < :radius",
            nativeQuery = true
    )
    List<Report> findNearbyReports(@Param("lat") Double user_latitude,
                                   @Param("lon") Double user_longitude,
                                   @Param("radius") Double user_radius);

    @Modifying
    @Query("DELETE FROM Report as r WHERE r.expireDate <= CURRENT_TIMESTAMP " +
            "OR r.status = 'REMOVED'")
    void deleteExpiredReports();
}
