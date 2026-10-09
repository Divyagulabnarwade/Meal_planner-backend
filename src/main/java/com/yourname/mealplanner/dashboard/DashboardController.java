package com.yourname.mealplanner.dashboard;

import com.yourname.mealplanner.common.ApiResponse;
import com.yourname.mealplanner.dashboard.dto.DashboardSummaryResponse;
import com.yourname.mealplanner.user.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Endpoints for user overview metrics and activity summary")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    @Operation(summary = "Get high-level dashboard metrics for the authenticated user")
    public ResponseEntity<ApiResponse<DashboardSummaryResponse>> getSummary(
            @AuthenticationPrincipal UserPrincipal principal) {
        DashboardSummaryResponse summary = dashboardService.getSummary(principal.getId());
        return ResponseEntity.ok(ApiResponse.success(summary));
    }
}
