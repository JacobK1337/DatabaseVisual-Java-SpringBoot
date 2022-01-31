package pl.base.dataApi;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DataApiRepo extends JpaRepository<DataApi, Long> {

    public List<DataApi> getDataApiByUserId(Long userId);

    public List<DataApi> getDataApiByTableId(Long tableId);

    public DataApi getDataApiByDataApiId(Long dataApiId);

}
