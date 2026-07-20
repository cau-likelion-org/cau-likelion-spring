#!/bin/bash
set -e

# 공식 mysql 이미지는 MYSQL_USER/MYSQL_PASSWORD로 '${MYSQL_USER}'@'%' 계정만 생성한다.
# 그런데 MySQL은 '%'가 'localhost'를 매칭하지 않기 때문에,
# 호스트 머신에서 localhost:3306(127.0.0.1)로 접속하는 경우(IDE에서 local 프로필로 직접 실행할 때 등)
# 'user'@'localhost' 계정이 따로 없으면 Access denied가 발생한다.
# 이 스크립트는 최초 초기화 시(볼륨이 비어있을 때만) 'localhost' 계정을 추가로 만들어준다.

mysql -uroot -p"${MYSQL_ROOT_PASSWORD}" <<-EOSQL
    CREATE USER IF NOT EXISTS '${MYSQL_USER}'@'localhost' IDENTIFIED BY '${MYSQL_PASSWORD}';
    GRANT ALL PRIVILEGES ON \`${MYSQL_DATABASE}\`.* TO '${MYSQL_USER}'@'localhost';
    FLUSH PRIVILEGES;
EOSQL
