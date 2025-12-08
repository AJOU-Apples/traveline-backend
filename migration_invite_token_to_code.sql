-- Migration: invite_token 컬럼을 invite_code로 변경
-- 날짜: 2025-12-03

-- 1. 기존 인덱스 및 제약조건 삭제
DROP INDEX IF EXISTS idx_invite_token;
ALTER TABLE invite_links DROP CONSTRAINT IF EXISTS uk_invite_token;

-- 2. invite_code 컬럼이 없으면 생성
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'invite_links' AND column_name = 'invite_code'
    ) THEN
        ALTER TABLE invite_links ADD COLUMN invite_code VARCHAR(6);
    END IF;
END $$;

-- 3. 기존 데이터가 있다면 임시 코드 생성 (invite_token에서 변환하거나 랜덤 생성)
-- 기존 데이터가 있다면 아래 주석을 해제하고 실행
UPDATE invite_links 
SET invite_code = UPPER(SUBSTRING(MD5(RANDOM()::TEXT || id::TEXT) FROM 1 FOR 6))
WHERE invite_code IS NULL;

-- 4. invite_code에 NOT NULL 제약조건 추가
ALTER TABLE invite_links ALTER COLUMN invite_code SET NOT NULL;

-- 5. UNIQUE 제약조건 추가 (기존 제약조건이 있다면 먼저 삭제)
ALTER TABLE invite_links DROP CONSTRAINT IF EXISTS uk_invite_code;
ALTER TABLE invite_links ADD CONSTRAINT uk_invite_code UNIQUE (invite_code);

-- 6. 인덱스 생성
CREATE INDEX IF NOT EXISTS idx_invite_code ON invite_links(invite_code);

-- 7. invite_token 컬럼 삭제 (모든 작업 완료 후)
ALTER TABLE invite_links DROP COLUMN IF EXISTS invite_token;

