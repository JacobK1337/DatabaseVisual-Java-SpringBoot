package pl.base.tabledetails;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pl.base.tabledetails.TableDetails;
import javax.transaction.Transactional;
import java.util.List;

@Repository
public interface TableDetailsRepo extends JpaRepository<TableDetails, Long> {

    public TableDetails findByTableId(Long tableId);

    @Modifying
    @Transactional
    @Query("UPDATE TableDetails td SET td.pageX = ?2, td.pageY = ?3 WHERE td.tableId = ?1")
    public void setNewTablePlacement(Long tableId, Integer pageX, Integer pageY);

    @Modifying
    @Transactional
    @Query("UPDATE TableDetails td SET td.color = ?2 WHERE td.tableId = ?1")
    public void setNewTableColor(Long tableId, String color);

    @Modifying
    @Transactional
    @Query("UPDATE TableDetails td SET td.tableName = ?2 WHERE td.tableId = ?1")
    public void setNewTableName(Long tableId, String newName);
}
