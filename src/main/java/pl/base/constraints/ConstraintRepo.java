package pl.base.constraints;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

import java.util.List;

@Repository
public interface ConstraintRepo extends JpaRepository<FieldConstraint, Long>{

    public List<FieldConstraint> findByFieldId(Long fieldId);

    public FieldConstraint findByConstraintId(Long constraintId);

    @Query(nativeQuery = true, value = "SELECT * FROM field_constraints WHERE JSON_EXTRACT(constraint_info_json, '$.type') = 'foreignkey' AND database_id = ?1")
    public List<FieldConstraint> findForeignKeysByDatabaseId(Long databaseId);

    @Query(nativeQuery = true, value = "SELECT * FROM field_constraints WHERE JSON_EXTRACT(constraint_info_json, '$.type') = 'primarykey' AND database_id = ?1")
    public List<FieldConstraint> findPrimaryKeysByDatabaseId(Long databaseId);

    @Modifying
    @Transactional
    @Query(nativeQuery = true, value = "DELETE FROM field_constraints WHERE field_id = ?1 AND JSON_EXTRACT(constraint_info_json, '$.type') = 'primarykey'")
    public void removePrimaryKeyConstraint(Long fieldId);

    @Modifying
    @Transactional
    @Query(nativeQuery = true, value = "DELETE FROM field_constraints WHERE JSON_EXTRACT(constraint_info_json, '$.type') = 'foreignkey' AND JSON_EXTRACT(constraint_info_json, '$.linkedFieldId') = ?1")
    public void removeForeignKeysByPrimaryKey(Long fieldId);


    @Modifying
    @Transactional
    @Query(nativeQuery = true, value = "DELETE FROM field_constraints WHERE JSON_EXTRACT(constraint_info_json, '$.type') = 'foreignkey' AND field_id = ?1")
    public void dropForeignKeyConstraint(Long fieldId);

    @Modifying
    @Transactional
    @Query(nativeQuery = true, value = "DELETE FROM field_constraints WHERE JSON_EXTRACT(constraint_info_json, '$.type') = 'notnull' AND field_id = ?1")
    public void dropNotNullConstraint(Long fieldId);

    @Modifying
    @Transactional
    @Query(nativeQuery = true, value = "DELETE FROM field_constraints WHERE JSON_EXTRACT(constraint_info_json, '$.type') = 'unique' AND field_id = ?1")
    public void dropUniqueConstraint(Long fieldId);

}
