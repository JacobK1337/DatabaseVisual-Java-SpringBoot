package pl.base.services;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import pl.base.entities.DataApi;
import pl.base.repositories.DataApiRepo;

import java.util.List;


@Service
public class DataApiService {

    private final DataApiRepo dataApiRepo;

    public DataApiService(DataApiRepo dataApiRepo){
        this.dataApiRepo = dataApiRepo;
    }

    public void saveNewData(String data, Long userId, Long tableId, String primaryKeyName) {

        DataApi newData = new DataApi(
                0L,
                userId,
                tableId,
                primaryKeyName,
                data
        );

        dataApiRepo.save(newData);
    }
    public List<DataApi> getDataApiByTableId(Long tableId){return dataApiRepo.getDataApiByTableId(tableId);}


    public DataApi getDataApiByDataId(Long dataApiId){
        return dataApiRepo.getDataApiByDataApiId(dataApiId);
    }

    public void deleteSavedData(Long dataApiId){dataApiRepo.deleteDataApiByDataApiId(dataApiId);}
}
