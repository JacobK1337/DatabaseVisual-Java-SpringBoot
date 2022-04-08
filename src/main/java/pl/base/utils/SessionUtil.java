package pl.base.utils;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import pl.base.services.UserService;

import java.util.regex.Pattern;

@Component
public class SessionUtil {


    private final UserService userService;

    public SessionUtil(UserService userService){
        this.userService = userService;
    }

    public String username() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    public Long id() {
        return userService.getUserId(username());
    }

    public static Boolean validUserInput(String userInput) {

        //accepting only numbers and letters, empty string
        Pattern p = Pattern.compile("^[A-Za-z0-9 ]*$");
        return p.matcher(userInput).matches();
    }
}
