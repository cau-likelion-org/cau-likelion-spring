-- 부하 테스트용 데이터 (로컬 DB 전용, 운영 DB에서 실행 금지)
-- 개선 전/후 테스트에 똑같이 사용해서 조건을 맞춘다.
--
-- 테스트 데이터 표시 (cleanup.sql이 이 표시로만 삭제함)
--   - 기수 번호 901~905
--   - FAQ 질문 '[LT]'로 시작
--
-- 데이터 양
--   - 기수 5개, 파트 15개 (기수마다 3개)
--   - FAQ 10개
--   - 프로젝트 100개 + 이미지 4개 / 링크 3개 / 멤버 5개씩  → GET /api/projects 쿼리 1 + 100*3 = 301번
--   - 블로그 100개
--   - 갤러리 프로젝트 100개 + 이미지 3개씩
--
-- 실행: loadtest/README.md 참고

SET SESSION cte_max_recursion_depth = 1000;

-- 1) 기수 901~905
INSERT INTO generation (number, status, year, created_at, updated_at)
WITH RECURSIVE seq(n) AS (SELECT 1 UNION ALL SELECT n + 1 FROM seq WHERE n < 5)
SELECT 900 + n, 'AFTER_ACTIVITY', 2020 + n, NOW(6), NOW(6) FROM seq;

-- 2) 파트 (기수마다 3개)
INSERT INTO part (generation_id, name, created_at, updated_at)
SELECT g.id, p.name, NOW(6), NOW(6)
FROM generation g
CROSS JOIN (SELECT '기획디자인' AS name UNION ALL SELECT '프론트엔드' UNION ALL SELECT '백엔드') p
WHERE g.number BETWEEN 901 AND 905;

-- 3) FAQ 10개
INSERT INTO faq (question, answer, created_at, updated_at)
WITH RECURSIVE seq(n) AS (SELECT 1 UNION ALL SELECT n + 1 FROM seq WHERE n < 10)
SELECT CONCAT('[LT] 자주 묻는 질문 ', n, '번입니다. 지원 자격이 어떻게 되나요?'),
       CONCAT('부하 테스트용 답변 ', n, '. ', REPEAT('멋쟁이사자처럼 중앙대학교 답변 내용입니다. ', 8)),
       NOW(6), NOW(6)
FROM seq;

-- 4) 프로젝트 100개 (기수 5개에 20개씩, 카테고리 순환)
INSERT INTO project (generation_id, title, category, stack, tagline, summary, team_name,
                     start_date, end_date, banner, is_exposed, created_at, updated_at)
WITH RECURSIVE seq(n) AS (SELECT 1 UNION ALL SELECT n + 1 FROM seq WHERE n < 100)
SELECT (SELECT id FROM generation WHERE number = 901 + (n % 5)),
       CONCAT('[LT] 프로젝트 ', LPAD(n, 3, '0')),
       ELT(1 + (n % 4), 'IDEATHON', 'HACKATHON', 'CHUNGKATHON', 'ETC'),
       'Spring Boot, React, MySQL, AWS',
       CONCAT('프로젝트 ', n, '의 한 줄 소개입니다'),
       REPEAT('부하 테스트용 프로젝트 요약 내용입니다. ', 10),
       CONCAT('팀', n),
       DATE_SUB('2026-10-01', INTERVAL n DAY),
       DATE_SUB('2026-11-01', INTERVAL n DAY),
       IF(n % 10 = 0, '2025 해커톤 본선 진출작', NULL),
       n % 10 = 0,
       NOW(6), NOW(6)
FROM seq;

-- 5) 프로젝트 이미지 4개씩
INSERT INTO project_image (project_id, image_url, is_main, display_order, created_at, updated_at)
SELECT p.id, CONCAT('https://example.com/loadtest/project/', p.id, '/', k.n, '.png'), k.n = 1, k.n, NOW(6), NOW(6)
FROM project p
JOIN generation g ON g.id = p.generation_id AND g.number BETWEEN 901 AND 905
CROSS JOIN (SELECT 1 AS n UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4) k;

-- 6) 프로젝트 링크 3개씩 (platform은 @Enumerated가 없어 ORDINAL: 0=GITHUB, 1=WEB, 2=BEHANCE)
INSERT INTO project_link (project_id, platform, url, created_at, updated_at)
SELECT p.id, k.n, CONCAT('https://example.com/loadtest/project/', p.id, '/link/', k.n), NOW(6), NOW(6)
FROM project p
JOIN generation g ON g.id = p.generation_id AND g.number BETWEEN 901 AND 905
CROSS JOIN (SELECT 0 AS n UNION ALL SELECT 1 UNION ALL SELECT 2) k;

-- 7) 프로젝트 멤버 5명씩
INSERT INTO project_member (project_id, name, part, created_at, updated_at)
SELECT p.id, CONCAT('멤버', p.id, '-', k.n), ELT(1 + (k.n % 3), '기획디자인', '프론트엔드', '백엔드'), NOW(6), NOW(6)
FROM project p
JOIN generation g ON g.id = p.generation_id AND g.number BETWEEN 901 AND 905
CROSS JOIN (SELECT 1 AS n UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5) k;

-- 8) 블로그 100개
INSERT INTO blog (generation_id, title, thumbnail_url, category, summary, writer, url, published_date,
                  created_at, updated_at)
WITH RECURSIVE seq(n) AS (SELECT 1 UNION ALL SELECT n + 1 FROM seq WHERE n < 100)
SELECT (SELECT id FROM generation WHERE number = 901 + (n % 5)),
       CONCAT('[LT] 블로그 글 ', LPAD(n, 3, '0')),
       CONCAT('https://example.com/loadtest/blog/', n, '.png'),
       ELT(1 + (n % 4), 'ACTIVITY_REVIEW', 'PROJECT_REVIEW', 'CAREER', 'ETC'),
       REPEAT('부하 테스트용 블로그 요약입니다. ', 6),
       CONCAT('작성자', n),
       CONCAT('https://example.com/loadtest/blog/', n),
       DATE_SUB('2026-11-01', INTERVAL n DAY),
       NOW(6), NOW(6)
FROM seq;

-- 9) 갤러리 프로젝트 100개
INSERT INTO project_gallery (generation_id, category, title, thumbnail_url, description, start_date, end_date,
                             created_at, updated_at)
WITH RECURSIVE seq(n) AS (SELECT 1 UNION ALL SELECT n + 1 FROM seq WHERE n < 100)
SELECT (SELECT id FROM generation WHERE number = 901 + (n % 5)),
       ELT(1 + (n % 4), 'IDEATHON', 'HACKATHON', 'CHUNGKATHON', 'ETC'),
       CONCAT('[LT] 갤러리 프로젝트 ', LPAD(n, 3, '0')),
       CONCAT('https://example.com/loadtest/gallery/', n, '.png'),
       REPEAT('부하 테스트용 갤러리 설명입니다. ', 6),
       DATE_SUB('2026-10-01', INTERVAL n DAY),
       DATE_SUB('2026-11-01', INTERVAL n DAY),
       NOW(6), NOW(6)
FROM seq;

-- 10) 갤러리 프로젝트 이미지 3개씩
INSERT INTO project_gallery_image (project_gallery_id, image_url, created_at, updated_at)
SELECT pg.id, CONCAT('https://example.com/loadtest/gallery/', pg.id, '/', k.n, '.png'), NOW(6), NOW(6)
FROM project_gallery pg
JOIN generation g ON g.id = pg.generation_id AND g.number BETWEEN 901 AND 905
CROSS JOIN (SELECT 1 AS n UNION ALL SELECT 2 UNION ALL SELECT 3) k;
