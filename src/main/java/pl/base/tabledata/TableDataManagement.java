package pl.base.tabledata;

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
import pl.base.fields.TableField;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

@Component
public class TableDataManagement {

    @Autowired
    private TableDataRepo tableDataRepo;

    @Autowired
    private FieldManagement fieldManagement;

    @Autowired
    private ConstraintManagement constraintManagement;

    @Autowired
    private DataApiManagement dataApiManagement;

    @Autowired
    private TableDataFilteredImpl tableDataFiltered;

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

    public List<List<String>> getFilteredTableData(Map<String, String> params){

        List<List<String>> result = new ArrayList<>();
        for (TableData td : tableDataFiltered.findFilteredTableData(params)) {
            result.add(Arrays.asList(
                    String.valueOf(td.getDataId()),
                    td.getFieldJsonValue()
            ));
        }
        return result;
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


        //getting all primary keys
        fieldManagement.getFieldsByTableId(tableId)
                .stream()
                .filter(TableField::isPrimaryKey)
                .forEach(tableField -> {
                    Long primaryKeyId = tableField.getFieldId();
                    String primaryKeyName = tableField.getFieldName();

                    String primaryKeyData = dataJson.get(primaryKeyName).toString();
                    primaryKeyData = primaryKeyData.substring(1, primaryKeyData.length() - 1);

                    if (constraintManagement.isReferencedByForeignKey(primaryKeyId, databaseId))
                        fixForeignKeyValues(primaryKeyId, primaryKeyData, dataId);

                    else
                        commitDelete(dataId);

                });

    }

    public void importDataByDataApi(Long tableId,
                                    Long dataApiId,
                                    Long databaseId) {

        Long primaryKeyFieldId = fieldManagement.getPrimaryKeyFieldId(tableId);

        if (!constraintManagement.isReferencedByForeignKey(primaryKeyFieldId, databaseId)) {

            fieldManagement.deleteTableFieldsByTableId(tableId);
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

            for (String key : tableFields) {

                String fieldType = autoFieldType(key, dataJson);
                boolean isPrimaryKey = false;
                boolean unique = false;
                boolean notNull = false;

                if (key.equals(primaryKeyName)) {
                    isPrimaryKey = true;
                    unique = true;
                    notNull = true;
                }

                fieldManagement.addNewField(
                        tableId,
                        databaseId,
                        key,
                        fieldType,
                        notNull,
                        unique,
                        "",
                        isPrimaryKey);
            }

            for (int i = 0; i < dataJson.size(); i++) {
                JsonObject jsonValue = dataJson.get(i).getAsJsonObject();
                String jsonData = jsonValue.toString();

                TableData newData = new TableData(
                        0L,
                        tableId,
                        jsonData
                );

                tableDataRepo.save(newData);
            }
        } else
            System.out.println("Cannot import - primary key is referenced in other table!");

    }

    private String autoFieldType(String fieldName,
                                 JsonArray data) {

        boolean isInteger = IntStream
                .range(0, data.size())
                .mapToObj(data::get)
                .allMatch(i -> {
                    JsonObject currentData = i
                            .getAsJsonObject();

                    String currentDataValue = currentData
                            .get(fieldName)
                            .toString();

                    currentDataValue = currentDataValue.substring(1, currentDataValue.length() - 1);

                    return isInteger(currentDataValue);
                });

        if (isInteger)
            return "int";

        else
            return "varchar";

    }

    private void commitDelete(Long dataId) {
        tableDataRepo.deleteTableDataByDataId(dataId);
    }


    //cascade deletion of all records containing currently deleted primary key (or set null).
    private void fixForeignKeyValues(Long primaryKeyId, String primaryKeyValue, Long dataToDeleteId) {

        constraintManagement.getForeignKeysByPrimaryKeyId(primaryKeyId)
                .forEach(fieldConstraint -> {
                    Long currentFieldId = fieldConstraint.getFieldId();

                    TableField currentField = fieldManagement.getTableFieldById(currentFieldId);

                    Long currentFieldTableId = currentField.getTableId();

                    String data = fieldConstraint.getConstraintInfoJson();
                    JsonObject jsonData = new JsonParser()
                            .parse(data)
                            .getAsJsonObject();

                    String onDeleteAction = jsonData.get("ondelete").toString();
                    onDeleteAction = onDeleteAction.substring(1, onDeleteAction.length() - 1);

                    switch (onDeleteAction) {
                        case "cascade" -> {
                            cascadeDelete(currentFieldTableId, currentFieldId, primaryKeyValue);
                            commitDelete(dataToDeleteId);
                        }
                        case "setnull" -> {
                            setNullDelete(currentFieldTableId, currentFieldId, primaryKeyValue);
                            commitDelete(dataToDeleteId);
                        }
                    }
                });

    }

    private void setNullDelete(Long tableId, Long fieldId, String valueToFind) {
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

    public List<String> newDataErrors(Long tableId, JsonObject toVerify) {
        List<TableField> tableFields = fieldManagement.getFieldsByTableId(tableId);

        List<String> errorLogs = new ArrayList<>();
        tableFields
                .forEach(tableField -> {

                    String fieldName = tableField.getFieldName();
                    String toVerifyStr = toVerify.get(fieldName).toString();
                    toVerifyStr = toVerifyStr.substring(1, toVerifyStr.length() - 1);

                    errorLogs.addAll(generateNewValueErrorLogs(tableField, toVerifyStr));

                });

        return errorLogs;
    }

    public List<String> modifiedDataErrors(Long tableId, String key, String newValue) {
        List<TableField> tableFields = fieldManagement.getFieldsByTableId(tableId);

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
        boolean unique = tableField.isUnique();
        boolean isForeignKey = tableField.isForeignKey();

        if (fieldType.equals("int") && !isInteger(valueToVerify))
            errorLogs.add("Trying to insert non-int element");

        if (unique && !isValueAvailable(tableId, fieldName, valueToVerify))
            errorLogs.add("Trying to insert used value to unique-value field");

        if (isForeignKey && !foreignKeyValueExistsInPrimaryKeyTable(fieldId, valueToVerify)) {
            errorLogs.add("Such value doesn't exist in field referenced by foreign key!");
        }

        return errorLogs;
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

    private Boolean isValueAvailable(Long tableId, String fieldName, String valueToFind) {

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
                            .equals(valueToFind);

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
}




