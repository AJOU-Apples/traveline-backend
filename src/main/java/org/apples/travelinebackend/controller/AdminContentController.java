package org.apples.travelinebackend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apples.travelinebackend.dto.ContentHideResponse;
import org.apples.travelinebackend.dto.HideContentRequest;
import org.apples.travelinebackend.entity.User;
import org.apples.travelinebackend.service.AdminContentService;
import org.apples.travelinebackend.util.AdminAuthUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/admin/content")
@RequiredArgsConstructor
public class AdminContentController {

    private final AdminContentService adminContentService;

    /**
     * 콘텐츠 비공개 처리
     */
    @PostMapping("/{contentType}/{contentId}/hide")
    public ResponseEntity<ContentHideResponse> hideContent(
            @AuthenticationPrincipal User currentUser,
            @PathVariable String contentType,
            @PathVariable Long contentId,
            @Valid @RequestBody HideContentRequest request) {
        User user = getCurrentUser();
        AdminAuthUtil.checkAdminRole(user);
        ContentHideResponse response = adminContentService.hideContent(contentType, contentId, request, user);
        return ResponseEntity.ok(response);
    }

    /**
     * 콘텐츠 공개 처리 (숨김 해제)
     */
    @PostMapping("/{contentType}/{contentId}/unhide")
    public ResponseEntity<ContentHideResponse> unhideContent(
            @AuthenticationPrincipal User currentUser,
            @PathVariable String contentType,
            @PathVariable Long contentId) {
        User user = getCurrentUser();
        AdminAuthUtil.checkAdminRole(user);
        ContentHideResponse response = adminContentService.unhideContent(contentType, contentId, user);
        return ResponseEntity.ok(response);
    }

    /**
     * SecurityContext에서 현재 사용자 가져오기
     */
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User) {
            return (User) authentication.getPrincipal();
        }
        return null;
    }
}

