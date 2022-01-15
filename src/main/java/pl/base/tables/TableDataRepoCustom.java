package pl.base.tables;

import java.util.List;
import java.util.Map;

public interface TableDataRepoCustom {
    public List<TableData> findFilteredTableData(Map<String, String> params);
}
