package pl.base.tables;


import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pl.base.constraints.ConstraintManagement;
import pl.base.constraints.FieldConstraint;
import pl.base.dataApi.DataApi;
import pl.base.dataApi.DataApiManagement;
import pl.base.fields.FieldManagement;
import pl.base.fields.FieldRepo;
import pl.base.fields.TableField;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

@Component
public class TableManagement {

    @Autowired
    private TableRepo tableRepo;

    @Autowired
    private FieldRepo fieldRepo;

    @Autowired
    private TableDataRepo tableDataRepo;

    @Autowired
    private TableDetailsRepo tableDetailsRepo;

    @Autowired
    private TableDataFilteredImpl tableDataFiltered;

    @Autowired
    private ConstraintManagement constraintManagement;

    @Autowired
    private FieldManagement fieldManagement;

    @Autowired
    DataApiManagement dataApiManagement;

    public List<DatabaseTable> getDatabaseTables(Long databaseId) {
        return tableRepo.findByDatabaseId(databaseId);
    }

    public DatabaseTable getTable(Long tableId) {
        return tableRepo.findByTableId(tableId);
    }


    public TableDetails getTableDetails(Long tableId) {
        return tableDetailsRepo.findByTableId(tableId);
    }

    public String getTableDetailsJson(Long tableId){
        TableDetails currentTable = getTableDetails(tableId);
        JsonObject jsonObject = new JsonObject();

        String tableName = currentTable.getTableName();
        String pageX = String.valueOf(currentTable.getPageX());
        String pageY = String.valueOf(currentTable.getPageY());
        String color = currentTable.getColor();

        jsonObject.addProperty("table_id", tableId);
        jsonObject.addProperty("table_name", tableName);
        jsonObject.addProperty("page_x", pageX);
        jsonObject.addProperty("page_y", pageY);
        jsonObject.addProperty("color", color);
        return jsonObject.toString();
    }
    public void createNewTable(Long databaseId,
                               String tableName,
                               String primaryKeyName,
                               String primaryKeyType) {
        DatabaseTable newTable = new DatabaseTable();

        newTable.setDatabaseId(databaseId);
        tableRepo.save(newTable);


        Long newTableId = newTable.getTableId();

        TableDetails newTableDetails = new TableDetails(
                newTableId,
                tableName,
                1000,
                1000,
                "dimgrey"
        );

        tableDetailsRepo.save(newTableDetails);

        fieldManagement.addNewField(
                newTableId,
                primaryKeyName,
                primaryKeyType,
                true,
                true,
                "",
                true);

    }

    public List<List<String>> getTableData(Long tableId) {

        List<List<String>> result = new ArrayList<>();
        for (TableData td : tableDataRepo.findByTableId(tableId)) {
            result.add(Arrays.asList(
                    Long.toString(td.getDataId()),
                    td.getFieldJsonValue()
            ));
        }

        return result;
    }

    public List<List<String>> getFilteredTableData(Map<String, String> params) {

        List<List<String>> result = new ArrayList<>();
        for (TableData td : tableDataFiltered.findFilteredTableData(params)) {
            result.add(Arrays.asList(
                    String.valueOf(td.getDataId()),
                    td.getFieldJsonValue()
            ));
        }
        return result;
    }

    public void deleteTable(Long tableId, Long databaseId) {
        Long tablePrimaryKey = getPrimaryKeyFieldId(tableId);

        if (!isReferencedByForeignKey(tablePrimaryKey, databaseId))
            tableRepo.deleteDatabaseTableByTableId(tableId);
    }

    public void modifyJsonData(Long dataId,
                               Long tableId,
                               String key,
                               String newValue) {

        if (modifiedDataErrors(tableId, key, newValue).size() == 0)
            tableDataRepo.updateJsonValueByDataId(dataId, "$." + key, newValue);

        else
            for (String error : modifiedDataErrors(tableId, key, newValue))
                System.out.println(error);
    }

