# chap 3 Template

> OCP: 개방 폐쇄 원칙  
> - 외부 모듈의 변화에는 개방되어 있고 모듈의 변화로 인한 내부적인 코드의 변화에는 닫혀있는 원칙
> - 객체지향 프로그래밍 원칙 중 하나

## 3.2 변하는 것과 변하지 않는 것
- 템플릿 메소드 패턴 적용
    - DAO 로직마다 상속을 통해 새로운 클래스 만들어야 한다.  
    `UserDaoAdd`, `UserDaoDeleteAll`, `UserDaoGet` 등 기능마다 새로운 클래스 생성해야 함
    - 클래스 레벨에서 컴파일 시점에 이미 관계가 결정되어 있음  
    UserDao - UserDaoAdd 관계가 클래스 레벨에서 이미 결정되어 있음(유연하지 못함)
- 전략 패턴(p219)
    - 인터페이스를 템플릿과 구현클래스 사이에 두어 유연성 확보
    - 클래스 레벨에서 인터페이스를 통해 의존하기에 확장성 확보

**마이크로 DI**: 매우 작은 단위의 코드와 메소드 사이에서 일어나는 DI  
(deleteAll() - context method 사이)

## 3.3 JDBC 전략 패턴의 최적화
- 익명 내부클래스 적용(anonymous inner class)
  - `new 인터페이스이름() {...}`
  - 메소드 기능마다 statement 생성하는 구현클래스를 따로 만들 필요가 없음
  - 내부 클래스의 코드에서 외부의 메소드 지역변수에 직접 접근가능  
    (add에서 insert할 User 오브젝트를 생성자로 만들지 않고 직접 메소드의 로컬 변수에서 가져옴)
    
## 3.5 템플릿과 콜백(p242)
```
io.spring.toby.learningtest.template

- Calculator
  - 템플릿 메소드
  - 기능 메소드: 계산하는 callback 인터페이스를 구현
- LineCallback
  - 달라지는 코드를 구현하도록 지시하는 인터페이스
- CalcSumTest
  - 해당 Calculator의 기능들 테스트
```

- **템플릿**: 어떤 목적을 위해 미리 만들어둔 모양이 있는 틀(context)
- **콜백**: 실행을 목적으로 다른 오브젝트 메소드에 전달되는 오브젝트
  - `functional object`라고도 한다.
  - 메소드 자체를 템플릿 안에 전달하기 위해 메소드가 담긴 오브젝트를 전달
  - 콜백은 하나의 메소드를 가진 인터페이스를 구현한 `익명 내부 클래스`로 생성
  
```java
public void setUp() {
    getClass().getResource("numbers.txt").getPath();
}
```
`target/test-classes/io/spring/toby/learningtest/template/numbers.txt`  
컴파일된 패키지 경로 안에 위치해야 한다.

- `LineCallback` 인터페이스를 통해 달라지는 부분만 구현
- 나머지는 Calculator 내부에 템플릿 메소드에서 구현한 callback을 받아서 처리
- Generic을 적용하면 더 범용적으로 사용가능(`LineCallback<T>`)

## 3.6 스프링의 JdbcTemplate
스프링에서 제공하는 템플릿/콜백 기술 중 JDBC를 사용할 수 있게 한 기술  

```java
public int getCount() throws SQLException {
    return this.jdbcTemplate.queryForObject("select count(*) from users", Integer.class);
}
@Deprecated
queryForInt("select count(*) from users")
```

```java
return this.jdbcTemplate.queryForObject(
        "select * from users where id = ?",
        new RowMapper<User>() {
            @Override
            public User mapRow(ResultSet rs, int i) throws SQLException {
                User user = new User();
                user.setId(rs.getString("id"));
                user.setName(rs.getString("name"));
                user.setPassword(rs.getString("password"));
                return user;
            }
        },
        id
);
```
```java
@Deprecated
queryForObject(String sql, @Nullable Object[] args, RowMapper<T> rowMapper)

queryForObject(String sql, RowMapper<T> rowMapper, @Nullable Object... args)
```
