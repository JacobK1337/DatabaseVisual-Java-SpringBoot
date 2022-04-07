package pl.base.repositories;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pl.base.entities.TableField;

import javax.transaction.Transactional;
import java.util.List;

@Repository
public interface FieldRepo extends JpaRepository<TableField, Long> {

    List<TableField> findByTableId(Long tableId);
    List<TableField> findAll();
    TableField findByFieldId(Long fieldId);

    @Modifying
    @Transactional
    @Query(nativeQuery = true, value="DELETE FROM table_fields WHERE field_name = ?1 AND table_id = ?2")
    void deleteFieldByFieldName(String fieldName, Long tableId);

    @Modifying
    @Transactional
    void deleteTableFieldsByTableId(Long tableId);

    @Modifying
    @Transactional
    @Query("UPDATE TableField tf SET tf.isPrimaryKey = true WHERE tf.fieldId = ?1")
    void setFieldAsPrimaryKey(Long fieldId);

    @Modifying
    @Transactional
    @Query("UPDATE TableField tf SET tf.isForeignKey = true WHERE tf.fieldId = ?1")
    void setFieldAsForeignKey(Long fieldId);

    @Modifying
    @Transactional
    @Query("UPDATE TableField tf SET tf.isForeignKey = false WHERE tf.fieldId = ?1")
    void setFieldAsNotForeignKey(Long fieldId);

    @Modifying
    @Transactional
    @Query("UPDATE TableField tf SET tf.isUnique = true WHERE tf.fieldId = ?1")
    void setAsUnique(Long fieldId);

    @Modifying
    @Transactional
    @Query("UPDATE TableField tf SET tf.isUnique = false WHERE tf.fieldId = ?1")
    void setAsNotUnique(Long fieldId);

    @Modifying
    @Transactional
    @Query("UPDATE TableField tf SET tf.notNull = false WHERE tf.fieldId = ?1")
     void setAsNullable(Long fieldId);

    @Modifying
    @Transactional
    @Query("UPDATE TableField tf SET tf.notNull = true WHERE tf.fieldId = ?1")
    void setAsNotNull(Long fieldId);
}
