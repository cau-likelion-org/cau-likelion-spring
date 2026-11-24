-- seed.sql로 넣은 부하 테스트 데이터만 삭제한다.
-- 기준: 기수 번호 901~905에 속한 데이터, '[LT]'로 시작하는 FAQ
-- 자식 테이블부터 삭제 (FK 제약 때문)

DELETE pi FROM project_image pi
JOIN project p ON p.id = pi.project_id
JOIN generation g ON g.id = p.generation_id
WHERE g.number BETWEEN 901 AND 905;

DELETE pl FROM project_link pl
JOIN project p ON p.id = pl.project_id
JOIN generation g ON g.id = p.generation_id
WHERE g.number BETWEEN 901 AND 905;

DELETE pm FROM project_member pm
JOIN project p ON p.id = pm.project_id
JOIN generation g ON g.id = p.generation_id
WHERE g.number BETWEEN 901 AND 905;

DELETE p FROM project p
JOIN generation g ON g.id = p.generation_id
WHERE g.number BETWEEN 901 AND 905;

DELETE b FROM blog b
JOIN generation g ON g.id = b.generation_id
WHERE g.number BETWEEN 901 AND 905;

DELETE pgi FROM project_gallery_image pgi
JOIN project_gallery pg ON pg.id = pgi.project_gallery_id
JOIN generation g ON g.id = pg.generation_id
WHERE g.number BETWEEN 901 AND 905;

DELETE pg FROM project_gallery pg
JOIN generation g ON g.id = pg.generation_id
WHERE g.number BETWEEN 901 AND 905;

DELETE pt FROM part pt
JOIN generation g ON g.id = pt.generation_id
WHERE g.number BETWEEN 901 AND 905;

DELETE FROM generation WHERE number BETWEEN 901 AND 905;

DELETE FROM faq WHERE question LIKE '[LT]%';
