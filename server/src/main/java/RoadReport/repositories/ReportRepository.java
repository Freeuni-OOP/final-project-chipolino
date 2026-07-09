package RoadReport.repositories;

import RoadReport.entities.Report;
import RoadReport.enums.ReportStatus;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {

    @NonNull List<Report> findAll();

    Report findReportById(Long id);

    List<Report> findByUserId(Long user_id);

    List<Report> findByStatus(ReportStatus status);

    List<Report> findByStatusNotAndExpireDateAfter(ReportStatus reportStatus, LocalDateTime now);

    /**
     * Finds road reports located within a specified radius from the user's location.
     * <p>
     * This method uses the <b>Haversine formula</b> to calculate the great-circle distance
     * between two points on a sphere given their longitudes and latitudes.
     * The result is compared against the provided search radius in kilometers.
     * </p>
     * @param user_latitude  The latitude of the user's current location in degrees.
     * @param user_longitude The longitude of the user's current location in degrees.
     * @param user_radius    The search radius in kilometers (e.g., 5.0 for a 5km radius).
     * @return A list of {@link Report} entities found within the specified circular area.
     */
    @Query(value = "SELECT * FROM reports AS r " +
            "WHERE (6371 * acos(cos(radians(:lat)) * cos(radians(r.latitude)) *" +
            " cos(radians(r.longitude) - radians(:lon)) + sin(radians(:lat)) *" +
            " sin(radians(r.latitude)))) < :radius",
            nativeQuery = true
    )

    List<Report> findNearbyReports(@Param("lat") Double user_latitude,
                                   @Param("lon") Double user_longitude,
                                   @Param("radius") Double user_radius);

    /**
     Same as findByStatusNotAndExpireDateAfter but for specific type of report.
     */
    @Query(value = "SELECT * FROM reports AS r " +
            "WHERE :type = r.type " +
            "AND r.latitude BETWEEN (:lat - 0.0006) AND (:lat + 0.0006) " +
            "AND r.longitude BETWEEN (:lon - 0.0008) AND (:lon + 0.0008) " +
            "AND (6371 * acos(cos(radians(:lat)) * cos(radians(r.latitude)) *" +
            " cos(radians(r.longitude) - radians(:lon)) + sin(radians(:lat)) *" +
            " sin(radians(r.latitude)))) < :radius",
            nativeQuery = true
    )
    List<Report> findNearbyReportsByType(@Param("lat") Double user_latitude,
                                   @Param("lon") Double user_longitude,
                                   @Param("radius") Double user_radius,
                                   @Param("type") String type);


    /** Performs a cleanup of the reports table by removing invalid or outdated entries.  */
    @Modifying
    @Query("DELETE FROM Report r WHERE r.expireDate <= CURRENT_TIMESTAMP OR r.status = RoadReport.enums.ReportStatus.REMOVED")
    void deleteExpiredReports();

    List<Report> findByExpireDateBeforeOrStatus(LocalDateTime now, ReportStatus reportStatus);
}
