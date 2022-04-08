package pl.base.services;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.stereotype.Service;
import pl.base.entities.FieldConstraint;
import pl.base.entities.DataApi;
import pl.base.entities.TableData;
import pl.base.entities.TableField;
import pl.base.repositories.TableDataRepo;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class TableDataService {


    private final TableDataRepo tableDataRepo;
    private final TableFieldService tableFieldService;
    private final ConstraintService constraintService;
    private final DataApiService dataApiService;
    private final TableDataFilteredImpl tableDataFiltered;


    public TableDataService(TableDataRepo tableDataRepo,
                            TableFieldService tableFieldService,
                            ConstraintService constraintService,
                            DataApiService dataApiService,
                            TableDataFilteredImpl tableDataFiltered){
        this.tableDataRepo = tableDataRepo;
        this.tableFieldService = tableFieldService;
        this.constraintService = constraintService;
        this.dataApiService = dataApiService;
        this.tableDataFiltered = tableDataFiltered;
    }

    public List<List<String>> getTableData(Long tableId) {



        var tableData = tableDataRepo.findByTableId(tableId);

        var result =
                tableData.stream()
                .map(data -> List.of(
                        Long.toString(data.getDataId()),
                        data.getFieldJsonValue()

                ))
                .collect(Collectors.toList());

        return result;
    }

    public List<List<String>> getFilteredTableData(Map<String, String> params){


        var filteredTableData = tableDataFiltered.findFilteredTableData(params);
        var result =
            filteredTableData.stream()
                    .map(data -> List.of(
                            String.valueOf(data.getDataId()),
                            data.getFieldJsonValue()

                    ))
                    .collect(Collectors.toList());

        return result;
    }


    public void addJsonData(Long tableId, JsonObject newValue) {


        var errors = newDataErrors(tableId, newValue);

        if (errors.size() == 0) {

            String newValueStr = newValue.toString();

            TableData newTableData = new TableData(
                    0L,
                    tableId,
                    newValueStr

            );

            tableDataRepo.save(newTableData);
        }
        else
            errors.forEach(error ->{
                throw new RuntimeException(error);
            });

    }

    public void insertAddedFieldToAll(Long tableId,
                                       String fieldName,
                                       String fieldDefaultValue) {

        tableDataRepo.findByTableId(tableId)
                .stream()
                .filter(tableData -> tableData.getTableId().equals(tableId))
                .forEach(tableData ->
                        tableDataRepo.updateJsonValueByDataId(tableData.getDataId(), "$." + fieldName, fieldDefaultValue)
                );

    }

    public void eraseFieldFromAll(String fieldName,
                                   Long tableId) {
        tableDataRepo.findByTableId(tableId)
                .stream()
                .filter(tableData -> tableData.getTableId().equals(tableId))
                .forEach(tableData -> {
                    tableDataRepo.eraseJsonFieldByKey("$." + fieldName, tableId);
                });

    }

    public void modifyJsonData(Long dataId,
                               Long tableId,
                               String key,
                               String newValue) {

        var errors = modifiedDataErrors(tableId, key, newValue);

        if (errors.size() == 0){

            //if modified value is a primary key, we change foreign key values as well.
            var tableFields = tableFieldService.getFieldsByTableId(tableId);

            var primaryKeyFieldWithGivenKey =
                    tableFields
                            .stream()
                            .filter(TableField::isPrimaryKey)
                            .filter(tableField -> tableField.getFieldName().equals(key))
                            .findFirst()
                            .orElse(null);

            if(primaryKeyFieldWithGivenKey != null){
                var foreignKeys =
                        constraintService
                                .getForeignKeysByPrimaryKeyId(primaryKeyFieldWithGivenKey.getFieldId());


                //getting current primary key value, before modification
                var primaryKeyModifiedRow = tableDataRepo.findByDataId(dataId);
                var primaryKeyModifiedRowJson = new JsonParser()
                        .parse(primaryKeyModifiedRow.getFieldJsonValue())
                        .getAsJsonObject();

                var primaryKeyValue = primaryKeyModifiedRowJson.get(key).toString();


                foreignKeys.stream()
                        .map(foreignKey ->
                                tableFieldService.getTableFieldById(foreignKey.getFieldId()))
                        .forEach(tableField ->
                                tableDataRepo.findByTableId(tableField.getTableId())
                                        .stream()
                                        .filter(tableData -> {
                                            var fieldJsonValue = new JsonParser()
                                                    .parse(tableData.getFieldJsonValue())
                                                    .getAsJsonObject();
                                            var foreignKeyValue = fieldJsonValue.get(tableField.getFieldName()).toString();

                                            return foreignKeyValue.equals(primaryKeyValue);
                                        })
                                        .forEach(tableData ->
                                                tableDataRepo.updateJsonValueByDataId(
                                                        tableData.getDataId(),
                                                        "$." + tableField.getFieldName(),
                                                        newValue))
                        );
            }

            tableDataRepo.updateJsonValueByDataId(dataId, "$." + key, newValue);
        }

        else
            errors.forEach(error -> {
                throw new RuntimeException(error);
            });
    }

    public void deleteJsonData(Long tableId, Long databaseId, Long dataId) {

        String data = tableDataRepo.findByDataId(dataId).getFieldJsonValue();
        JsonObject dataJson = new JsonParser()
                .parse(data)
                .getAsJsonObject();


        //getting all primary keys
        tableFieldService.getFieldsByTableId(tableId)
                .stream()
                .filter(TableField::isPrimaryKey)
                .forEach(tableField -> {
                    Long primaryKeyId = tableField.getFieldId();
                    String primaryKeyName = tableField.getFieldName();

                    String primaryKeyData = dataJson.get(primaryKeyName).toString();
                    primaryKeyData = primaryKeyData.substring(1, primaryKeyData.length() - 1);

                    if (constraintService.isReferencedByForeignKey(primaryKeyId, databaseId))
                        fixForeignKeyValues(primaryKeyId, primaryKeyData, dataId);

                    else
                        commitDelete(dataId);

                });

    }


    public Boolean foreignKeyValuesMatchPrimaryKey(TableField foreignKeyField,
                                                    TableField primaryKeyField) {

        Long foreignKeyTableId = foreignKeyField.getTableId();
        String foreignKeyName = foreignKeyField.getFieldName();

        Long primaryKeyTableId = primaryKeyField.getTableId();
        String primaryKeyName = primaryKeyField.getFieldName();

        var tableData = tableDataRepo.findByTableId(foreignKeyTableId);
        var primaryTableData = tableDataRepo.findByTableId(primaryKeyTableId);

        return
                tableData.stream()
                .map(data -> {
                    var foreignKeyData = data.getFieldJsonValue();
                    var foreignKeyJson = new JsonParser()
                            .parse(foreignKeyData)
                            .getAsJsonObject();

                    var foreignKeyValue = foreignKeyJson.get(foreignKeyName).toString();

                    return foreignKeyValue;
                })
                .allMatch(foreignKeyValue ->
                        primaryTableData.stream()
                        .map(data -> {
                            var primaryKeyData = data.getFieldJsonValue();
                            var primaryKeyJson = new JsonParser()
                                    .parse(primaryKeyData)
                                    .getAsJsonObject();

                            var primaryKeyValue = primaryKeyJson.get(primaryKeyName).toString();

                            return primaryKeyValue;
                        })
                        .anyMatch(primaryKeyValue -> primaryKeyValue.equals(foreignKeyValue))
                );


    }

    public void importDataByDataApi(Long tableId,
                                    Long dataApiId,
                                    Long databaseId) {

        Long primaryKeyFieldId = tableFieldService.getPrimaryKeyFieldId(tableId);

        if (!constraintService.isReferencedByForeignKey(primaryKeyFieldId, databaseId)) {

            tableFieldService.deleteTableFieldsByTableId(tableId);
            tableDataRepo.deleteTableDatasByTableId(tableId);

            DataApi importedData = dataApiService.getDataApiByDataId(dataApiId);
            String primaryKeyName = importedData.getPrimaryKeyName();

            String data = importedData.getDataApiJson();

            JsonArray dataJson = new JsonParser()
                    .parse(data).getAsJsonArray();

            List<String> tableFields = dataJson.
                    get(0)
                    .getAsJsonObject()
                    .keySet()
                    .stream().toList();

            tableFields
                    .forEach(fieldName -> tableFieldService.addNewField(
                            tableId,
                            databaseId,
                            fieldName,
                            autoFieldType(fieldName, dataJson),
                            fieldName.equals(primaryKeyName),
                            fieldName.equals(primaryKeyName),
                            fieldName.equals(primaryKeyName) ? "" : "null",
                            fieldName.equals(primaryKeyName)

                    ));

            IntStream.range(0, dataJson.size())
                    .mapToObj(i -> dataJson.get(i).getAsJsonObject().toString())
                    .map(jsonData -> new TableData(
                            0L,
                            tableId,
                            jsonData
                    ))
                    .forEach(tableDataRepo::save);

        } else
            throw new RuntimeException("Cannot import - primary key is referenced in other table!");


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

        constraintService.getForeignKeysByPrimaryKeyId(primaryKeyId)
                .forEach(fieldConstraint -> {
                    var currentFieldId = fieldConstraint.getFieldId();

                    var currentField = tableFieldService.getTableFieldById(currentFieldId);

                    var currentFieldTableId = currentField.getTableId();

                    var data = fieldConstraint.getConstraintInfoJson();

                    var jsonData = new JsonParser()
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
        String fieldName = tableFieldService.
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
        String fieldName = tableFieldService.
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
        List<TableField> tableFields = tableFieldService.getFieldsByTableId(tableId);

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
        List<TableField> tableFields = tableFieldService.getFieldsByTableId(tableId);

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

        FieldConstraint foreignKeyConstraint = constraintService.getForeignKeyByFieldId(fieldId);

        JsonObject foreignKeyDescription = new JsonParser()
                .parse(foreignKeyConstraint.getConstraintInfoJson())
                .getAsJsonObject();

        String referencedFieldId = foreignKeyDescription.get("linkedFieldId").toString();

        referencedFieldId = referencedFieldId.substring(1, referencedFieldId.length() - 1);

        Long referencedFieldIdLong = Long.parseLong(referencedFieldId);

        TableField referencedField = tableFieldService.getTableFieldById(referencedFieldIdLong);

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




