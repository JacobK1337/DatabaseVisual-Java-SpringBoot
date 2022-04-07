package pl.base.controllers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import pl.base.services.ConstraintManagement;
import pl.base.entities.FieldConstraint;
import pl.base.entities.DataApi;
import pl.base.services.DataApiManagement;
import pl.base.utils.SessionUtil;
import pl.base.services.FieldManagement;
import pl.base.entities.TableField;
import pl.base.services.TableDataManagement;
import pl.base.entities.DatabaseTable;
import pl.base.services.TableManagement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Controller
public class UserTableManageController {


    private final SessionUtil sessionUtil;
    private final TableDataManagement tableDataManagement;
    private final FieldManagement fieldManagement;
    private final TableManagement tableManagement;
    private final DataApiManagement dataApiManagement;
    private final ConstraintManagement constraintManagement;

    public UserTableManageController(TableDataManagement tableDataManagement,
                                     FieldManagement fieldManagement,
                                     TableManagement tableManagement,
                                     DataApiManagement dataApiManagement,
                                     ConstraintManagement constraintManagement,
                                     SessionUtil sessionUtil){
        this.tableManagement = tableManagement;
        this.fieldManagement = fieldManagement;
        this.tableDataManagement = tableDataManagement;
        this.dataApiManagement = dataApiManagement;
        this.constraintManagement = constraintManagement;
        this.sessionUtil = sessionUtil;
    }


    @GetMapping("/getTableData")
    @ResponseBody
    public String getTableData(@RequestParam("tableId") String tableId) {

        long tableIdLong;

        try {
            tableIdLong = Long.parseLong(tableId);
            return new Gson().toJson(tableDataManagement.getTableData(tableIdLong));

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

            for (TableField tf : fieldManagement.getFieldsByTableId(tableIdLong)) {
                fieldInfo.add(fieldManagement.getTableFieldInfoJson(tf.getFieldId()));
            }

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
                .toJson(tableDataManagement.getFilteredTableData(params));

    }

    @PostMapping("/modifyData")
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public void modifyData(@RequestParam Map<String, String> params) {


        long tableIdLong;

        try {
            tableIdLong = Long.parseLong(params.get("tableId"));
            for (List<String> td : tableDataManagement.getTableData(tableIdLong)) {

                Long dataId = Long.parseLong(td.get(0));
                JsonObject newData = new JsonObject();

                for (var tf : fieldManagement.getFieldsByTableId(tableIdLong)) {

                    String currentKey = tf.getFieldName();
                    String newParam = params.get(dataId + currentKey);


                    if(newParam != null){
                        if (!SessionUtil.validUserInput(newParam)) throw new Exception("Invalid input");
                        newData.addProperty(currentKey, newParam);
                        tableDataManagement.modifyJsonData(dataId, tableIdLong, currentKey, newParam);
                    }

                }

            }
        } catch (Exception e) {
            System.out.println(e.getMessage() + ": only numbers and letters");
        }


    }

    @PostMapping("/updateConstraints")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public void updateConstraints(@RequestParam Map<String, String> params) {


        long tableIdLong;

        try {
            tableIdLong = Long.parseLong(params.get("tableId"));

            DatabaseTable currentTable = tableManagement.getTable(tableIdLong);
            Long databaseId = currentTable.getDatabaseId();

            for (TableField tf : fieldManagement.getFieldsByTableId(tableIdLong)) {

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

                    if(tableDataManagement.foreignKeyValuesMatchPrimaryKey(tf, primaryKeyField))
                        fieldManagement.setAsForeignKey(tf, primaryKeyField, databaseId, onDeleteAction);

                    else throw new IllegalStateException("Foreign key values don't match primary key values!");


                } else if (params.get("isForeignKey" + tableFieldId).equals("None")) {
                    fieldManagement.setAsNotForeignKey(tableFieldId);
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
                    .forEach(entry -> {
                        Long dataId = Long.parseLong(entry.getValue());
                        tableDataManagement.deleteJsonData(
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

        try {
            params
                    .entrySet()
                    .stream()
                    .filter(entry -> entry.getKey().startsWith("savedDataId-"))
                    .forEach(entry -> {
                        Long savedDataId = Long.parseLong(entry.getValue());
                        dataApiManagement.deleteSavedData(savedDataId);
                    });

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
            JsonObject newData = new JsonObject();

            for (TableField tf : fieldManagement.getFieldsByTableId(tableIdLong)) {


                String newValue = params.get(tf.getFieldName());

                if (!SessionUtil.validUserInput(newValue)) throw new Exception("Invalid input");

                newData.addProperty(tf.getFieldName(), newValue);

            }
            tableDataManagement.addJsonData(tableIdLong, newData);

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
            Boolean notNullVar = notNull != null;
            Boolean uniqueVar = unique != null;

            if (!SessionUtil.validUserInput(fieldName)
                    || fieldName.equals("")) throw new Exception("Invalid input");

            fieldManagement.addNewField(
                    tableIdLong,
                    databaseIdLong,
                    fieldName,
                    fieldType,
                    notNullVar,
                    uniqueVar,
                    defaultValue,
                    false);

            tableDataManagement.insertAddedFieldToAll(tableIdLong, fieldName, defaultValue);

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
            for (TableField tf : fieldManagement.getFieldsByTableId(tableIdLong)) {
                if (params.get(tf.getFieldName()) != null) {
                    fieldManagement.deleteFieldByName(
                            tf.getFieldName(),
                            tableIdLong);

                    tableDataManagement.eraseFieldFromAll(tf.getFieldName(), tableIdLong);

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

                String tableName = tableManagement
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

        Long tableIdLong;
        try {

            tableIdLong = Long.parseLong(tableId);
            Long primaryKeyFieldId = fieldManagement.getPrimaryKeyFieldId(tableIdLong);

            String primaryKeyName = fieldManagement
                    .getTableFieldById(primaryKeyFieldId)
                    .getFieldName();

            dataApiManagement.saveNewData(
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
            tableDataManagement.importDataByDataApi(
                    tableIdLong,
                    dataTokenLong,
                    databaseIdLong);

        } catch (NumberFormatException nfe) {
            System.out.println(nfe.getMessage());
        }

    }

}
