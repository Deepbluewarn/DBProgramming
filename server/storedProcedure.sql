ALTER TABLE "공구항목" ADD "대여횟수" NUMBER(10);

CREATE OR REPLACE PROCEDURE UpdateRentalCount 
    (toolItems out SYS_REFCURSOR)
AS
BEGIN
    FOR item IN (SELECT "공구ID", "항목ID" FROM "공구항목") LOOP
        DECLARE
            v_RentalCount NUMBER;
        BEGIN
            -- 해당 공구 항목의 대여 횟수 계산
            SELECT COUNT(*) INTO v_RentalCount
            FROM "대여기록"
            WHERE "공구ID" = item."공구ID" AND "항목ID" = item."항목ID";

            -- 대여 횟수를 "공구항목" 테이블에 업데이트
            UPDATE "공구항목"
            SET "대여횟수" = v_RentalCount
            WHERE "공구ID" = item."공구ID" AND "항목ID" = item."항목ID";
        EXCEPTION
            WHEN NO_DATA_FOUND THEN
                -- 대여 기록이 없을 경우 대여 횟수를 0으로 설정
                UPDATE "공구항목"
                SET "대여횟수" = 0
                WHERE "공구ID" = item."공구ID" AND "항목ID" = item."항목ID";
        END;
        
        OPEN toolItems for 
            select 공구항목.공구ID, 공구.공구이름, 공구.제조사, 공구.종류, count(공구항목.대여횟수) as 대여횟수
            from 공구 JOIN 공구항목 on 공구.공구ID = 공구항목.공구ID
            group by 공구항목.공구ID, 공구.공구이름, 공구.제조사, 공구.종류
            order by count(공구항목.대여횟수);
    END LOOP;
END UpdateRentalCount;

ALTER TABLE "회원" ADD "등급" VARCHAR2(10);

CREATE OR REPLACE PROCEDURE UpdateUserGrade 
    (TUsers out SYS_REFCURSOR)
AS
    userId 회원.회원ID%TYPE;
    rentCnt number(10);
    grade varchar2(10);
    
    cursor vCursor is 
        SELECT D.회원ID, COUNT(*) "대여횟수"
        FROM 대여기록 D
        GROUP BY D.회원ID;
BEGIN
    open vCursor;
        loop
            fetch vCursor into userId, rentCnt;
            exit when vCursor%NOTFOUND;
            
            if rentCnt between 1 and 3 then
                grade := 'B';
            else
                grade := 'A';
            end if;
            
            update 회원 set 등급 = grade where 회원.회원ID = userId;
        end loop;
    close vCursor;
    
    OPEN TUsers for
        select M.회원ID, M.이름, M.연락처, M.이메일, M.등록일, M.등급, count(*) as "대여횟수"
        from 회원 M
        join 대여기록 D on M.회원ID = D.회원ID
        group by M.회원ID, M.이름, M.연락처, M.이메일, M.등록일, M.등급
        order by M.회원ID;
END UpdateUserGrade;

-- 실행 예시

execute updaterentalcount();

declare
    vResCursor SYS_REFCURSOR;
begin
    UpdateUserGrade(vResCursor);
end;
