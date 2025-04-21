package com.eeerrorcode.lottomate.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.eeerrorcode.lottomate.domain.dto.CommonResponse;
import com.eeerrorcode.lottomate.domain.dto.user.*;
import com.eeerrorcode.lottomate.security.CustomUserDetails;
import com.eeerrorcode.lottomate.service.user.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
@Tag(name = "User API", description = "회원 관련 기능을 테스트할 수 있는 API입니다")
@Log4j2
public class UserController {

    private final UserService userService;

    @Operation(
        summary = "회원가입",
        description = "새로운 회원을 등록합니다",
        responses = {
            @ApiResponse(
                responseCode = "201", 
                description = "회원가입 성공", 
                content = @Content(schema = @Schema(implementation = UserResponseDto.class))
            ),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 요청")
        }
    )
    @PostMapping("/register")
    public ResponseEntity<CommonResponse<UserResponseDto>> register(
            @Valid @RequestBody UserRegistrationDto registrationDto) {
        
        log.info("회원가입 요청: {}", registrationDto.getEmail());
        UserResponseDto userDto = userService.registerUser(registrationDto);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CommonResponse.success(userDto, "회원가입이 성공적으로 완료되었습니다"));
    }
    
    @Operation(
        summary = "회원 정보 조회",
        description = "현재 로그인한 회원의 정보를 조회합니다",
        responses = {
            @ApiResponse(
                responseCode = "200", 
                description = "회원 정보 조회 성공", 
                content = @Content(schema = @Schema(implementation = UserResponseDto.class))
            ),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "404", description = "회원 정보 없음")
        }
    )
    @GetMapping("/info")
    public ResponseEntity<CommonResponse<UserResponseDto>> getUserInfo(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        Long userId = userDetails.getUser().getId();
        log.info("회원 정보 조회: userId={}", userId);
        
        UserResponseDto userDto = userService.getUserInfo(userId);
        return ResponseEntity.ok(CommonResponse.success(userDto, "회원 정보 조회 성공"));
    }
    
    @Operation(
        summary = "회원 정보 수정",
        description = "현재 로그인한 회원의 정보를 수정합니다",
        responses = {
            @ApiResponse(
                responseCode = "200", 
                description = "회원 정보 수정 성공", 
                content = @Content(schema = @Schema(implementation = UserResponseDto.class))
            ),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 요청"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "404", description = "회원 정보 없음")
        }
    )
    @PutMapping("/update")
    public ResponseEntity<CommonResponse<UserResponseDto>> updateUserInfo(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UserUpdateDto updateDto) {
        
        Long userId = userDetails.getUser().getId();
        log.info("회원 정보 수정: userId={}", userId);
        
        UserResponseDto updatedUser = userService.updateUserInfo(userId, updateDto);
        return ResponseEntity.ok(CommonResponse.success(updatedUser, "회원 정보가 성공적으로 수정되었습니다"));
    }
    
    @Operation(
        summary = "비밀번호 변경",
        description = "현재 로그인한 회원의 비밀번호를 변경합니다",
        responses = {
            @ApiResponse(responseCode = "200", description = "비밀번호 변경 성공"),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 요청"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "404", description = "회원 정보 없음")
        }
    )
    @PutMapping("/password")
    public ResponseEntity<CommonResponse<Void>> changePassword(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody PasswordChangeDto passwordChangeDto) {
        
        Long userId = userDetails.getUser().getId();
        log.info("비밀번호 변경: userId={}", userId);
        
        userService.changePassword(userId, passwordChangeDto);
        return ResponseEntity.ok(CommonResponse.success(null, "비밀번호가 성공적으로 변경되었습니다"));
    }
    
    @Operation(
        summary = "회원 탈퇴",
        description = "현재 로그인한 회원의 계정을 비활성화합니다",
        responses = {
            @ApiResponse(responseCode = "200", description = "회원 탈퇴 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "404", description = "회원 정보 없음")
        }
    )
    @DeleteMapping
    public ResponseEntity<CommonResponse<Void>> deactivateAccount(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        Long userId = userDetails.getUser().getId();
        log.info("회원 탈퇴: userId={}", userId);
        
        userService.deactivateUser(userId);
        return ResponseEntity.ok(CommonResponse.success(null, "회원 탈퇴가 성공적으로 처리되었습니다"));
    }
    
    @Operation(
        summary = "이메일 중복 확인",
        description = "회원가입 시 이메일 중복 여부를 확인합니다",
        responses = {
            @ApiResponse(
                responseCode = "200", 
                description = "이메일 중복 확인 결과", 
                content = @Content(schema = @Schema(implementation = EmailDuplicationResponseDto.class))
            )
        }
    )
    @GetMapping("/check-email")
    public ResponseEntity<CommonResponse<EmailDuplicationResponseDto>> checkEmailDuplication(
            @RequestParam("email") String email) {
        
        log.info("이메일 중복 확인: {}", email);
        
        boolean isDuplicated = userService.isEmailDuplicated(email);
        EmailDuplicationResponseDto responseDto = new EmailDuplicationResponseDto(email, isDuplicated);
        
        String message = isDuplicated ? "이미 사용 중인 이메일입니다" : "사용 가능한 이메일입니다";
        return ResponseEntity.ok(CommonResponse.success(responseDto, message));
    }
}