package pl.base.databases;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Pattern;

@Component
public class DatabaseManagement {

    @Autowired
    private DatabaseRepo databaseRepo;

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
