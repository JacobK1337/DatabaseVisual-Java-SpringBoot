package pl.base.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UserManagement {

    @Autowired
    UserRepo repo;

    private BCryptPasswordEncoder passwordEncoder(){return new BCryptPasswordEncoder();}


    public Long getUserId(String username){
        User tempUser = repo.findByUsername(username);
        return Optional.ofNullable(tempUser.getId()).orElse(0L);
    }

    public void createNewAccount(String username, String password) throws Exception{
        if(repo.findByUsername(username) != null) throw new Exception("Username exists");

        String encodedPassword = passwordEncoder().encode(password);

        User newUser = new User(
                0L,
                username,
                encodedPassword
        );

        repo.save(newUser);
    }

}
