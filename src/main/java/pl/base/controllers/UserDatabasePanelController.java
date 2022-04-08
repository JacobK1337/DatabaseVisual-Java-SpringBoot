package pl.base.controllers;

import com.google.gson.Gson;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import pl.base.utils.SessionUtil;
import pl.base.entities.DatabaseTable;
import pl.base.services.UserTableService;

import java.util.ArrayList;
import java.util.stream.Collectors;

@Controller
public class UserDatabasePanelController {

    private final UserTableService userTableService;

    public UserDatabasePanelController(UserTableService userTableService){
        this.userTableService = userTableService;
    }

    @PostMapping("/createNewTable")
    @ResponseStatus(HttpStatus.OK)
    public void createNewTable(@RequestParam("databaseId") String databaseId,
                               @RequestParam("tableName") String tableName,
                               @RequestParam("primaryKeyName") String primaryKeyName,
                               @RequestParam("primaryKeyType") String primaryKeyType) {

        long databaseIdLong;
        try {
            databaseIdLong = Long.parseLong(databaseId);
            if ((!SessionUtil.validUserInput(tableName) || !SessionUtil.validUserInput(primaryKeyName))
                    || tableName.equals("") || primaryKeyName.equals("")) throw new Exception("Invalid input");


            userTableService.createNewTable(
                    databaseIdLong,
                    tableName,
                    primaryKeyName,
                    primaryKeyType);

        } catch (Exception nfe) {
            System.out.println(nfe.getMessage() + ": only numbers and letters");
        }
    }

    @PostMapping("/deleteTable")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public void deleteTable(@RequestParam("tableId") String tableId,
                            @RequestParam("databaseId") String databaseId) {

        long tableIdLong;
        long databaseIdLong;
        try {
            tableIdLong = Long.parseLong(tableId);
            databaseIdLong = Long.parseLong(databaseId);
            userTableService.deleteTable(tableIdLong, databaseIdLong);

        } catch (NumberFormatException nfe) {
            nfe.printStackTrace();
        }

    }


    @PostMapping("/updatePlacement")
    @ResponseStatus(value = HttpStatus.OK)
    public void updateTablePlacement(@RequestParam("tableId") String tableId,
                                     @RequestParam("pageX") String pageX,
                                     @RequestParam("pageY") String pageY) {

        long tableIdLong;
        try {
            tableIdLong = Long.parseLong(tableId);
            double pageXF = Double.parseDouble(pageX);
            double pageYF = Double.parseDouble(pageY);
            int pageXINT = (int) pageXF;
            int pageYINT = (int) pageYF;

            userTableService.modifyTablePlacement(
                    tableIdLong,
                    pageXINT,
                    pageYINT);

        } catch (NumberFormatException nfe) {
            nfe.printStackTrace();
        }

    }

    @GetMapping("/getDatabaseTableDetails")
    @ResponseBody
    public String getDatabaseTableDetails(@RequestParam("databaseId") String databaseId) {

        long databaseIdLong;

        try {
            databaseIdLong = Long.parseLong(databaseId);


            var databaseTables = userTableService.getDatabaseTables(databaseIdLong);

            var allTablesDetails =
                databaseTables
                        .stream()
                        .map(DatabaseTable::getTableId)
                        .map(userTableService::getTableDetailsJson)
                        .collect(Collectors.toList());

            return new Gson().toJson(allTablesDetails);

        } catch (NumberFormatException nfe) {
            nfe.printStackTrace();
            return new Gson().toJson(new ArrayList<>());
        }


    }

    @GetMapping("/getTableDetails")
    @ResponseBody
    public String getTableDetails(@RequestParam("tableId") String tableId) {

        long tableIdLong;

        try {
            tableIdLong = Long.parseLong(tableId);
            return userTableService.getTableDetailsJson(tableIdLong);

        } catch (NumberFormatException nfe) {
            nfe.printStackTrace();
            return new Gson().toJson(new ArrayList<>());
        }

    }
}
