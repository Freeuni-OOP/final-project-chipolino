package RoadReport.services.core;

import RoadReport.controllers.dto.user.UserUpdateDTO;
import RoadReport.entities.Comment;
import RoadReport.entities.Report;
import RoadReport.entities.User;
import RoadReport.entities.Vote;
import RoadReport.enums.VoteType;
import RoadReport.exceptions.special.BadRequestException;
import RoadReport.exceptions.core.UserAlreadyExistsException;
import RoadReport.exceptions.core.UserNotFoundException;
import RoadReport.repositories.CommentRepository;
import RoadReport.repositories.ReportRepository;
import RoadReport.repositories.UserRepository;
import RoadReport.repositories.VoteRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


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
    private final static int VOTE_POINT = 1;
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
        if (userRepository.findUserByEmail(email).isPresent()) {
            throw new UserAlreadyExistsException("This email is taken");
        }
        if(userRepository.findUserByUsername(name).isPresent()){
            throw new UserAlreadyExistsException("This name is taken");
        }
        String password = user.getPassword();
        String hashedPassword = passwordEncoder.encode(password);
        user.setPassword(hashedPassword);
        userRepository.save(user);
    }

    /**
     * Gets a user by their id.
     *
     * @param userId the ID of the user to get
     * @return the found User
     * @throws IllegalArgumentException if no user is found with the given ID
     */
    public User getUserById(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));
    }

    /**
     * Gets a user by their username
     *
     * @param name the username of the user to get
     * @return the found User
     * @throws IllegalArgumentException if no user is found with the given username
     */
    public User getUserByUsername(String name) {
        return userRepository.findUserByUsername(name).orElseThrow(() -> new UserNotFoundException("User not found with name: " + name));
    }

    /**
     * gets a user by their email address.
     *
     * @param email the email address of the user to get
     * @return the found User
     * @throws IllegalArgumentException if no user is found with the given email
     */
    public User getUserByEmail(String email) {
        return userRepository.findUserByEmail(email).orElseThrow(() -> new UserNotFoundException("User not found with name: " + email));
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
     * Updates the profile information of an existing user.
     * @param userId     id of user, whose information should be changed
     * @param updateData the DTO containing username, email, and password
     * @return new data of updated user
     */
    @Transactional
    public User updateUser(Long userId, UserUpdateDTO updateData) {
        User user = getUserById(userId);

        if (StringUtils.hasText(updateData.username())) {
            Optional<User> existingUser = userRepository.findUserByUsername(updateData.username());
            if (existingUser.isPresent() && !existingUser.get().getId().equals(userId)) {
                throw new UserAlreadyExistsException("The username "+ updateData.username() +" is already taken");
            }
            user.setUsername(updateData.username());
        }

        if (StringUtils.hasText(updateData.email())) {
            Optional<User> existingUser = userRepository.findUserByEmail(updateData.email());
            if (existingUser.isPresent() && !existingUser.get().getId().equals(userId)) {
                throw new UserAlreadyExistsException("The email "+ updateData.email() +" is already taken");
            }
            user.setEmail(updateData.email());
        }

        if (StringUtils.hasText(updateData.password())) {
            user.setPassword(passwordEncoder.encode(updateData.password()));
        }

        userRepository.save(user);
        return user;
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
        User ghostUser = userRepository.findUserByUsername("ghostUser").
                orElseThrow(() -> new IllegalStateException("Ghost user account not found."));

        if (user.getId().equals(ghostUser.getId())) {
            throw new BadRequestException("Cannot delete the system ghost user account.");
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

        deleteUserVotes(userId);

        userRepository.delete(user);
    }


    /**
     * Deletes all votes associated with a specific user and synchronizes the
     * affected reports vote counts.
     * @param userId the unique identifier of the user whose votes are to be deleted
     */
    private void deleteUserVotes(Long userId) {

        List<Vote> votes = voteRepository.findByUserId(userId);
        List<Report> reportsToUpdate = new ArrayList<>();

        for (Vote vote : votes) {
            Report report = vote.getReport();
            if (vote.getType() == VoteType.POSITIVE) {
                report.setUpvotes(Math.max(0, report.getUpvotes() - 1));
            } else {
                report.setDownvotes(Math.max(0, report.getDownvotes() - 1));
            }
            reportsToUpdate.add(report);
        }

        reportRepository.saveAll(reportsToUpdate);
        voteRepository.deleteAll(votes);
    }

}
