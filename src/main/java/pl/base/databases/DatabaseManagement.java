package pl.base.databases;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DatabaseManagement {

    @Autowired
    private DatabaseRepo dbRepo;


    public List<Database> getUserDatabases(Long userId){
        return dbRepo.findByUserId(userId);
    }

    public Database getDatabase(Long databaseId){return dbRepo.findByDatabaseId(databaseId);}

    public void deleteDatabase(Long databaseId){dbRepo.deleteByDatabaseId(databaseId);}

    public void createNewDatabase(Long userId, String databaseName){
        Database newDB = new Database(
                0L,
                userId,
                databaseName
        );
        dbRepo.save(newDB);
    }
}
