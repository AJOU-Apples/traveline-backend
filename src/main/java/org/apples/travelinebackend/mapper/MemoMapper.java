package org.apples.travelinebackend.mapper;

import lombok.RequiredArgsConstructor;
import org.apples.travelinebackend.dto.AuthUserDto;
import org.apples.travelinebackend.dto.MemoDto;
import org.apples.travelinebackend.entity.Memo;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MemoMapper {

    public MemoDto toDto(Memo memo) {
        if (memo == null) {
            return null;
        }

        return MemoDto.builder()
                .id(memo.getId())
                .placeId(memo.getPlace().getId())
                .author(AuthUserDto.builder()
                        .id(memo.getAuthor().getId())
                        .email(memo.getAuthor().getEmail())
                        .name(memo.getAuthor().getName())
                        .username(memo.getAuthor().getNickname())
                        .profileImageUrl(memo.getAuthor().getProfileImage())
                        .build())
                .content(memo.getContent())
                .visibility(memo.getVisibility())
                .createdAt(memo.getCreatedAt())
                .updatedAt(memo.getUpdatedAt())
                .build();
    }
}