    public void deleteJsonData(Long tableId, Long databaseId, Long dataId) {

        String data = tableDataRepo.findByDataId(dataId).getFieldJsonValue();
        JsonObject dataJson = new JsonParser()
                .parse(data)
                .getAsJsonObject();

        fieldRepo.findByTableId(tableId)
                .stream()
                .filter(TableField::isPrimaryKey)
                .forEach(tableField -> {
                    long primaryKeyId = tableField.getFieldId();
                    String primaryKeyName = tableField.getFieldName();

                    String primaryKeyData = dataJson.get(primaryKeyName).toString();
                    primaryKeyData = primaryKeyData.substring(1, primaryKeyData.length() - 1);

                    if (isReferencedByForeignKey(primaryKeyId, databaseId))
                        fixForeignKeyValues(primaryKeyId, primaryKeyData, dataId);

                    else
                        commitDelete(dataId);

                });

        //tableDataRepo.deleteTableDataByDataId(dataId);
    }

    public void importDataByDataApi(Long tableId,
                                    Long dataApiId){

        fieldRepo.deleteTableFieldsByTableId(tableId);
        tableDataRepo.deleteTableDatasByTableId(tableId);

        DataApi importedData = dataApiManagement.getDataApiByDataId(dataApiId);
        String primaryKeyName = importedData.getPrimaryKeyName();

        String data = importedData.getDataApiJson();

        JsonArray dataJson = new JsonParser()
                .parse(data).getAsJsonArray();

        List<String> tableFields = dataJson.
                get(0)
                .getAsJsonObject()
                .keySet()
                .stream().toList();

        for(String key : tableFields){

            String fieldType = autoFieldType(key, dataJson);
            boolean isPrimaryKey = false;
            boolean unique = false;
            boolean notNull = false;

            if(key.equals(primaryKeyName)){
                isPrimaryKey = true;
                unique = true;
                notNull = true;
            }

            fieldManagement.addNewField(
                    tableId,
                    key,
                    fieldType,
                    notNull,
                    unique,
                    "",
                    isPrimaryKey);
        }

        for(int i = 0; i < dataJson.size(); i++){
            JsonObject jsonValue = dataJson.get(i).getAsJsonObject();
            String jsonData = jsonValue.toString();

            TableData newData = new TableData(
                    0L,
                    tableId,
                    jsonData
            );

            tableDataRepo.save(newData);
        }
    }

    ///TO_FIX
    private String autoFieldType(String fieldName,
                                     JsonArray data){

        boolean isInteger = IntStream
                .range(0, data.size())
                .mapToObj(data::get)
                .filter(i -> i
                        .getAsJsonObject()
                        .toString()
                        .equals(fieldName))
                .allMatch(i -> {

                   JsonObject currentData = i
                           .getAsJsonObject();

                   String currentDataValue = currentData
                           .get(fieldName)
                           .toString();

                   currentDataValue = currentDataValue.substring(1, currentDataValue.length() - 1);

                   return isInteger(currentDataValue);

                });

        if(isInteger)
            return "int";

        else
            return "varchar";

    }

    private void commitDelete(Long dataId) {
        tableDataRepo.deleteTableDataByDataId(dataId);
    }

    private void fixForeignKeyValues(Long primaryKeyId, String primaryKeyValue, Long dataToDeleteId) {

        constraintManagement.getForeignKeysByPrimaryKeyId(primaryKeyId)
                .stream()
                .forEach(fieldConstraint -> {
                    Long currentFieldId = fieldConstraint.getFieldId();

                    TableField currentField = fieldRepo.findByFieldId(currentFieldId);

                    Long currentFieldTableId = currentField.getTableId();

                    String data = fieldConstraint.getConstraintInfoJson();
                    JsonObject jsonData = new JsonParser()
                            .parse(data)
                            .getAsJsonObject();

                    String onDeleteAction = jsonData.get("ondelete").toString();
                    onDeleteAction = onDeleteAction.substring(1, onDeleteAction.length() - 1);

                    switch (onDeleteAction) {
                        case "cascade":
                            cascadeDelete(currentFieldTableId, currentFieldId, primaryKeyValue);
                            commitDelete(dataToDeleteId);
                            break;

                        case "setnull":
                            if (!currentField.isNotNull()) {
                                setForeignKeysAsNull(currentFieldTableId, currentFieldId, primaryKeyValue);
                                commitDelete(dataToDeleteId);
                            } else
                                System.out.println("Cannot set as null - referenced field cant be null! -> change on delete action to Cascade");

                            break;
                    }
                });

    }


