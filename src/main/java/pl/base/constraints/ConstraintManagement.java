package pl.base.constraints;


import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


import java.util.List;


@Component
public class ConstraintManagement {

    @Autowired
    private ConstraintRepo constraintRepo;

    public List<FieldConstraint> getForeignKeysByDatabaseId(Long databaseId) {
        return constraintRepo.findForeignKeysByDatabaseId(databaseId);
    }

    public FieldConstraint getForeignKeyByFieldId(Long fieldId) {
        return constraintRepo.findForeignKeyByFieldId(fieldId);
    }

    public List<FieldConstraint> getPrimaryKeysByDatabaseId(Long databaseId) {
        return constraintRepo.findPrimaryKeysByDatabaseId(databaseId);
    }

    public List<FieldConstraint> getForeignKeysByPrimaryKeyId(Long primaryKeyId) {
        return constraintRepo.findForeignKeysByReferencedId(primaryKeyId);
    }

    public void setPrimaryKeyConstraint(Long fieldId, Long databaseId) {

        String jsonConstraintStringValue = "{\"type\": \"primarykey\"}";

        FieldConstraint newConstraint = new FieldConstraint(
                0L,
                fieldId,
                databaseId,
                jsonConstraintStringValue
        );
        constraintRepo.save(newConstraint);
    }

    public void setForeignKeyConstraint(Long fieldId, Long referencesFieldId, Long databaseId, String onDeleteAction) {
        String jsonConstraintStringValue = "{\"type\": \"foreignkey\", \"ondelete\":" + "\"" + onDeleteAction + "\"" + ", \"linkedFieldId\":" + "\"" + referencesFieldId + "\"}";

        FieldConstraint newConstraint = new FieldConstraint(
                0L,
                fieldId,
                databaseId,
                jsonConstraintStringValue
        );

        constraintRepo.save(newConstraint);
    }

    public void dropForeignKeyConstraint(Long fieldId) {
        constraintRepo.dropForeignKeyConstraint(fieldId);
    }

    public void setUniqueConstraint(Long fieldId, Long databaseId) {
        String jsonConstraintStringValue = "{\"type\": \"unique\"}";

        FieldConstraint newConstraint = new FieldConstraint(
                0L,
                fieldId,
                databaseId,
                jsonConstraintStringValue
        );

        constraintRepo.save(newConstraint);
    }

    public void dropUniqueConstraint(Long fieldId) {
        constraintRepo.dropUniqueConstraint(fieldId);
    }

    public void setNotNullConstraint(Long fieldId, Long databaseId) {
        String jsonConstraintStringValue = "{\"type\": \"notnull\"}";

        FieldConstraint newConstraint = new FieldConstraint(
                0L,
                fieldId,
                databaseId,
                jsonConstraintStringValue
        );

        constraintRepo.save(newConstraint);
    }

    public void dropNotNullConstraint(Long fieldId) {
        constraintRepo.dropNotNullConstraint(fieldId);
    }


    public Boolean isReferencedByForeignKey(Long fieldId, Long databaseId) {
        return
                constraintRepo.findForeignKeysByDatabaseId(databaseId)
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


}
