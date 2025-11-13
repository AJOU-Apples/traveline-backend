package org.apples.travelinebackend.entity;

/**
 * 여행 계획 초대 상태
 */
public enum InvitationStatus {
    /**
     * 초대 대기중
     */
    PENDING,
    
    /**
     * 초대 수락됨
     */
    ACCEPTED,
    
    /**
     * 초대 거절됨
     */
    REJECTED
}