    private void setForeignKeysAsNull(Long tableId, Long fieldId, String valueToFind) {


        String fieldName = fieldManagement.
                getTableFieldById(fieldId).
                getFieldName();

        tableDataRepo.
                findByTableId(tableId)
                .stream()
                .filter(tableData -> matchesPrimaryKeyValue(tableData, fieldName, valueToFind))
                .forEach(tableData -> {

                    Long dataId = tableData.getDataId();
                    tableDataRepo.updateJsonValueByDataId(dataId, "$." + fieldName, "null");

                });

    }

    private void cascadeDelete(Long tableId, Long fieldId, String valueToFind) {
        String fieldName = fieldManagement.
                getTableFieldById(fieldId).
                getFieldName();

        tableDataRepo.
                findByTableId(tableId)
                .stream()
                .filter(tableData -> matchesPrimaryKeyValue(tableData, fieldName, valueToFind))
                .forEach(tableData -> {

                    Long dataId = tableData.getDataId();
                    tableDataRepo.deleteTableDataByDataId(dataId);

                });
    }

    private boolean matchesPrimaryKeyValue(TableData tableData, String keyToCheck, String primaryKeyValue) {

        String data = tableData.getFieldJsonValue();
        JsonObject dataJson = new JsonParser()
                .parse(data)
                .getAsJsonObject();

        String fieldValueInDataJson = dataJson
                .get(keyToCheck)
                .toString();

        fieldValueInDataJson = fieldValueInDataJson.substring(1, fieldValueInDataJson.length() - 1);

        return fieldValueInDataJson
                .equals(primaryKeyValue);

    }

    public void addJsonData(Long tableId, JsonObject newValue) {


        if (newDataErrors(tableId, newValue).size() == 0) {

            String newValueStr = newValue.toString();

            TableData newTableData = new TableData(
                    0L,
                    tableId,
                    newValueStr

            );

            tableDataRepo.save(newTableData);
        } else
            for (String error : newDataErrors(tableId, newValue))
                System.out.println(error);

    }

    public void updateTableColor(Long tableId, String color) {
        tableDetailsRepo.setNewTableColor(tableId, color);
    }

    public void updateTableName(Long tableId, String newName) {
        Long databaseId = tableRepo.
                findByTableId(tableId).
                getDatabaseId();

        if (isUniqueTableName(databaseId, newName) && !newName.equals(""))
            tableDetailsRepo.setNewTableName(tableId, newName);

    }

    public void modifyTablePlacement(Long tableId,
                                     Integer pageX,
                                     Integer pageY) {

        tableDetailsRepo.setNewTablePlacement(tableId, pageX, pageY);
    }

    public List<String> newDataErrors(Long tableId, JsonObject toVerify) {
        List<TableField> tableFields = fieldRepo.findByTableId(tableId);

        List<String> errorLogs = new ArrayList<>();
        tableFields
                .stream()
                .forEach(tableField -> {

                    String fieldName = tableField.getFieldName();
                    String toVerifyStr = toVerify.get(fieldName).toString();
                    toVerifyStr = toVerifyStr.substring(1, toVerifyStr.length() - 1);

                    errorLogs.addAll(generateNewValueErrorLogs(tableField, toVerifyStr));

                });

        return errorLogs;
    }

    public List<String> modifiedDataErrors(Long tableId, String key, String newValue) {
        List<TableField> tableFields = fieldRepo.findByTableId(tableId);

        List<String> errorLogs = new ArrayList<>();
        tableFields
                .stream()
                .filter(tableField -> tableField.getFieldName().equals(key))
                .forEach(tableField ->
                        errorLogs.addAll(generateNewValueErrorLogs(tableField, newValue)));

        return errorLogs;
    }


