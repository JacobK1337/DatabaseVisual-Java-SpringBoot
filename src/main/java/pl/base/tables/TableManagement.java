package pl.base.tables;


import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pl.base.constraints.ConstraintManagement;
import pl.base.constraints.FieldConstraint;
import pl.base.fields.FieldManagement;
import pl.base.fields.FieldRepo;
import pl.base.fields.TableField;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

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

    public List<DatabaseTable> getDatabaseTables(Long databaseId) {
        return tableRepo.findByDatabaseId(databaseId);
    }

    public DatabaseTable getTable(Long tableId) {
        return tableRepo.findByTableId(tableId);
    }


    public TableDetails getTableDetails(Long tableId) {
        return tableDetailsRepo.findByTableId(tableId);
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
                "green"
        );

        tableDetailsRepo.save(newTableDetails);

        fieldManagement.addNewField(newTableId,
                primaryKeyName,
                primaryKeyType,
                false,
                true,
                "null",
                true);

        Long newPrimaryKeyId = fieldRepo.
                findTableFieldPrimaryKey(newTableId).
                getFieldId();

        constraintManagement.setPrimaryKeyConstraint(newPrimaryKeyId, databaseId);
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

                    if(isReferencedByForeignKey(primaryKeyId, databaseId))
                        fixForeignKeyValues(primaryKeyId, tableId, primaryKeyData);

                });

        tableDataRepo.deleteTableDataByDataId(dataId);
    }

    private void fixForeignKeyValues(Long primaryKeyId, Long tableId,  String primaryKeyValue){

        constraintManagement.getForeignKeysByPrimaryKeyId(primaryKeyId)
                .stream()
                .forEach(fieldConstraint -> {
                    Long currentFieldId = fieldConstraint.getFieldId();

                    Long currentFieldTableId = fieldRepo.findByFieldId(currentFieldId).getTableId();

                    String data = fieldConstraint.getConstraintInfoJson();
                    JsonObject jsonData = new JsonParser()
                            .parse(data)
                            .getAsJsonObject();

                    String onDeleteAction = jsonData.get("ondelete").toString();
                    onDeleteAction = onDeleteAction.substring(1, onDeleteAction.length() - 1);


                    switch(onDeleteAction){
                        case "cascade":
                            //cascadeDelete(fieldId);
                            break;

                        case "setnull":
                            setValueAsNull(currentFieldTableId, currentFieldId, primaryKeyValue);
                            break;
                    }
                });
    }


    private void setValueAsNull(Long tableId, Long fieldId, String valueToFind){


        String fieldName = fieldManagement.
                getTableFieldById(fieldId).
                getFieldName();

        tableDataRepo.
                findByTableId(tableId)
                .stream()
                .filter(tableData -> {
                    String data = tableData.getFieldJsonValue();
                    JsonObject dataJson = new JsonParser()
                            .parse(data).getAsJsonObject();

                    String fieldValueInDataJson = dataJson.
                            get(fieldName).
                            toString();

                    fieldValueInDataJson = fieldValueInDataJson.substring(1, fieldValueInDataJson.length() - 1);
                    return fieldValueInDataJson.
                            equals(valueToFind);

                })
                .forEach(tableData -> {

                    Long dataId = tableData.getDataId();
                    tableDataRepo.updateJsonValueByDataId(dataId, "$." + fieldName, "null");
                });

    }

    private void cascadeDelete(Long fieldId){

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
        Boolean nullable = tableField.isNullable();
        Boolean unique = tableField.isUnique();
        Boolean isForeignKey = tableField.isForeignKey();

        if (fieldType.equals("int") && !isInteger(valueToVerify))
            errorLogs.add("Trying to insert non-int element");

        if (!nullable && valueToVerify.equals(""))
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
