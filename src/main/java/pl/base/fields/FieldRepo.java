package pl.base.fields;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

@Repository
public interface FieldRepo extends JpaRepository<TableField, Long> {

    public List<TableField> findByTableId(Long tableId);
    public List<TableField> findAll();
    public TableField findByFieldId(Long fieldId);

    @Modifying
    @Transactional
    @Query("UPDATE TableField tf SET tf.fieldName = ?2 WHERE tf.fieldId = ?1")
    public void setFieldNameByFieldId(Long fieldId, String newValue);

    @Modifying
    @Transactional
    @Query(nativeQuery = true, value="DELETE FROM table_fields WHERE field_name = ?1 AND table_id = ?2")
    public void deleteFieldByFieldName(String fieldName, Long tableId);

}
