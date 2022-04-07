package pl.base.services;

import org.springframework.stereotype.Component;
import pl.base.entities.Database;
import pl.base.repositories.DatabaseRepo;

import java.util.List;

@Component
public class DatabaseManagement {

    private final DatabaseRepo databaseRepo;

    public DatabaseManagement(DatabaseRepo databaseRepo){
        this.databaseRepo = databaseRepo;
    }

    public List<Database> getUserDatabases(Long userId) {
        return databaseRepo.findByUserId(userId);
    }

    public Database getDatabase(Long databaseId) {
        return databaseRepo.findByDatabaseId(databaseId);
    }

    public void deleteDatabase(Long databaseId) {
        databaseRepo.deleteByDatabaseId(databaseId);
    }

    public void createNewDatabase(Long userId, String databaseName) {
        Database newDB = new Database(
                0L,
                userId,
                databaseName
        );

        databaseRepo.save(newDB);

    }

}
