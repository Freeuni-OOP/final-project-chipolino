package RoadReport.TestControllers;


import RoadReport.config.SecurityConfig;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.util.List;
import RoadReport.controllers.AdminController;
import RoadReport.enums.ReportStatus;
import RoadReport.security.service.JwtService;
import RoadReport.security.service.RoadUserDetails;
import RoadReport.security.service.RoadUserDetailsService;
import RoadReport.services.core.AdminService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminController.class)
@ExtendWith(MockitoExtension.class)
@EnableMethodSecurity
@Import(SecurityConfig.class)
public class TestAdminController {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private AdminService adminService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private RoadUserDetailsService roadUserDetailsService;

    @Mock
    private RoadUserDetails mockAdminDetails;

    @BeforeEach
    public void setUp() {
        lenient().when(mockAdminDetails.getId()).thenReturn(1L);
        lenient().when(mockAdminDetails.getUsername()).thenReturn("AdminGio");

        lenient().doReturn(List.of(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .when(mockAdminDetails).getAuthorities();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testBanUserOK() throws Exception {
        doNothing().when(adminService).banUser(2L, 5);

        mvc.perform(patch("/api/admin/users/{userId}/ban", 2L)
                        .param("daysToBan", "5")
                        .with(csrf())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("User banned successfully."));

        verify(adminService).banUser(2L, 5);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testUnbanUserOK() throws Exception {
        doNothing().when(adminService).unbanUser(2L);

        mvc.perform(patch("/api/admin/users/{userId}/unban", 2L)
                        .with(csrf())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("User unbanned successfully."));

        verify(adminService).unbanUser(2L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testDeleteUserOK() throws Exception {
        doNothing().when(adminService).deleteUser(2L);

        mvc.perform(delete("/api/admin/users/{userId}", 2L)
                        .with(csrf())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("User deleted successfully."));

        verify(adminService).deleteUser(2L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testAdjustReputation_WithScore_OK() throws Exception {
        doNothing().when(adminService).adjustReputation(2L, false, 10);

        mvc.perform(patch("/api/admin/users/{userId}/reputation", 2L)
                        .param("isReset", "false")
                        .param("score", "10")
                        .with(csrf())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("Reputation adjusted successfully."));

        verify(adminService).adjustReputation(2L, false, 10);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testAdjustReputation_Reset_OK() throws Exception {
        doNothing().when(adminService).adjustReputation(2L, true, null);

        mvc.perform(patch("/api/admin/users/{userId}/reputation", 2L)
                        .param("isReset", "true")
                        .with(csrf())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("Reputation adjusted successfully."));

        verify(adminService).adjustReputation(2L, true, null);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testOverrideReportStatusOK() throws Exception {
        doNothing().when(adminService).overrideReportStatus(10L, ReportStatus.PERMANENT);

        mvc.perform(patch("/api/admin/reports/{reportId}/status", 10L)
                        .param("newReportStatus", "PERMANENT")
                        .with(csrf())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("Report status updated successfully."));

        verify(adminService).overrideReportStatus(10L, ReportStatus.PERMANENT);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testDeleteReportOK() throws Exception {
        doNothing().when(adminService).deleteReport(10L);

        mvc.perform(delete("/api/admin/reports/{reportId}", 10L)
                        .with(csrf())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("Report deleted successfully."));

        verify(adminService).deleteReport(10L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testDeleteCommentOK() throws Exception {
        doNothing().when(adminService).deleteComment(eq(100L), any(RoadUserDetails.class));

        mvc.perform(delete("/api/admin/comments/{commentId}", 100L)
                        .with(user(mockAdminDetails))
                        .with(csrf())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("Comment deleted successfully."));

        verify(adminService).deleteComment(eq(100L), any(RoadUserDetails.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    public void testAdminEndpointsForbiddenForNormalUser() throws Exception {
        mvc.perform(delete("/api/admin/users/{userId}", 2L)
                        .with(csrf())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(adminService);
    }
}
