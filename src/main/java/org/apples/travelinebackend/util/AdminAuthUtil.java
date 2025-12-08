package org.apples.travelinebackend.util;

import lombok.extern.slf4j.Slf4j;
import org.apples.travelinebackend.entity.User;
import org.apples.travelinebackend.entity.UserRole;
import org.apples.travelinebackend.exception.ForbiddenException;

@Slf4j
public class AdminAuthUtil {

    /**
     * 관리자 권한 체크
     */
    public static void checkAdminRole(User user) {
        if (user == null) {
            log.warn("관리자 권한 체크 실패: 사용자가 null입니다.");
            throw new ForbiddenException("인증이 필요합니다.");
        }
        
        if (user.getRole() == null) {
            log.warn("관리자 권한 체크 실패: userId={}, role이 null입니다.", user.getId());
            throw new ForbiddenException("사용자 역할이 설정되지 않았습니다.");
        }
        
        if (user.getRole() != UserRole.ADMIN) {
            log.warn("관리자 권한 체크 실패: userId={}, role={}", user.getId(), user.getRole());
            throw new ForbiddenException("관리자 권한이 필요합니다. 현재 역할: " + user.getRole());
        }
        
        log.debug("관리자 권한 체크 성공: userId={}, role={}", user.getId(), user.getRole());
    }
}

