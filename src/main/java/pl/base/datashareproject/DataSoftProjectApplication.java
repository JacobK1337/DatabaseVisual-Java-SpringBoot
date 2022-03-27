package pl.base.datashareproject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import pl.base.constraints.ConstraintRepo;
import pl.base.constraints.FieldConstraint;
import pl.base.dataApi.DataApi;
import pl.base.dataApi.DataApiRepo;
import pl.base.databases.*;
import pl.base.fields.FieldRepo;
import pl.base.fields.TableField;
import pl.base.tabledata.TableData;
import pl.base.tabledata.TableDataRepo;
import pl.base.tabledetails.TableDetails;
import pl.base.tables.*;
import pl.base.user.User;
import pl.base.user.UserRepo;

@SpringBootApplication
@EnableJpaRepositories(basePackageClasses = {UserRepo.class, ConstraintRepo.class, DatabaseRepo.class, FieldRepo.class, TableRepo.class, TableDataRepo.class, TableDetails.class, DataApiRepo.class})
@EntityScan(basePackageClasses = {User.class, Database.class, DatabaseTable.class, FieldConstraint.class, TableField.class, TableDetails.class, TableData.class, DataApi.class})
public class DataSoftProjectApplication {

    public static void main(String[] args) {
        SpringApplication.run(DataSoftProjectApplication.class, args);
    }

}
