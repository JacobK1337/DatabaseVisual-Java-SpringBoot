package pl.base.tabledata;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import pl.base.datashareproject.DataSoftProjectApplication;
import pl.base.entities.TableData;
import pl.base.repositories.TableDataRepo;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ContextConfiguration(classes = DataSoftProjectApplication.class)
@DataJpaTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@AutoConfigureTestDatabase(replace= AutoConfigureTestDatabase.Replace.NONE)
class TableDataRepoTest {

    @Autowired
    private TableDataRepo testRepo;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    void findByTableId() {

        //Test size

        //given

        TableData testDataOne = new TableData(
                1L,
                1L,
                "{\"testKey\": \"testValue1\"}"
        );

        TableData testDataTwo = new TableData(
                2L,
                2L,
                "{\"testKey\": \"testValue2\"}"
        );

        TableData testDataThree = new TableData(
                3L,
                1L,
                "{\"testKey\": \"testValue3\"}"
        );
        testRepo.save(testDataOne);
        testRepo.save(testDataTwo);
        testRepo.save(testDataThree);


        //when
        Long tableIdToSearch = 1l;
        List<TableData> listByTableId = testRepo.findByTableId(tableIdToSearch);

        //expect
        int expectedSize = 2;
        assertEquals(expectedSize, listByTableId.size());

        //Test content

        //when
        long matchingContentCount =
                listByTableId.stream()
                        .filter(tableData -> tableData.getDataId() == 1 || tableData.getDataId() == 3)
                        .count();


        assertEquals(2, matchingContentCount);
    }

    @Test
    void findByDataId() {

        //given
        TableData testDataOne = new TableData(
                1L,
                1L,
                "{\"testKey\": \"testValue1\"}"
        );

        TableData testDataTwo = new TableData(
                2L,
                2L,
                "{\"testKey\": \"testValue2\"}"
        );

        TableData testDataThree = new TableData(
                3L,
                1L,
                "{\"testKey\": \"testValue3\"}"
        );
        testRepo.save(testDataOne);
        testRepo.save(testDataTwo);
        testRepo.save(testDataThree);

        //when
        Long dataIdToSearch = 2L;
        TableData toSearch = testRepo.findByDataId(dataIdToSearch);

        //expected
        boolean expected = toSearch.getFieldJsonValue().equals("{\"testKey\": \"testValue2\"}");

        assertTrue(expected);
    }

    @Test
    void updateJsonValueByDataId() {
        //given
        TableData testDataOne = new TableData(
                1L,
                1L,
                "{\"testKey\": \"testValue1\"}"
        );
        testRepo.save(testDataOne);

        //when
        testRepo.updateJsonValueByDataId(testDataOne.getDataId(), "$.testKey", "TestValueNew");
        entityManager.clear();

        //expected
        TableData justAdded = testRepo.findByDataId(testDataOne.getDataId());

        boolean expected = justAdded
                .getFieldJsonValue()
                .equals("{\"testKey\": \"TestValueNew\"}");

        assertTrue(expected);

    }

    @Test
    void eraseJsonFieldByKey() {

        //given
        TableData testDataOne = new TableData(
                1L,
                1L,
                "{\"testKey\": \"testValue1\", \"testKey2\": \"toBeDeleted\"}"
        );

        TableData testDataTwo = new TableData(
                2L,
                2L,
                "{\"testKey\": \"testValue2\", \"testKey2\": \"toBeDeleted\"}"
        );

        TableData testDataThree = new TableData(
                3L,
                1L,
                "{\"testKey\": \"testValue3\", \"testKey2\": \"toBeDeleted\"}"
        );

        testRepo.save(testDataOne);
        testRepo.save(testDataTwo);
        testRepo.save(testDataThree);

        //given
        entityManager.clear();
        testRepo.eraseJsonFieldByKey("$.testKey2", 1L);

        //expected
        boolean dataOneHasNoTestKey2 = testRepo
                .findByDataId(1L)
                .getFieldJsonValue()
                .equals("{\"testKey\": \"testValue1\"}");

        boolean dataThreeHasNoTestKey2 = testRepo
                .findByDataId(3L)
                .getFieldJsonValue()
                .equals("{\"testKey\": \"testValue3\"}");

        boolean dataTwoHasTestKey2 = testRepo
                .findByDataId(2L)
                .getFieldJsonValue()
                .equals("{\"testKey\": \"testValue2\", \"testKey2\": \"toBeDeleted\"}");

        assertTrue(dataOneHasNoTestKey2 && dataThreeHasNoTestKey2 && dataTwoHasTestKey2);
    }

    @Test
    void deleteTableDataByDataId() {
        //provided by spring
    }

    @Test
    void deleteTableDatasByTableId() {
        //provided by spring
    }
}