# chap 6 AOP

선언적 트랜잭션 기능 > 스프링에서 가장 인기 있는 AOP 적용 대상  
Proxy 방식으로 구현

## 6.1 트랜잭션 코드의 분리
- 비즈니스 로직 코드에서 직접 DB 사용 X
- 트랜잭션 준비 과정에서 만들어진 DB 커넥션 정보 등을 직접 참조할 필요가 없음

```java
// Proxy 생성을 위한 인터페이스화
public interface UserService {
}

// 실제 비즈니스 로직이 담긴 클래스
public class UserServiceImpl implements UserService{
    UserDao userDao;
    MailSender mailSender;
    UserLevelUpgradePolicy policy;
}

// 트랜잭션 기능만을 다루는 클래스
public class UserServiceTx implements UserService {
    UserService userService; // 실제 비즈니스 로직이 담긴 오브젝트를 주입
    PlatformTransactionManager transactionManager;
}
```
client에서는 표면상 UserServiceTx를 접근하는 것인데 내부적으로 트랜잭션 처리 후 실제 비즈니스 로직을 호출한다.  

## 6.2 고립된 단위 테스트
```
UserService
- UserDaoJdbc
- DataSourceTransactionManager
- JavaMailSenderImpl
- (UserLevelUpgradePolicy)
```
- `UserService` 오브젝트가 의존관계를 맺고 있는 오브젝트들이 많아서 테스트하기가 까다롭다.  
- 이들과 고립시킬 필요성이 있음
- Mock을 이용해 UserService를 다른 오브젝트들로부터 고립시킬 수 있다.
```
UserServiceImpl
- MockUserDao
- MockMailSender
- MockUserLevelUpgradePolicy
- UserServiceTx(PlatformTransactionManager와 이미 분리)
```
- **단위 테스트**  
    Mock 오브젝트 등의 테스트 대역을 이용해 의존 오브젝트나 외부 리소스를 사용하지 않도록 고립시켜 테스트
- **통합테스트**  
    두 개 이상의, 성격 혹은 계층이 다른 오브젝트가 연동하도록 만들어 테스트  
    또는 외부 DB나 파일, 서비스 등의 리소스가 참여하는 테스트
  
### Mockito Framework
- Mock을 만드는데 도움을 주는 프레임워크  
- 하나하나 목 클래스를 안 만들어도 된다.
- 인터페이스를 이용해 목 오브젝트를 만든다.

```java
UserDao mockUserDao = mock(UserDao.class);
when(mockUserDao.getAll()).thenReturn(this.users);
userServiceImpl.setUserDao(mockUserDao);

MailSender mockMailSender = mock(MailSender.class);
userServiceImpl.setMailSender(mockMailSender);

UserLevelUpgradePolicy mockPolicy = mock(UserLevelUpgradePolicy.class);
// 두번쨰, 네번째 사용자에 대해 upgrade 조건을 true로 설정
when(mockPolicy.canUpgradeLevel(users.get(1))).thenReturn(true);
when(mockPolicy.canUpgradeLevel(users.get(3))).thenReturn(true);
userServiceImpl.setUserLevelUpgradePolicy(mockPolicy);
```
UserService가 의존하고 있는 오브젝트를 대상으로 가짜객체 생성해서  
setter 주입을 한다.

## 6.3 다이내믹 프록시와 팩토리 빈
### Proxy
- UserService를 인터페이스화해 핵심기능을 가진 클래스와 부가기능을 가진 클래스를 구현
- 부가기능을 가진 클래스 내부에서 핵심기능 클래스를 사용하고 대리자 역할로 본인이 클라이언트에 노출
- `Proxy`(대리자), `Target`(실제 오브젝트)
    
### Decorator Pattern
여러 프록시를 타겟에 적용

### Proxy Pattern
- 프록시와 다르게 타깃에 대한 접근 방법을 제어하려는 목적을 가진 경우
- 타깃의 기능 자체에는 관여하지 않고 접근하는 방법을 제어해주는 프록시를 이용함

### 다이나믹 프록시
- 프록시 팩토리에 의해 런타임 시 동적으로 만들어지는 오브젝트
- 프록시 팩토리에게 인터페이스 정보만 제공하면 해당 인터페이스를 구현한 클래스의 오브젝트를 자동 생성해줌
- 프록시 팩토리로 만들어진 인터페이스 구현 클래스의 오브젝트에 필요한 부가기능은 직접 구현
- 부가기능은 `InvocationHandler`를 구현한 오브젝트에 담는다.
````java
public class UppercaseHandler implements InvocationHandler {
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 여기서 부가기능 수행
        return null;
    }
}
````
- InvocationHandler는 invoke 메소드 하나만 구현하면 된다.  
- 클라이언트의 모든 요청을 리플렉션 정보로 변환해서 invoke 메소드로 넘김
- 여기서 Method parameter를 통해 타깃 오브젝트의 메소드를 호출할 수 있다.

