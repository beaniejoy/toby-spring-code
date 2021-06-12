# chap 5 Service Abstraction(서비스 추상화)

> 성격이 비슷한 여러 종류의 기술을 추상화하고 이를 **일관된** 방법을 사용할 수 있도록 지원  
> 일관된 방법을 사용하면 어떤 기술의 내용이 변해도 이를 적용하는 오브젝트에서는 코드 변화 없이 유연하게 적용가능

## 5.1 사용자 레벨 관리 기능 추가
- User
    - level, login, recommend 필드 추가
    - upgrade level 로직에 대한 메소드 추가
- Level
    - 레벨을 표현하는 enum class 추가
    - level의 value, next level 내용을 담고 있음
    
- UserService
    - level upgrade를 수행하는 비즈니스 로직
    - user를 추가할 때 해당 user에 level이 없는 경우 default level(`BASIC`) 적용
    - level upgrade policy에 대한 인터페이스화를 통해 유연한 업그레이드 정책 주입 구현
