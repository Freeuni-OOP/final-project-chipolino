package RoadReport.TestRepositories;

import RoadReport.entities.Report;
import RoadReport.entities.User;
import RoadReport.enums.ReportStatus;
import RoadReport.enums.ReportType;
import RoadReport.enums.Role;
import RoadReport.repositories.ReportRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class TestReportRepository {

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private TestEntityManager entityManager;

    private User user1;
    private User user2;
    private Report report1;
    private Report report2;

    private void createUser(User user, String username, String email, String password,
                            Role role, Date createDate) {
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(password);
        user.setRoles(role);
        user.setCreateDate(createDate.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime());
    }

    @BeforeEach
    public void setUp() {
        user1 = new User();
        createUser(user1, "nikushabendo", "nbend24@freeuni.edu.ge",
                "123456", RoadReport.enums.Role.USER,
                java.util.Date.from(java.time.Instant.now()));

        user2 = new User();
        createUser(user2, "kokhora", "gezug24@freeuni.edu.ge",
                "bacho", Role.USER, java.util.Date.from(java.time.Instant.now()));
        entityManager.persist(user1);
        entityManager.persist(user2);

        report1 = Report.builder(). user(user1).votes(new ArrayList<>()).
                comments(new ArrayList<>()).type(ReportType.POLICE).
                longitude(16.00).latitude(42.00).status(ReportStatus.TEMPORARY).
                expireDate(java.time.LocalDateTime.now().plusDays(5)).
                createDate(java.time.LocalDateTime.now()).build();

        report2 = Report.builder(). user(user2).votes(new ArrayList<>()).
                comments(new ArrayList<>()).type(ReportType.ACCIDENT).
                longitude(139.00).latitude(35.00).status(ReportStatus.PERMANENT).
                createDate(java.time.LocalDateTime.now()).build();

        entityManager.persist(report1);
        entityManager.persist(report2);
        entityManager.flush();
    }

    @Test
    public void testFindByUserId() {
        List<Report> reports1 = reportRepository.findByUserId(user1.getId());
        List<Report> reports2 = reportRepository.findByUserId(user2.getId());

        assertEquals(1, reports1.size());
        assertEquals(ReportType.POLICE, reports1.get(0).getType());
        assertEquals(1, reports2.size());
        assertEquals(ReportType.ACCIDENT, reports2.get(0).getType());
    }

    @Test
    public void testFindByStatus() {
        List<Report> temporary1 = reportRepository.findByStatus(ReportStatus.TEMPORARY);
        assertEquals(1, temporary1.size());
        assertEquals(ReportType.POLICE, temporary1.get(0).getType());
        assertEquals(user1.getId(), temporary1.get(0).getUser().getId());

        List<Report> permanent = reportRepository.findByStatus(ReportStatus.PERMANENT);
        assertEquals(1, permanent.size());
        assertEquals(ReportType.ACCIDENT, permanent.get(0).getType());
        assertEquals(user2.getId(), permanent.get(0).getUser().getId());

        List<Report> removed = reportRepository.findByStatus(ReportStatus.REMOVED);
        assertTrue(removed.isEmpty());

        report1.setStatus(ReportStatus.REMOVED);
        reportRepository.save(report1);
        entityManager.flush();

        List<Report> temporary2 = reportRepository.findByStatus(ReportStatus.TEMPORARY);
        assertTrue(temporary2.isEmpty());;

        report1.setStatus(ReportStatus.TEMPORARY);
        reportRepository.save(report1);
        entityManager.flush();
    }

    @Test
    public void testFindNearbyReports() {
        List<Report> nearby = reportRepository.findNearbyReports(42.00, 16.00, 5.0);

        assertEquals(1, nearby.size());
        assertEquals(ReportType.POLICE, nearby.get(0).getType());
        assertEquals(user1.getId(), nearby.get(0).getUser().getId());

        List<Report> smallRadius = reportRepository.findNearbyReports(00.00, 00.00, 0.001);
        assertTrue(smallRadius.isEmpty());

        List<Report> bigRadius = reportRepository.findNearbyReports(42.00, 16.00, 15000.0);
        assertEquals(1, bigRadius.size());
    }

    @Test
    public void testFindNearbyByType() {
        List<Report> nearby = reportRepository.findNearbyReportsByType(
                42.00, 16.00, 5.0, "POLICE");

        assertEquals(1, nearby.size());
        assertEquals(ReportType.POLICE, nearby.get(0).getType());
        assertEquals(user1.getId(), nearby.get(0).getUser().getId());

        List<Report> bigRadius = reportRepository.findNearbyReportsByType
                (42.00, 16.00, 15000.0, "ACCIDENT");
        assertEquals(0, bigRadius.size());
    }

    @Test
    public void testDeleteExpiredReports() {
        report1.setExpireDate(java.time.LocalDateTime.now().minusDays(1));
        reportRepository.save(report1);
        entityManager.flush();

        reportRepository.deleteExpiredReports();
        entityManager.flush();

        List<Report> allReports = reportRepository.findAll();
        assertEquals(1, allReports.size());
        assertEquals(ReportType.ACCIDENT, allReports.get(0).getType());
    }

    @Test
    public void testFindByStatusNotAndExpireDateAfter() {
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        java.time.LocalDateTime searchTime = now.minusHours(1);
        Report validReport = Report.builder().user(user1).votes(new ArrayList<>()).comments(new ArrayList<>())
                .type(ReportType.POLICE).longitude(20.0).latitude(20.0).status(ReportStatus.TEMPORARY)
                .expireDate(now.plusDays(5)).createDate(now).build();

        Report expiredReport = Report.builder().user(user1).votes(new ArrayList<>()).comments(new ArrayList<>())
                .type(ReportType.POLICE).longitude(10.0).latitude(10.0).status(ReportStatus.TEMPORARY)
                .expireDate(now.minusDays(2)).createDate(now).build();

        Report removedReport = Report.builder().user(user1).votes(new ArrayList<>()).comments(new ArrayList<>())
                .type(ReportType.ACCIDENT).longitude(11.0).latitude(11.0).status(ReportStatus.REMOVED)
                .expireDate(now.plusDays(2)).createDate(now).build();

        entityManager.persist(validReport);
        entityManager.persist(expiredReport);
        entityManager.persist(removedReport);
        entityManager.flush();
        List<Report> result = reportRepository.findByStatusNotAndExpireDateAfter(ReportStatus.REMOVED, searchTime);

        List<Long> resultIds = result.stream().map(Report::getId).collect(Collectors.toList());

        assertTrue(resultIds.contains(validReport.getId()));
        assertTrue(resultIds.contains(report1.getId()));
        assertFalse(resultIds.contains(expiredReport.getId()));
        assertFalse(resultIds.contains(removedReport.getId()));
    }


    @Test
    public void testFindActiveReports() {
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        Report inactiveReport = Report.builder().user(user1).votes(new ArrayList<>()).comments(new ArrayList<>())
                .type(ReportType.ACCIDENT).longitude(12.0).latitude(12.0).status(ReportStatus.REMOVED)
                .expireDate(now.minusDays(5)).createDate(now).build();
        entityManager.persist(inactiveReport);
        entityManager.flush();
        List<Report> activeReports = reportRepository.findByStatusNotAndExpireDateAfter(ReportStatus.REMOVED, now);
        assertTrue(activeReports.contains(report1));
        assertTrue(activeReports.contains(report2));
        assertFalse(activeReports.contains(inactiveReport));
    }


    @Test
    public void testDeleteExpiredReportsRemovedStatus() {
        report1.setExpireDate(java.time.LocalDateTime.now().plusDays(10));
        report1.setStatus(ReportStatus.REMOVED);
        reportRepository.save(report1);
        entityManager.flush();
        reportRepository.deleteExpiredReports();
        entityManager.flush();

        List<Report> allReports = reportRepository.findAll();
        assertEquals(1, allReports.size());
        assertFalse(allReports.contains(report1));
        assertTrue(allReports.contains(report2));
    }

}
