package pl.base.databases;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.persistence.Table;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "user_databases")
public class Database {
    @Id
    @Column(name = "database_id", unique = true)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long databaseId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "database_name")
    private String databaseName;

}
