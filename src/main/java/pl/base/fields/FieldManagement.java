package pl.base.fields;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pl.base.tables.TableDataRepo;
import pl.base.tables.TableRepo;


@Component
public class FieldManagement {
    @Autowired
    private TableRepo tableRepo;

    @Autowired
    private FieldRepo fieldRepo;

    @Autowired
    private TableDataRepo tableDataRepo;

    public TableField getTableFieldById(Long fieldId){
        return fieldRepo.findByFieldId(fieldId);
    }

    public void deleteFieldByName(String fieldName, Long tableId){
        fieldRepo.deleteFieldByFieldName(fieldName, tableId);

        eraseFieldFromAll(fieldName, tableId);
    }
    public void setAsPrimaryKey(Long fieldId){
        fieldRepo.setFieldAsPrimaryKey(fieldId);
    }
    public void setAsForeignKey(Long fieldId){fieldRepo.setFieldAsForeignKey(fieldId);}

    public void setAsNotForeignKey(Long fieldId){fieldRepo.setFieldAsNotForeignKey(fieldId);}

    public void setAsUnique(Long fieldId){fieldRepo.setAsUnique(fieldId);}
    public void setAsNotUnique(Long fieldId){fieldRepo.setAsNotUnique(fieldId);}

    public void setAsNotNullable(Long fieldId){fieldRepo.setAsNotNullable(fieldId);}

    public void setAsNullable(Long fieldId){fieldRepo.setAsNullable(fieldId);}

    public void addNewField(Long tableId,
                            String fieldName,
                            String fieldType,
                            Boolean nullable,
                            Boolean unique,
                            String defaultValue){

        String type = "";
        String defaultVal = "null";

        if(fieldType.equals("Integer"))
            type = "int";

        else if(fieldType.equals("String"))
            type = "varchar";

        if(!defaultValue.equals(""))
            defaultVal = defaultValue;

        if(uniqueFieldName(tableId, fieldName)){

            TableField newField = new TableField(
                    0L,
                    tableId,
                    fieldName,
                    type,
                    nullable,
                    unique,
                    defaultVal,
                    false,
                    false
            );

            fieldRepo.save(newField);
            insertAddedFieldToAll(tableId, fieldName, defaultVal);
        }

    }


    private void insertAddedFieldToAll(Long tableId,
                                  String fieldName,
                                  String fieldDefaultValue){

        tableDataRepo.findByTableId(tableId)
                .stream()
                .filter(tableData -> tableData.getTableId().equals(tableId))
                .forEach(tableData -> {
                    tableDataRepo.updateJsonValueByDataId(tableData.getDataId(), "$." + fieldName, fieldDefaultValue);
                });

    }

    private void eraseFieldFromAll(String fieldName,
                                   Long tableId){
        tableDataRepo.findByTableId(tableId)
                .stream()
                .filter(tableData -> tableData.getTableId().equals(tableId))
                .forEach(tableData ->{
                    tableDataRepo.eraseJsonFieldByKey("$." + fieldName, tableId);
                });

    }
    private boolean uniqueFieldName(Long tableId,
                                   String fieldName){

        return fieldRepo.findByTableId(tableId)
                .stream()
                .noneMatch(tableField -> tableField.getFieldName().equals(fieldName));
    }

}