package RoadReport.TestController;

import RoadReport.controllers.CommentController;
import RoadReport.controllers.dto.CommentRequest;
import RoadReport.entities.Comment;
import RoadReport.entities.User;
import RoadReport.security.service.JwtService;
import RoadReport.services.core.CommentService;
import RoadReport.services.core.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CommentController.class)
public class TestCommentController {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CommentService commentService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtService jwtService;

    private User mockUser;
    private Comment mockComment;

    @BeforeEach
    public void setUp() {
        mockUser = User.builder()
                .id(1L)
                .username("Giorgi")
                .email("gezug@gmail.com")
                .password("gezug2000")
                .build();

        mockComment = new Comment();
        mockComment.setId(10L);
        mockComment.setText("This is a test comment");
        mockComment.setUser(mockUser);
        mockComment.setCreateDate(LocalDateTime.now());
    }

    @Test
    @WithMockUser(username = "Giorgi")
    public void testAddCommentOK() throws Exception {
        CommentRequest request = new CommentRequest("New awesome comment");

        when(userService.getUserByUsername("Giorgi")).thenReturn(mockUser);

        mvc.perform(post("/api/reports/100/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isCreated());

        verify(userService).getUserByUsername("Giorgi");
        verify(commentService).addComment(1L, 100L, "New awesome comment");
    }

    @Test
    @WithMockUser(username = "Giorgi")
    public void testAddCommentError() throws Exception {
        mvc.perform(post("/api/reports/100/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
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
    @WithMockUser(username = "Giorgi")
    public void testUpdateComment() throws Exception {
        CommentRequest request = new CommentRequest("Updated comment text");

        Comment updatedComment = new Comment();
        updatedComment.setId(10L);
        updatedComment.setText("Updated comment text");
        updatedComment.setUser(mockUser);
        updatedComment.setCreateDate(mockComment.getCreateDate());

        when(userService.getUserByUsername("Giorgi")).thenReturn(mockUser);
        when(commentService.updateComment(10L, 1L, "Updated comment text")).thenReturn(updatedComment);

        mvc.perform(put("/api/comments/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .accept(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.content").value("Updated comment text"))
                .andExpect(jsonPath("$.authorUsername").value("Giorgi"));

        verify(userService).getUserByUsername("Giorgi");
        verify(commentService).updateComment(10L, 1L, "Updated comment text");
    }

    @Test
    @WithMockUser(username = "Giorgi")
    public void testUpdateCommentError() throws Exception {
        mvc.perform(put("/api/comments/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(commentService);
    }

    @Test
    @WithMockUser(username = "Giorgi")
    public void testDeleteComment() throws Exception {
        when(userService.getUserByUsername("Giorgi")).thenReturn(mockUser);

        mvc.perform(delete("/api/comments/10")
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(userService).getUserByUsername("Giorgi");
        verify(commentService).deleteComment(10L, 1L);
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testAdminDeleteComment() throws Exception {
        mvc.perform(delete("/api/admin/comments/10")
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(commentService).adminDeleteComment(10L);
    }
}
