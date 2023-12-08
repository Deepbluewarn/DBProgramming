-- 테이블 전체 삭제

DROP TABLE 관리기록;
DROP TABLE 대여기록;
DROP TABLE 후기;
DROP TABLE 공구항목;
DROP TABLE 공구;
DROP TABLE 관리자;
DROP TABLE 회원;

DROP SEQUENCE 회원_SEQ;
DROP SEQUENCE 관리자_SEQ;
DROP SEQUENCE 공구_SEQ;
DROP SEQUENCE 공구항목_SEQ;
DROP SEQUENCE 후기_SEQ;
DROP SEQUENCE 대여기록_SEQ;
DROP SEQUENCE 관리기록_SEQ;

CREATE TABLE "회원" (
   "회원ID" NUMBER,
   "이름" VARCHAR2(50) NOT NULL,
   "연락처" VARCHAR2(50) NOT NULL,
   "이메일" VARCHAR2(50),
   "등록일" DATE NOT NULL,
   PRIMARY KEY ("회원ID"),
   CONSTRAINT UQ_Member_Name_Contact UNIQUE (이름, 연락처)
);

CREATE TABLE "관리자" (
   "관리자ID" NUMBER,
   "이름" VARCHAR2(50),
   "연락처" VARCHAR2(50),
   "이메일" VARCHAR2(50),
   "등록일" DATE,
   "감독관ID" NUMBER,
   PRIMARY KEY ("관리자ID")
   -- 이 제약조건은 Admins 테이블이 생성된 다음 추가되어야 합니다.
  -- FOREIGN KEY (감독관ID) REFERENCES 관리자(관리자ID) 
);

CREATE TABLE "공구" (
   "공구ID" NUMBER,
   "공구이름" VARCHAR2(50),
   "제조사" VARCHAR2(50),
   "종류" VARCHAR2(50),
   "보유수량" NUMBER(11),
   "대여수량" NUMBER(11),
   PRIMARY KEY ("공구ID"),
   CONSTRAINT CHK_QUANTITY CHECK (보유수량 >= 대여수량),
   CONSTRAINT CHK_RENTCNT CHECK (대여수량 >= 0)
);

CREATE TABLE "공구항목" (
   "항목ID" NUMBER,
   "보관위치" VARCHAR2(50),
   "구매일" DATE,
   "공구ID" NUMBER NOT NULL,
   PRIMARY KEY ("공구ID", "항목ID"),
   CONSTRAINT "포함_공구ID" FOREIGN KEY ("공구ID") REFERENCES "공구" ("공구ID")
);

CREATE TABLE "후기" (
   "후기ID" NUMBER,
   "평점" NUMBER,
   "내용" VARCHAR2(100),
   "작성일" DATE,
   "회원ID" NUMBER,
   "공구ID" NUMBER,
   PRIMARY KEY ("후기ID"),
   CONSTRAINT "FK_검토_공구ID" FOREIGN KEY ("공구ID") REFERENCES "공구" ("공구ID"),
   CONSTRAINT "FK_작성_회원ID" FOREIGN KEY ("회원ID") REFERENCES "회원" ("회원ID"),
   CONSTRAINT CHK_RATE CHECK (평점 >= 0 AND 평점 <= 5)
);

CREATE TABLE "대여기록" (
   "대여ID" NUMBER,
   "대여시작일" DATE NOT NULL,
   "반납일" DATE,
   "반납예정일" DATE NOT NULL,
   "공구ID" NUMBER,
   "항목ID" NUMBER,
   "관리자ID" NUMBER,
   "회원ID" NUMBER,
   PRIMARY KEY ("대여ID"),
   FOREIGN KEY ("공구ID", "항목ID") REFERENCES "공구항목"("공구ID", "항목ID"),
   CONSTRAINT "FK_관리자ID" FOREIGN KEY ("관리자ID") REFERENCES "관리자" ("관리자ID"),
   CONSTRAINT "FK_대여_회원ID" FOREIGN KEY ("회원ID") REFERENCES "회원" ("회원ID"),
   CONSTRAINT CHK_DATE_1 CHECK (대여시작일 < 반납예정일),
   CONSTRAINT CHK_DATE_2 CHECK (대여시작일 <= 반납일)
);

CREATE TABLE "관리기록" (
   "점검일" DATE NOT NULL,
   "점검ID" NUMBER,
   "점검내용" VARCHAR2(500),
   "관리자ID" NUMBER,
   "공구ID" NUMBER,
   "항목ID" NUMBER,
   PRIMARY KEY (점검ID),
   FOREIGN KEY (공구ID, 항목ID) REFERENCES "공구항목"(공구ID, 항목ID),
   CONSTRAINT "관리_관리자ID" FOREIGN KEY (관리자ID) REFERENCES "관리자" (관리자ID)
);