#### 다이나믹 프록시 장점
- 프록시를 적용할 인터페이스의 메소드 개수가 수십개라면 프록시는 모든 메소드에 대해 부가기능 코드를 추가해야 한다.
- 다이내믹 프록시는 프록시가 동적으로 만들어질 때 해당 구현 메소드가 자동으로 추가된다.
- 타깃의 종류에 상관없이 프록시 적용 가능

### 팩토리 빈
````java
Class.forName("Class Name").newInstance();
````
- 스프링은 위와 같이 내부적으로 리플렉션 API를 이용해 빈 오브젝트 생성(등록)
```java
// Proxy 오브젝트 생성 -> 문제는 이 자체만으로 스프링에 빈으로 등록 불가
UserService txUserService = (UserService) Proxy.newProxyInstance(
        getClass().getClassLoader(),    // 동적으로 생성되는 다이내믹 프록시 클래스의 로딩에 사용될 클래스로더 설정
        new Class[]{UserService.class}, // 구현할 인터페이스 대상
        txHandler                       // 부가기능과 위임 코드를 담은 InvocationHandler 구현체
);
```
- 다이내믹 프록시는 위의 방식과 다르게 빈 설정이 불가
- 다이내믹 프록시는 일반적인 스프링 빈으로 등록할 방법이 없음
- 팩토리 빈(`FactoryBean`)을 사용하게 된다.
```xml
<bean id="userService" class="io.spring.toby.user.service.TxProxyFactoryBean">
    <property name="target" ref="userServiceImpl"/>
    <property name="transactionManager" ref="transactionManager"/>
    <property name="pattern" value="upgradeLevels"/>
    <property name="serviceInterface" value="io.spring.toby.user.service.UserService"/>
</bean>
```
FactoryBean을 bean으로 설정함으로써 getObject에 의한 새로운 오브젝트를 빈으로 설정

#### 팩토리 빈 장점
- 프록시 팩토리 빈 재사용성
    - TxProxyFactoryBean 클래스는 다양한 서비스 인터페이스에 적용 가능
    - UserService에만 적용되는 것이 아닌 여러 Service 인터페이스에 범용적으로 사용가능
    - TxProxyFactoryBean에 해당 인터페이스의 Class 정보만 주입해주면 된다.
    
#### 팩토리 빈 한계
- 한 클래스 안에서의 여러 메소드에 대해 부가기능을 한번에 제공하는 것은 가능
- but, 여러 클래스에서 해당 부가기능 적용해야 할 때 팩토리 빈은 구현할 수 없음
- 여러 개의 부가기능 적용할 때도 하나의 부가기능을 위한 xml bean 설정 내용을 부가기능마다 다 설정해줘야 한다.
- xml 설정 내용에 대한 부담이 커짐
- `InvocationHandler` 오브젝트가 프록시 팩토리 빈 개수만큼 새로 생성됨(중복 발생)

## 6.4 스프링의 프록시 팩토리 빈
- 스프링에서 여러 JDK 기반 프록시 생성 기술을 일관된 방법으로 만들 수 있게 도와주는 추상 레이어
- **프록시 오브젝트를 생성해주는 기술을 추상화한 팩토리 빈을 스프링에서 제공**(`ProxyFactoryBean`)

### ProxyFactoryBean
- `MethodInterceptor`
    - `InvocationHandler` 인터페이스의 `invoke()` 단계에서 타깃 오브젝트에 대한 정보 제공하지 않음  
    이것을 구현한 클래스에서 타깃을 알고 있어야 함(주입 받아야 한다.)
    - `MethodInterceptor` `invoke()` 단계에서 ProxyFactoryBean에서 타깃 오브젝트 정보까지 함께 제공 받음
    - 타깃 오브젝트에 상관없이 독립적으로 생성 가능(DI가 필요없어서 타깃과 의존관계가 없어짐)
    
- `Advice`
    - 타깃 오브젝트에 종속되지 않는 순수한 부가기능을 담은 오브젝트
    - 어드바이스는 일종의 템플릿이 되고 `MethodInvocation`이 콜백이 된다. (템플릿 콜백 패턴)
    - 여기서는 `MethodInterceptor`의 구현체가 `Advice`가 된다.
    
