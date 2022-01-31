package pl.base.tables;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pl.base.fields.FieldRepo;
import pl.base.fields.TableField;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class TableDataFilteredImpl implements TableDataRepoCustom{

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private FieldRepo fieldRepo;

    @Autowired
    private TableDataRepo tableDataRepo;


    @Override
    public List<TableData> findFilteredTableData(Map<String, String> params) {
        Long tabId = Long.parseLong(params.get("tableId"));

        String SQL_QUERY = "SELECT * FROM tables_data WHERE table_id = " + tabId;
        Map<Integer, String> queryParameterName = new HashMap<>();
        int queryParameter = 1;

        List<TableData> result = new ArrayList<>();
        for(TableField tf : fieldRepo.findByTableId(tabId)){
            //info about field and filters on them
            Long id = tf.getFieldId();
            String fieldType = tf.getFieldType();
            String fieldName = tf.getFieldName();
            String comparator = params.get(id + "comparator");
            String filterValue = params.get(id + "filterValue");


            if(!comparator.equals("null") && !filterValue.equals("")){

                if(fieldType.equals("int")){
                    SQL_QUERY += " AND CAST(JSON_EXTRACT(field_json_value, ?" + queryParameter  + ") AS UNSIGNED)" + comparator + filterValue;
                    queryParameterName.put(queryParameter, fieldName);
                    queryParameter ++;
                }

                else{
                    SQL_QUERY += " AND JSON_EXTRACT(field_json_value, " + "?" + queryParameter + ")" + comparator + "'" + filterValue + "'";
                    queryParameterName.put(queryParameter, fieldName );
                    queryParameter ++;
                }

            }

        }


        Query q = em.createNativeQuery(SQL_QUERY);

        for(int i = 1; i < queryParameter; i++){
            q.setParameter(i, "$." + queryParameterName.get(i));
        }

        List<Object[]> res = q.getResultList();

        for(Object[] elements : res){
            TableData temp = new TableData(
                    Long.valueOf(String.valueOf(elements[0])),
                    Long.valueOf(String.valueOf(elements[1])),
                    String.valueOf(elements[2])
            );

            result.add(temp);
        }

        return result;


    }



}
