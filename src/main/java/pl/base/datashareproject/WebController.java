package pl.base.datashareproject;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
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
import pl.base.dataApi.DataApi;
import pl.base.dataApi.DataApiManagement;
import pl.base.databases.DatabaseManagement;
import pl.base.fields.FieldManagement;
import pl.base.fields.TableField;
import pl.base.tables.DatabaseTable;
import pl.base.tables.TableManagement;
import pl.base.user.UserManagement;

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

    @Autowired
    private DataApiManagement dataApiManagement;

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
            userManagement.createNewAccount(username, password);
            return "redirect:/login";
        } catch (Exception e) {
            return "redirect:/register";
        }

    }

    @GetMapping("/databases")
    public String databases(Model model) {
        model.addAttribute("userDatabases", dbManagement.getUserDatabases(id()));

        return "databases";
    }

    @PostMapping("/createDatabase")
    public String createDatabase(@RequestParam("databaseName") String databaseName) {
        dbManagement.createNewDatabase(id(), databaseName);

        return "redirect:/databases";
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
            tabManagement.createNewTable(
                    databaseIdLong,
                    tableName,
                    primaryKeyName,
                    primaryKeyType);

        } catch (NumberFormatException nfe) {
            nfe.printStackTrace();
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

    @GetMapping("/getDatabaseTableDetails")
    @ResponseBody
    public String getDatabaseTableDetails(@RequestParam("databaseId") String databaseId) {

        long databaseIdLong;
        try {
            databaseIdLong = Long.parseLong(databaseId);

            List<String> allTablesDetails = new ArrayList<>();

            for (DatabaseTable dt : tabManagement.getDatabaseTables(databaseIdLong)) {
                Long tableId = dt.getTableId();
                allTablesDetails.add(tabManagement.getTableDetailsJson(tableId));
            }
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
            return tabManagement.getTableDetailsJson(tableIdLong);

        } catch (NumberFormatException nfe) {
            nfe.printStackTrace();
            return new Gson().toJson(new ArrayList<>());
        }

    }

    @GetMapping("/getTableFields")
    @ResponseBody
    public String getTableFields(@RequestParam("tableId") String tableId) {


        long tableIdLong;
        try {
            tableIdLong = Long.parseLong(tableId);
            List<String> fieldInfo = new ArrayList<>();

            for (TableField tf : tabManagement.getTableFields(tableIdLong)) {
                fieldInfo.add(fieldManagement.getTableFieldInfoJson(tf.getFieldId()));
            }

            return new Gson().toJson(fieldInfo);

        } catch (NumberFormatException nfe) {
            nfe.printStackTrace();
            return new Gson().toJson(new ArrayList<>());
        }

    }

    @GetMapping("/getTableData")
    @ResponseBody
    public String getTableData(@RequestParam("tableId") String tableId) {

        long tableIdLong;
        try {
            tableIdLong = Long.parseLong(tableId);
            return new Gson().toJson(tabManagement.getTableData(tableIdLong));

        } catch (NumberFormatException nfe) {
            nfe.printStackTrace();
            return new Gson().toJson(new ArrayList<>());
        }

    }

    @GetMapping("/getFilteredTableData")
    @ResponseBody
    public String getFilteredTableData(@RequestParam Map<String, String> params) {

        return new Gson()
                .toJson(tabManagement.getFilteredTableData(params));

    }

    @PostMapping("/modifyData")
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public void modifyData(@RequestParam Map<String, String> params) {


        long tableIdLong;
        try {
            tableIdLong = Long.parseLong(params.get("tableId"));
            for (List<String> td : tabManagement.getTableData(tableIdLong)) {

                Long dataId = Long.parseLong(td.get(0));
                JsonObject newData = new JsonObject();

                for (TableField tf : tabManagement.getTableFields(tableIdLong)) {

                    String currentKey = tf.getFieldName();
                    String newParam = params.get(dataId + currentKey);
                    if (newParam != null) {

                        newData.addProperty(currentKey, newParam);

                        tabManagement.modifyJsonData(dataId, tableIdLong, currentKey, newParam);
                    }

                }

            }
        } catch (NumberFormatException nfe) {
            nfe.printStackTrace();
        }


    }

    @PostMapping("/updateConstraints")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public void updateConstraints(@RequestParam Map<String, String> params) {


        long tableIdLong;
        try {
            tableIdLong = Long.parseLong(params.get("tableId"));

            DatabaseTable currentTable = tabManagement.getTable(tableIdLong);
            Long databaseId = currentTable.getDatabaseId();

            for (TableField tf : tabManagement.getTableFields(tableIdLong)) {

                Long tableFieldId = tf.getFieldId();

                if (params.get("isNotNull" + tableFieldId) != null && !tf.isNotNull())
                    fieldManagement.setAsNotNull(tableFieldId, databaseId);

                else if (params.get("isNotNull" + tableFieldId) == null && tf.isNotNull() && !tf.isPrimaryKey())
                    fieldManagement.setAsNullable(tableFieldId);

                if (params.get("isUnique" + tableFieldId) != null && !tf.isUnique())
                    fieldManagement.setAsUnique(tableFieldId, databaseId);

                else if (params.get("isUnique" + tableFieldId) == null && tf.isUnique() && !tf.isPrimaryKey())
                    fieldManagement.setAsNotUnique(tableFieldId);


                if (!params.get("isForeignKey" + tableFieldId).equals("None")) {

                    String referencingFieldId = params.get("isForeignKey" + tableFieldId);
                    String onDeleteAction = params.get("onDelete" + tableFieldId);
                    Long referencingFieldIdLong = Long.parseLong(referencingFieldId);

                    TableField primaryKeyField = fieldManagement.getTableFieldById(referencingFieldIdLong);

                    fieldManagement.setAsForeignKey(tf, primaryKeyField, databaseId, onDeleteAction);


                } else if (params.get("isForeignKey" + tableFieldId).equals("None")) {
                    fieldManagement.setAsNotForeignKey(tableFieldId);
                }

            }

        } catch (NumberFormatException nfe) {
            nfe.printStackTrace();
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
            tabManagement.deleteTable(tableIdLong, databaseIdLong);

        } catch (NumberFormatException nfe) {
            nfe.printStackTrace();
        }

    }

    @PostMapping("/deleteData")
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public void deleteData(@RequestParam Map<String, String> params) {

        long databaseIdLong;
        long tableIdLong;

        try {
            databaseIdLong = Long.parseLong(params.get("databaseId"));
            tableIdLong = Long.parseLong(params.get("tableId"));
            params
                    .entrySet()
                    .stream()
                    .filter(entry -> entry.getKey().startsWith("dataId-"))
                    .forEach(entry -> {
                        long dataId = Long.parseLong(entry.getValue());
                        tabManagement.deleteJsonData(
                                tableIdLong,
                                databaseIdLong,
                                dataId);
                    });

        } catch (NumberFormatException nfe) {
            nfe.printStackTrace();
        }

    }

    @PostMapping("/deleteSavedData")
    @ResponseStatus(HttpStatus.OK)
    public void deleteSavedData(@RequestParam Map<String, String> params) {

        params
                .entrySet()
                .stream()
                .filter(entry -> entry.getKey().startsWith("savedDataId-"))
                .forEach(entry -> {
                    long savedDataId = Long.parseLong(entry.getValue());
                    dataApiManagement.deleteSavedData(savedDataId);
                });

    }

    @PostMapping("/addData")
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public void addData(@RequestParam Map<String, String> params) {

        long tableIdLong;
        try {
            tableIdLong = Long.parseLong(params.get("tableId"));
            JsonObject newData = new JsonObject();

            for (TableField tf : tabManagement.getTableFields(tableIdLong)) {

                String newValue = params.get(tf.getFieldName());
                newData.addProperty(tf.getFieldName(), newValue);

            }
            tabManagement.addJsonData(tableIdLong, newData);

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

            tabManagement.modifyTablePlacement(
                    tableIdLong,
                    pageXINT,
                    pageYINT);

        } catch (NumberFormatException nfe) {
            nfe.printStackTrace();
        }

    }

    @PostMapping("/addField")
    @ResponseStatus(value = HttpStatus.OK)
    public void addField(@RequestParam("tableId") String tableId,
                         @RequestParam("fieldName") String fieldName,
                         @RequestParam("fieldType") String fieldType,
                         @RequestParam(value = "notNull", required = false) String notNull,
                         @RequestParam(value = "unique", required = false) String unique,
                         @RequestParam("defaultValue") String defaultValue) {

        long tableIdLong;
        try {
            tableIdLong = Long.parseLong(tableId);
            Boolean notNullVar = notNull != null;
            Boolean uniqueVar = unique != null;

            fieldManagement.addNewField(
                    tableIdLong,
                    fieldName,
                    fieldType,
                    notNullVar,
                    uniqueVar,
                    defaultValue,
                    false);

        } catch (NumberFormatException nfe) {
            nfe.printStackTrace();
        }


    }

    @PostMapping("/deleteField")
    @ResponseStatus(HttpStatus.OK)
    public void deleteField(@RequestParam Map<String, String> params) {

        long tableIdLong;
        try {
            tableIdLong = Long.parseLong(params.get("tableId"));
            for (TableField tf : tabManagement.getTableFields(tableIdLong)) {
                if (params.get(tf.getFieldName()) != null) {
                    fieldManagement.deleteFieldByName(
                            tf.getFieldName(),
                            tableIdLong);

                }
            }
        } catch (NumberFormatException nfe) {
            nfe.printStackTrace();
        }

    }

    @GetMapping("/getPrimaryKeys")
    @ResponseBody
    public String getPrimaryKeys(@RequestParam("databaseId") String databaseId) {

        long databaseIdLong;
        try {
            databaseIdLong = Long.parseLong(databaseId);
            List<FieldConstraint> primaryKeys = constraintManagement.getPrimaryKeysByDatabaseId(databaseIdLong);

            List<String> response = new ArrayList<>();

            for (FieldConstraint fc : primaryKeys) {

                TableField currentTableField = fieldManagement.getTableFieldById(fc.getFieldId());
                String primaryKeyName = currentTableField.getFieldName();

                String tableName = tabManagement
                        .getTableDetails(currentTableField.getTableId())
                        .getTableName();

                String tableId = currentTableField.getTableId().toString();

                JsonObject jsonResult = new JsonObject();

                jsonResult.addProperty("fieldId", fc.getFieldId());
                jsonResult.addProperty("fieldName", primaryKeyName);
                jsonResult.addProperty("tableName", tableName);
                jsonResult.addProperty("tableId", tableId);

                response.add(jsonResult.toString());
            }

            return new Gson().toJson(response);

        } catch (NumberFormatException nfe) {
            nfe.printStackTrace();
            return new Gson().toJson(new ArrayList<>());
        }

    }

    @GetMapping("/getForeignKeys")
    @ResponseBody
    public String getForeignKeys(@RequestParam("databaseId") String databaseId) {

        long databaseIdLong;
        try {
            databaseIdLong = Long.parseLong(databaseId);
            List<FieldConstraint> foreignKeys = constraintManagement.getForeignKeysByDatabaseId(databaseIdLong);

            List<List<String>> response = new ArrayList<>();

            for (FieldConstraint fc : foreignKeys) {
                response.add(Arrays.asList(
                        fc.getFieldId().toString(),
                        fieldManagement
                                .getTableFieldById(fc.getFieldId())
                                .getFieldName(),

                        fc.getConstraintInfoJson()
                ));
            }
            return new Gson().toJson(response);

        } catch (NumberFormatException nfe) {
            nfe.printStackTrace();
            return new Gson().toJson(new ArrayList<>());
        }

    }


    @GetMapping("/getSharedData")
    @ResponseBody
    public String getJsonData(@RequestParam("dataApiId") String dataApiId) {

        long dataApiIdLong;
        try {
            dataApiIdLong = Long.parseLong(dataApiId);
            String data = dataApiManagement
                    .getDataApiByDataId(dataApiIdLong)
                    .getDataApiJson();

            data = data.replaceAll(", ", ",<br><br>");

            return data;

        } catch (NumberFormatException nfe) {
            return "[]";
        }

    }


    @GetMapping("/getTableSavedData")
    @ResponseBody
    public String getUserSavedData(@RequestParam("tableId") String tableId) {

        long tableIdLong;
        try {
            tableIdLong = Long.parseLong(tableId);
            List<DataApi> userData = dataApiManagement.getDataApiByTableId(tableIdLong);

            List<Long> dataIds = new ArrayList<>();
            for (DataApi data : userData) {
                dataIds.add(data.getDataApiId());
            }

            return new Gson().toJson(dataIds);

        } catch (NumberFormatException nfe) {
            nfe.printStackTrace();
            return new Gson().toJson(new ArrayList<>());
        }

    }

    @PostMapping("/saveJsonData")
    @ResponseStatus(HttpStatus.OK)
    public void generateJsonData(@RequestParam("jsonData") String jsonData,
                                 @RequestParam("tableId") String tableId) {

        long tableIdLong;
        try {

            tableIdLong = Long.parseLong(tableId);
            Long primaryKeyFieldId = tabManagement.getPrimaryKeyFieldId(tableIdLong);

            String primaryKeyName = fieldManagement
                    .getTableFieldById(primaryKeyFieldId)
                    .getFieldName();

            dataApiManagement.saveNewData(
                    jsonData,
                    id(),
                    tableIdLong,
                    primaryKeyName);

        } catch (NumberFormatException nfe) {
            nfe.printStackTrace();
        }

    }

    @PostMapping("/importData")
    @ResponseStatus(HttpStatus.OK)
    public void importJsonData(@RequestParam("dataToken") String dataToken,
                               @RequestParam("tableId") String tableId,
                               @RequestParam("databaseId") String databaseId) {

        long dataTokenLong;
        long tableIdLong;
        long databaseIdLong;
        try {
            dataTokenLong = Long.parseLong(dataToken);

            tableIdLong = Long.parseLong(tableId);
            databaseIdLong = Long.parseLong(databaseId);
            tabManagement.importDataByDataApi(
                    tableIdLong,
                    dataTokenLong,
                    databaseIdLong);

        } catch (NumberFormatException nfe) {
            System.out.println(nfe.getMessage());
        }

    }

    public String username() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    public Long id() {
        return userManagement.getUserId(username());
    }
}
