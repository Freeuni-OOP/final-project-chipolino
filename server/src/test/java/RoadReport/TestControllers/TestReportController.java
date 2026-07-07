package RoadReport.TestControllers;

import RoadReport.controllers.ReportController;
import RoadReport.controllers.dto.report.ReportRequestDTO;
import RoadReport.entities.Report;
import RoadReport.entities.User;
import RoadReport.enums.ReportStatus;
import RoadReport.enums.ReportType;
import RoadReport.enums.VoteType;
import RoadReport.exceptions.GlobalExceptionHandler;
import RoadReport.exceptions.special.BadRequestException;
import RoadReport.security.service.JwtService;
import RoadReport.security.service.RoadUserDetails;
import RoadReport.security.service.RoadUserDetailsService;
import RoadReport.services.core.ReportService;
import RoadReport.services.core.VoteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
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
@Import(GlobalExceptionHandler.class)
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

    @MockitoBean
    private RoadUserDetailsService roadUserDetailsService;

    private UsernamePasswordAuthenticationToken mockAuth;
    private Report mockReport;

    @BeforeEach
    public void setUp() {
        RoadUserDetails mockUserDetails = mock(RoadUserDetails.class);
        when(mockUserDetails.getId()).thenReturn(1L);
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
                        .with(authentication(mockAuth))
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
        User dummyUser = User.builder()
                .id(1L)
                .username("Luka")
                .build();

        Report mockReport = Report.builder()
                .id(100L)
                .user(dummyUser)
                .type(ReportType.POLICE)
                .upvotes(5)
                .downvotes(0)
                .latitude(41.7)
                .longitude(44.8)
                .status(ReportStatus.TEMPORARY)
                .build();

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
    public void testGetMyReportsOK() throws Exception {
        User dummyUser = User.builder()
                .id(1L)
                .username("Giorgi")
                .build();

        Report userReport = Report.builder()
                .id(200L)
                .user(dummyUser)
                .type(ReportType.SPEED_CAMERA)
                .upvotes(1)
                .downvotes(0)
                .latitude(41.7)
                .longitude(44.8)
                .status(ReportStatus.TEMPORARY)
                .createDate(LocalDateTime.now())
                .build();

        when(reportService.getReportsByUserId(1L)).thenReturn(List.of(userReport));

        mvc.perform(get("/api/reports/me")
                        .with(authentication(mockAuth))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(200))
                .andExpect(jsonPath("$[0].type").value("SPEED_CAMERA"))
                .andExpect(jsonPath("$[0].authorUsername").value("Giorgi"));
        verify(reportService).getReportsByUserId(1L);
    }

    @Test
    public void testFindNearbyReportsZeroOrNegativeRadius() throws Exception {
        User dummyUser = User.builder()
                .id(2L)
                .username("Nika")
                .build();

        Report mockReport2 = Report.builder()
                .id(101L)
                .user(dummyUser)
                .type(ReportType.ROAD_CLOSURE)
                .latitude(41.8)
                .longitude(44.9)
                .status(ReportStatus.PERMANENT)
                .build();

        when(reportService.findAllReports()).thenReturn(List.of(mockReport2));
        when(voteService.findByUserId(1L)).thenReturn(Collections.emptyList());

        mvc.perform(get("/api/reports")
                        .with(authentication(mockAuth))
                        .param("latitude", "41.7")
                        .param("longitude", "44.8")
                        .param("radius", "0.0")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(101))
                .andExpect(jsonPath("$[0].type").value("ROAD_CLOSURE"));

        verify(reportService).findAllReports();
        verify(reportService, never()).findNearbyReports(anyDouble(), anyDouble(), anyDouble());
    }
    @Test
    public void testFindNearbyReportsWithExistingUserVotes() throws Exception {
        User dummyUser = User.builder()
                .id(3L)
                .username("Anano")
                .build();

        Report reportWithVote = Report.builder()
                .id(300L)
                .user(dummyUser)
                .type(ReportType.CUSTOM)
                .latitude(41.7)
                .longitude(44.8)
                .status(ReportStatus.TEMPORARY)
                .build();

        RoadReport.entities.Vote mockVote = mock(RoadReport.entities.Vote.class);
        when(mockVote.getReport()).thenReturn(reportWithVote);
        when(mockVote.getType()).thenReturn(VoteType.POSITIVE);

        when(reportService.findNearbyReports(41.7, 44.8, 5.0)).thenReturn(List.of(reportWithVote));
        when(voteService.findByUserId(1L)).thenReturn(List.of(mockVote));

        mvc.perform(get("/api/reports")
                        .with(authentication(mockAuth))
                        .param("latitude", "41.7")
                        .param("longitude", "44.8")
                        .param("radius", "5.0")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(300))
                .andExpect(jsonPath("$[0].vote").value("POSITIVE"));
        verify(voteService).findByUserId(1L);
        verify(reportService).findNearbyReports(41.7, 44.8, 5.0);
    }
}