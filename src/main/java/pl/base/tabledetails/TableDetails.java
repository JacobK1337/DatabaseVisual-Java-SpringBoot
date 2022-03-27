package pl.base.tabledetails;

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
@Table(name = "table_details")
public class TableDetails {

    @Id
    @Column(name = "table_id")
    private Long tableId;

    @Column(name = "table_name")
    private String tableName;

    @Column(name = "page_x")
    private int pageX;

    @Column(name = "page_y")
    private int pageY;

    @Column(name = "color")
    private String color;
}
