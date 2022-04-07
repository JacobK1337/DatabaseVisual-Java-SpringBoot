package pl.base.datashareproject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import pl.base.entities.Database;
import pl.base.entities.DatabaseTable;
import pl.base.repositories.ConstraintRepo;
import pl.base.entities.FieldConstraint;
import pl.base.entities.DataApi;
import pl.base.repositories.DataApiRepo;
import pl.base.repositories.FieldRepo;
import pl.base.entities.TableField;
import pl.base.repositories.DatabaseRepo;
import pl.base.repositories.TableRepo;
import pl.base.entities.TableData;
import pl.base.repositories.TableDataRepo;
import pl.base.entities.TableDetails;
import pl.base.entities.User;
import pl.base.repositories.UserRepo;

@SpringBootApplication

@EnableJpaRepositories(basePackageClasses = {
        UserRepo.class,
        ConstraintRepo.class,
        DatabaseRepo.class,
        FieldRepo.class,
        TableRepo.class,
        TableDataRepo.class,
        TableDetails.class,
        DataApiRepo.class})

@EntityScan(basePackageClasses = {
        User.class,
        Database.class,
        DatabaseTable.class,
        FieldConstraint.class,
        TableField.class,
        TableDetails.class,
        TableData.class,
        DataApi.class})

@ComponentScan(basePackages = {
        "pl.base.entities",
        "pl.base.repositories",
        "pl.base.services",
        "pl.base.utils",
        "pl.base.controllers",
        "pl.base.configs"})

public class DataSoftProjectApplication {

    public static void main(String[] args) {
        SpringApplication.run(DataSoftProjectApplication.class, args);
    }

}
