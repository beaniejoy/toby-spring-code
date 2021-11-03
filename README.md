# Toby's Spring 3.1

토비의 스프링 3.1 코드 중심의 내용정리

## Branches
  - chapter1 오브젝트, 의존관계
  - chapter2 테스트
  - chapter3 템플릿
  - chapter4 예외
  - chapter5 서비스 추상화
  - chapter6 AOP
  - chapter7 스프링 핵심 기술의 응용

## Docker mysql setup

```shell
$ docker run --name <CONTAINER_NAME> -e MYSQL_ROOT_PASSWORD=springtest -d -p 3306:3306 mysql:latest
```

```sql
CREATE SCHEMA <database_name> DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE <database_name>;

CREATE TABLE users
(
  id 		    varchar(10) primary key,
  name		varchar(20) not null,
  password	varchar(10) not null,
  level       tinyint     not null,
  login       int         not null,
  recommend   int         not null
);
```