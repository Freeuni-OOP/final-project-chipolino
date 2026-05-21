package RoadReport.TestRepositories;

import RoadReport.entities.Report;
import RoadReport.entities.User;
import RoadReport.entities.Vote;
import RoadReport.enums.ReportStatus;
import RoadReport.enums.ReportType;
import RoadReport.enums.Role;
import RoadReport.enums.VoteType;
import RoadReport.repositories.VoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
public class TestVoteRepository {
    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private VoteRepository voteRepository;

    User userA, userB;
    Report reportA, reportB;

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

    private void addVote(User user, Report report, VoteType type) {
        Vote vote = new Vote();
        vote.setUser(user);
        vote.setReport(report);
        vote.setType(type);
        entityManager.persist(vote);
    }

    @Test
    public void testFindByReportIdAndUserId() {
        addVote(userA, reportA, VoteType.POSITIVE);
        entityManager.flush();

        Optional<Vote> foundVote = voteRepository.findByReportIdAndUserId(reportA.getId(), userA.getId());
        assertTrue(foundVote.isPresent());
        assertEquals(VoteType.POSITIVE, foundVote.get().getType());

        Optional<Vote> emptyVote = voteRepository.findByReportIdAndUserId(reportA.getId(), userB.getId());
        assertTrue(emptyVote.isEmpty());

        Optional<Vote> nonExistentReportVote = voteRepository.findByReportIdAndUserId(999L, userA.getId());
        assertTrue(nonExistentReportVote.isEmpty());
    }

    @Test
    public void testCountByReportIdAndType() {
        addVote(userA, reportA, VoteType.POSITIVE);
        addVote(userB, reportA, VoteType.POSITIVE);
        addVote(userB, reportB, VoteType.NEGATIVE);
        entityManager.flush();

        long upCount = voteRepository.countByReportIdAndType(reportA.getId(), VoteType.POSITIVE);
        assertEquals(2, upCount);

        long downCount = voteRepository.countByReportIdAndType(reportA.getId(), VoteType.NEGATIVE);
        assertEquals(0, downCount);

        long reportBDownCount = voteRepository.countByReportIdAndType(reportB.getId(), VoteType.NEGATIVE);
        assertEquals(1, reportBDownCount);
    }

    @Test
    public void testFindByUserId() {
        addVote(userA, reportA, VoteType.POSITIVE);
        addVote(userA, reportB, VoteType.NEGATIVE);
        addVote(userB, reportA, VoteType.POSITIVE);
        entityManager.flush();

        List<Vote> userAVotes = voteRepository.findByUserId(userA.getId());
        assertEquals(2, userAVotes.size());

        List<Vote> userBVotes = voteRepository.findByUserId(userB.getId());
        assertEquals(1, userBVotes.size());
        assertEquals(VoteType.POSITIVE, userBVotes.get(0).getType());

        List<Vote> emptyVotes = voteRepository.findByUserId(999L);
        assertTrue(emptyVotes.isEmpty());
    }
}
