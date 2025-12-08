-- Memo 테이블에 visibility 컬럼 추가
-- PhotoVisibility enum 사용: PERSONAL, SHARED

ALTER TABLE memos 
ADD COLUMN visibility VARCHAR(20) NOT NULL DEFAULT 'SHARED';

-- 기존 데이터는 모두 SHARED로 설정 (기본값이 이미 적용됨)
UPDATE memos SET visibility = 'SHARED' WHERE visibility IS NULL;

-- 인덱스 추가 (선택 사항 - 성능 최적화)
CREATE INDEX idx_memo_visibility ON memos(place_id, visibility, deleted_at);

COMMENT ON COLUMN memos.visibility IS '공개 설정: PERSONAL(개인), SHARED(공유)';

