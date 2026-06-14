package RoadReport.TestControllers;

import RoadReport.config.SecurityConfig;
import RoadReport.controllers.CommentController;
import RoadReport.controllers.dto.CommentRequest;
import RoadReport.entities.Comment;
import RoadReport.entities.User;
import RoadReport.security.service.JwtService;
import RoadReport.security.service.RoadUserDetails;
import RoadReport.security.service.RoadUserDetailsService;
import RoadReport.services.core.CommentService;
import RoadReport.services.core.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CommentController.class)
@ExtendWith(MockitoExtension.class)
@EnableMethodSecurity
@Import(SecurityConfig.class)
public class TestCommentController {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CommentService commentService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private RoadUserDetailsService roadUserDetailsService;

    private User mockUser;
    private Comment mockComment;
    private RoadUserDetails mockUserDetails;

    @BeforeEach
    public void setUp() {
        mockUser = User.builder()
                .id(1L)
                .username("Giorgi")
                .build();

        mockComment = new Comment();
        mockComment.setId(10L);
        mockComment.setText("This is a test comment");
        mockComment.setUser(mockUser);
        mockComment.setCreateDate(LocalDateTime.now());

        mockUserDetails = mock(RoadUserDetails.class);

        lenient().when(mockUserDetails.getId()).thenReturn(1L);
        lenient().when(mockUserDetails.getUsername()).thenReturn("Giorgi");
        lenient().when(mockUserDetails.getPassword()).thenReturn("password");
    }

    @Test
    public void testAddCommentOK() throws Exception {
        CommentRequest request = new CommentRequest("New awesome comment");

        mvc.perform(post("/api/reports/100/comments")
                        .with(user(mockUserDetails))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        verify(commentService).addComment(1L, 100L, "New awesome comment");
    }

    @Test
    public void testAddCommentError() throws Exception {
        mvc.perform(post("/api/reports/100/comments")
                        .with(user(mockUserDetails))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(commentService);
    }

    @Test
    @WithMockUser(username = "Giorgi")
    public void testGetCommentsByReportOK() throws Exception {
        when(commentService.getCommentsByReport(100L)).thenReturn(List.of(mockComment));

        mvc.perform(get("/api/reports/100/comments")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10L))
                .andExpect(jsonPath("$[0].content").value("This is a test comment"))
                .andExpect(jsonPath("$[0].authorUsername").value("Giorgi"));

        verify(commentService).getCommentsByReport(100L);
    }

    @Test
    public void testUpdateCommentOK() throws Exception {
        CommentRequest request = new CommentRequest("Updated comment text");

        Comment updatedComment = new Comment();
        updatedComment.setId(10L);
        updatedComment.setText("Updated comment text");
        updatedComment.setUser(mockUser);
        updatedComment.setCreateDate(mockComment.getCreateDate());

        when(commentService.updateComment(10L, 1L, "Updated comment text")).thenReturn(updatedComment);

        mvc.perform(put("/api/comments/10")
                        .with(user(mockUserDetails))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.content").value("Updated comment text"))
                .andExpect(jsonPath("$.authorUsername").value("Giorgi"));

        verify(commentService).updateComment(10L, 1L, "Updated comment text");
    }

    @Test
    public void testUpdateCommentError() throws Exception {
        mvc.perform(put("/api/comments/10")
                        .with(user(mockUserDetails))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(commentService);
    }

    @Test
    public void testDeleteCommentOK() throws Exception {
        mvc.perform(delete("/api/comments/10")
                        .with(user(mockUserDetails))
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(commentService).deleteComment(10L, 1L);
    }
}