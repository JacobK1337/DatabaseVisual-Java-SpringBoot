package pl.base.controllers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import pl.base.services.ConstraintService;
import pl.base.entities.DataApi;
import pl.base.services.DataApiService;
import pl.base.utils.SessionUtil;
import pl.base.services.TableFieldService;
import pl.base.entities.TableField;
import pl.base.services.TableDataService;
import pl.base.services.UserTableService;
import java.util.*;
import java.util.stream.Collectors;


@Controller
public class UserTableManageController {


    private final SessionUtil sessionUtil;
    private final TableDataService tableDataService;
    private final TableFieldService tableFieldService;
    private final UserTableService userTableService;
    private final DataApiService dataApiService;
    private final ConstraintService constraintService;

    public UserTableManageController(TableDataService tableDataService,
                                     TableFieldService tableFieldService,
                                     UserTableService userTableService,
                                     DataApiService dataApiService,
                                     ConstraintService constraintService,
                                     SessionUtil sessionUtil){
        this.userTableService = userTableService;
        this.tableFieldService = tableFieldService;
        this.tableDataService = tableDataService;
        this.dataApiService = dataApiService;
        this.constraintService = constraintService;
        this.sessionUtil = sessionUtil;
    }


    @GetMapping("/getTableData")
    @ResponseBody
    public String getTableData(@RequestParam("tableId") String tableId) {

        long tableIdLong;

        try {
            tableIdLong = Long.parseLong(tableId);
            return new Gson().toJson(tableDataService.getTableData(tableIdLong));

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

            var tableFields = tableFieldService.getFieldsByTableId(tableIdLong);

            var fieldInfo =
                    tableFields.stream()
                    .map(TableField::getFieldId)
                    .map(tableFieldService::getTableFieldInfoJson)
                    .collect(Collectors.toList());

            return new Gson().toJson(fieldInfo);

        } catch (NumberFormatException nfe) {
            nfe.printStackTrace();
            return new Gson().toJson(new ArrayList<>());
        }

    }

    @GetMapping("/getFilteredTableData")
    @ResponseBody
    public String getFilteredTableData(@RequestParam Map<String, String> params){

        return new Gson()
                .toJson(tableDataService.getFilteredTableData(params));

    }

    @PostMapping("/modifyData")
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public void modifyData(@RequestParam Map<String, String> params) {


        long tableIdLong;

        try {
            tableIdLong = Long.parseLong(params.get("tableId"));

            var tableData = tableDataService.getTableData(tableIdLong);
            var tableFields = tableFieldService.getFieldsByTableId(tableIdLong);

            tableData.stream()
                    .map(content -> Long.parseLong(content.get(0)))
                    .forEach(dataId -> tableFields.stream()
                            .map(TableField::getFieldName)
                            .filter(fieldName -> params.get(dataId + fieldName) != null && SessionUtil.validUserInput(params.get(dataId + fieldName)))
                            .forEach(fieldName -> tableDataService.modifyJsonData(dataId, tableIdLong, fieldName, params.get(dataId + fieldName))));


        } catch (Exception e) {
            System.out.println(e.getMessage());
        }


    }

