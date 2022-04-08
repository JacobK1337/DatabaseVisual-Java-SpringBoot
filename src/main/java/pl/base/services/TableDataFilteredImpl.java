package pl.base.services;

import org.springframework.stereotype.Service;
import pl.base.entities.TableData;
import pl.base.repositories.FieldRepo;
import pl.base.repositories.TableDataRepoCustom;
import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.stream.IntStream;


@Service
public class TableDataFilteredImpl implements TableDataRepoCustom {

    @PersistenceContext
    private EntityManager em;

    private final FieldRepo fieldRepo;

    public TableDataFilteredImpl(FieldRepo fieldRepo){
        this.fieldRepo = fieldRepo;
    }

    @Override
    public List<TableData> findFilteredTableData(Map<String, String> params){

        Long tableId = Long.parseLong(params.get("tableId"));

        String SQLBaseQuery = "SELECT * FROM tables_data WHERE table_id = " + tableId;

        StringBuilder SQL_QUERY_BUILDER = new StringBuilder(SQLBaseQuery);

        var tableFields = fieldRepo.findByTableId(tableId);

        var queryParamName = new ArrayList<String>();
        var queryParamCount = new AtomicInteger(1);

        tableFields.stream()
                .filter(tableField -> validFilterValue(params.get(tableField.getFieldId() + "filterValue")))
                .filter(tableField -> !params.get(tableField.getFieldId() + "comparator").equals("null"))
                .map(tableField ->{
                    Long id = tableField.getFieldId();
                    String fieldType = tableField.getFieldType();
                    String fieldName = tableField.getFieldName();
                    String comparator = params.get(id + "comparator");
                    String filterValue = params.get(id + "filterValue");

                    String SQLtoAppend;
                    if (fieldType.equals("int")) {

                        SQLtoAppend = " AND CAST(JSON_EXTRACT(field_json_value, ?" + queryParamCount.getAndIncrement() + ") AS UNSIGNED)" + comparator + filterValue;

                    } else {

                        SQLtoAppend = " AND JSON_EXTRACT(field_json_value, " + "?" + queryParamCount.getAndIncrement() + ")" + comparator + "'" + filterValue + "'";

                    }

                    queryParamName.add(fieldName);
                    return SQLtoAppend;
                })
                .forEach(SQL_QUERY_BUILDER::append);


        var newQuery = em.createNativeQuery(SQL_QUERY_BUILDER.toString(), TableData.class);

        IntStream.range(1, queryParamCount.get())
                .forEach(intNum -> newQuery.setParameter(intNum, "$." + queryParamName.get(intNum - 1)));


        List<TableData> queryResultList = newQuery.getResultList();

        return queryResultList;


    }

    private Boolean validFilterValue(String comparatorValue){
        Pattern p = Pattern.compile("[A-Za-z0-9]+");

        return comparatorValue != null && p.matcher(comparatorValue).matches();
    }

}
