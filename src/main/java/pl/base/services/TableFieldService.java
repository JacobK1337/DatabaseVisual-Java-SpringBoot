package pl.base.services;

import com.google.gson.JsonObject;
import org.springframework.stereotype.Service;
import pl.base.entities.TableField;
import pl.base.repositories.FieldRepo;

import java.util.List;

@Service
public class TableFieldService {

    private final FieldRepo fieldRepo;
    private final ConstraintService constraintService;

    public TableFieldService(FieldRepo fieldRepo, ConstraintService constraintService){
        this.fieldRepo = fieldRepo;
        this.constraintService = constraintService;
    }


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

    }


    public void setAsForeignKey(TableField foreignKeyField,
                                TableField primaryKeyField,
                                Long databaseId,
                                String onDeleteAction) {


        if (foreignKeyField.getFieldType().equals(primaryKeyField.getFieldType())
                && (!(onDeleteAction.equals("setnull") && foreignKeyField.isNotNull())
                || !foreignKeyField.isNotNull())) {

            Long foreignKeyFieldId = foreignKeyField.getFieldId();
            Long primaryKeyFieldId = primaryKeyField.getFieldId();

            fieldRepo.setFieldAsForeignKey(foreignKeyFieldId);

            constraintService.dropForeignKeyConstraint(foreignKeyFieldId);

            constraintService.setForeignKeyConstraint(foreignKeyFieldId, primaryKeyFieldId, databaseId, onDeleteAction);

        } else
            throw new IllegalStateException("This field type don't match with chosen field, or can't set this field to null!");

    }

    public void setAsNotForeignKey(Long fieldId) {
        fieldRepo.setFieldAsNotForeignKey(fieldId);
        constraintService.dropForeignKeyConstraint(fieldId);
    }

    public void setAsUnique(Long fieldId, Long databaseId) {
        fieldRepo.setAsUnique(fieldId);
        constraintService.setUniqueConstraint(fieldId, databaseId);
    }

    public void setAsNotUnique(Long fieldId) {
        fieldRepo.setAsNotUnique(fieldId);
        constraintService.dropUniqueConstraint(fieldId);
    }

    public void setAsNullable(Long fieldId) {
        fieldRepo.setAsNullable(fieldId);
        constraintService.dropNotNullConstraint(fieldId);
    }

    public void setAsNotNull(Long fieldId, Long databaseId) {
        fieldRepo.setAsNotNull(fieldId);
        constraintService.setNotNullConstraint(fieldId, databaseId);
    }

    public void addNewField(Long tableId,
                            Long databaseId,
                            String fieldName,
                            String fieldType,
                            Boolean notNull,
                            Boolean unique,
                            String defaultValue,
                            Boolean isPrimaryKey){



        String defaultVal = notNull ? "" : "null";

        if (!defaultValue.equals(""))
            defaultVal = defaultValue;

        if (uniqueFieldName(tableId, fieldName)) {



            TableField newField = new TableField(
                    0L,
                    tableId,
                    fieldName,
                    fieldType,
                    notNull,
                    unique,
                    defaultVal,
                    isPrimaryKey,
                    false
            );


            var savedField = fieldRepo.save(newField);

            if (notNull)
                constraintService.setNotNullConstraint(savedField.getFieldId(), databaseId);

            if (unique)
                constraintService.setUniqueConstraint(savedField.getFieldId(), databaseId);

            if (isPrimaryKey)
                constraintService.setPrimaryKeyConstraint(savedField.getFieldId(), databaseId);

        }

        else
            throw new IllegalStateException("Field name is already in use!");

    }

    public Long getPrimaryKeyFieldId(Long tableId) {

        var primaryKey = fieldRepo
                .findByTableId(tableId)
                .stream()
                .filter(TableField::isPrimaryKey)
                .findFirst();

        return primaryKey.isPresent() ? primaryKey.get().getFieldId() : -1L;

    }

    private boolean uniqueFieldName(Long tableId,
                                    String fieldName) {

        return fieldRepo.findByTableId(tableId)
                .stream()
                .noneMatch(tableField -> tableField.getFieldName().equals(fieldName));
    }


}
