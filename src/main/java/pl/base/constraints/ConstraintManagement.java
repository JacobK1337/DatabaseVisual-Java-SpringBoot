package pl.base.constraints;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


import java.util.List;


@Component
public class ConstraintManagement {

    @Autowired
    private ConstraintRepo constraintRepo;

    public List<FieldConstraint> getFieldConstraints(Long fieldId){
        return constraintRepo.findByFieldId(fieldId);
    }
    public List<FieldConstraint> getAllForeignKeys(){
        return constraintRepo.findAllForeignKeys();
    }



}
