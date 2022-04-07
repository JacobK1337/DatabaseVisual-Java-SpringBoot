package pl.base.entities;

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
@Table(name = "tables_data")
public class TableData {

    @Id
    @Column(name = "data_id", unique = true)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long dataId;

    @Column(name = "table_id")
    private Long tableId;

    @Column(name = "field_json_value")
    private String fieldJsonValue;

}
