# chap 7 스프링 핵심 기술의 응용

## 7.1 SQL과 DAO의 분리

- 기존의 코드에서는 DAO 코드안에 SQL 쿼리를 담고 있는 형태
- SQL 수정시 DAO 코드를 건드리게 됨
- SQL 변경 포인트와 DAO 코드 변경 포인트를 따로 두는 것이 중요(DIP, SRP 원칙같이 객체지향관점에서)
- SQL를 DAO 코드에서 분리하는 것이 이번 챕터에서의 중점사안

### 분리 방법
- xml 설정을 이용한 분리
  - `UserDaoJdbc` 클래스에 `sqlAdd` 맴버변수 추가해서 xml 주입
  - `String` 타입 - sql 쿼리 스트링 데이터
```xml
<!-- userDao bean -->
<property name="sqlAdd"
          value="insert into users(id, name, password, level, login, recommend) values(?,?,?,?,?,?)"/>
```
- SQL 맵 프로퍼티 방식
  - SQL이 점점 많아지면 xml에 프로퍼티를 계속 추가하는 것은 너무 번거로움
  - SQL을 하나의 컬랙션으로 담아두는 방법(`Map` 컬랙션)

```xml
<!-- userDao bean -->
<property name="sqlMap">
  <map>
    <entry key="add"
           value="insert into users(id, name, password, level, login, recommend) values(?,?,?,?,?,?)"/>
    <!-- entry 추가하는 방식으로 sql 쿼리 추가 -->
  </map>
</property>
```
- 위의 두 개의 방법의 단점은 SQL쿼리가 String 형식으로 되어있어서 메소드가 실행되기 전까지 오류를 확인하기 힘들다.

### SQL과 DI 설정 정보 분리
- SQL 쿼리 문장과 DI 설정정보가 담긴 애플리케이션 구성정보가 하나의 설정파일에 있는 것은 지저분하다.
- 여기서 **독립적인 SQL 제공 서비스가 필요**
- `SqlService` interface를 통해 bean을 따로 설정해 sqlMap property를 관리
```java
public interface SqlService {
    String getSql(String key) throws SqlRetrievalFailureException;
}

public class SimpleSqlService implements SqlService {
  private Map<String, String> sqlMap;

  public void setSqlMap(Map<String, String> sqlMap) {
    this.sqlMap = sqlMap;
  }
  
  //...
}
```