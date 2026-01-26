package com.aivle.project.auth.controller;

import com.aivle.project.user.service.EmailVerificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 이메일 인증 컨트롤러.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@Tag(name = "인증", description = "이메일 인증 API")
public class EmailVerificationController {

    private final EmailVerificationService emailVerificationService;

    /**
     * 이메일 인증 처리.
     */
    @GetMapping("/verify-email")
    @Operation(summary = "이메일 인증", description = "이메일 인증 토큰을 검증합니다.", security = {})
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "인증 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "인증 실패"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<String> verifyEmail(
        @Parameter(description = "이메일 인증 토큰", example = "a1b2c3d4")
        @RequestParam String token
    ) {
        try {
            emailVerificationService.verifyEmail(token);
            return ResponseEntity.ok("이메일 인증이 완료되었습니다.");
        } catch (IllegalArgumentException e) {
            log.warn("이메일 인증 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 이메일 인증 재전송.
     */
    @GetMapping("/resend-verification")
    @Operation(summary = "이메일 인증 재전송", description = "사용자에게 인증 메일을 재전송합니다.", security = {})
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "재전송 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "재전송 실패"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<String> resendVerification(
        @Parameter(description = "사용자 ID", example = "1")
        @RequestParam Long userId
    ) {
        try {
            emailVerificationService.resendVerificationEmail(userId);
            return ResponseEntity.ok("인증 이메일이 재전송되었습니다.");
        } catch (IllegalStateException | IllegalArgumentException e) {
            log.warn("인증 이메일 재전송 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
