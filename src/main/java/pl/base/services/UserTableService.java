package pl.base.services;

import com.google.gson.JsonObject;
import org.springframework.stereotype.Service;
import pl.base.entities.DatabaseTable;
import pl.base.repositories.TableRepo;
import pl.base.entities.TableDetails;
import pl.base.repositories.TableDetailsRepo;

import java.util.List;
import java.util.Locale;


@Service
public class UserTableService {


    private final TableRepo tableRepo;
    private final TableDetailsRepo tableDetailsRepo;
    private final ConstraintService constraintService;
    private final TableFieldService tableFieldService;


    public UserTableService(TableRepo tableRepo,
                            TableDetailsRepo tableDetailsRepo,
                            ConstraintService constraintService,
                            TableFieldService tableFieldService){

        this.tableRepo = tableRepo;
        this.tableDetailsRepo = tableDetailsRepo;
        this.constraintService = constraintService;
        this.tableFieldService = tableFieldService;
    }

    public List<DatabaseTable> getDatabaseTables(Long databaseId) {
        return tableRepo.findByDatabaseId(databaseId);
    }

    public DatabaseTable getTable(Long tableId) {
        return tableRepo.findByTableId(tableId);
    }


    public TableDetails getTableDetails(Long tableId) {
        return tableDetailsRepo.findByTableId(tableId);
    }

    public String getTableDetailsJson(Long tableId) {
        TableDetails currentTable = getTableDetails(tableId);
        JsonObject jsonObject = new JsonObject();

        String tableName = currentTable.getTableName();
        String pageX = String.valueOf(currentTable.getPageX());
        String pageY = String.valueOf(currentTable.getPageY());
        String color = currentTable.getColor();

        jsonObject.addProperty("table_id", tableId);
        jsonObject.addProperty("table_name", tableName);
        jsonObject.addProperty("page_x", pageX);
        jsonObject.addProperty("page_y", pageY);
        jsonObject.addProperty("color", color);

        return jsonObject.toString();
    }

    public void createNewTable(Long databaseId,
                               String tableName,
                               String primaryKeyName,
                               String primaryKeyType) {

        if (tableNameAvailable(databaseId, tableName)) {

            DatabaseTable newTable = new DatabaseTable();
            newTable.setDatabaseId(databaseId);
            tableRepo.save(newTable);

            Long newTableId = newTable.getTableId();

            TableDetails newTableDetails = new TableDetails(
                    newTableId,
                    tableName,
                    1000,
                    1000,
                    "dimgrey"
            );

            tableDetailsRepo.save(newTableDetails);
            tableFieldService.addNewField(
                    newTableId,
                    databaseId,
                    primaryKeyName,
                    primaryKeyType,
                    true,
                    true,
                    "",
                    true);
        }

        else
            System.out.println("Table name is not available!");

    }

    private boolean tableNameAvailable(Long databaseId, String newTableName) {

        var databasesTables = tableRepo.findByDatabaseId(databaseId);

        return
            databasesTables.stream()
                    .map(DatabaseTable::getTableId)
                    .map(tableDetailsRepo::findByTableId)
                    .map(tableDetail -> tableDetail.getTableName().toLowerCase(Locale.ROOT))
                    .noneMatch(tableName -> tableName.equals(newTableName.toLowerCase(Locale.ROOT)));


    }

    public void deleteTable(Long tableId, Long databaseId) {
        Long tablePrimaryKey = tableFieldService.getPrimaryKeyFieldId(tableId);

        if (!constraintService.isReferencedByForeignKey(tablePrimaryKey, databaseId))
            tableRepo.deleteDatabaseTableByTableId(tableId);
    }

    public void modifyTablePlacement(Long tableId,
                                     Integer pageX,
                                     Integer pageY) {

        tableDetailsRepo.setNewTablePlacement(tableId, pageX, pageY);
    }

}
