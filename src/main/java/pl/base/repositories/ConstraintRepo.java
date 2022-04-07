package pl.base.repositories;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pl.base.entities.FieldConstraint;

import javax.transaction.Transactional;

import java.util.List;

@Repository
public interface ConstraintRepo extends JpaRepository<FieldConstraint, Long>{

    @Query(nativeQuery = true, value = "SELECT * FROM field_constraints WHERE JSON_EXTRACT(constraint_info_json, '$.type') = 'foreignkey' AND database_id = ?1")
    List<FieldConstraint> findForeignKeysByDatabaseId(Long databaseId);

    @Query(nativeQuery = true, value = "SELECT * FROM field_constraints WHERE JSON_EXTRACT(constraint_info_json, '$.type') = 'primarykey' AND database_id = ?1")
    List<FieldConstraint> findPrimaryKeysByDatabaseId(Long databaseId);

    @Query(nativeQuery = true, value = "SELECT * FROM field_constraints WHERE JSON_EXTRACT(constraint_info_json, '$.type') = 'foreignkey' AND field_id = ?1")
    FieldConstraint findForeignKeyByFieldId(Long fieldId);

    @Query(nativeQuery = true, value = "SELECT * FROM field_constraints WHERE JSON_EXTRACT(constraint_info_json, '$.type') = 'foreignkey' AND CAST(JSON_EXTRACT(constraint_info_json, '$.linkedFieldId') AS UNSIGNED) = ?1")
    List<FieldConstraint> findForeignKeysByReferencedId(Long referencedId);

    @Modifying
    @Transactional
    @Query(nativeQuery = true, value = "DELETE FROM field_constraints WHERE field_id = ?1 AND JSON_EXTRACT(constraint_info_json, '$.type') = 'primarykey'")
    void removePrimaryKeyConstraint(Long fieldId);

    @Modifying
    @Transactional
    @Query(nativeQuery = true, value = "DELETE FROM field_constraints WHERE JSON_EXTRACT(constraint_info_json, '$.type') = 'foreignkey' AND CAST(JSON_EXTRACT(constraint_info_json, '$.linkedFieldId') AS UNSIGNED) = ?1")
    void removeForeignKeysByPrimaryKey(Long fieldId);


    @Modifying
    @Transactional
    @Query(nativeQuery = true, value = "DELETE FROM field_constraints WHERE JSON_EXTRACT(constraint_info_json, '$.type') = 'foreignkey' AND field_id = ?1")
    void dropForeignKeyConstraint(Long fieldId);

    @Modifying
    @Transactional
    @Query(nativeQuery = true, value = "DELETE FROM field_constraints WHERE JSON_EXTRACT(constraint_info_json, '$.type') = 'notnull' AND field_id = ?1")
    void dropNotNullConstraint(Long fieldId);

    @Modifying
    @Transactional
    @Query(nativeQuery = true, value = "DELETE FROM field_constraints WHERE JSON_EXTRACT(constraint_info_json, '$.type') = 'unique' AND field_id = ?1")
    void dropUniqueConstraint(Long fieldId);

}
