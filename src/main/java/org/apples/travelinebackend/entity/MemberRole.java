package org.apples.travelinebackend.entity;

/**
 * 여행 계획 멤버의 역할
 */
public enum MemberRole {
    /**
     * 소유자: 모든 권한 (삭제, 멤버 관리, 편집)
     */
    OWNER,
    
    /**
     * 편집자: 장소, 지출, 사진 등 콘텐츠 편집 가능
     */
    EDITOR,
    
    /**
     * 뷰어: 읽기만 가능
     */
    VIEWER;
    
    /**
     * 특정 역할이 요구되는 최소 역할을 만족하는지 확인
     */
    public boolean hasPermission(MemberRole requiredRole) {
        if (requiredRole == null) return true;
        
        switch (this) {
            case OWNER:
                return true;  // 소유자는 모든 권한
            case EDITOR:
                return requiredRole == EDITOR || requiredRole == VIEWER;
            case VIEWER:
                return requiredRole == VIEWER;
            default:
                return false;
        }
    }
}

