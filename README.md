# Chap 2 Test

## 2.4 스프링 테스트 적용

```java
@DirtiesContext
```
- 해당 클래스의 테스트에서 applicationContext의 상태를 변경한다는 것을 알려줌
- 테스트 컨텍스트는 이 Annotation이 붙은 클래스(혹은 메서드)에는 applicationContext를 공유하지 않는다.