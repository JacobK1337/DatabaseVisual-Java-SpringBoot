package pl.base.datashareproject;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import pl.base.constraints.ConstraintManagement;
import pl.base.constraints.FieldConstraint;
import pl.base.databases.DatabaseManagement;
import pl.base.fields.FieldManagement;
import pl.base.fields.TableField;
import pl.base.tables.DatabaseTable;

import pl.base.tables.TableDetails;
import pl.base.tables.TableManagement;
import pl.base.user.UserManagement;

import java.lang.reflect.Field;
import java.util.*;

@Controller
public class WebController {

    @Autowired
    private UserManagement userManagement;

    @Autowired
    private DatabaseManagement dbManagement;

    @Autowired
    private TableManagement tabManagement;

    @Autowired
    private ConstraintManagement constraintManagement;

    @Autowired
    private FieldManagement fieldManagement;

    @GetMapping("/login")
    public String login(){
        Authentication check = SecurityContextHolder.getContext().getAuthentication();

        if(check == null || check instanceof AnonymousAuthenticationToken)
            return "login";
        else return "redirect:/home";
    }

    @GetMapping("/register")
    String register(){
        return "register";
    }

    @GetMapping("/home")
    String home(){
        return "home";
    }

    @GetMapping("/")
    String welcome(){
        Authentication check = SecurityContextHolder.getContext().getAuthentication();
        if(check == null || check instanceof AnonymousAuthenticationToken)
            return "welcome";
        else
            return "redirect:/home";
    }

    @PostMapping("/createAccount")
    public String createAccount(@RequestParam("username") String username,
                                @RequestParam("password") String password){
        try{

            userManagement.createNewAccount(username, password);
            return "redirect:/login";
        }
        catch(Exception e){
            return "redirect:/register";
        }

    }

    @GetMapping("/databases")
    public String databases(Model model){
        model.addAttribute("userDatabases", dbManagement.getUserDatabases(id()));

        return "databases";
    }

    @PostMapping("/createDatabase")
    public String createDatabase(@RequestParam("databaseName") String databaseName){
        dbManagement.createNewDatabase(id(), databaseName);

        return "redirect:/databases";
    }

    @PostMapping("/panel")
    public String panel(@RequestParam("databaseId") String databaseId, Model model){
        Long dbId = Long.parseLong(databaseId);

        model.addAttribute("databaseId", dbId);
        return "panel";

    }

    @GetMapping("/manage_data")
    public ModelAndView manageData(@RequestParam("tableId") String tableId){
        ModelAndView manageDataPage = new ModelAndView("manage_data");
        manageDataPage.addObject("tableId", tableId);

        return manageDataPage;
    }

    @GetMapping("/tableDetails")
    @ResponseBody
    public String tableDetails(@RequestParam("databaseId") String databaseId){
        Long databaseIdL = Long.parseLong(databaseId);

        List<List<String>> tableDetails = new ArrayList<>();

        for(DatabaseTable dt : tabManagement.getDatabaseTables(databaseIdL)){
            Long tableId = dt.getTableId();

            TableDetails currentTableDetails = tabManagement.getTableDetails(tableId);

            String tableIdStr = String.valueOf(tableId);
            String tableName = currentTableDetails.getTableName();
            String pageX = String.valueOf(currentTableDetails.getPageX());
            String pageY = String.valueOf(currentTableDetails.getPageY());
            tableDetails.add(Arrays.asList(
                    tableIdStr,
                    tableName,
                    pageX,
                    pageY
            ));
        }

        return new Gson().toJson(tableDetails);
    }

    @GetMapping("/getTableName")
    @ResponseBody
    public String getTableName(@RequestParam("tableId") String tableId){
        Long tabId = Long.parseLong(tableId);

        return new Gson().toJson(tabManagement.getTableName(tabId));
    }

    @GetMapping("/getTableFields")
    @ResponseBody
    public String getTableFields(@RequestParam("tableId") String tableId){
        Long tabId = Long.parseLong(tableId);

        List<List<String>> fieldInfo = new ArrayList<>();
        for(TableField tf : tabManagement.getTableFields(tabId)){
            String id = tf.getFieldId().toString();
            String type = tf.getFieldType();
            String name = tf.getFieldName();

            fieldInfo.add(Arrays.asList(
                    id,
                    type,
                    name
            ));
        }

        return new Gson().toJson(fieldInfo);
    }
    @GetMapping("/getTableData")
    @ResponseBody
    public String getTableData(@RequestParam("tableId") String tableId){

        Long tabId = Long.parseLong(tableId);
        return new Gson().toJson(tabManagement.getTableData(tabId));

    }

    @GetMapping("/getFilteredTableData")
    @ResponseBody
    public String getFilteredTableData(@RequestParam Map<String, String> params){

        Long tabId = Long.parseLong(params.get("tableId"));
        return new Gson().toJson(tabManagement.getFilteredTableData(params));

    }

