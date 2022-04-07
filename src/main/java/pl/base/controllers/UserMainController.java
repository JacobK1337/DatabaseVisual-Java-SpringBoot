package pl.base.controllers;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import pl.base.services.DatabaseManagement;
import pl.base.utils.SessionUtil;
import pl.base.services.UserManagement;

@Controller
public class UserMainController {


    private final SessionUtil sessionUtil;

    private final UserManagement userManagement;

    private final DatabaseManagement dbManagement;


    public UserMainController(UserManagement userManagement,
                              DatabaseManagement dbManagement,
                              SessionUtil sessionUtil){

        this.userManagement = userManagement;
        this.dbManagement = dbManagement;
        this.sessionUtil = sessionUtil;
    }

    @GetMapping("/login")
    public String login() {
        Authentication check = SecurityContextHolder.getContext().getAuthentication();

        if (check == null || check instanceof AnonymousAuthenticationToken)
            return "login";

        else return "redirect:/home";
    }

    @GetMapping("/register")
    String register() {
        return "register";
    }

    @GetMapping("/home")
    String home() {
        return "home";
    }

    @GetMapping("/")
    String welcome() {
        Authentication check = SecurityContextHolder.getContext().getAuthentication();
        if (check == null || check instanceof AnonymousAuthenticationToken)
            return "welcome";
        else
            return "redirect:/home";
    }

    @PostMapping("/createAccount")
    public String createAccount(@RequestParam("username") String username,
                                @RequestParam("password") String password) {
        try {

            if (!SessionUtil.validUserInput(username) || !SessionUtil.validUserInput(password)) throw new Exception("Invalid input");

            userManagement.createNewAccount(username, password);
            return "redirect:/login";

        } catch (Exception e) {
            return "redirect:/register";
        }

    }

    @GetMapping("/databases")
    public String databases(Model model) {
        model.addAttribute("userDatabases", dbManagement.getUserDatabases(sessionUtil.id()));

        return "databases";
    }

    @PostMapping("/createDatabase")
    public String createDatabase(@RequestParam("databaseName") String databaseName) {

        try {
            if (!SessionUtil.validUserInput(databaseName)) throw new Exception("Invalid input");

            dbManagement.createNewDatabase(sessionUtil.id(), databaseName);
            return "redirect:/databases";
        } catch (Exception e) {
            return "redirect:/home";
        }

    }

    @GetMapping("/panel")
    public String panel(@RequestParam("databaseId") String databaseId, Model model) {

        long databaseIdLong;

        try {

            databaseIdLong = Long.parseLong(databaseId);
            model.addAttribute("databaseId", databaseIdLong);
            return "panel";

        } catch (NumberFormatException nfe) {

            nfe.printStackTrace();
            return "redirect:/home";

        }

    }

    @GetMapping("/manage_data")
    public ModelAndView manageData(@RequestParam("tableId") String tableId,
                                   @RequestParam("databaseId") String databaseId) {

        ModelAndView manageDataPage = new ModelAndView("manage_data");
        manageDataPage.addObject("tableId", tableId);
        manageDataPage.addObject("databaseId", databaseId);

        return manageDataPage;
    }





}
