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

}
