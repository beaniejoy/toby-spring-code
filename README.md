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

## 7.2 인터페이스 분리와 자기참조 빈
- 위의 방식은 하나의 xml 설정파일에 SQL정보와 DI정보들을 담고 있다.
- SQL 전용 독립적인 설정파일로 관리하는 것이 좋음

### JAXB
- Java Architecture for XML Binding
- JDK 6, java.xml.bind package내 존재
- XML 문서정보를 동일한 구조의 오브젝트로 직접 매핑
  - XML 정보를 그대로 담고 있는 오브젝트 트리 구조로 만들어 줌
  - **XML 정보를 오브젝트처럼 다룰 수 있다는 장점 존재**
#### maven dependency
```xml
<dependency>
  <groupId>com.sun.xml.bind</groupId>
  <artifactId>jaxb-core</artifactId>
  <version>2.2.11</version>
</dependency>
<dependency>
  <groupId>com.sun.xml.bind</groupId>
  <artifactId>jaxb-impl</artifactId>
  <version>2.2.11</version>
</dependency>
<dependency>
  <groupId>javax.xml.bind</groupId>
  <artifactId>jaxb-api</artifactId>
  <version>2.2.11</version>
</dependency>
```
#### sqlmap.xsd
```xml
<?xml version="1.0" encoding="UTF-8"?>
<schema xmlns="http://www.w3.org/2001/XMLSchema"
        targetNamespace="http://www.example.org/sqlmap"
        xmlns:tns="http://www.example.org/sqlmap"
        elementFormDefault="qualified">
    <element name="sqlmap">
        <complexType>
            <sequence>
                <element name="sql" maxOccurs="unbounded" type="tns:sqlType"></element>
            </sequence>
        </complexType>
    </element>
    <complexType name="sqlType">
        <simpleContent>
            <extension base="string">
                <attribute name="key" use="required" type="string" />
            </extension>
        </simpleContent>
    </complexType>
</schema>
```
#### run JAXB compiler
```shell
$ xjc -p io.spring.toby.user.sqlservice.jaxb sqlmap.xsd -d src/main/java
```

- `SqlType.java` -> `<sql>` 태그 정보를 담을 클래스
- `Sqlmap.java` -> `<sqlmap>`이 바인딩될 클래스

#### JaxbTest시 유의사항
```java
Sqlmap sqlmap = (Sqlmap) unmarshaller.unmarshal(
    getClass().getResourceAsStream("sqlmap.xml")
);
```
- `getClass().getResourceAsStream("name")`은 maven에서 package된 `target` 폴더내의 `test-classes` 안에서의 같은 경로로 해야한다.

#### UserDao에 sqlmap.xml 파일 적용하기

```java
// XmlSqlService.java
InputStream is = UserDao.class.getResourceAsStream("/jaxb/sqlmap.xml");
```
- maven 프로젝트로 관리되기 때문에 설정파일은 `resources` 디렉토리에 관리하는 것이 좋다.
- `resources/jaxb/sqlmap.xml` 으로 관리되도록 구성함
- java 코드 내에서는 `getResourceAsStream` 기준이 `/` 로 설정하면 된다.

### Bean 초기화 작업
- 지금까지의 XmlSqlService 클래스는 기본 생성자에 JAXB 관련 xml 파일 언마샬링하는 작업을 진행하고 있다.
- 기본 생성자에 예외 발생하는 부분이 있으므로 이를 빈 초기화 작업에서 하는 것은 좋지 않다.
- 읽어들일 파일의 위치와 이름이 코드에 고정되어 있다는 점도 단점(`/jaxb/sqlmap.xml`)
- 이를 분리하는 작업 진행

```xml
<beans xmlns:context="http://www.springframework.org/schema/context"
       xsi:schemaLocation="http://www.springframework.org/schema/context
                           http://www.springframework.org/schema/context/spring-context-3.0.xsd">
  
    <context:annotation-config />
</beans>
```
- bean의 제어권은 스프링에 있기 때문에 스프링에서 처리해주어야 한다.
- AOP의 빈 후처리기를 사용(스프링 컨테이너가 빈 생성 후 부가적인 작업 수행)
  - proxy 자동생성기
  - 애노테이션을 이용한 후처리기 등록
```java
@PostConstruct
public void loadSql() { 
    //... 
}
```
1. XML 빈 설정파일을 읽는다.(`applicationContext.xml`)
2. 빈의 오브젝트 생성 (`<bean>`)
3. 프로퍼티에 의존 오브젝트(`ref`) 또는 값(`value`)을 주입(`<property>`)
4. 빈이나 태그로 등록된 후처리기를 동작(`@PostConstruct`)

