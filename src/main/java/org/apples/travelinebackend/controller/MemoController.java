package org.apples.travelinebackend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apples.travelinebackend.dto.CreateMemoRequest;
import org.apples.travelinebackend.dto.MemoDto;
import org.apples.travelinebackend.dto.UpdateMemoRequest;
import org.apples.travelinebackend.entity.User;
import org.apples.travelinebackend.service.MemoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/memos")
@RequiredArgsConstructor
public class MemoController {

    private final MemoService memoService;

    @PostMapping
    public ResponseEntity<MemoDto> createMemo(
            @Valid @RequestBody CreateMemoRequest request,
            @AuthenticationPrincipal User user) {
        log.info("POST /api/memos - 메모 생성 요청: placeId={}", request.getPlaceId());
        MemoDto memo = memoService.createMemo(request, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(memo);
    }

    @GetMapping("/place/{placeId}")
    public ResponseEntity<List<MemoDto>> getMemosByPlace(
            @PathVariable Long placeId,
            @AuthenticationPrincipal User currentUser) {
        log.info("GET /api/memos/place/{} - 장소별 메모 목록 조회, userId={}", placeId, currentUser.getId());
        List<MemoDto> memos = memoService.getMemosByPlace(placeId, currentUser);
        return ResponseEntity.ok(memos);
    }

    @GetMapping("/{memoId}")
    public ResponseEntity<MemoDto> getMemoById(@PathVariable Long memoId) {
        log.info("GET /api/memos/{} - 메모 상세 조회", memoId);
        MemoDto memo = memoService.getMemoById(memoId);
        return ResponseEntity.ok(memo);
    }

    @PutMapping("/{memoId}")
    public ResponseEntity<MemoDto> updateMemo(
            @PathVariable Long memoId,
            @Valid @RequestBody UpdateMemoRequest request,
            @AuthenticationPrincipal User user) {
        log.info("PUT /api/memos/{} - 메모 수정 요청", memoId);
        MemoDto memo = memoService.updateMemo(memoId, request, user);
        return ResponseEntity.ok(memo);
    }

    @DeleteMapping("/{memoId}")
    public ResponseEntity<Void> deleteMemo(
            @PathVariable Long memoId,
            @AuthenticationPrincipal User user) {
        log.info("DELETE /api/memos/{} - 메모 삭제 요청", memoId);
        memoService.deleteMemo(memoId, user);
        return ResponseEntity.noContent().build();
    }
}

