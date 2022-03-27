package pl.base.tables;

import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pl.base.constraints.ConstraintManagement;
import pl.base.fields.FieldManagement;
import pl.base.tabledetails.TableDetails;
import pl.base.tabledetails.TableDetailsRepo;

import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;


@Component
public class TableManagement {

    @Autowired
    private TableRepo tableRepo;

    @Autowired
    private TableDetailsRepo tableDetailsRepo;

    @Autowired
    private ConstraintManagement constraintManagement;

    @Autowired
    private FieldManagement fieldManagement;


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
            fieldManagement.addNewField(
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

        return tableRepo.findByDatabaseId(databaseId)
                .stream()
                .noneMatch(table -> {

                    TableDetails tableDetails = tableDetailsRepo.findByTableId(table.getTableId());
                    return tableDetails.getTableName().toLowerCase(Locale.ROOT)
                            .equals(newTableName.toLowerCase(Locale.ROOT));
                });
    }

    public void deleteTable(Long tableId, Long databaseId) {
        Long tablePrimaryKey = fieldManagement.getPrimaryKeyFieldId(tableId);

        if (!constraintManagement.isReferencedByForeignKey(tablePrimaryKey, databaseId))
            tableRepo.deleteDatabaseTableByTableId(tableId);
    }

    public void modifyTablePlacement(Long tableId,
                                     Integer pageX,
                                     Integer pageY) {

        tableDetailsRepo.setNewTablePlacement(tableId, pageX, pageY);
    }

    private Boolean isUniqueTableName(Long databaseId, String tableName) {

        return
                tableRepo
                        .findByDatabaseId(databaseId)
                        .stream()
                        .noneMatch(databaseTable -> {
                            Long tableId = databaseTable.getTableId();

                            TableDetails details = tableDetailsRepo.findByTableId(tableId);

                            return details.getTableName().equals(tableName);

                        });


    }


}
