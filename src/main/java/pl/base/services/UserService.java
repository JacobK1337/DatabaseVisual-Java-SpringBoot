package pl.base.services;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import pl.base.entities.User;
import pl.base.repositories.UserRepo;

import java.util.Optional;

@Service
public class UserService implements UserDetailsService {

    private final UserRepo userRepo;

    public UserService(UserRepo userRepo){
        this.userRepo = userRepo;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User temp = userRepo.findByUsername(username);
        if(temp == null) throw new UsernameNotFoundException("User not found!");
        else return new UserPrincipal(temp);
    }

    private BCryptPasswordEncoder passwordEncoder(){return new BCryptPasswordEncoder();}


    public Long getUserId(String username){
        User tempUser = userRepo.findByUsername(username);
        return Optional.ofNullable(tempUser.getId()).orElse(0L);
    }

    public void createNewAccount(String username, String password) throws Exception{
        if(userRepo.findByUsername(username) != null) throw new Exception("Username exists");

        String encodedPassword = passwordEncoder().encode(password);

        User newUser = new User(
                0L,
                username,
                encodedPassword
        );

        userRepo.save(newUser);
    }
}
