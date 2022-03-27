package pl.base.dataApi;

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
@Table(name = "data_api")
public class DataApi {

    @Id
    @Column(name = "data_api_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long dataApiId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "table_id")
    private Long tableId;

    @Column(name = "primary_key_name")
    private String primaryKeyName;

    @Column(name = "data_api_json")
    private String dataApiJson;

}
