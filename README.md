# *Database Visual Online* project
### Software imitating database created using Java 17 and Spring Boot 2.6.1.
#### Other used technologies are: *Thymeleaf, Spring Security, Spring Data JPA, mySQL, JavaScript, JQuery, HTML, CSS.*
![image](https://user-images.githubusercontent.com/81765291/155874547-0a482f2a-0626-4cd9-88df-198bbe4add34.png)
# Contents
#### The goal of this project was to create a software, where every user could store and share their data. To illustrate the relations between the created tables in the database, a visualization system has been implemented.
#### The basic principles of relational databases have been introduced. Entered information is checked against defined constraints, such as the uniqueness of the value in the field, or the inability to add null values. Each table must have a primary key that must be defined when it is created. The values of the foreign key fields are checked against the values in the primary keys.
#### The user can smoothly modify his tables by adding, deleting or changing the contents of records. It can interfere with the currently existing field definitions in a similar way. 
#### Visualization of the database was craeted in JavaScript together with JQuery 3.6.0 and JQuery UI 1.13.0. Relations between tables are provided by Scalable Vector Graphics (SVG).
# How it works
#### The entire operation of the project is based on the communication between the user and the server, which continuously downloads information from the API about its database and tables. Updating resources automatically updates the page and introduces significant changes to the visualized database schema.
#### All operations performed on user data required processing of values stored in JSON format, which were stored in the database. 
# Presentation
## Login screen
#### Simple login screen page.
![image](https://user-images.githubusercontent.com/81765291/155875641-4d731215-32db-4a2e-9bed-aefcdd158b58.png)
## Database menu
#### Here user can choose which database to use or create a new one.
![databasemenu](https://user-images.githubusercontent.com/81765291/155875766-1274a65e-3ec1-412f-b62e-155c6d99d43f.gif)
## Database visualization
#### User can drag tables. Relationship lines are responsive and their position is updated as you drag tables. Primary key field names are displayed in the middle of the lines.
![dbvisualization](https://user-images.githubusercontent.com/81765291/155876196-d01c8a92-8882-42f0-9fe2-cf78b76fb791.gif)
#### While creating a table user need to specify the name of the table, also as the name and the type of the primary key.
![dbadding](https://user-images.githubusercontent.com/81765291/155876351-b5dce8b3-c452-42fd-b03e-61574a9fc2d8.gif)

#### You can only delete table if the primary key is not referenced by another tables.
![dbdeleting](https://user-images.githubusercontent.com/81765291/155876443-49357bea-d22b-489a-88c4-916fda670487.gif)

# Table management
User is able to do following things:
- Filter existing data
- Add new data
- Delete existing data
- Update table constraints
- Add new field
- Delete existing field
- Save current data
- Construct table by saved data token

## Adding new field
#### User needs to specify the name, type, default value and define if the field can store null values and if it has to store unique values. If the name is invalid the field won't be created.
![tablefieldadd](https://user-images.githubusercontent.com/81765291/155877746-8973b0f3-fef3-40f2-b122-c111916fc4dc.gif)

## Adding new data
#### User needs to specify all values for existing fields in order to add a new record. Entered data has to meet all conditions specified by defined constraints or it simply won't be inserted. Existing records can be ordered in relation to the indicated field. To get this effect, just click on the selected field. 
![tableDataAddFinal](https://user-images.githubusercontent.com/81765291/155878805-3e6b77ad-93a2-4740-881b-06ad6ad1f333.gif)
## Modifying existing data
#### User can modify existing records. To do so, simply double-click chosen value and then edit it.
![dataModify](https://user-images.githubusercontent.com/81765291/155878948-d19bf264-9923-4b15-8293-c88d50be2e77.gif)
## Field and data deletion
#### User can delete existing data records and fields. In order to delete fields, user has to click control button and then click the field name.
![fieldDataDelete](https://user-images.githubusercontent.com/81765291/155879168-481831c3-b860-4d7b-bea1-19b42bb6186d.gif)

## Sharing and constructing table
#### After saving data, user can share it in JSON format via link. Users can construct the table from the stored values if they know the appropriate token.
### Saving
![SaveDataSmall](https://user-images.githubusercontent.com/81765291/155879753-89d59053-bf3b-4003-b5f1-00805fc35df8.gif)
### Constructing
![dataImport](https://user-images.githubusercontent.com/81765291/155879818-e0df81e5-3773-43a6-8bfe-9155f94ab82d.gif)

## Updating constraints
#### User can set constraints to table fields.
Defined constraints are: 
- Foreign key (relationship line will be drawn in visualization page). 
- Unique
- Not Null
![constr](https://user-images.githubusercontent.com/81765291/155880003-99ae3b30-1aa9-43d9-97cd-134f72d760fb.gif)

## Filters
#### User can filter displayed data by applying selected comparators.

![filter](https://user-images.githubusercontent.com/81765291/155880093-ec1355e3-206d-452c-895b-ec0c2ead7048.gif)

