package otkhongluong.gamestoremanagement.dao;

import otkhongluong.gamestoremanagement.model.User;
import java.util.List;

/**
 * Interface DAO cho User.
 * Controller/Service chỉ phụ thuộc vào interface này,
 * không phụ thuộc vào implementation cụ thể → dễ mock khi test.
 */
public interface IUserDAO {
    boolean insert(User user);
    boolean update(User user);
    boolean delete(int maUser);
    User   findById(int maUser);
    User   findByUsername(String username);
    List<User> findAll();
}