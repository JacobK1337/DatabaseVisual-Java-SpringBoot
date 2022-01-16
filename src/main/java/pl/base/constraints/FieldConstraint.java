package pl.base.constraints;

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
@Table(name = "field_constraints")
public class FieldConstraint {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "constraint_id")
    private Long constraintId;

    @Column(name = "field_id")
    private Long fieldId;

    @Column(name = "database_id")
    private Long database_id;

    @Column(name = "constraint_info_json")
    private String constraintInfoJson;




}
