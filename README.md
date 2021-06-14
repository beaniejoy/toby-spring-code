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
