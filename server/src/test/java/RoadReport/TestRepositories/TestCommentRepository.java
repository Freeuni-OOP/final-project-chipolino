package RoadReport.TestRepositories;

import RoadReport.entities.Comment;
import RoadReport.entities.Report;
import RoadReport.entities.User;
import RoadReport.enums.ReportStatus;
import RoadReport.enums.ReportType;
import RoadReport.enums.Role;
import RoadReport.repositories.CommentRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class TestCommentRepository {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private TestEntityManager entityManager;

    private User userA;
    private User userB;
    private Report reportA;
    private Report reportB;

    @BeforeEach
    public void setUp() {
        userA = createAndPersistUser("ezuga", "gezug24@gmail.com");
        userB = createAndPersistUser("bendo", "nbend24@gmail.com");

        reportA = createAndPersistReport(userA);
        reportB = createAndPersistReport(userA);
    }

    private User createAndPersistUser(String username, String email) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword("ezuzu");
        user.setRoles(Role.ADMIN);
        return entityManager.persist(user);
    }

    private Report createAndPersistReport(User user) {
        Report report = new Report();
        report.setUser(user);
        report.setLatitude(41.7151);
        report.setLongitude(44.8271);
        report.setType(ReportType.ACCIDENT);
        report.setStatus(ReportStatus.TEMPORARY);
        return entityManager.persist(report);
    }

    private void addComment(User user, Report report, String text) {
        Comment comment = new Comment();
        comment.setText(text);
        comment.setUser(user);
        comment.setReport(report);
        entityManager.persist(comment);
    }

    @Test
    public void testCountByReportId() {
        long count = commentRepository.countByReportId(reportA.getId());
        assertEquals(0, count);

        addComment(userA, reportA, "bendo");
        entityManager.flush();
        count = commentRepository.countByReportId(reportA.getId());
        assertEquals(1, count);

        addComment(userA, reportA, "is");
        entityManager.flush();
        count = commentRepository.countByReportId(reportA.getId());
        assertEquals(2, count);

        addComment(userA, reportA, "working");
        entityManager.flush();
        count = commentRepository.countByReportId(reportA.getId());
        assertEquals(3, count);
    }

    @Test
    public void testFindByReportId() {
        addComment(userA, reportA, "bendo");
        addComment(userA, reportA, "nika");
        addComment(userA, reportA, "zuka");
        addComment(userA, reportA, "luka");
        addComment(userA, reportA, "fuka");

        addComment(userA, reportB, "other report");

        entityManager.flush();

        List<Comment> comments = commentRepository.findByReportId(reportA.getId());
        assertNotNull(comments);
        assertEquals(5, comments.size());
        assertEquals("bendo", comments.get(0).getText());
        assertEquals("fuka", comments.get(4).getText());

        List<Comment> emptyComments = commentRepository.findByReportId(999L);
        assertTrue(emptyComments.isEmpty());

        List<Comment> comments2 = commentRepository.findByReportId(reportB.getId());
        assertEquals(1, comments2.size());
        assertEquals("other report", comments2.get(0).getText());
    }

    @Test
    public void testFindByUserId() {
        addComment(userA, reportA, "bendo");
        addComment(userA, reportA, "nika");
        addComment(userA, reportA, "zuka");
        addComment(userA, reportA, "luka");
        addComment(userA, reportA, "fuka");
        addComment(userA, reportA, "harden");

        addComment(userB, reportB, "kertis");
        addComment(userB, reportB, "jones");
        addComment(userB, reportB, "is");
        addComment(userB, reportB, "the");
        addComment(userB, reportB, "GOAT");

        entityManager.flush();

        List<Comment> comments = commentRepository.findByUserId(userA.getId());
        assertNotNull(comments);
        assertEquals(6, comments.size());
        assertEquals("bendo", comments.get(0).getText());
        assertEquals("harden", comments.get(5).getText());

        List<Comment> emptyComments = commentRepository.findByUserId(999L);
        assertTrue(emptyComments.isEmpty());

        List<Comment> comments2 = commentRepository.findByUserId(userB.getId());
        assertEquals(5, comments2.size());
        assertEquals("kertis", comments2.get(0).getText());
    }

}
