package io.spring.toby.user.dao;

import io.spring.toby.user.domain.User;

import java.sql.SQLException;

public class UserDaoTest {
    public static void main(String[] args) throws SQLException, ClassNotFoundException {
        ConnectionMaker connectionMaker = new MySQLConnectionMaker();

//        UserDao dao = new UserDao(connectionMaker);
        UserDao dao = new DaoFactory().userDao();
        User user = new User();
        user.setId("chap1.3");
        user.setName("chap1.3 분리");
        user.setPassword("complete");

        dao.add(user);

        System.out.println(user.getId() + " 등록 성공");

        User user2 = dao.get(user.getId());
        System.out.println(user2.getName());
        System.out.println(user2.getPassword());

        System.out.println(user2.getId() + " 조회 성공");
    }
}
