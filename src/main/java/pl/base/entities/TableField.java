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
@Table(name = "table_fields")
public class TableField {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "field_id")
    private Long fieldId;

    @Column(name = "table_id")
    private Long tableId;

    @Column(name = "field_name")
    private String fieldName;

    @Column(name = "field_type")
    private String fieldType;

    @Column(name = "not_null")
    private boolean notNull;

    @Column(name = "unique_value")
    private boolean isUnique;

    @Column(name = "default_value")
    private String defaultValue;

    @Column(name = "is_primary_key")
    private boolean isPrimaryKey;

    @Column(name = "is_foreign_key")
    private boolean isForeignKey;
}
