package pl.base.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pl.base.entities.TableDetails;
import javax.transaction.Transactional;

@Repository
public interface TableDetailsRepo extends JpaRepository<TableDetails, Long> {

    TableDetails findByTableId(Long tableId);

    @Modifying
    @Transactional
    @Query("UPDATE TableDetails td SET td.pageX = ?2, td.pageY = ?3 WHERE td.tableId = ?1")
    void setNewTablePlacement(Long tableId, Integer pageX, Integer pageY);

    @Modifying
    @Transactional
    @Query("UPDATE TableDetails td SET td.color = ?2 WHERE td.tableId = ?1")
    void setNewTableColor(Long tableId, String color);

    @Modifying
    @Transactional
    @Query("UPDATE TableDetails td SET td.tableName = ?2 WHERE td.tableId = ?1")
    void setNewTableName(Long tableId, String newName);
}
