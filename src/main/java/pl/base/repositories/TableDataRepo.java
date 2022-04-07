package pl.base.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pl.base.entities.TableData;

import javax.transaction.Transactional;
import java.util.List;


@Repository
public interface TableDataRepo extends JpaRepository<TableData, Long> {
    List<TableData> findByTableId(Long tableId);
    TableData findByDataId(Long dataId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE tables_data SET field_json_value = JSON_SET(field_json_value, ?2 , ?3) WHERE data_id = ?1", nativeQuery = true)
    void updateJsonValueByDataId(Long dataId, String key, String newValue);

    @Modifying
    @Transactional
    @Query(nativeQuery = true, value="UPDATE tables_data SET field_json_value = JSON_REMOVE(field_json_value, ?1) WHERE table_id = ?2")
    void eraseJsonFieldByKey(String key, Long tableId);


    @Modifying
    @Transactional
    void deleteTableDataByDataId(Long dataId);

    @Modifying
    @Transactional
    void deleteTableDatasByTableId(Long tableId);
}
