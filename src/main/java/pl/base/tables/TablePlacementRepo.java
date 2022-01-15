package pl.base.tables;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

@Repository
public interface TablePlacementRepo extends JpaRepository<TablePlacement, Long> {

    public TablePlacement findByTableId(Long tableId);


    @Modifying
    @Transactional
    @Query("UPDATE TablePlacement tp SET tp.pageX = ?2, tp.pageY = ?3 WHERE tp.tableId = ?1")
    public void setNewTablePlacement(Long tableId, Integer pageX, Integer pageY);
}