    public List<String> generateNewValueErrorLogs(TableField tableField,
                                                  String valueToVerify) {

        List<String> errorLogs = new ArrayList<>();

        Long fieldId = tableField.getFieldId();
        Long tableId = tableField.getTableId();
        String fieldType = tableField.getFieldType();
        String fieldName = tableField.getFieldName();
        Boolean isNotNull = tableField.isNotNull();
        Boolean unique = tableField.isUnique();
        Boolean isForeignKey = tableField.isForeignKey();

        if (fieldType.equals("int") && !isInteger(valueToVerify))
            errorLogs.add("Trying to insert non-int element");

        if (isNotNull && (valueToVerify.equals("") || valueToVerify.equals("null")))
            errorLogs.add("Trying to insert null into non-null field");

        if (unique && !isUniqueField(tableId, fieldName, valueToVerify))
            errorLogs.add("Trying to insert used value to unique-value field");

        if (isForeignKey && !foreignKeyValueExistsInPrimaryKeyTable(fieldId, valueToVerify)) {
            errorLogs.add("Such value doesn't exist in field referenced by foreign key!");
        }

        return errorLogs;
    }

    public Long getPrimaryKeyFieldId(Long tableId) {
        return
                fieldRepo.findByTableId(tableId)
                        .stream()
                        .filter(TableField::isPrimaryKey)
                        .findFirst()
                        .get()
                        .getFieldId();
    }


    private Boolean isReferencedByForeignKey(Long fieldId, Long databaseId) {
        return
                constraintManagement.getForeignKeysByDatabaseId(databaseId)
                        .stream()
                        .anyMatch(fieldConstraint -> {
                            JsonObject jsonField = new JsonParser()
                                    .parse(fieldConstraint.getConstraintInfoJson())
                                    .getAsJsonObject();

                            String referencedFieldId = jsonField.get("linkedFieldId").toString();
                            referencedFieldId = referencedFieldId.substring(1, referencedFieldId.length() - 1);

                            return Long.parseLong(referencedFieldId) == fieldId;
                        });
    }


    private Boolean foreignKeyValueExistsInPrimaryKeyTable(Long fieldId,
                                                           String toVerify) {

        FieldConstraint foreignKeyConstraint = constraintManagement.getForeignKeyByFieldId(fieldId);

        JsonObject foreignKeyDescription = new JsonParser()
                .parse(foreignKeyConstraint.getConstraintInfoJson())
                .getAsJsonObject();

        String referencedFieldId = foreignKeyDescription.get("linkedFieldId").toString();

        referencedFieldId = referencedFieldId.substring(1, referencedFieldId.length() - 1);

        Long referencedFieldIdLong = Long.parseLong(referencedFieldId);

        TableField referencedField = fieldManagement.getTableFieldById(referencedFieldIdLong);

        Long referencedFieldTableId = referencedField.getTableId();

        String referencedFieldName = referencedField.getFieldName();


        return
                tableDataRepo.findByTableId(referencedFieldTableId)
                        .stream()
                        .anyMatch(tableData -> {
                            JsonObject jsonField = new JsonParser()
                                    .parse(tableData.getFieldJsonValue())
                                    .getAsJsonObject();

                            String jsonFieldValue = jsonField.get(referencedFieldName).toString();
                            jsonFieldValue = jsonFieldValue.substring(1, jsonFieldValue.length() - 1);

                            return jsonFieldValue
                                    .equals(toVerify);
                        });

    }

    private Boolean isUniqueTableName(Long databaseId, String tableName) {

        return
                tableRepo
                        .findByDatabaseId(databaseId)
                        .stream()
                        .noneMatch(databaseTable -> {
                            Long tableId = databaseTable.getTableId();

                            TableDetails details = tableDetailsRepo.findByTableId(tableId);

                            return details.getTableName().equals(tableName);

                        });


    }



    private Boolean isUniqueField(Long tableId, String fieldName, String toVerify) {

        return tableDataRepo
                .findByTableId(tableId)
                .stream()
                .noneMatch(tableData -> {
                    JsonObject jsonField = new JsonParser()
                            .parse(tableData.getFieldJsonValue())
                            .getAsJsonObject();

                    String jsonFieldValue = jsonField.get(fieldName).toString();
                    jsonFieldValue = jsonFieldValue.substring(1, jsonFieldValue.length() - 1);

                    return jsonFieldValue
                            .equals(toVerify);

                });


    }

    private Boolean isInteger(String toVerify) {
        if (toVerify == null)
            return false;

        try {
            int parsedValue = Integer.parseInt(toVerify);
        } catch (NumberFormatException nfe) {
            return false;
        }

        return true;
    }

    public List<TableField> getTableFields(Long tableId) {
        return fieldRepo.findByTableId(tableId);
    }
}
