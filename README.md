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