    @PostMapping("/modifyData")
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public void modifyData(@RequestParam Map<String, String> params){

        Long tabId = Long.parseLong(params.get("tableId"));


        for(List<String> td : tabManagement.getTableData(tabId)){

            Long dataId = Long.parseLong(td.get(0));
            for(TableField tf : tabManagement.getTableFields(tabId)){

                String currentKey = tf.getFieldName();
                //new param gotten from panel.html
                String newParam = params.get(dataId + currentKey);

                //could be null when filtered data is modified
                if((newParam != null && !newParam.equals("")) || (newParam != null  && tf.isNullable()))
                    //newJsonVal.addProperty(finalKey, newParam);
                    tabManagement.modifyJsonData(dataId, "$." + currentKey, newParam);


            }
        }
    }
    @PostMapping("/deleteTable")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public void deleteTable(@RequestParam("tableId") String tableId){

        Long tableIdL = Long.parseLong(tableId);
        tabManagement.deleteTable(tableIdL);

    }

    @PostMapping("/deleteData")
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public void deleteData(@RequestParam Map<String, String> params){
        params
                .entrySet()
                .stream()
                .forEach(entry ->{
                    Long dataId = Long.parseLong(entry.getValue());
                    tabManagement.deleteJsonData(dataId);
                });
    }

    @PostMapping("/addData")
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public void addData(@RequestParam Map<String, String> params){

        Long tabId = Long.parseLong(params.get("tableId"));

        JsonObject newData = new JsonObject();

        for(TableField tf : tabManagement.getTableFields(tabId)){

            String newValue = params.get(tf.getFieldName());
            newData.addProperty(tf.getFieldName(), newValue);

        }
        tabManagement.addJsonData(tabId, newData);
    }

    @PostMapping("/updatePlacement")
    @ResponseStatus(value = HttpStatus.OK)
    public void updateTablePlacement(@RequestParam("tableId") String tableId,
                                     @RequestParam("pageX") String pageX,
                                     @RequestParam ("pageY") String pageY){

        Long tableIdL = Long.parseLong(tableId);

        double pageXF = Double.parseDouble(pageX);
        double pageYF = Double.parseDouble(pageY);
        Integer pageXINT = (int) pageXF;
        Integer pageYINT = (int) pageYF;

        tabManagement.modifyTablePlacement(tableIdL, pageXINT, pageYINT);
    }

    @PostMapping("/addField")
    @ResponseStatus(value = HttpStatus.OK)
    public void addField(@RequestParam("tableId") String tableId,
                         @RequestParam("fieldName") String fieldName,
                         @RequestParam("fieldType") String fieldType,
                         @RequestParam(value = "nullable", required = false) String nullable,
                         @RequestParam(value = "unique", required = false) String unique,
                         @RequestParam("defaultValue") String defaultValue){

        Long tableIdL = Long.parseLong(tableId);

        Boolean nullableVar = nullable != null;
        Boolean uniqueVar = unique != null;

        fieldManagement.addNewField(
                tableIdL,
                fieldName,
                fieldType,
                nullableVar,
                uniqueVar,
                defaultValue);


    }
    @PostMapping("/deleteField")
    @ResponseStatus(HttpStatus.OK)
    public void deleteField(@RequestParam Map<String, String> params){

        Long tableId = Long.parseLong(params.get("tableId"));

        for(TableField tf : tabManagement.getTableFields(tableId)){
            if(params.get(tf.getFieldName()) != null){
                fieldManagement.deleteFieldByName(tf.getFieldName(), tableId);
            }
        }

    }

    @GetMapping("/getForeignKeys")
    @ResponseBody
    public String getForeignKeys(@RequestParam("databaseId") String databaseId){

        Long databaseIdL = Long.parseLong(databaseId);

        List<FieldConstraint> foreignKeys = constraintManagement.getForeignKeysByDatabaseId(databaseIdL);

        List<String> response = new ArrayList<>();

        for(FieldConstraint fc : foreignKeys){
            JsonObject jsonParser = new JsonParser()
                    .parse(fc.getConstraintInfoJson())
                    .getAsJsonObject();

            JsonObject jsonResult = new JsonObject();

            TableField currentField = fieldManagement.getTableFieldById(fc.getFieldId());

            String fieldName = currentField.getFieldName();
            String linkedFieldId = jsonParser.get("linkedFieldId").toString();
            linkedFieldId = linkedFieldId.substring(1, linkedFieldId.length() - 1);

            jsonResult.addProperty("fieldId", fc.getFieldId());
            jsonResult.addProperty("linkedFieldId", linkedFieldId);
            jsonResult.addProperty("fieldName", fieldName);
            response.add(jsonResult.toString());
        }

        return new Gson().toJson(response);
    }

    public String username(){
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
    public Long id(){
        return userManagement.getUserId(username());
    }
}
