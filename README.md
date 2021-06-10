# Chap 2 Test

## 2.4 스프링 테스트 적용

```java
@DirtiesContext
```
- 해당 클래스의 테스트에서 applicationContext의 상태를 변경한다는 것을 알려줌
- 테스트 컨텍스트는 이 Annotation이 붙은 클래스(혹은 메서드)에는 applicationContext를 공유하지 않는다.

## 2.5 학습 테스트

```java
public class JUnitTest {
    @Test
    public void test1() {
        this;
    }

    @Test
    public void test2() {
        this; // 위의 this와 다른 오브젝트
    }
}
```
- `@Test`가 붙은 메서드를 테스트할 때 매 테스트를 실행할 때마다 새로운 테스트 오브젝트를 생성
- 매 테스트 실행 때마다 JUnitTest 오브젝트가 새로 생성(p182 참고)