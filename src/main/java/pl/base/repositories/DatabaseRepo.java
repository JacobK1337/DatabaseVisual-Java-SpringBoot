package pl.base.repositories;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import pl.base.entities.Database;

import javax.transaction.Transactional;
import java.util.List;

@Repository
public interface DatabaseRepo extends JpaRepository<Database, Long> {

    List<Database> findByUserId(Long userId);
    List<Database> findAll();
    Database findByDatabaseId(Long databaseId);

    @Transactional
    @Modifying
    void deleteByDatabaseId(Long databaseId);

}
