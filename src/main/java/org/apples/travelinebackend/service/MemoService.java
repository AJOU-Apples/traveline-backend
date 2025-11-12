package org.apples.travelinebackend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apples.travelinebackend.dto.CreateMemoRequest;
import org.apples.travelinebackend.dto.MemoDto;
import org.apples.travelinebackend.dto.UpdateMemoRequest;
import org.apples.travelinebackend.entity.Memo;
import org.apples.travelinebackend.entity.Place;
import org.apples.travelinebackend.entity.User;
import org.apples.travelinebackend.exception.ForbiddenException;
import org.apples.travelinebackend.exception.ResourceNotFoundException;
import org.apples.travelinebackend.mapper.MemoMapper;
import org.apples.travelinebackend.repository.MemoRepository;
import org.apples.travelinebackend.repository.PlaceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemoService {

    private final MemoRepository memoRepository;
    private final PlaceRepository placeRepository;
    private final MemoMapper memoMapper;

    @Transactional
    public MemoDto createMemo(CreateMemoRequest request, User user) {
        log.info("메모 생성 요청: placeId={}, userId={}", request.getPlaceId(), user.getId());

        // Place 존재 확인
        Place place = placeRepository.findById(request.getPlaceId())
                .orElseThrow(() -> new ResourceNotFoundException("장소를 찾을 수 없습니다: " + request.getPlaceId()));

        // Memo 생성
        Memo memo = Memo.builder()
                .place(place)
                .author(user)
                .content(request.getContent())
                .build();

        Memo savedMemo = memoRepository.save(memo);
        log.info("메모 생성 완료: memoId={}, placeId={}, userId={}", 
                savedMemo.getId(), place.getId(), user.getId());

        return memoMapper.toDto(savedMemo);
    }

    @Transactional(readOnly = true)
    public List<MemoDto> getMemosByPlace(Long placeId) {
        log.info("장소별 메모 목록 조회: placeId={}", placeId);

        // Place 존재 확인
        if (!placeRepository.existsById(placeId)) {
            throw new ResourceNotFoundException("장소를 찾을 수 없습니다: " + placeId);
        }

        List<Memo> memos = memoRepository.findByPlaceIdAndDeletedAtIsNull(placeId);
        return memos.stream()
                .map(memoMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public MemoDto getMemoById(Long memoId) {
        log.info("메모 상세 조회: memoId={}", memoId);

        Memo memo = memoRepository.findByIdAndDeletedAtIsNull(memoId)
                .orElseThrow(() -> new ResourceNotFoundException("메모를 찾을 수 없습니다: " + memoId));

        return memoMapper.toDto(memo);
    }

    @Transactional
    public MemoDto updateMemo(Long memoId, UpdateMemoRequest request, User user) {
        log.info("메모 수정 요청: memoId={}, userId={}", memoId, user.getId());

        Memo memo = memoRepository.findByIdAndDeletedAtIsNull(memoId)
                .orElseThrow(() -> new ResourceNotFoundException("메모를 찾을 수 없습니다: " + memoId));

        // 작성자 본인만 수정 가능
        if (!memo.getAuthor().getId().equals(user.getId())) {
            throw new ForbiddenException("메모 작성자만 수정할 수 있습니다");
        }

        memo.setContent(request.getContent());
        Memo updatedMemo = memoRepository.save(memo);

        log.info("메모 수정 완료: memoId={}", memoId);
        return memoMapper.toDto(updatedMemo);
    }

    @Transactional
    public void deleteMemo(Long memoId, User user) {
        log.info("메모 삭제 요청: memoId={}, userId={}", memoId, user.getId());

        Memo memo = memoRepository.findByIdAndDeletedAtIsNull(memoId)
                .orElseThrow(() -> new ResourceNotFoundException("메모를 찾을 수 없습니다: " + memoId));

        // 작성자 본인만 삭제 가능
        if (!memo.getAuthor().getId().equals(user.getId())) {
            throw new ForbiddenException("메모 작성자만 삭제할 수 있습니다");
        }

        memo.setDeletedAt(LocalDateTime.now());
        memoRepository.save(memo);

        log.info("메모 삭제 완료: memoId={}", memoId);
    }
}

