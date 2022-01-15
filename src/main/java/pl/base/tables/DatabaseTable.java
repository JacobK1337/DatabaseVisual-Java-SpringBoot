package pl.base.tables;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "database_tables")
public class DatabaseTable {
    @Id
    @Column(name = "table_id", unique = true)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long tableId;

    @Column(name = "database_id")
    private Long databaseId;

    @Column(name = "table_name")
    private String tableName;

}
