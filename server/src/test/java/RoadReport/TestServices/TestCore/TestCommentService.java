package RoadReport.TestServices.TestCore;

import RoadReport.entities.Comment;
import RoadReport.entities.Report;
import RoadReport.entities.User;
import RoadReport.enums.ReportStatus;
import RoadReport.enums.ReportType;
import RoadReport.enums.Role;
import RoadReport.exceptions.core.CommentNotFoundException;
import RoadReport.exceptions.core.ReportNotFoundException;
import RoadReport.exceptions.core.UserNotFoundException;
import RoadReport.repositories.CommentRepository;
import RoadReport.repositories.ReportRepository;
import RoadReport.repositories.UserRepository;
import RoadReport.services.core.CommentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TestCommentService {
    @Mock
    private CommentRepository commentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ReportRepository reportRepository;

    @InjectMocks
    private CommentService commentService;

    private User reportAuthor;
    private User commentAuthor;
    private User randomUser;
    private Report report;

    private Comment standardComment;


    @BeforeEach
    void setUp() {
        initUsers();
        initReport();
        standardComment = createComment(100L, "Be careful, pothole!", commentAuthor, report, LocalDateTime.now());
    }

    private void initUsers() {
        reportAuthor = User.builder()
                .id(1L)
                .username("report_creator")
                .roles(Role.USER)
                .build();

        commentAuthor = User.builder()
                .id(2L)
                .username("comment_author")
                .roles(Role.USER)
                .build();

        randomUser = User.builder()
                .id(3L)
                .username("random_user")
                .roles(Role.USER)
                .build();
    }

    private void initReport() {
        report = Report.builder()
                .id(10L)
                .user(reportAuthor)
                .description("Test")
                .latitude(41.71)
                .longitude(44.82)
                .type(ReportType.ACCIDENT)
                .status(ReportStatus.TEMPORARY)
                .comments(new ArrayList<>())
                .build();
    }

    private Comment createComment(Long id, String text, User user, Report targetReport, LocalDateTime date) {
        Comment newComment = new Comment();
        newComment.setId(id);
        newComment.setText(text);
        newComment.setUser(user);
        newComment.setReport(targetReport);
        newComment.setCreateDate(date);
        return newComment;
    }

    // -----------------------------------------------
    //                 TESTS
    //------------------------------------------------

    @Test
    public void testDeleteCommentSuccess() {
        when(commentRepository.findById(100L)).thenReturn(Optional.of(standardComment));
        when(userRepository.findById(commentAuthor.getId())).thenReturn(Optional.of(commentAuthor));
        commentService.deleteComment(100L, commentAuthor.getId());

        ArgumentCaptor<Comment> commentCaptor = ArgumentCaptor.forClass(Comment.class);
        verify(commentRepository, times(1)).delete(commentCaptor.capture());

        Comment deletedComment = commentCaptor.getValue();

        assertAll(
                () -> assertNotNull(deletedComment),
                () -> assertEquals(100L, deletedComment.getId()),
                () -> assertEquals(commentAuthor.getId(), deletedComment.getUser().getId())
        );
        verify(commentRepository, times(1)).findById(100L);
        verifyNoMoreInteractions(commentRepository);
    }

    @Test
    public void testDeleteCommentUnSuccessful() {
        when(commentRepository.findById(100L)).thenReturn(Optional.of(standardComment));
        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
            commentService.deleteComment(100L, randomUser.getId());
        });

        verify(commentRepository, never()).delete(any(Comment.class));
        verify(commentRepository, times(1)).findById(100L);
        verifyNoMoreInteractions(commentRepository);

        when(commentRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(CommentNotFoundException.class, () -> {
            commentService.deleteComment(999L, commentAuthor.getId());
        });

        verify(commentRepository, never()).delete(any(Comment.class));
        verify(commentRepository, times(1)).findById(999L);
        verifyNoMoreInteractions(commentRepository);
    }

    @Test
    public void testAddComment() {
        when(userRepository.findById(commentAuthor.getId())).thenReturn(Optional.of(commentAuthor));
        when(reportRepository.findById(report.getId())).thenReturn(Optional.of(report));

        commentService.addComment(commentAuthor.getId(), report.getId(), standardComment.getText());
        ArgumentCaptor<Comment> saveCaptor = ArgumentCaptor.forClass(Comment.class);
        verify(commentRepository, times(1)).save(saveCaptor.capture());

        Comment savedComment = saveCaptor.getValue();

        assertAll(
                () -> assertEquals("Be careful, pothole!", savedComment.getText()),
                () -> assertNotNull(savedComment.getUser()),
                () -> assertEquals(commentAuthor.getId(), savedComment.getUser().getId()),
                () -> assertNotNull(savedComment.getReport()),
                () -> assertEquals(report.getId(), savedComment.getReport().getId())
        );

        verifyNoMoreInteractions(commentRepository);
    }

    @Test
    public void testAddCommentUnSuccessful() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> {
            commentService.addComment(999L, report.getId(), "This should fail");
        });
        verify(commentRepository, never()).save(any(Comment.class));

        when(userRepository.findById(commentAuthor.getId())).thenReturn(Optional.of(commentAuthor));
        when(reportRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(ReportNotFoundException.class, () -> {
            commentService.addComment(commentAuthor.getId(), 999L, "This should fail");
        });
        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    public void testGetCommentsByReport() {
        when(commentRepository.findByReportId(report.getId())).thenReturn(List.of(standardComment));
        List<Comment> comments = commentService.getCommentsByReport(report.getId());

        assertAll(
                () -> assertNotNull(comments),
                () -> assertEquals(1, comments.size()),
                () -> assertEquals(standardComment.getId(), comments.get(0).getId())
        );
        verify(commentRepository, times(1)).findByReportId(report.getId());
        verifyNoMoreInteractions(commentRepository);

        when(commentRepository.findByReportId(999L)).thenReturn(new ArrayList<>());
        List<Comment> comments2 = commentService.getCommentsByReport(999L);

        assertAll(
                () -> assertNotNull(comments2),
                () -> assertTrue(comments2.isEmpty())
        );

        verify(commentRepository, times(1)).findByReportId(999L);
        verifyNoMoreInteractions(commentRepository);
    }

    @Test
    public void testGetCommentsByUser() {
        when(commentRepository.findByUserId(commentAuthor.getId())).thenReturn(List.of(standardComment));
        List<Comment> comments = commentService.getCommentsByUser(commentAuthor.getId());

        assertAll(
                () -> assertNotNull(comments),
                () -> assertEquals(1, comments.size()),
                () -> assertEquals(standardComment.getId(), comments.get(0).getId()),
                () -> assertEquals(commentAuthor.getId(), comments.get(0).getUser().getId())
        );

        verify(commentRepository, times(1)).findByUserId(commentAuthor.getId());
        verifyNoMoreInteractions(commentRepository);

        when(commentRepository.findByUserId(999L)).thenReturn(new ArrayList<>());
        List<Comment> comments2 = commentService.getCommentsByUser(999L);
        assertAll(
                () -> assertNotNull(comments2),
                () -> assertTrue(comments2.isEmpty())
        );
        verify(commentRepository, times(1)).findByUserId(999L);
        verifyNoMoreInteractions(commentRepository);
    }

}
