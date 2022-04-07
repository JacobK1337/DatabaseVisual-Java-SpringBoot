package pl.base.services;

import org.springframework.stereotype.Component;
import pl.base.entities.TableData;
import pl.base.repositories.FieldRepo;
import pl.base.entities.TableField;
import pl.base.repositories.TableDataRepoCustom;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Component
public class TableDataFilteredImpl implements TableDataRepoCustom {

    @PersistenceContext
    private EntityManager em;

    private final FieldRepo fieldRepo;

    public TableDataFilteredImpl(FieldRepo fieldRepo){
        this.fieldRepo = fieldRepo;
    }

    @Override
    public List<TableData> findFilteredTableData(Map<String, String> params){
        Long tabId = Long.parseLong(params.get("tableId"));


        String SQLBaseQuery = "SELECT * FROM tables_data WHERE table_id = " + tabId;

        StringBuilder SQL_QUERY_BUILDER = new StringBuilder(SQLBaseQuery);

        Map<Integer, String> queryParameterName = new HashMap<>();
        int queryParameter = 1;

        List<TableData> result = new ArrayList<>();
        for (TableField tf : fieldRepo.findByTableId(tabId)) {

            Long id = tf.getFieldId();
            String fieldType = tf.getFieldType();
            String fieldName = tf.getFieldName();
            String comparator = params.get(id + "comparator");
            String filterValue = params.get(id + "filterValue");

            if(validFilterValue(filterValue)){
                if (fieldType.equals("int")) {

                    String SQLtoAppend = " AND CAST(JSON_EXTRACT(field_json_value, ?" + queryParameter + ") AS UNSIGNED)" + comparator + filterValue;

                    SQL_QUERY_BUILDER.append(SQLtoAppend);
                    queryParameterName.put(queryParameter, fieldName);
                    queryParameter++;
                } else {

                    String SQLtoAppend = " AND JSON_EXTRACT(field_json_value, " + "?" + queryParameter + ")" + comparator + "'" + filterValue + "'";

                    SQL_QUERY_BUILDER.append(SQLtoAppend);
                    queryParameterName.put(queryParameter, fieldName);
                    queryParameter++;
                }
            }



        }


        Query q = em.createNativeQuery(SQL_QUERY_BUILDER.toString());

        for (int i = 1; i < queryParameter; i++) {
            q.setParameter(i, "$." + queryParameterName.get(i));
        }

        List<Object[]> res = q.getResultList();

        for (Object[] elements : res) {
            TableData temp = new TableData(
                    Long.valueOf(String.valueOf(elements[0])),
                    Long.valueOf(String.valueOf(elements[1])),
                    String.valueOf(elements[2])
            );

            result.add(temp);
        }

        return result;


    }

    private Boolean validFilterValue(String comparatorValue){
        Pattern p = Pattern.compile("[A-Za-z0-9]+");

        return comparatorValue != null && p.matcher(comparatorValue).matches();
    }

}
