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

    @Query(nativeQuery = true, value = "SELECT * FROM table_fields WHERE table_id = ?1 AND is_primary_key = true")
    public TableField findTableFieldPrimaryKey(Long tableId);

    @Modifying
    @Transactional
    @Query("UPDATE TableField tf SET tf.fieldName = ?2 WHERE tf.fieldId = ?1")
    public void setFieldNameByFieldId(Long fieldId, String newValue);

    @Modifying
    @Transactional
    @Query(nativeQuery = true, value="DELETE FROM table_fields WHERE field_name = ?1 AND table_id = ?2")
    public void deleteFieldByFieldName(String fieldName, Long tableId);

    @Modifying
    @Transactional
    @Query("UPDATE TableField tf SET tf.isPrimaryKey = true WHERE tf.fieldId = ?1")
    public void setFieldAsPrimaryKey(Long fieldId);

    @Modifying
    @Transactional
    @Query("UPDATE TableField tf SET tf.isForeignKey = true WHERE tf.fieldId = ?1")
    public void setFieldAsForeignKey(Long fieldId);

    @Modifying
    @Transactional
    @Query("UPDATE TableField tf SET tf.isForeignKey = false WHERE tf.fieldId = ?1")
    public void setFieldAsNotForeignKey(Long fieldId);

    @Modifying
    @Transactional
    @Query("UPDATE TableField tf SET tf.isUnique = true WHERE tf.fieldId = ?1")
    public void setAsUnique(Long fieldId);

    @Modifying
    @Transactional
    @Query("UPDATE TableField tf SET tf.isUnique = false WHERE tf.fieldId = ?1")
    public void setAsNotUnique(Long fieldId);

    @Modifying
    @Transactional
    @Query("UPDATE TableField tf SET tf.nullable = false WHERE tf.fieldId = ?1")
    public void setAsNotNullable(Long fieldId);

    @Modifying
    @Transactional
    @Query("UPDATE TableField tf SET tf.nullable = true WHERE tf.fieldId = ?1")
    public void setAsNullable(Long fieldId);
}
