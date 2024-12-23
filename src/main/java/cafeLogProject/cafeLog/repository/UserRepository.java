package cafeLogProject.cafeLog.repository;

import cafeLogProject.cafeLog.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findById(Long userId);
    User save(User user);
}
