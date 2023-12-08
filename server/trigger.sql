-- 대여 기록이 추가될 때 실행되는 트리거
SET SERVEROUTPUT ON;
CREATE OR REPLACE TRIGGER RantalTrigger
BEFORE INSERT OR UPDATE ON "대여기록"
FOR EACH ROW
DECLARE
    v_availableQuantity NUMBER;
    v_alreadyRented NUMBER;
BEGIN
    -- 대여 기록이 추가될 때 해당 공구의 대여수량 갱신
    IF INSERTING THEN

        -- 대여 가능 여부 확인 및 예외 발생
        
        -- 대여 기록에서 반납일이 NULL 이고 공구ID 와 항목ID 가 일치하는 행이 있으면 예외 발생
        
        SELECT COUNT(*) INTO v_alreadyRented
        FROM "대여기록"
        WHERE "반납일" IS NULL AND "공구ID" = :NEW."공구ID" AND "항목ID" = :NEW."항목ID";
        
        IF v_alreadyRented > 0 THEN
            RAISE_APPLICATION_ERROR(-20001, '대여 불가: 이미 대여중인 항목입니다');
        END IF;
        
        UPDATE "공구"
        SET "대여수량" = "대여수량" + 1
        WHERE "공구ID" = :NEW."공구ID";
    ELSIF UPDATING('반납일') THEN
        -- 대여 상태 업데이트
        UPDATE "공구"
        SET "대여수량" = "대여수량" - 1
        WHERE "공구ID" = :NEW."공구ID";
    END IF;
END RantalTrigger;

CREATE OR REPLACE TRIGGER ReviewTrigger
BEFORE INSERT OR UPDATE ON "후기"
FOR EACH ROW
DECLARE
    TYPE BadWordsArray IS TABLE OF VARCHAR2(100);
    v_badWords BadWordsArray := BadWordsArray('비속어1', '비속어2', '비속어3');
    v_index NUMBER;
BEGIN
    FOR v_index IN 1..v_badWords.COUNT LOOP
        IF INSTR(:NEW."내용", v_badWords(v_index)) > 0 THEN
            RAISE_APPLICATION_ERROR(-20002, '비속어는 사용할 수 없습니다.');
        END IF;
    END LOOP;
END ReviewTrigger;

alter table 회원 add constraint uq_name_contact unique(이름, 연락처);
-- 대여기록 테이블에 행이 추가되면 RantalTrigger 에 의해 공구 테이블의 대여 수량이 1 증가한다.

INSERT INTO "대여기록" (대여ID, 대여시작일, 반납예정일, 공구ID, 항목ID, 회원ID) 
VALUES (대여기록_SEQ.nextval, '23/11/27', '23/11/30', 5, 45, 29);
INSERT INTO "대여기록" (대여ID, 대여시작일, 반납예정일, 공구ID, 항목ID, 회원ID) 
VALUES (대여기록_SEQ.nextval, '23/11/27', '23/11/30', 5, 46, 29);
INSERT INTO "대여기록" (대여ID, 대여시작일, 반납예정일, 공구ID, 항목ID, 회원ID) 
VALUES (대여기록_SEQ.nextval, '23/11/27', '23/11/30', 5, 47, 29);
INSERT INTO "대여기록" (대여ID, 대여시작일, 반납예정일, 공구ID, 항목ID, 회원ID) 
VALUES (대여기록_SEQ.nextval, '23/11/27', '23/11/30', 5, 48, 29);

-- 반납일을 업데이트하면 공구 테이블의 대여 수량이 1 감소한다.

UPDATE "대여기록" SET 반납일 = '23/11/30' WHERE 항목ID = 45;
UPDATE "대여기록" SET 반납일 = '23/11/30' WHERE 항목ID = 46;
UPDATE "대여기록" SET 반납일 = '23/11/30' WHERE 항목ID = 47;
UPDATE "대여기록" SET 반납일 = '23/11/30' WHERE 항목ID = 48;

UPDATE 후기 SET "내용" = '이 제품은 절대 추천하지 않습니다!' WHERE 후기ID = 1;
UPDATE 후기 SET "내용" = '이 제품은 절대 추천하지 않습니다. 비속어2' WHERE 후기ID = 1;