### 인터페이스 분리
- XML 방식 이외의 SQL read 기술에 대한 범용적인 적용 가능한 구조로 전환
- `SqlReader`, `SqlRegistry` 두 개의 인터페이스를 `SqlService`에 적용
- `SqlReader`
  - JAXB와 같이 특정 구현에 의존하도록 정의되지 않게 구성해야 한다.
- `SqlRegistry`
  - `SqlReader`가 읽어들인 SQL 내용을 담는 인터페이스
  - `SqlService`가 이 인터페이스를 통해 SQL 검색
- 여기서는 XmlSqlService 하나의 클래스 코드에 `SqlService`, `SqlReader`, `SqlRegistry` 세 개의 인터페이스를 구현해서 사용함
  - 세 개의 인터페이스 각각의 구현 클래스를 구성하는 것보다 기존의 XmlSqlService에서 같이 상속받아 구현하는 것이 좋아보임
```xml
<bean id="sqlService" class="io.spring.toby.user.sqlservice.XmlSqlService">
  <property name="sqlReader" ref="sqlService"/>
  <property name="sqlRegistry" ref="sqlService"/>
  <property name="sqlmapFile" value="/jaxb/sqlmap.xml"/>
</bean>
```

### 디폴트 의존관계
- `XmlSqlService` 클래스에서 세 인터페이스의 구현 클래스를 각각 구현
  - `BaseSqlService`
  - `HashMapSqlRegistry`
  - `JaxbXmlReader`
- 이렇게 되면 xml 설정파일에도 세 부분에 대한 bean 등록을 해주어야 한다.
```java
public class DefaultSqlService extends BaseSqlService{
    public DefaultSqlService() {
        setSqlReader(new JaxbXmlSqlReader());
        setSqlRegistry(new HashMapSqlRegistry());
    }
}
```
- 만약 JAXB 기술을 기본적으로 사용한다면 디폴트 설정을 자바코드에 해줄 수 있다.
- **`디폴트 의존관계`: 외부에서 DI 받지 않는 경우 기본적으로 자동 적용되는 의존관계**
- 위 코드처럼 생성자에 기본 관계를 설정할 수 있다.
````java
// JaxbXmlSqlReader.java
private static final String DEFAULT_SQLMAP_FILE = "/jaxb/sqlmap.xml";
private String sqlmapFile = DEFAULT_SQLMAP_FILE;

public void setSqlmapFile(String sqlmapFile) {
    this.sqlmapFile = sqlmapFile;
}
````
- 대신 `JaxbXmlSqlReader` 코드에 `sqlmap.xml` 경로에 대한 디폴트 설정이 필요하다.

```xml
<bean id="sqlService" class="io.spring.toby.user.sqlservice.DefaultSqlService">
  <property name="sqlRegistry" ref="ultraSuperFastSqlRegistry" />
</bean>
```
- 이런 식으로 디폴트 의존 오브젝트 대신 사용하고 싶은 구현 오브젝트가 있으면 따로 프로퍼티로 지정하면 된다.
- 디폴트 의존 오브젝트의 단점
  - `DefaultSqlService` 생성자에서 일단 디폴트 의존 오브젝트를 다 만들어 버린다.
  - 한 두개면 상관없지만 많은 오브젝트를 쓸데 없이 만들어낸다면 이것 또한 비용
  - `@PostConstruct` 등의 초기화 메소드를 통해 이에 대해 적절히 제한 가능

## 7.3 서비스 추상화 적용
- JAXB 외에도 다양한 XML - Object 매핑하는 기술이 있음. 필요에 따라 다른 기술로 손쉽게 바꿔 사용할 수 있어야 함(`OCP`)
- 위에 내용까지는 UserDao 클래스와 같은 클래스 패스 안에서만 XML을 읽어올 수 있음
  - 임의의 클래스패스, 파일시스템 상절대위치, HTTP 프로토콜 통한 원격에서요가져올 수 있도록 확장이 필요
- OXM (Object-XML Mapping): XML과 자바 오브젝트를 매핑해서 상호 변환해주는 기술
- **스프링은 OXM에 대해서도 서비스 추상화 기능을 제공**
- 

> OxmTest 때 사용되는 sqlmap.xml은 test-classes 디렉토리 내 실행파일과 같은 위치에 있어야 한다.

