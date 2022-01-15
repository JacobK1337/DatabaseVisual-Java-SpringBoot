package pl.base.databases;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

@Repository
public interface DatabaseRepo extends JpaRepository<Database, Long> {

    public List<Database> findByUserId(Long userId);
    public List<Database> findAll();
    public Database findByDatabaseId(Long databaseId);

    @Transactional
    @Modifying
    public void deleteByDatabaseId(Long databaseId);

}
