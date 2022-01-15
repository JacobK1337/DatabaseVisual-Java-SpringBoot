package pl.base.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepo extends JpaRepository<User, Long> {
    public User findByUsername(String username);
    public Optional<User> findById(Long id);
}
