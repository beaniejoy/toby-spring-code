package io.spring.toby.user.service;

import io.spring.toby.user.domain.User;

// 레벨 업그레이드 정책이 이벤트마다 다른 경우 유연하게 변경하기 위한 인터페이스 지정
public interface UserLevelUpgradePolicy {
    boolean canUpgradeLevel(User user);
}
