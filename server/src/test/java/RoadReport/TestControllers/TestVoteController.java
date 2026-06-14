package RoadReport.TestControllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import RoadReport.controllers.VoteController;
import RoadReport.enums.VoteType;
import RoadReport.security.service.JwtService;
import RoadReport.security.service.RoadUserDetails;
import RoadReport.security.service.RoadUserDetailsService;
import RoadReport.services.core.VoteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(VoteController.class)
@ExtendWith(MockitoExtension.class)
public class TestVoteController {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private VoteService voteService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private RoadUserDetailsService roadUserDetailsService;

    @Mock
    private RoadUserDetails mockDetails;

    @BeforeEach
    public void setUp() {
        lenient().when(mockDetails.getId()).thenReturn(1L);
        lenient().when(mockDetails.getUsername()).thenReturn("Giorgi");
        lenient().when(mockDetails.getPassword()).thenReturn("password");
    }

    @Test
    @WithMockUser(username = "Giorgi")
    public void testUpvoteReportOK() throws Exception {
        mvc.perform(post("/api/vote/{reportId}/upvote", 100L)
                        .with(csrf())
                        .with(user(mockDetails)))
                .andExpect(status().isOk());

        verify(voteService).createVote(100L, 1L, VoteType.POSITIVE);
    }

    @Test
    public void testUpvoteReportUnauthorized() throws Exception {
        mvc.perform(post("/api/vote/{reportId}/upvote", 100L)
                        .with(csrf()))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(voteService);
    }

    @Test
    @WithMockUser(username = "Giorgi")
    public void testDownvoteReportOK() throws Exception {
        mvc.perform(post("/api/vote/{reportId}/downvote", 100L)
                        .with(csrf())
                        .with(user(mockDetails)))
                .andExpect(status().isOk());

        verify(voteService).createVote(100L, 1L, VoteType.NEGATIVE);
    }

    @Test
    public void testDownvoteReportUnauthorized() throws Exception {
        mvc.perform(post("/api/vote/{reportId}/downvote", 100L)
                        .with(csrf()))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(voteService);
    }

    @Test
    @WithMockUser(username = "Giorgi")
    public void testGetVotesOK() throws Exception {
        when(voteService.countByReportIdAndType(100L, VoteType.POSITIVE)).thenReturn(25L);
        when(voteService.countByReportIdAndType(100L, VoteType.NEGATIVE)).thenReturn(4L);

        mvc.perform(get("/api/vote/{reportId}/votes", 100L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(voteService).countByReportIdAndType(100L, VoteType.POSITIVE);
        verify(voteService).countByReportIdAndType(100L, VoteType.NEGATIVE);
    }
}