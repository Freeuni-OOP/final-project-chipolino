package RoadReport.TestControllers;

import RoadReport.controllers.ReportController;
import RoadReport.controllers.dto.ReportRequestDTO;
import RoadReport.entities.Report;
import RoadReport.enums.ReportStatus;
import RoadReport.enums.ReportType;
import RoadReport.enums.VoteType;
import RoadReport.exceptions.special.BadRequestException;
import RoadReport.security.service.JwtService;
import RoadReport.security.service.RoadUserDetails;
import RoadReport.services.core.ReportService;
import RoadReport.services.core.VoteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReportController.class)
public class TestReportController {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ReportService reportService;

    @MockitoBean
    private VoteService voteService;

    @MockitoBean
    private JwtService jwtService;

    private UsernamePasswordAuthenticationToken mockAuth;
    private Report mockReport;

    @BeforeEach
    public void setUp() {
        RoadUserDetails mockUserDetails = mock(RoadUserDetails.class);
        when(mockUserDetails.getUserId()).thenReturn(1L);
        when(mockUserDetails.getUsername()).thenReturn("Giorgi");

        mockAuth = new UsernamePasswordAuthenticationToken(
                mockUserDetails, null, Collections.emptyList()
        );

        mockReport = Report.builder()
                .id(100L)
                .type(ReportType.POLICE)
                .description("test report")
                .latitude(41.7)
                .longitude(44.8)
                .status(ReportStatus.TEMPORARY)
                .upvotes(5)
                .downvotes(2)
                .createDate(LocalDateTime.now())
                .build();
    }


    @Test
    public void testCreateReportOK() throws Exception {
        ReportRequestDTO requestDTO = new ReportRequestDTO(
                ReportType.POLICE, "test report", 41.7, 44.8
        );

        mvc.perform(post("/api/reports")
                        .with(authentication(mockAuth)) // Injects our custom user
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated());

        verify(reportService).createReport(eq(1L), any(Report.class));
    }

    @Test
    public void testCreateReportInvalidBody() throws Exception {
        mvc.perform(post("/api/reports")
                        .with(authentication(mockAuth))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(reportService);
    }


    @Test
    public void testFindNearbyReportsOK() throws Exception {
        when(reportService.findNearbyReports(41.7, 44.8, 10.0))
                .thenReturn(List.of(mockReport));

        mvc.perform(get("/api/reports")
                        .with(authentication(mockAuth))
                        .param("latitude", "41.7")
                        .param("longitude", "44.8")
                        .param("radius", "10.0")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].type").value("POLICE"))
                .andExpect(jsonPath("$[0].upvotes").value(5));
    }


    @Test
    public void testUpvoteReportOK() throws Exception {
        mvc.perform(post("/api/reports/{id}/upvote", 100L)
                        .with(authentication(mockAuth))
                        .with(csrf()))
                .andExpect(status().isOk());

        verify(voteService).createVote(1L, 100L, VoteType.POSITIVE);
    }

    @Test
    public void testDownvoteReportOK() throws Exception {
        mvc.perform(post("/api/reports/{id}/downvote", 100L)
                        .with(authentication(mockAuth))
                        .with(csrf()))
                .andExpect(status().isOk());

        verify(voteService).createVote(1L, 100L, VoteType.NEGATIVE);
    }


    @Test
    public void testUpvoteReportNotFound() throws Exception {
        doThrow(new BadRequestException("Report not found"))
                .when(voteService).createVote(anyLong(), eq(999L), any(VoteType.class));

        mvc.perform(post("/api/reports/{id}/upvote", 999L)
                        .with(authentication(mockAuth))
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }
}