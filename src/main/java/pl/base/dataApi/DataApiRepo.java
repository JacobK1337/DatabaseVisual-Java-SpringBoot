package pl.base.dataApi;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

@Repository
public interface DataApiRepo extends JpaRepository<DataApi, Long> {

    public List<DataApi> getDataApiByUserId(Long userId);

    public List<DataApi> getDataApiByTableId(Long tableId);

    public DataApi getDataApiByDataApiId(Long dataApiId);


    @Modifying
    @Transactional
    public void deleteDataApiByDataApiId(Long dataApiId);
}
