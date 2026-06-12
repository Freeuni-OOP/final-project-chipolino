package RoadReport.TestControllers;

import RoadReport.config.SecurityConfig;
import RoadReport.controllers.RouteController;
import RoadReport.controllers.dto.RouteRequest;
import RoadReport.entities.Report;
import RoadReport.security.service.JwtService;
import RoadReport.security.service.RoadUserDetailsService;
import RoadReport.services.core.ReportService;
import RoadReport.services.map.GraphHopperService;
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

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RouteController.class)
@ExtendWith(MockitoExtension.class)
@EnableMethodSecurity
@Import(SecurityConfig.class)
public class TestRouteController {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private GraphHopperService graphHopperService;

    @MockitoBean
    private ReportService reportService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private RoadUserDetailsService roadUserDetailsService;

    private List<double[]> mockWaypoints;
    private List<Report> activeReports;
    private GraphHopperService.RouteResult mockServiceResult;

    @BeforeEach
    public void setUp() {
        mockWaypoints = List.of(
                new double[]{41.7151, 44.8271},
                new double[]{41.7200, 44.8300}
        );

        activeReports = new ArrayList<>();
        mockServiceResult = new GraphHopperService.RouteResult(
                1540.5,
                360000L,
                mockWaypoints
        );
    }

    @Test
    @WithMockUser
    public void testCalculateOptimalRoute() throws Exception {
        RouteRequest request = new RouteRequest(mockWaypoints);

        when(reportService.getActiveReports()).thenReturn(activeReports);
        when(graphHopperService.getRouteViaWaypoints(anyList(), anyList())).thenReturn(mockServiceResult);

        mvc.perform(post("/api/routes/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.distanceMeters").value(1540.5))
                .andExpect(jsonPath("$.timeMillis").value(360000))
                .andExpect(jsonPath("$.points").isArray());

        verify(reportService, times(1)).getActiveReports();
        verify(graphHopperService, times(1)).getRouteViaWaypoints(anyList(), anyList());
    }

    @Test
    @WithMockUser
    public void testCalculateOptimalRouteError() throws Exception {
        mvc.perform(post("/api/routes/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(reportService);
        verifyNoInteractions(graphHopperService);
    }
}