package pl.base.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.base.entities.User;

import java.util.Optional;

public interface UserRepo extends JpaRepository<User, Long> {
    User findByUsername(String username);
    Optional<User> findById(Long id);
}
