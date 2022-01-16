package pl.base.tables;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

@Repository
public interface TableDetailsRepo extends JpaRepository<TableDetails, Long> {

    public TableDetails findByTableId(Long tableId);


    @Modifying
    @Transactional
    @Query("UPDATE TableDetails td SET td.pageX = ?2, td.pageY = ?3 WHERE td.tableId = ?1")
    public void setNewTableDetails(Long tableId, Integer pageX, Integer pageY);
}
