package pl.base.constraints;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


import java.lang.reflect.Field;
import java.util.List;


@Component
public class ConstraintManagement {

    @Autowired
    private ConstraintRepo constraintRepo;

    public List<FieldConstraint> getFieldConstraints(Long fieldId){
        return constraintRepo.findByFieldId(fieldId);
    }
    public List<FieldConstraint> getForeignKeysByDatabaseId(Long databaseId){
        return constraintRepo.findForeignKeysByDatabaseId(databaseId);
    }

    public List<FieldConstraint> getPrimaryKeysByDatabaseId(Long databaseId){
        return constraintRepo.findPrimaryKeysByDatabaseId(databaseId);
    }

    public void setPrimaryKeyConstraint(Long fieldId, Long databaseId){

        String jsonConstraintStringValue = "{\"type\": \"primarykey\"}";

        FieldConstraint newConstraint = new FieldConstraint(
                0L,
                fieldId,
                databaseId,
                jsonConstraintStringValue
        );
        constraintRepo.save(newConstraint);
    }

    public void setForeignKeyConstraint(Long fieldId, Long referencesFieldId, Long databaseId, String onDeleteAction){
        String jsonConstraintStringValue = "{\"type\": \"foreignkey\", \"ondelete\":" + "\"" + onDeleteAction + "\"" + ", \"linkedFieldId\":" + "\"" + referencesFieldId + "\"}";

        FieldConstraint newConstraint = new FieldConstraint(
                0L,
                fieldId,
                databaseId,
                jsonConstraintStringValue
        );

        constraintRepo.save(newConstraint);
    }

    public void dropForeignKeyConstraint(Long fieldId){
        constraintRepo.dropForeignKeyConstraint(fieldId);
    }

    public void setUniqueConstraint(Long fieldId, Long databaseId){
        String jsonConstraintStringValue = "{\"type\": \"unique\"}";

        FieldConstraint newConstraint = new FieldConstraint(
                0L,
                fieldId,
                databaseId,
                jsonConstraintStringValue
        );

        constraintRepo.save(newConstraint);
    }

    public void dropUniqueConstraint(Long fieldId){
        constraintRepo.dropUniqueConstraint(fieldId);
    }

    public void setNotNullConstraint(Long fieldId, Long databaseId){
        String jsonConstraintStringValue = "{\"type\": \"notnull\"}";

        FieldConstraint newConstraint = new FieldConstraint(
                0L,
                fieldId,
                databaseId,
                jsonConstraintStringValue
        );

        constraintRepo.save(newConstraint);
    }

    public void dropNotNullConstraint(Long fieldId){
        constraintRepo.dropNotNullConstraint(fieldId);
    }

}
