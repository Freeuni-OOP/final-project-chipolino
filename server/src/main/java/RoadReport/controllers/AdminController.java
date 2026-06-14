package RoadReport.controllers;

import RoadReport.enums.ReportStatus;
import RoadReport.security.service.RoadUserDetails;
import RoadReport.services.core.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @PatchMapping("/users/{userId}/ban")
    public ResponseEntity<String> banUser(@PathVariable Long userId, @RequestParam Integer daysToBan) {
        adminService.banUser(userId, daysToBan);
        return ResponseEntity.ok("User banned successfully.");
    }

    @PatchMapping("/users/{userId}/unban")
    public ResponseEntity<String> unbanUser(@PathVariable Long userId) {
        adminService.unbanUser(userId);
        return ResponseEntity.ok("User unbanned successfully.");
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<String> deleteUser(@PathVariable Long userId) {
        adminService.deleteUser(userId);
        return ResponseEntity.ok("User deleted successfully.");
    }

    @PatchMapping("/users/{userId}/reputation")
    public ResponseEntity<String> adjustReputation(@PathVariable Long userId,
                                                   @RequestParam boolean isReset,
                                                   @RequestParam(required = false) Integer score) {
        adminService.adjustReputation(userId, isReset, score);
        return ResponseEntity.ok("Reputation adjusted successfully.");
    }

    @PatchMapping("/reports/{reportId}/status")
    public ResponseEntity<String> overrideReportStatus(@PathVariable Long reportId,
                                                   @RequestParam ReportStatus newReportStatus) {
        adminService.overrideReportStatus(reportId, newReportStatus);
        return ResponseEntity.ok("Report status updated successfully.");
    }

    @DeleteMapping("/reports/{reportId}")
    public ResponseEntity<String> deleteReport(@PathVariable Long reportId) {
        adminService.deleteReport(reportId);
        return ResponseEntity.ok("Report deleted successfully.");
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<String> deleteComment(@PathVariable Long commentId,
                                                @AuthenticationPrincipal RoadUserDetails roadUserDetails) {
        adminService.deleteComment(commentId, roadUserDetails);
        return ResponseEntity.ok("Comment deleted successfully.");
    }
}
