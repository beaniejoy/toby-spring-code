# Chap 1 오브젝트

오브젝트 & 의존관계

## 1.2 DAO 분리

### 템플릿 메소드 패턴
슈퍼클래스(UserDao)에서 기본적인 로직 흐름 만들고  
기능의 일부(getConnection)를 추상 메소드나 오버라이딩 가능한 protected로 만든 뒤  
서브 클래스에서 구현

### 팩토리 메소드 패턴
서브클래스에서 구체적인 오브젝트 생성 방법을 결정하게 하는 것  
(getConnection을 통해 Connection 클래스의 오브젝트를 생성하는데 서브클래스에서 어떠한 Connection 객체를 생성할 것인지를 결정)

- UserDao (abstract class)
  - add() > 템플릿 메소드
  - get() > 템플릿 메소드
  - abstract getConnection() > 팩토리 메소드
- NUserDao: getConnection()
- DUserDao: getConnection()

## 1.3 DAO 확장

### OCP (Open-Closed Principle)
개방 폐쇄 원칙  
클래스나 모듈은 확장에는 열려 있어야 하고 (자신의 코드) 변경에는 닫혀 있어야 한다.

### 높은 응집도와 낮은 결합도
- 높은 응집도  
하나의 모듈, 클래스가 하나의 책임 또는 관심사에만 집중되어 있다는 의미  
높은 응집도 -> 변화에 대해 해당 모듈에서 변하는 부분이 크다는 의미

- 낮은 결합도  
  책임과 관심사가 다른 오브젝트, 모듈간에는 낮은 결합도, 느슨하게 연결된 형태를 유지해야 함.  
  `결합도`: 하나의 오브젝트 변경이 관계를 맺고 있는 다른 오브젝트에게 변화를 요구하는 정도.
  
### 전략 패턴
- UserDaoTest (Client)  
- UserDao (Context)  
- ConnectionMaker (Strategy)  

Context에서 변경이 필요한 부분에 대해 인터페이스로 외부에 분리시키고  
필요한 전략을 해당 인터페이스에 바꾸면서 사용하는 패턴 
