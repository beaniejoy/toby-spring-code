package io.spring.toby.user.service;

import io.spring.toby.user.dao.UserDao;
import io.spring.toby.user.domain.Level;
import io.spring.toby.user.domain.User;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;

import java.util.Arrays;
import java.util.List;

import static io.spring.toby.user.service.UserLevelUpgradePolicyImpl.MIN_LOGCOUNT_FOR_SILVER;
import static io.spring.toby.user.service.UserLevelUpgradePolicyImpl.MIN_RECOMMEND_FOR_GOLD;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class UserServiceTestWithMock {

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
    public void mockUpgradeLevels() throws Exception {
        UserServiceImpl userServiceImpl = new UserServiceImpl();

        UserDao mockUserDao = mock(UserDao.class);
        when(mockUserDao.getAll()).thenReturn(this.users);
        userServiceImpl.setUserDao(mockUserDao);

        MailSender mockMailSender = mock(MailSender.class);
        userServiceImpl.setMailSender(mockMailSender);

        UserLevelUpgradePolicy mockPolicy = mock(UserLevelUpgradePolicy.class);
        // 두번쨰, 네번째 사용자에 대해 upgrade 조건을 true로 설정
        when(mockPolicy.canUpgradeLevel(users.get(1))).thenReturn(true);
        when(mockPolicy.canUpgradeLevel(users.get(3))).thenReturn(true);
        userServiceImpl.setUserLevelUpgradePolicy(mockPolicy);

        userServiceImpl.upgradeLevels();

        verify(mockUserDao, times(2)).update(any(User.class));
        verify(mockUserDao, times(2)).update(any(User.class));
        verify(mockUserDao).update(users.get(1));
        assertThat(users.get(1).getLevel(), is(Level.SILVER));
        verify(mockUserDao).update(users.get(3));
        assertThat(users.get(3).getLevel(), is(Level.GOLD));

        ArgumentCaptor<SimpleMailMessage> mailMessageArg =
                ArgumentCaptor.forClass(SimpleMailMessage.class);
        // 실제 MailSender mock 오브젝트에 전달된 파라미터를 가져와 내용 검증 (getTo() 수신자 이메일)
        verify(mockMailSender, times(2)).send(mailMessageArg.capture());
        List<SimpleMailMessage> mailMessages = mailMessageArg.getAllValues();
        assertThat(mailMessages.get(0).getTo()[0], is(users.get(1).getEmail()));
        assertThat(mailMessages.get(1).getTo()[0], is(users.get(3).getEmail()));
    }
}
