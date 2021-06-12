package io.spring.toby.user.dao;

import io.spring.toby.user.domain.Level;
import io.spring.toby.user.domain.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import javax.sql.DataSource;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class UserDaoJdbc implements UserDao{

    private JdbcTemplate jdbcTemplate;
    private RowMapper<User> userMapper =
            new RowMapper<User>() {
                @Override
                public User mapRow(ResultSet rs, int rowNum) throws SQLException {
                    User user = new User();
                    user.setId(rs.getString("id"));
                    user.setName(rs.getString("name"));
                    user.setPassword(rs.getString("password"));
                    user.setLevel(Level.valueOf(rs.getInt("level")));
                    user.setLogin(rs.getInt("login"));
                    user.setRecommend(rs.getInt("recommend"));
                    return user;
                }
            };

    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public void add(final User user) {
        // 익명 내부클래스 적용
        this.jdbcTemplate.update(
                "insert into users(id, name, password, level, login, recommend) values(?,?,?,?,?,?)",
                user.getId(),
                user.getName(),
                user.getPassword(),
                user.getLevel().intValue(),
                user.getLogin(),
                user.getRecommend());
    }


    @Override
    public User get(String id) {
        return this.jdbcTemplate.queryForObject("select * from users where id = ?", userMapper, id);
    }

    @Override
    public List<User> getAll() {
        return this.jdbcTemplate.query("select * from users order by id", userMapper);
    }

    @Override
    public int getCount() {
        return this.jdbcTemplate.queryForObject("select count(*) from users", Integer.class);
    }

    @Override
    public void update(User user) {
        this.jdbcTemplate.update(
                "update users " +
                        "set name = ?, password = ?, level = ?, login = ?, recommend = ? " +
                        "where id = ? ",
                user.getName(),
                user.getPassword(),
                user.getLevel().intValue(),
                user.getLogin(),
                user.getRecommend(),
                user.getId());
    }

    @Override
    public void deleteAll() {
        this.jdbcTemplate.update("delete from users");
    }

}
