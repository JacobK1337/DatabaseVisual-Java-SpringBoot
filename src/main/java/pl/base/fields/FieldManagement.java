package pl.base.fields;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pl.base.constraints.ConstraintManagement;
import pl.base.tabledata.TableData;
import pl.base.tabledata.TableDataRepo;


import java.util.List;
import java.util.regex.Pattern;

@Component
public class FieldManagement {

    @Autowired
    private FieldRepo fieldRepo;

    @Autowired
    private TableDataRepo tableDataRepo;

    @Autowired
    private ConstraintManagement constraintManagement;

    public TableField getTableFieldById(Long fieldId) {
        return fieldRepo.findByFieldId(fieldId);
    }

    public String getTableFieldInfoJson(Long fieldId) {

        TableField currentTableField = fieldRepo.findByFieldId(fieldId);
        JsonObject jsonObject = new JsonObject();

        String type = currentTableField.getFieldType();
        String name = currentTableField.getFieldName();

        boolean isNotNull = currentTableField.isNotNull();
        boolean isUnique = currentTableField.isUnique();

        boolean primaryKey = currentTableField.isPrimaryKey();
        boolean foreignKey = currentTableField.isPrimaryKey();

        jsonObject.addProperty("field_id", fieldId);
        jsonObject.addProperty("type", type);
        jsonObject.addProperty("name", name);

        jsonObject.addProperty("not_null", isNotNull);
        jsonObject.addProperty("is_unique", isUnique);

        jsonObject.addProperty("is_primary_key", primaryKey);
        jsonObject.addProperty("is_foreign_key", foreignKey);

        return jsonObject.toString();

    }

    public List<TableField> getFieldsByTableId(Long tableId) {
        return fieldRepo.findByTableId(tableId);
    }

    public void deleteTableFieldsByTableId(Long tableId){
        fieldRepo.deleteTableFieldsByTableId(tableId);
    }

    public void deleteFieldByName(String fieldName, Long tableId) {

        fieldRepo.deleteFieldByFieldName(fieldName, tableId);

        eraseFieldFromAll(fieldName, tableId);
    }


    public void setAsForeignKey(TableField foreignKeyField,
                                TableField primaryKeyField,
                                Long databaseId,
                                String onDeleteAction) {


        if (foreignKeyField.getFieldType().equals(primaryKeyField.getFieldType())
                && foreignKeyValuesMatchPrimaryKey(foreignKeyField, primaryKeyField)
                && (!(onDeleteAction.equals("setnull") && foreignKeyField.isNotNull())
                || !foreignKeyField.isNotNull())) {

            Long foreignKeyFieldId = foreignKeyField.getFieldId();
            Long primaryKeyFieldId = primaryKeyField.getFieldId();

            fieldRepo.setFieldAsForeignKey(foreignKeyFieldId);

            constraintManagement.dropForeignKeyConstraint(foreignKeyFieldId);

            constraintManagement.setForeignKeyConstraint(foreignKeyFieldId, primaryKeyFieldId, databaseId, onDeleteAction);

        } else
            System.out.println("Primary and foreign key types don't match or foreign key values don't match primary key values!");

    }

    public void setAsNotForeignKey(Long fieldId) {
        fieldRepo.setFieldAsNotForeignKey(fieldId);
        constraintManagement.dropForeignKeyConstraint(fieldId);
    }

    public void setAsUnique(Long fieldId, Long databaseId) {
        fieldRepo.setAsUnique(fieldId);
        constraintManagement.setUniqueConstraint(fieldId, databaseId);
    }

    public void setAsNotUnique(Long fieldId) {
        fieldRepo.setAsNotUnique(fieldId);
        constraintManagement.dropUniqueConstraint(fieldId);
    }

    public void setAsNullable(Long fieldId) {
        fieldRepo.setAsNullable(fieldId);
        constraintManagement.dropNotNullConstraint(fieldId);
    }

    public void setAsNotNull(Long fieldId, Long databaseId) {
        fieldRepo.setAsNotNull(fieldId);
        constraintManagement.setNotNullConstraint(fieldId, databaseId);
    }

    public void addNewField(Long tableId,
                            Long databaseId,
                            String fieldName,
                            String fieldType,
                            Boolean notNull,
                            Boolean unique,
                            String defaultValue,
                            Boolean isPrimaryKey) {



        String defaultVal = notNull ? "" : "null";

        if (!defaultValue.equals(""))
            defaultVal = defaultValue;

        if (uniqueFieldName(tableId, fieldName)) {

            TableField newField = new TableField();

            newField.setTableId(tableId);

            newField.setFieldName(fieldName);

            newField.setFieldType(fieldType);

            newField.setNotNull(notNull);

            newField.setUnique(unique);

            newField.setDefaultValue(defaultVal);

            newField.setPrimaryKey(isPrimaryKey);

            newField.setForeignKey(false);

            fieldRepo.save(newField);
            insertAddedFieldToAll(tableId, fieldName, defaultVal);

            if (notNull)
                constraintManagement.setNotNullConstraint(newField.getFieldId(), databaseId);

            if (unique)
                constraintManagement.setUniqueConstraint(newField.getFieldId(), databaseId);

            if (isPrimaryKey)
                constraintManagement.setPrimaryKeyConstraint(newField.getFieldId(), databaseId);

        }

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

    private Boolean foreignKeyValuesMatchPrimaryKey(TableField foreignKeyField,
                                                    TableField primaryKeyField) {

        Long foreignKeyTableId = foreignKeyField.getTableId();
        String foreignKeyName = foreignKeyField.getFieldName();

        Long primaryKeyTableId = primaryKeyField.getTableId();
        String primaryKeyName = primaryKeyField.getFieldName();

        return
                tableDataRepo.findByTableId(foreignKeyTableId)
                        .stream()
                        .allMatch(tableData -> {
                            String foreignKeyData = tableData.getFieldJsonValue();
                            JsonObject foreignKeyJson = new JsonParser()
                                    .parse(foreignKeyData)
                                    .getAsJsonObject();

                            String foreignKeyValue = foreignKeyJson.get(foreignKeyName).toString();

                            for (TableData td : tableDataRepo.findByTableId(primaryKeyTableId)) {
                                String primaryKeyData = td.getFieldJsonValue();
                                JsonObject primaryKeyJson = new JsonParser()
                                        .parse(primaryKeyData)
                                        .getAsJsonObject();

                                String primaryKeyValue = primaryKeyJson.get(primaryKeyName).toString();

                                if (primaryKeyValue.equals(foreignKeyValue))
                                    return true;

                            }

                            return false;
                        });
    }

    private void insertAddedFieldToAll(Long tableId,
                                       String fieldName,
                                       String fieldDefaultValue) {

        tableDataRepo.findByTableId(tableId)
                .stream()
                .filter(tableData -> tableData.getTableId().equals(tableId))
                .forEach(tableData -> {
                    tableDataRepo.updateJsonValueByDataId(tableData.getDataId(), "$." + fieldName, fieldDefaultValue);
                });

    }

    private void eraseFieldFromAll(String fieldName,
                                   Long tableId) {
        tableDataRepo.findByTableId(tableId)
                .stream()
                .filter(tableData -> tableData.getTableId().equals(tableId))
                .forEach(tableData -> {
                    tableDataRepo.eraseJsonFieldByKey("$." + fieldName, tableId);
                });

    }

    private boolean uniqueFieldName(Long tableId,
                                    String fieldName) {

        return fieldRepo.findByTableId(tableId)
                .stream()
                .noneMatch(tableField -> tableField.getFieldName().equals(fieldName));
    }


}
