package pl.base.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import pl.base.entities.User;
import pl.base.repositories.UserRepo;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepo repo;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User temp = repo.findByUsername(username);
        if(temp == null) throw new UsernameNotFoundException("User not found!");
        else return new UserPrincipal(temp);
    }
}