- `Pointcut`
    - 기존에 `InvocationHandler`에서는 pattern을 주입받아서 적용 대상 메소드를 설정함
    - `MethodInterceptor`는 여러 프록시가 공유해서 사용하기에 pattern 주입방식은 적절하지 못함  
      (모든 프록시가 주입받은 하나의 pattern 적용방식을 따를 수 밖에 없음)
    - 프록시로부터 pattern을 분리 -> `Pointcut` 사용
    
- `Advisor`
    - `Pointcut + Advice`
    - `ProxyFactoryBean`에는 여러 `Advice`, `Pointcut`이 추가되기에 이를 매핑해야 함  
    
Proxy - Advice, Pointcut 분리와 DI 적용 > 전략 패턴 구조  

## 6.5 스프링 AOP
- 한번에 여러 개의 빈에 ProxyFactoryBean에 의한 프록시를 적용할 수 없을까에서 출발
- 이전까지는 타깃 빈에 대해서 ProxyFactoryBean 설정부분을 추가했어야 했다.

### BeanPostProcessor
- 빈 후처리기
- 스프링 빈 오브젝트로 만들어지고 난 후에 빈 오브젝트를 다시 가공할 수 있게 해줌
- `DefaultAdvisorAutoProxyCreator`
    - 스프링이 제공하는 빈 후처리기 중 하나
    - Advisor를 이용한 자동 프록시 생성기
    - 등록된 빈 중에서 Advisor 인터페이스를 구현한 것을 모두 찾는다.

```xml
<bean class="org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator"/>
```
    
### Pointcut
- 포인트컷은 프록시를 적용할 클래스 확인과 어드바이스 적용할 메소드 확인 기능 둘다 가지고 있음
- 프록시 적용 대상 클래스 여부 판단 > 어드바이스 적용할 메소드 확인

```java
public interface Pointcut {
    ClassFilter getClassFilter();       // 프록시 적용할 클래스 확인
    MethodMatcher getMethodMatcher();   // 어드바이스를 적용할 메소드 확인
}
```

### 포인트컷 표현식
```xml
<!-- AspectJ-->
<dependency>
    <groupId>org.aspectj</groupId>
    <artifactId>aspectjrt</artifactId>
    <version>1.9.6</version>
</dependency>
<dependency>
    <groupId>org.aspectj</groupId>
    <artifactId>aspectjweaver</artifactId>
    <version>1.9.6</version>
</dependency>
```

- `execution(* *(..))`
- `execution(* hello(..))`
- `execution(* hello())`
- `execution(* hello(String))`
- `@annotation(org.springframework.transaction.annotation.Transactional)`  
  이런 식으로 `@Transactional`에 대해 프록시를 생성해줄 수 있다.
- 포인트컷 표현식의 클래스 이름에 적용되는 패턴은 이름 패턴이 아닌 타입패턴  
  ex) `*..UserService`라고 한다면 `UserService` 인터페이스를 구현한 빈들 모두에 해당 
  
### AOP
- `Aspect Oriented Programing`  
- 부가기능 모듈화에 대해 기존 객체지향 설계 패러다임과는 구분되는 새로운 특성이 존재.
- Aspect(관점): 애플리케이션 핵심기능을 담고있지는 않지만 핵심기능에 부가되어 의미를 갖는 특별한 모듈  
  (Logging 관점, Transaction 처리 관점 등등 기능의 관심사에 따라 나뉘어서 관점이 붙은듯하다.)
- `Spring AOP`는 프록시 방식의 AOP
- `AspectJ`도 존재  
  - 바이트코드 조작을 통한 타깃 오브젝트 수정 방식
  - 컴파일된 클래스 파일 직접 수정 방식
  
### AOP Namespace
```xml
<beans ...
    xmlns:aop="http://www.springframework.org/schema/aop"
    xsi:schemaLocation="...
                        http://www.springframework.org/schema/aop
                        http://www.springframework.org/schema/aop/spring-aop-3.0.xsd">

    <aop:config>
        <aop:pointcut id="transactionPoincut"
                      expression="execution(* *..*ServiceImpl.upgrade*(..))" />
        <aop:advisor advice-ref="transactionAdvice" pointcut-ref="transactionPointcut" />
    </aop:config>
</beans>
```
위의 xml로 aop 등록 가능
```xml
<aop:config>
    <aop:advisor advice-ref="transactionAdvice" 
                 pointcut="execution(* *..*ServiceImpl.upgrade*(..))" />
</aop:config>
```
이렇게 하나의 태그로 advice, pointcut 등록 가능  
