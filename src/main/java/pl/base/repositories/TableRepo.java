package pl.base.repositories;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import pl.base.entities.DatabaseTable;

import javax.transaction.Transactional;
import java.util.List;

@Repository
public interface TableRepo extends JpaRepository<DatabaseTable, Long> {
    List<DatabaseTable> findByDatabaseId(Long databaseId);
    DatabaseTable findByTableId(Long tableId);

    DatabaseTable getDatabaseTableByTableId(Long tableId);

    @Transactional
    @Modifying
    void deleteDatabaseTableByTableId(Long tableId);
}
