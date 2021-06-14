# chap 5 Service Abstraction(서비스 추상화)

> 성격이 비슷한 여러 종류의 기술을 추상화하고 이를 **일관된** 방법을 사용할 수 있도록 지원  
> 일관된 방법을 사용하면 어떤 기술의 내용이 변해도 이를 적용하는 오브젝트에서는 코드 변화 없이 유연하게 적용가능

## 5.1 사용자 레벨 관리 기능 추가
- User
    - level, login, recommend 필드 추가
    - upgrade level 로직에 대한 메소드 추가
- Level
    - 레벨을 표현하는 enum class 추가
    - level의 value, next level 내용을 담고 있음
    
- UserService
    - level upgrade를 수행하는 비즈니스 로직
    - user를 추가할 때 해당 user에 level이 없는 경우 default level(`BASIC`) 적용
    - level upgrade policy에 대한 인터페이스화를 통해 유연한 업그레이드 정책 주입 구현

- UserLevelUpgradePolicy
    - level upgrade 하는 조건과 조건에 만족하는 user의 level를 실제 level up 반영하는 로직을 담고 있음
    - 이벤트마다 upgrade 조건(정책)이 달라질 수 있기에 유연한 관계(인터페이스화)를 통한 확장을 열어둠 

## 5.2 트랜잭션 서비스 추상화
- `트랜잭션`: 더 이상 나눌 수 없는 단위 작업 > `원자성`
- upgrade level 작업 진행하다 중간에 에러발생 > 모든 진행상황 취소해야함
- `commit`, `rollback`
- 트랜잭션 경계: 트랜잭션이 시작되고 끝나는 위치
  - `setAutoCommit(false)` --- `commit()` or `rollback()`
  - 하나의 Connection 안에서 만들어지는 트랜잭션: `로컬 트랜잭션`
- JdbcTemplate 메소드를 사용할 때마다 독립적인 트랜잭션으로 수행(문제 발생)

### UserService 트랜잭션 경계설정의 문제점
- JdbcTemplate을 더 이상 활용 불가  
  `try - catch`가 UserService에서도 발생
- UserService에서도 Connection을 만들어서 전달해야 함  
  비즈니스 로직을 담고 있는 Service 계층에서 연결 생성 역할을 같이 담는 것은 SRP에 위반
- UserDao는 더 이상 데이터 엑세스 기술에 독립적일 수 없음
  - Connection을 UserDao 인터페이스 메소드에 각각 적용해야 함
  - 이는 테스트 코드에도 영향을 미친다. (각 메소드에 Connection을 넣어야 하기에)
  
### 트랜잭션 동기화(p361 그림 참고)
- `TransactionSynchronizationManager` 사용(스프링이 제공하는 트랜잭션 동기화 관리 클래스)
```java
TransactionSynchronizationManager.initSynchronization();
Connection c = DataSourceUtils.getConnection(dataSource);
c.setAutoCommit(false);

try {
    //...
    c.commit();
} catch (Exception e) {
    c.rollback();
    throw e;
} finally {
  // DB Connection을 안전하게 close
  DataSourceUtils.releaseConnection(c, dataSource);
  // 동기화 작업 종료 및 정리
  TransactionSynchronizationManager.unbindResource(this.dataSource);
  TransactionSynchronizationManager.clearSynchronization();
}
```

### JdbcTemplate 트랜잭션 동기화
- JdbcTemplate은 스스로 Connection을 생성해서 사용
- 트랜잭션 동기화 저장소에 등록된 커낵션, 트랜잭션이 없는 경우 직접 생성해서 JDBC 작업 수행
- 있으면 가져와서 사용

### 글로벌 트랜잭션 적용(p367 그림 참고)
- 하나의 트랜잭션 안에서 여러 DB에 대한 작업이 필요한 경우 로컬 트랜잭션으로는 불가능  
  **로컬 트랜잭션은 하나의 DB Connection에 종속**
- 별도 트랜잭션 관리자를 통해 트랜잭션을 관리하는 글로벌 트랜잭션을 적용해야함
- 여러 DB가 참여하는 작업을 하나의 트랜잭션으로 가능하게 해줌
- `JTA(Java Transaction API)`: 글로벌 트랜잭션 지원  
  - DB 또는 메시징 서버(JMS)에 대한 트랜잭션 관리
  - 애플리케이션에서는 JDBC, JMS 같은 기존 방식의 API 적용
  - 트랜잭션 부문에 있어서는 JTA를 통해 트랜잭션 매니저에 위임
  - `Resource Manager`, `XA 프로토콜` 통해 연결
- 문제점
  - hibernate의 트랜잭션 관리 코드와 다르다. (독자적인 관리 API 사용)
  - Service 레이어에서 기술에 따라 로직이 바뀌어야 한다. (JTA 적용 유무에 따라)
  
### 트랜잭션의 추상화
- 스프링에서 제공하는 트랜잭션 추상화 기술 적용(`PlatformTransactionManager`)
- JDBC, JTA, Hibernate, JPA, JMS 등 각각의 트랜잭션 처리 코드의 추상화를 도입
- `PlatformTransactionManager`를 구현한 여러 기술들의 Manager 오브젝트 적용 가능
- Manager 계층에서 각 기술에 해당하는 Transaction을 관리

## 5.4 메일 서비스 추상화

- 스프링에서 제공하는 MailSender 인터페이스 사용
```xml
<bean id="mailSender" class="org.springframework.mail.javamail.JavaMailSenderImpl">
  <property name="host" value="mail.server.com"/>
</bean>
```
MailSender > JavaMailSenderImpl