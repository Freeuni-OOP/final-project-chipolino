package RoadReport.services.core;

import RoadReport.entities.Comment;
import RoadReport.entities.Report;
import RoadReport.entities.User;
import RoadReport.entities.Vote;
import RoadReport.enums.Role;
import RoadReport.repositories.CommentRepository;
import RoadReport.repositories.ReportRepository;
import RoadReport.repositories.UserRepository;
import RoadReport.repositories.VoteRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;


/**
 * Service class responsible for managing user operations.
 * Handles user registration, secure password hashing,
 * and business logic related to user reputation, status, and bans.
 */
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ReportRepository reportRepository;
    private final CommentRepository commentRepository;
    private final VoteRepository voteRepository;

    private final static int MINUS_REP_REPORT = 5;
    private final int VOTE_POINT = 1;
    private final static int NON_RELIABLE  = -15;
    private final static int RELIABLE_RESTORE_POINT = 20;
    private final static int MAX_REJECTED_REPORTS = 3;



    /**
     * Registers a new user in the system.
     * Validates the uniqueness of the username and email,hashes the
     * user's password, and saves to the database.
     *
     * @param user the User which is trying to register
     * @throws IllegalArgumentException if the provided username or email is already in use
     */
    @Transactional
    public void registerUser(User user) {
        String name = user.getUsername();
        String email = user.getEmail();
        if (userRepository.findUserByEmail(email).isPresent() || userRepository.findUserByUsername(name).isPresent()) {
            throw new IllegalArgumentException("This name or email is taken");
        }
        String password = user.getPassword();
        String hashedPassword = passwordEncoder.encode(password);
        user.setPassword(hashedPassword);
        userRepository.save(user);
    }

    /**
     * gets a user by their id.
     *
     * @param userId the ID of the user to get
     * @return the found User
     * @throws IllegalArgumentException if no user is found with the given ID
     */
    public User getUserById(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
    }

    /**
     * gets a user by their username.
     *
     * @param name the username of the user to get
     * @return the found User
     * @throws IllegalArgumentException if no user is found with the given username
     */
    public User getUserByUsername(String name) {
        return userRepository.findUserByUsername(name).orElseThrow(() -> new IllegalArgumentException("User not found with name: " + name));
    }

    /**
     * gets a user by their email address.
     *
     * @param email the email address of the user to get
     * @return the found User
     * @throws IllegalArgumentException if no user is found with the given email
     */
    public User getUserByEmail(String email) {
        return userRepository.findUserByEmail(email).orElseThrow(() -> new IllegalArgumentException("User not found with name: " + email));
    }

    /**
     * Punish a user when a report they submitted is rejected by the system.
     * Decreases their reputation score and increase the count of rejected reports.
     * Automatically applies a 3-day ban if the rejected reports max amount is reached.
     *
     * @param userId the ID of the user who submitted the rejected report
     */
    @Transactional
    public void handleRejectedReport(Long userId) {
        User user = getUserById(userId);
        user.setRejectedReportsCount(user.getRejectedReportsCount() + 1);
        user.setReputationScore(user.getReputationScore() - MINUS_REP_REPORT);

        if (user.getRejectedReportsCount() >= MAX_REJECTED_REPORTS) {
            user.setRejectedReportsCount(0);
            user.setBanned(true);
            user.setBanExpiration(LocalDateTime.now().plusDays(3));
        }
    }

    /**
     * Punish a user when a vote they submit is deemed incorrect or rejected.
     * Decreases their reputation score and marks them as non-reliable if their
     * score falls to or below the designated score.
     *
     * @param userId the ID of the user whose vote was rejected
     */
    @Transactional
    public void handleRejectedVote(Long userId) {
        User user = getUserById(userId);
        user.setReputationScore(user.getReputationScore() - VOTE_POINT);

        if (user.getReputationScore() <= NON_RELIABLE) {
            user.setReputationScore(0);
            user.setNonReliable(true);
        }
    }

    /**
     * Rewards a user when a vote they cast is accepted and verified.
     * Increases their reputation score and restores their reliable status if
     * their score reaches the required restoration threshold.
     *
     * @param userId the ID of the user whose vote was accepted
     */
    @Transactional
    public void handleAcceptedVote(Long userId) {
        User user = getUserById(userId);
        user.setReputationScore(user.getReputationScore() + VOTE_POINT);

        if (user.getReputationScore() >= RELIABLE_RESTORE_POINT) {
            user.setNonReliable(false);
        }
    }


    /**
     * Checks the current ban status of a user.
     * if the user is banned but their ban
     * expiration date has passed, the ban is automatically canceled.
     *
     * @param userId the ID of the user to check
     * @return true if the user is currently banned, false otherwise
     */
    @Transactional
    public boolean userIsBanned(Long userId) {
        User user = getUserById(userId);
        if (user.getBanned()) {
            if (user.getBanExpiration() != null && user.getBanExpiration().isBefore(LocalDateTime.now())) {
                user.setBanned(false);
                user.setBanExpiration(null);
                userRepository.save(user);
                return false;
            }
            return true;
        }
        return false;
    }

    /**
     * Safely deletes a user and reassigning all their submitted reports to a
     * "Ghost User" before removal to preserve application history.
     *
     * @param userId the ID of the user to delete
     * @throws IllegalArgumentException if the user to delete is not found
     */
    @Transactional
    public void deleteUser(Long userId) {
        User user = getUserById(userId);
        User ghostUser = getOrCreateGhostUser();
        if (user.getId().equals(ghostUser.getId())) {
            throw new IllegalArgumentException("Cannot delete the system ghost user account.");
        }

        List<Report> reports = reportRepository.findByUserId(userId);
        for (Report report : reports) {
            report.setUser(ghostUser);
        }
        reportRepository.saveAll(reports);

        List<Comment> comments = commentRepository.findByUserId(userId);
        for (Comment comment : comments) {
            comment.setUser(ghostUser);
        }
        commentRepository.saveAll(comments);

        List<Vote> votes = voteRepository.findByUserId(userId);
        voteRepository.deleteAll(votes);

        userRepository.delete(user);
    }


    // creates user safely, and ensure that 2 ghost users cant be created at the same time.
    private synchronized User getOrCreateGhostUser() {
        return userRepository.findUserByUsername("ghostUser")
                .orElseGet(() -> {
                    User newGhost = new User();
                    newGhost.setUsername("ghostUser");
                    newGhost.setEmail("ghost@roadreport.ge");
                    newGhost.setPassword("PROTECTED_SYSTEM_ACCOUNT_" + java.util.UUID.randomUUID());
                    newGhost.setRoles(Role.USER);
                    newGhost.setBanned(true);

                    return userRepository.saveAndFlush(newGhost);
                });
    }

}
