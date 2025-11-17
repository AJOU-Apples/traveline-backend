package org.apples.travelinebackend.mapper;

import org.apples.travelinebackend.dto.MemberDto;
import org.apples.travelinebackend.entity.TravelPlanMember;
import org.springframework.stereotype.Component;

/**
 * TravelPlanMember <-> MemberDto 매퍼
 */
@Component
public class TravelPlanMemberMapper {

    /**
     * Entity -> DTO
     */
    public MemberDto toDto(TravelPlanMember entity) {
        if (entity == null) {
            return null;
        }

        return MemberDto.builder()
                .id(entity.getId())
                .userId(entity.getUser().getId())
                .username(entity.getUser().getNickname()) // getUsername() 대신 getNickname() 사용
                .name(entity.getUser().getName())
                .email(entity.getUser().getEmail())
                .profileImage(entity.getUser().getProfileImage())
                .role(entity.getRole())
                .status(entity.getStatus())
                .invitedAt(entity.getInvitedAt())
                .joinedAt(entity.getJoinedAt())
                .invitedByName(entity.getInvitedBy() != null ? entity.getInvitedBy().getName() : null)
                .build();
    }
}
