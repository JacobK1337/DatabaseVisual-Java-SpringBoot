package pl.base.dataApi;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
public class DataApiManagement {

    @Autowired
    private DataApiRepo dataApiRepo;

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
