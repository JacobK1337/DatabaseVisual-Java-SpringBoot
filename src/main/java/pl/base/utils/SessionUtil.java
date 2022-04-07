package pl.base.utils;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import pl.base.services.UserManagement;

import java.util.regex.Pattern;

@Component
public class SessionUtil {


    private final UserManagement userManagement;

    public SessionUtil(UserManagement userManagement){
        this.userManagement = userManagement;
    }

    public String username() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    public Long id() {
        return userManagement.getUserId(username());
    }

    public static Boolean validUserInput(String userInput) {

        //accepting only numbers and letters, empty string
        Pattern p = Pattern.compile("^[A-Za-z0-9 ]*$");
        return p.matcher(userInput).matches();
    }
}