    @PostMapping("/updateConstraints")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public void updateConstraints(@RequestParam Map<String, String> params) {


        long tableIdLong;

        try {
            tableIdLong = Long.parseLong(params.get("tableId"));

            var currentTable = userTableService.getTable(tableIdLong);
            var databaseId = currentTable.getDatabaseId();
            var tableFields = tableFieldService.getFieldsByTableId(tableIdLong);


            for (var tf : tableFields) {

                var tableFieldId = tf.getFieldId();

                if (params.get("isNotNull" + tableFieldId) != null && !tf.isNotNull())
                    tableFieldService.setAsNotNull(tableFieldId, databaseId);

                else if (params.get("isNotNull" + tableFieldId) == null && tf.isNotNull() && !tf.isPrimaryKey())
                    tableFieldService.setAsNullable(tableFieldId);

                if (params.get("isUnique" + tableFieldId) != null && !tf.isUnique())
                    tableFieldService.setAsUnique(tableFieldId, databaseId);

                else if (params.get("isUnique" + tableFieldId) == null && tf.isUnique() && !tf.isPrimaryKey())
                    tableFieldService.setAsNotUnique(tableFieldId);

                if (!params.get("isForeignKey" + tableFieldId).equals("None")) {

                    var referencingFieldId = params.get("isForeignKey" + tableFieldId);
                    var onDeleteAction = params.get("onDelete" + tableFieldId);
                    var referencingFieldIdLong = Long.parseLong(referencingFieldId);

                    var primaryKeyField = tableFieldService.getTableFieldById(referencingFieldIdLong);

                    if(tableDataService.foreignKeyValuesMatchPrimaryKey(tf, primaryKeyField))
                        tableFieldService.setAsForeignKey(tf, primaryKeyField, databaseId, onDeleteAction);

                    else
                        throw new RuntimeException("Foreign key values don't match primary key values!");


                } else if (params.get("isForeignKey" + tableFieldId).equals("None")) {
                    tableFieldService.setAsNotForeignKey(tableFieldId);
                }

            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
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
                    .map(entry -> Long.parseLong(entry.getValue()))
                    .forEach(dataId -> tableDataService.deleteJsonData(tableIdLong, databaseIdLong, dataId));

        } catch (NumberFormatException nfe) {
            nfe.printStackTrace();
        }

    }

    @PostMapping("/deleteSavedData")
    @ResponseStatus(HttpStatus.OK)
    public void deleteSavedData(@RequestParam Map<String, String> params) {

        try {

            params
                    .entrySet()
                    .stream()
                    .filter(entry -> entry.getKey().startsWith("savedDataId-"))
                    .map(entry -> Long.parseLong(entry.getValue()))
                    .forEach(dataApiService::deleteSavedData);

        } catch (NumberFormatException nfe) {
            nfe.printStackTrace();
        }

    }

    @PostMapping("/addData")
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public void addData(@RequestParam Map<String, String> params) {

        long tableIdLong;

        try {
            tableIdLong = Long.parseLong(params.get("tableId"));
            var tableFields = tableFieldService.getFieldsByTableId(tableIdLong);

            var newData = new JsonObject();

            tableFields.stream()
                    .map(TableField::getFieldName)
                    .filter(fieldName -> SessionUtil.validUserInput(params.get(fieldName)))
                    .forEach(fieldName -> newData.addProperty(fieldName, params.get(fieldName)));


            tableDataService.addJsonData(tableIdLong, newData);

        } catch (Exception e) {
            System.out.println(e.getMessage() + ": only numbers and letters");
        }

    }

    @PostMapping("/addField")
    @ResponseStatus(value = HttpStatus.OK)
    public void addField(@RequestParam("tableId") String tableId,
                         @RequestParam("databaseId") String databaseId,
                         @RequestParam("fieldName") String fieldName,
                         @RequestParam("fieldType") String fieldType,
                         @RequestParam(value = "notNull", required = false) String notNull,
                         @RequestParam(value = "unique", required = false) String unique,
                         @RequestParam("defaultValue") String defaultValue) {

        long tableIdLong;
        long databaseIdLong;
        try {
            tableIdLong = Long.parseLong(tableId);
            databaseIdLong = Long.parseLong(databaseId);
            var notNullVar = notNull != null;
            var uniqueVar = unique != null;

            if (!SessionUtil.validUserInput(fieldName)
                    || fieldName.equals("")) throw new Exception("Invalid input");

            tableFieldService.addNewField(
                    tableIdLong,
                    databaseIdLong,
                    fieldName,
                    fieldType,
                    notNullVar,
                    uniqueVar,
                    defaultValue,
                    false);

            tableDataService.insertAddedFieldToAll(tableIdLong, fieldName, defaultValue);

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }


    }

    @PostMapping("/deleteField")
    @ResponseStatus(HttpStatus.OK)
    public void deleteField(@RequestParam Map<String, String> params) {

        long tableIdLong;
        try {
            tableIdLong = Long.parseLong(params.get("tableId"));

            var tableFields = tableFieldService.getFieldsByTableId(tableIdLong);
            tableFields.stream()
                    .map(TableField::getFieldName)
                    .filter(fieldName -> params.get(fieldName) != null)
                    .forEach(fieldName -> {
                        tableFieldService.deleteFieldByName(fieldName, tableIdLong);
                        tableDataService.eraseFieldFromAll(fieldName, tableIdLong);
                    });

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
            var primaryKeys = constraintService.getPrimaryKeysByDatabaseId(databaseIdLong);

            var response =
                primaryKeys.stream()
                        .map(primaryKey -> tableFieldService.getTableFieldById(primaryKey.getFieldId()))
                        .map(tableField ->{
                           var tableName = userTableService
                                   .getTableDetails(tableField.getTableId())
                                   .getTableName();

                           var tableId = tableField.getTableId().toString();
                           var jsonResult = new JsonObject();

                           jsonResult.addProperty("fieldId", tableField.getFieldId());
                           jsonResult.addProperty("fieldName", tableField.getFieldName());
                           jsonResult.addProperty("tableName", tableName);
                           jsonResult.addProperty("tableId", tableId);

                           return jsonResult.toString();
                        })
                        .collect(Collectors.toList());


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
            var foreignKeys = constraintService.getForeignKeysByDatabaseId(databaseIdLong);


            var response =
                foreignKeys.stream()
                        .map(foreignKey -> Arrays.asList(
                                foreignKey.getFieldId().toString(),
                                tableFieldService.getTableFieldById(foreignKey.getFieldId()).getFieldName(),
                                foreignKey.getConstraintInfoJson()

                        ))
                        .collect(Collectors.toList());


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
            var data = dataApiService
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
            var userData = dataApiService.getDataApiByTableId(tableIdLong);

            var dataIds =
                userData.stream()
                        .map(DataApi::getDataApiId)
                        .collect(Collectors.toList());


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

        Long tableIdLong;
        try {

            tableIdLong = Long.parseLong(tableId);
            Long primaryKeyFieldId = tableFieldService.getPrimaryKeyFieldId(tableIdLong);

            String primaryKeyName = tableFieldService
                    .getTableFieldById(primaryKeyFieldId)
                    .getFieldName();

            dataApiService.saveNewData(
                    jsonData,
                    sessionUtil.id(),
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
            tableDataService.importDataByDataApi(
                    tableIdLong,
                    dataTokenLong,
                    databaseIdLong);

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }

}
