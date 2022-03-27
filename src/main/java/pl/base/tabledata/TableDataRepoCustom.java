package pl.base.tabledata;

import pl.base.tabledata.TableData;

import java.util.List;
import java.util.Map;

public interface TableDataRepoCustom {
    public List<TableData> findFilteredTableData(Map<String, String> params) throws Exception;
}
