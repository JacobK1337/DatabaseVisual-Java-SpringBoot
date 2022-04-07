package pl.base.repositories;

import pl.base.entities.TableData;

import java.util.List;
import java.util.Map;

public interface TableDataRepoCustom {
    List<TableData> findFilteredTableData(Map<String, String> params) throws Exception;
}
