# Chap 1

## Connection 분리

### 템플릿 메소드 패턴
슈퍼클래스(UserDao)에서 기본적인 로직 흐름 만들고  
기능의 일부(getConnection)를 추상 메소드나 오버라이딩 가능한 protected로 만든 뒤  
서브 클래스에서 구현

### 팩토리 메소드 패턴
서브클래스에서 구체적인 오브젝트 생성 방법을 결정하게 하는 것  
(getConnection을 통해 Connection 클래스의 오브젝트를 생성하는데 서브클래스에서 어떠한 Connection 객체를 생성할 것인지를 결정)

