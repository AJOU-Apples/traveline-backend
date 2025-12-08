package org.apples.travelinebackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apples.travelinebackend.entity.MemberRole;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TravelPlanDto {
    private Long id;
    private String title;
    private CityDto destination;
    private String startDate;
    private String endDate;
    private Integer participants;
    private Boolean isArchived;
    
    @Builder.Default
    private List<TravelDayDto> days = new ArrayList<>();
    
    // 멤버 관련 정보 (선택적)
    @Builder.Default
    private List<MemberDto> members = new ArrayList<>();
    
    // 현재 사용자의 역할 (API 요청 시 추가)
    private MemberRole myRole;
}

