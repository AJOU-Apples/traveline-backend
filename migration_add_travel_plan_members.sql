-- ========================================
-- 여행 계획 다중 사용자 지원 마이그레이션
-- ========================================

-- 1. travel_plan_members 테이블 생성
CREATE TABLE IF NOT EXISTS travel_plan_members (
    id BIGSERIAL PRIMARY KEY,
    travel_plan_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    role VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACCEPTED',
    invited_at TIMESTAMP NOT NULL DEFAULT NOW(),
    joined_at TIMESTAMP,
    invited_by BIGINT,
    
    -- 외래 키 제약조건
    CONSTRAINT fk_member_travelplan FOREIGN KEY (travel_plan_id) 
        REFERENCES travel_plans(id) ON DELETE CASCADE,
    CONSTRAINT fk_member_user FOREIGN KEY (user_id) 
        REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_member_inviter FOREIGN KEY (invited_by) 
        REFERENCES users(id) ON DELETE SET NULL,
    
    -- 유니크 제약조건 (한 여행 계획에 같은 사용자는 한 번만)
    CONSTRAINT uk_travelplan_user UNIQUE (travel_plan_id, user_id)
);

-- 2. 인덱스 생성
CREATE INDEX IF NOT EXISTS idx_member_travelplan ON travel_plan_members(travel_plan_id);
CREATE INDEX IF NOT EXISTS idx_member_user ON travel_plan_members(user_id);
CREATE INDEX IF NOT EXISTS idx_member_status ON travel_plan_members(status);

-- 3. 기존 travel_plans의 user_id를 travel_plan_members로 마이그레이션
-- (모든 기존 여행 계획 소유자를 OWNER로 등록)
INSERT INTO travel_plan_members (travel_plan_id, user_id, role, status, invited_at, joined_at)
SELECT 
    id,
    user_id,
    'OWNER',
    'ACCEPTED',
    created_at,
    created_at
FROM travel_plans
WHERE user_id IS NOT NULL
ON CONFLICT (travel_plan_id, user_id) DO NOTHING;

-- 4. (선택사항) travel_plans.participants 컬럼은 유지
-- 기존 코드와의 호환성을 위해 컬럼을 남겨두되, 실제 멤버 수는 계산된 값 사용
-- 나중에 제거하려면 아래 주석을 해제:
-- ALTER TABLE travel_plans DROP COLUMN IF EXISTS participants;

-- 5. 확인 쿼리 (주석 해제하여 실행)
-- SELECT 
--     tp.id,
--     tp.title,
--     tp.user_id as old_owner_id,
--     u.username as old_owner_username,
--     COUNT(tpm.id) as member_count
-- FROM travel_plans tp
-- LEFT JOIN users u ON tp.user_id = u.id
-- LEFT JOIN travel_plan_members tpm ON tp.id = tpm.travel_plan_id AND tpm.status = 'ACCEPTED'
-- GROUP BY tp.id, tp.title, tp.user_id, u.username
-- ORDER BY tp.id;

-- ========================================
-- 롤백 스크립트 (문제 발생 시 사용)
-- ========================================
-- DROP TABLE IF EXISTS travel_plan_members CASCADE;

