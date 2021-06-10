# chap 3 Template

> OCP: 개방 폐쇄 원칙  
> - 외부 모듈의 변화에는 개방되어 있고 모듈의 변화로 인한 내부적인 코드의 변화에는 닫혀있는 원칙
> - 객체지향 프로그래밍 원칙 중 하나

## 3.2 변하는 것과 변하지 않는 것
- 템플릿 메소드 패턴 적용
    - DAO 로직마다 상속을 통해 새로운 클래스 만들어야 한다.  
    `UserDaoAdd`, `UserDaoDeleteAll`, `UserDaoGet` 등 기능마다 새로운 클래스 생성해야 함
    - 클래스 레벨에서 컴파일 시점에 이미 관계가 결정되어 있음  
    UserDao - UserDaoAdd 관계가 클래스 레벨에서 이미 결정되어 있음(유연하지 못함)
- 전략 패턴(p219)
    - 인터페이스를 템플릿과 구현클래스 사이에 두어 유연성 확보
    - 클래스 레벨에서 인터페이스를 통해 의존하기에 확장성 확보

**마이크로 DI**: 매우 작은 단위의 코드와 메소드 사이에서 일어나는 DI  
(deleteAll() - context method 사이)
