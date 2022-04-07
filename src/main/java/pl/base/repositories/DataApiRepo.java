package pl.base.repositories;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import pl.base.entities.DataApi;

import javax.transaction.Transactional;
import java.util.List;

@Repository
public interface DataApiRepo extends JpaRepository<DataApi, Long> {

    List<DataApi> getDataApiByTableId(Long tableId);

    DataApi getDataApiByDataApiId(Long dataApiId);

    @Modifying
    @Transactional
    void deleteDataApiByDataApiId(Long dataApiId);
}
