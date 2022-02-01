package pl.base.tables;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

@Repository
public interface TableRepo extends JpaRepository<DatabaseTable, Long> {
    public List<DatabaseTable> findByDatabaseId(Long databaseId);
    public DatabaseTable findByTableId(Long tableId);

    public DatabaseTable getDatabaseTableByTableId(Long tableId);

    @Transactional
    @Modifying
    public void deleteDatabaseTableByTableId(Long tableId);
}
