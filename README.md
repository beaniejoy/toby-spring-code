# chap 4 Exception

## 4.1 사라진 SQLException

- 체크예외: `RuntimeException`을 상속하지 않은 예외
- 언체크예외: `RuntimeException`을 상속하는 예외

최근에 새로 등장하는 자바 표준 스펙의 API들은 체크 예외로 만들지 않는 경향이 있다.

###  예외처리 방법
- 예외 복구
  - 예외상황을 파악하고 문제를 해결해 정상상태로 돌려놓음
  - 체크 예외들은 예외를 어떤 식으로든 복구할 가능성이 있는 경우에 사용  
    (`MAX_RETRY`만큼 재시도를 함으로써 예외를 복구하는 방법)
- 예외처리 회피
  - `throws`를 통해 자신을 호출한 쪽으로 예외처리 책임을 던지는 것
  - 별로 좋지 않은 방법(무책임)
- 예외 전환
  - 예외 처리를 밖으로 던지는 것은 똑같으나 적절한 예외로 전환해서 던짐
  - 1. **의미를 분명하게 해줄 수 있는 예외로 바꿔주기 위함**  
    (`SQLExcpetion` -> `DuplicateUserIdException`)
  - 보통 전환하는 예외에 원래 발생한 예외를 담아서 보내는 것이 좋다.(중첩 예외)
    - `throw DuplicateUserIdException(e)`, e: `SQLException`  
    - `throw DuplicateUserIdException().initCause(e)`
  - 2. **예외를 처리하기 쉽고 단순하게 만들기 위해 포장하기 위함**
    - 체크 예외 > 런타임 예외로 전환 (`EJBException(e)`)
  
### JdbcTemplate에서 사용하는 예외처리
- 예외 전환 방식을 사용
- `SQLExcpetion` > `DataAccessException` 전환(런타임 예외로 전환)
- 스프링 API 메소드에 정의되어 있는 대부분의 예외는 런타임 예외(예외 처리를 강제하지 않는다.)
