package pl.base.datashareproject;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = {"pl.base.user",
        "pl.base.datashareproject",
        "pl.base.databases",
        "pl.base.constraints",
        "pl.base.fields",
        "pl.base.tables",
        "pl.base.dataApi",
        "pl.base.tabledata",
        "pl.base.tabledetails"})
public class ComponentScanConfig { }
