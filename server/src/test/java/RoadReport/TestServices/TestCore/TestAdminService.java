package RoadReport.TestServices.TestCore;

import RoadReport.entities.Comment;
import RoadReport.entities.Report;
import RoadReport.entities.User;
import RoadReport.enums.ReportStatus;
import RoadReport.enums.Role;
import RoadReport.exceptions.core.AdminOperationException;
import RoadReport.exceptions.special.BadRequestException;
import RoadReport.security.service.RoadUserDetails;
import RoadReport.services.core.AdminService;
import RoadReport.services.core.CommentService;
import RoadReport.services.core.ReportService;
import RoadReport.services.core.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TestAdminService {

    @Mock
    private UserService userService;

    @Mock
    private ReportService reportService;

    @Mock
    private CommentService commentService;

    @InjectMocks
    private AdminService adminService;

    private User normalUser;
    private User adminUser;
    private Report normalReport;
    private Report adminReport;
    private Comment normalComment;
    private Comment adminComment;
    private RoadUserDetails adminUserDetails;

    @BeforeEach
    void setUp() {
        normalUser = User.builder()
                .id(1L)
                .username("regular_guy")
                .roles(Role.USER)
                .banned(false)
                .reputationScore(10)
                .build();

        adminUser = User.builder()
                .id(2L)
                .username("super_admin")
                .roles(Role.ADMIN)
                .build();

        normalReport = Report.builder().id(10L).user(normalUser).status(ReportStatus.TEMPORARY).build();
        adminReport = Report.builder().id(20L).user(adminUser).status(ReportStatus.TEMPORARY).build();

        normalComment = new Comment();
        normalComment.setId(100L);
        normalComment.setUser(normalUser);

        adminComment = new Comment();
        adminComment.setId(200L);
        adminComment.setUser(adminUser);

        adminUserDetails = new RoadUserDetails(adminUser);
    }


    @Test
    public void testBanUser_Success() {
        when(userService.getUserById(1L)).thenReturn(normalUser);

        adminService.banUser(1L, 5);

        assertTrue(normalUser.getBanned());
        assertNotNull(normalUser.getBanExpiration());
        assertTrue(normalUser.getBanExpiration().isAfter(LocalDateTime.now()));
    }

    @Test
    public void testBanUser_AdminShield_ThrowsException() {
        when(userService.getUserById(2L)).thenReturn(adminUser);

        assertThrows(AdminOperationException.class, () -> {
            adminService.banUser(2L, 5);
        });

        assertFalse(adminUser.getBanned() != null && adminUser.getBanned());
    }

    @Test
    public void testBanUser_NegativeDays_ThrowsException() {
        when(userService.getUserById(1L)).thenReturn(normalUser);

        assertThrows(BadRequestException.class, () -> {
            adminService.banUser(1L, 0);
        });

        assertThrows(BadRequestException.class, () -> {
            adminService.banUser(1L, -5);
        });
    }


    @Test
    public void testUnbanUser_Success() {
        normalUser.setBanned(true);
        normalUser.setBanExpiration(LocalDateTime.now().plusDays(5));
        when(userService.getUserById(1L)).thenReturn(normalUser);

        adminService.unbanUser(1L);

        assertFalse(normalUser.getBanned());
        assertNull(normalUser.getBanExpiration());
    }


    @Test
    public void testDeleteUser_Success() {
        when(userService.getUserById(1L)).thenReturn(normalUser);

        adminService.deleteUser(1L);

        verify(userService, times(1)).deleteUser(1L);
    }

    @Test
    public void testDeleteUser_AdminShield_ThrowsException() {
        when(userService.getUserById(2L)).thenReturn(adminUser);

        assertThrows(AdminOperationException.class, () -> {
            adminService.deleteUser(2L);
        });

        verify(userService, never()).deleteUser(anyLong());
    }

    @Test
    public void testAdjustReputation_Reset_Success() {
        when(userService.getUserById(1L)).thenReturn(normalUser);

        adminService.adjustReputation(1L, true, null);

        assertEquals(0, normalUser.getReputationScore());
    }

    @Test
    public void testAdjustReputation_AddScore_Success() {
        when(userService.getUserById(1L)).thenReturn(normalUser);

        adminService.adjustReputation(1L, false, 5);

        assertEquals(15, normalUser.getReputationScore());
    }

    @Test
    public void testAdjustReputation_NullScore_ThrowsException() {
        when(userService.getUserById(1L)).thenReturn(normalUser);

        assertThrows(BadRequestException.class, () -> {
            adminService.adjustReputation(1L, false, null);
        });
    }

    @Test
    public void testAdjustReputation_AdminShield_ThrowsException() {
        when(userService.getUserById(2L)).thenReturn(adminUser);

        assertThrows(AdminOperationException.class, () -> {
            adminService.adjustReputation(2L, true, 0);
        });
    }


    @Test
    public void testOverrideReportStatus_Success() {
        when(reportService.getReportById(10L)).thenReturn(normalReport);

        adminService.overrideReportStatus(10L, ReportStatus.PERMANENT);

        assertEquals(ReportStatus.PERMANENT, normalReport.getStatus());
    }

    @Test
    public void testOverrideReportStatus_AdminShield_ThrowsException() {
        when(reportService.getReportById(20L)).thenReturn(adminReport);

        assertThrows(AdminOperationException.class, () -> {
            adminService.overrideReportStatus(20L, ReportStatus.PERMANENT);
        });
    }

    @Test
    public void testDeleteReport_Success() {
        when(reportService.getReportById(10L)).thenReturn(normalReport);

        adminService.deleteReport(10L);

        verify(reportService, times(1)).deleteReport(normalReport);
    }

    @Test
    public void testDeleteReport_AdminShield_ThrowsException() {
        when(reportService.getReportById(20L)).thenReturn(adminReport);

        assertThrows(AdminOperationException.class, () -> {
            adminService.deleteReport(20L);
        });

        verify(reportService, never()).deleteReport(any());
    }


    @Test
    public void testDeleteComment_Success() {
        when(commentService.getCommentById(100L)).thenReturn(normalComment);

        adminService.deleteComment(100L, adminUserDetails);

        verify(commentService, times(1)).deleteComment(100L, adminUserDetails.getId());
    }

    @Test
    public void testDeleteComment_AdminShield_ThrowsException() {
        when(commentService.getCommentById(200L)).thenReturn(adminComment);

        assertThrows(AdminOperationException.class, () -> {
            adminService.deleteComment(200L, adminUserDetails);
        });

        verify(commentService, never()).deleteComment(anyLong(), anyLong());
    }
}