package org.apples.travelinebackend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apples.travelinebackend.dto.UpdateProfileRequest;
import org.apples.travelinebackend.dto.UserDto;
import org.apples.travelinebackend.entity.User;
import org.apples.travelinebackend.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 내 프로필 조회
     * GET /api/users/me
     */
    @GetMapping("/me")
    public ResponseEntity<UserDto> getMyProfile(@AuthenticationPrincipal User currentUser) {
        log.info("GET /api/users/me - userId: {}", currentUser.getId());
        UserDto userDto = userService.getUserById(currentUser.getId());
        return ResponseEntity.ok(userDto);
    }

    /**
     * 프로필 수정
     * PUT /api/users/me
     */
    @PutMapping("/me")
    public ResponseEntity<UserDto> updateMyProfile(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody UpdateProfileRequest request) {
        log.info("PUT /api/users/me - userId: {}", currentUser.getId());
        UserDto userDto = userService.updateProfile(currentUser.getId(), request);
        return ResponseEntity.ok(userDto);
    }
}

