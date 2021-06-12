package io.spring.toby.user.service;

import io.spring.toby.user.dao.UserDao;
import io.spring.toby.user.domain.Level;
import io.spring.toby.user.domain.User;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.List;

import static io.spring.toby.user.service.UserLevelUpgradePolicyImpl.MIN_LOGCOUNT_FOR_SILVER;
import static io.spring.toby.user.service.UserLevelUpgradePolicyImpl.MIN_RECOMMEND_FOR_GOLD;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/test-applicationContext.xml")
public class UserServiceTest {
    // 책과 다른부분
    // upgradeLevel()을 인터페이스화 했기 때문에 proxy 기반으로 구성
    static class TestUserLevelUpgradePolicy implements UserLevelUpgradePolicy {
        private String id;
        private UserLevelUpgradePolicy policy; // original policy

        public TestUserLevelUpgradePolicy(String id, UserLevelUpgradePolicy policy) {
            this.id = id;
            this.policy = policy;
        }

        @Override
        public boolean canUpgradeLevel(User user) {
            return policy.canUpgradeLevel(user);
        }

        @Override
        public void upgradeLevel(User user) {
            if (user.getId().equals(this.id)) throw new TestUserLevelUpgradePolicyException();
            policy.upgradeLevel(user);
        }
    }

    static class TestUserLevelUpgradePolicyException extends RuntimeException {
    }

    @Autowired
    UserService userService;

    @Autowired
    UserDao userDao;

    @Autowired
    UserLevelUpgradePolicy policy;

    List<User> users;

    @Before
    public void setUp() {
        users = Arrays.asList(
                new User("aaaaa", "비니", "spring1", Level.BASIC, MIN_LOGCOUNT_FOR_SILVER - 1, 0),
                new User("bbbbb", "조이", "spring2", Level.BASIC, MIN_LOGCOUNT_FOR_SILVER, 0),
                new User("ccccc", "결혼", "spring3", Level.SILVER, 60, MIN_RECOMMEND_FOR_GOLD - 1),
                new User("ddddd", "좋아", "spring4", Level.SILVER, 60, MIN_RECOMMEND_FOR_GOLD),
                new User("eeeee", "나이스", "spring5", Level.GOLD, 100, Integer.MAX_VALUE)
        );
    }

    @Test
    public void bean() {
        assertThat(this.userService, is(notNullValue()));
    }

    @Test
    public void upgradeLevels() throws Exception {
        userDao.deleteAll();
        for (User user : users) userDao.add(user);

        userService.upgradeLevels();

        checkLevelUpgraded(users.get(0), false);
        checkLevelUpgraded(users.get(1), true);
        checkLevelUpgraded(users.get(2), false);
        checkLevelUpgraded(users.get(3), true);
        checkLevelUpgraded(users.get(4), false);
    }

    @Test
    public void add() {
        userDao.deleteAll();

        User userWithLevel = users.get(4);
        User userWithoutLevel = users.get(0);
        userWithoutLevel.setLevel(null);

        userService.add(userWithLevel);
        userService.add(userWithoutLevel);

        User userWithLevelRead = userDao.get(userWithLevel.getId());
        User userWithoutLevelRead = userDao.get(userWithoutLevel.getId());

        assertThat(userWithLevelRead.getLevel(), is(userWithLevel.getLevel()));
        assertThat(userWithoutLevelRead.getLevel(), is(Level.BASIC));
    }

    @Test
    @DirtiesContext // UserService에 test용 proxy policy 주입 설정
    public void upgradeAllOrNothing() throws Exception {
        // 4번째 사용자 레벨 업그레이드 도중 에러발생 유도
        UserLevelUpgradePolicy testPolicy = new TestUserLevelUpgradePolicy(users.get(3).getId(), policy);
        // test용 proxy 객체 주입
        userService.setUserLevelUpgradePolicy(testPolicy);

        userDao.deleteAll();
        for (User user : users) userDao.add(user);

        try {
            userService.upgradeLevels();
            fail("TestUserLevelUpgradePolicyException expected");
        } catch (TestUserLevelUpgradePolicyException e) {
            System.out.println("test error");
        }

        checkLevelUpgraded(users.get(1), false);
    }

    // 업그레이드가 실제 이루어졌는지를 확인하는 방법으로 변경
    private void checkLevelUpgraded(User user, boolean upgraded) {
        User userUpdate = userDao.get(user.getId());
        if (upgraded) {
            assertThat(userUpdate.getLevel(), is(user.getLevel().nextLevel()));
        } else {
            assertThat(userUpdate.getLevel(), is(user.getLevel()));
        }
    }

}