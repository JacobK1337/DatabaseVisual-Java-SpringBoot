const tableId = document.getElementById('tableId').value;
const dbId = document.getElementById('databaseId').value;

const definedConstraints = new Map([
    [1, 'foreignkey'],
    [2, 'notnull'],
    [3, 'unique']
]);


$("#import-data-submit").on('click', (e) => {
    e.preventDefault();
    $.ajax({
        type: 'POST',
        url: '/importData',
        data: $("#import-data--form").serialize(),
        success: function () {
            alert("Table successfully imported!")
            reloadAllContents(tableId, dbId);
        }
    });
});

$("#new-field-submit").on('click', (e) => {
    e.preventDefault();
    $.ajax({
        type: 'POST',
        url: '/addField',
        data: $("#add-field--form").serialize(),
        success: function () {
            reloadAllContents(tableId, dbId);
        }
    });
});
$("#save-data--submit").on('click', (e) => {
    e.preventDefault();
    $.ajax({
        type: 'POST',
        url: '/saveJsonData',
        data: {
            'jsonData': JSON.stringify(loadedData),
            'tableId': tableId
        },
        success: function () {
            alert("Data successfully saved! You can now share it in the 'Share' tab!")
            reloadAllContents(tableId, dbId);
        }

    })
});

$("#modify-style--submit").on('click', (e) => {
    e.preventDefault();
    $.ajax({
        type: 'POST',
        url: '/updateStyle',
        data: $("#modify-style--form").serialize(),
        success: function () {
            alert("Style has been modified successfully!");
        }
    });
});

let controlPressed = false;

document.addEventListener('keydown', (e) => {
    if (e.keyCode === 17) {
        controlPressed = true;
    }

});

document.addEventListener('keyup', (e) => {
    if (e.keyCode === 17) {
        controlPressed = false;
    }

});

window.onload = async () => {
    reloadAllContents(tableId, dbId);
}

async function fetchTableFields(tableId) {

    let response = await fetch("/getTableFields?tableId=" + tableId);
    return await response.json();

}

async function fetchTableData(tableId) {

    let response = await fetch("/getTableData?tableId=" + tableId);
    return await response.json();

}

async function fetchTableDetails(tableId) {

    let response = await fetch("/getTableDetails?tableId=" + tableId);
    return await response.json();

}

async function fetchPrimaryKeys(databaseId) {
    let response = await fetch("/getPrimaryKeys?databaseId=" + databaseId);
    return await response.json();
}

async function fetchForeignKeys(databaseId) {
    let response = await fetch("/getForeignKeys?databaseId=" + databaseId);
    return await response.json();
}

async function fetchTableSavedData(tableId) {

    let response = await fetch("/getTableSavedData?tableId=" + tableId);
    return await response.json();

}

function reloadAllContents(tableId, databaseId) {
    reloadFilterMenu(tableId);
    reloadStructureMenu(tableId, databaseId);
    reloadAddDataMenu(tableId);
    reloadData(tableId);
    reloadSavedDataMenu(tableId);
    reloadTableDetailsInfo(tableId);
}

async function reloadTableDetailsInfo(tableId) {
    const tableDetails = await fetchTableDetails(tableId);
}

async function reloadStructureMenu(tableId, databaseId) {
    const tableFields = await fetchTableFields(tableId);
    const primaryKeys = await fetchPrimaryKeys(databaseId);
    const foreignKeys = await fetchForeignKeys(databaseId);

    loadStructureMenu(tableId, tableFields, primaryKeys, foreignKeys);
}

let loadedData;

async function reloadData(tableId) {

    loadedData = [];
    const freshData = await fetchTableData(tableId);
    const tableFields = await fetchTableFields(tableId);

    for (let i = 0; i < freshData.length; i++)
        loadedData.push(JSON.parse(freshData[i][1]));

    loadData(freshData, tableId, tableFields);
}

async function reloadDataFiltered(tableId, filteredData) {
    const tableFields = await fetchTableFields(tableId);

    loadData(filteredData, tableId, tableFields);
}

async function reloadFilterMenu(tableId) {
    const tableFields = await fetchTableFields(tableId);

    loadFilterMenu(tableId, tableFields);
}

async function reloadAddDataMenu(tableId) {
    const tableFields = await fetchTableFields(tableId);
    loadAddDataMenu(tableId, tableFields);
}

async function reloadSavedDataMenu(tableId) {
    const userSavedData = await fetchTableSavedData(tableId);

    loadUserSavedData(userSavedData, tableId);
}

function loadStructureMenu(tableId, tableFields, primaryKeys, foreignKeys) {
    const fieldConstrForm = document.getElementById('field-constr--form');

    const numOfDefConstraints = definedConstraints.size;

    clearForm(fieldConstrForm);

    const constrMenuTable = document.createElement('table');
    constrMenuTable.classList.add('constr-table');

    const constrMenuTableHeaders = constrMenuTable.insertRow();

    for (let i = 0; i <= numOfDefConstraints; i++) {
        const headersCell = constrMenuTableHeaders.insertCell();
        let cellTextContent;

        if (i === 0)
            cellTextContent = 'Field name';

        else {
            cellTextContent = definedConstraints.get(i);
        }

        if (cellTextContent === 'foreignkey')
            constrMenuTableHeaders.insertCell();

        headersCell.appendChild(document.createTextNode(cellTextContent));
    }

    const foreignKeyMapping = new Map();
    const onDeleteActionMapping = new Map();

    for (let i = 0; i < foreignKeys.length; i++) {
        const currentForeignKeyInfo = JSON.parse(foreignKeys[i][2]);

        const constraintFieldId = parseInt(foreignKeys[i][0]);
        const referencedFieldId = currentForeignKeyInfo['linkedFieldId'];
        const onDeleteAction = currentForeignKeyInfo['ondelete'];

        foreignKeyMapping.set(constraintFieldId, referencedFieldId);
        onDeleteActionMapping.set(constraintFieldId, onDeleteAction);
    }

    for (let i = 0; i < tableFields.length; i++) {
        const currentTableFieldInfo = JSON.parse(tableFields[i]);

        const fieldConstrRow = constrMenuTable.insertRow();
        const fieldId = currentTableFieldInfo['field_id'];
        const fieldName = currentTableFieldInfo['name'];
        const isNotNull = currentTableFieldInfo['not_null'];
        const isUnique = currentTableFieldInfo['is_unique'];
        const isPrimaryKey = currentTableFieldInfo['is_primary_key'];

        for (let j = 0; j <= numOfDefConstraints; j++) {
            const fieldConstrCell = fieldConstrRow.insertCell();

            if (j === 0)
                fieldConstrCell.appendChild(document.createTextNode(fieldName));

            else {
                const constrName = definedConstraints.get(j);

                switch (constrName) {
                    case 'unique':
                        const uniqueCheckButton = document.createElement('input');
                        uniqueCheckButton.type = 'checkbox';
                        uniqueCheckButton.id = 'uniq-checkbutton-' + fieldId;
                        uniqueCheckButton.name = 'isUnique' + fieldId;
                        if (isUnique === true)
                            uniqueCheckButton.checked = true;

                        if (isPrimaryKey === true)
                            uniqueCheckButton.disabled = true;

                        fieldConstrCell.appendChild(uniqueCheckButton);
                        break;


                    case 'notnull':
                        const nullCheckButton = document.createElement('input');
                        nullCheckButton.type = 'checkbox';
                        nullCheckButton.id = 'null-checkbutton-' + fieldId;
                        nullCheckButton.name = 'isNotNull' + fieldId;
                        if (isNotNull === true)
                            nullCheckButton.checked = true;

                        if (isPrimaryKey === true)
                            nullCheckButton.disabled = true;
                        fieldConstrCell.appendChild(nullCheckButton);
                        break;


                    case 'foreignkey':
                        const fkSelect = document.createElement('select');
                        fkSelect.id = 'fk-select-' + fieldId;
                        fkSelect.name = 'isForeignKey' + fieldId;
                        const fkDefaultOption = document.createElement('option');
                        fkDefaultOption.value = 'None';
                        fkDefaultOption.appendChild(document.createTextNode('None'));
                        fkSelect.appendChild(fkDefaultOption);

                        for (let k = 0; k < primaryKeys.length; k++) {
                            const fkOption = document.createElement('option');
                            const primaryKeyDescription = JSON.parse(primaryKeys[k]);

                            const primaryKeyFieldId = primaryKeyDescription['fieldId'].toString();
                            const primaryKeyName = primaryKeyDescription['fieldName'].toString();
                            const primaryKeyTableName = primaryKeyDescription['tableName'].toString();
                            const primaryKeyTableId = primaryKeyDescription['tableId'].toString();

                            fkOption.value = primaryKeyFieldId;

                            fkOption.appendChild(document.createTextNode(primaryKeyName + ' (' + primaryKeyTableName + ')'));
                            if (primaryKeyTableId !== tableId)
                                fkSelect.appendChild(fkOption);

                            if (foreignKeyMapping.get(fieldId) === primaryKeyFieldId)
                                fkOption.selected = true;

                        }

                        fkSelect.onchange = () => {
                            const onDeleteActionSelect = document.getElementById('on-delete-' + fieldId);

                            if (fkSelect.value !== 'None') {
                                onDeleteActionSelect.style.visibility = 'visible';
                            } else
                                onDeleteActionSelect.style.visibility = 'hidden';
                        };
                        fieldConstrCell.appendChild(fkSelect);

                        /////////////////

                        const onDeleteActionCell = fieldConstrRow.insertCell();

                        const onDeleteActionSelect = document.createElement('select');
                        onDeleteActionSelect.id = 'on-delete-' + fieldId;
                        onDeleteActionSelect.name = 'onDelete' + fieldId;
                        if (fkSelect.value === 'None')
                            onDeleteActionSelect.style.visibility = 'hidden';

                        const onDeleteOptionCascade = document.createElement('option');
                        onDeleteOptionCascade.value = 'cascade';
                        onDeleteOptionCascade.textContent = 'Cascade';

                        const onDeleteOptionSetNull = document.createElement('option');
                        onDeleteOptionSetNull.value = 'setnull';
                        onDeleteOptionSetNull.textContent = 'Set null';

                        onDeleteActionSelect.appendChild(onDeleteOptionCascade);
                        onDeleteActionSelect.appendChild(onDeleteOptionSetNull);

                        if (onDeleteActionMapping.get(fieldId) === 'cascade')
                            onDeleteOptionCascade.selected = true;

                        if (onDeleteActionMapping.get(fieldId) === 'setnull')
                            onDeleteOptionSetNull.selected = true;


                        onDeleteActionCell.appendChild(onDeleteActionSelect);

                        break;
                }
            }


        }

    }

    const fieldConstrSubmit = document.createElement('input');

    fieldConstrSubmit.type = 'submit';
    fieldConstrSubmit.value = 'Update table structure';
    fieldConstrSubmit.classList.add("table-config--submit");

    fieldConstrForm.appendChild(constrMenuTable);
    fieldConstrForm.appendChild(fieldConstrSubmit);

    $(fieldConstrSubmit).on('click', (e) => {
        e.preventDefault();
        $.ajax({
            type: 'POST',
            url: '/updateConstraints',
            data: $(fieldConstrForm).serialize(),
            success: function () {
                alert("Constraints saved!");
            }
        });
    });
}

function loadFilterMenu(tableId, tableFields) {

    const getFilteredDataForm = document.getElementById('get-filtered-data--form');

    clearForm(getFilteredDataForm);

    const filterOptionsTable = document.createElement('table');
    filterOptionsTable.classList.add('filter-table');

    const filterColumnNames = new Map([
        [0, 'Column'],
        [1, 'Comparator'],
        [2, 'Filter']
    ]);

    const filterTableHeader = filterOptionsTable.insertRow();

    for (let i = 0; i < 3; i++) {

        const td = filterTableHeader.insertCell();
        const columnName = filterColumnNames.get(i);

        td.appendChild(document.createTextNode(columnName));

    }

    for (let i = 0; i < tableFields.length; i++) {
        const currentTableFieldInfo = JSON.parse(tableFields[i]);

        const tableRow = filterOptionsTable.insertRow();

        const fieldName = tableRow.insertCell();
        fieldName.appendChild(document.createTextNode(currentTableFieldInfo['name']));

        const comparator = tableRow.insertCell();
        const comparatorOptionList = document.createElement('select');
        comparatorOptionList.name = currentTableFieldInfo['field_id'] + 'comparator';

        const comparatorOptionNoFilter = document.createElement('option');
        comparatorOptionNoFilter.value = 'null';
        comparatorOptionNoFilter.text = 'No filter';

        const comparatorOptionEqual = document.createElement('option');
        comparatorOptionEqual.value = '=';
        comparatorOptionEqual.text = 'Is equal';

        const comparatorOptionNotEqual = document.createElement('option');
        comparatorOptionNotEqual.value = '<>';
        comparatorOptionNotEqual.text = 'Is not equal';

        const comparatorOptionSmaller = document.createElement('option');
        comparatorOptionSmaller.value = '<';
        comparatorOptionSmaller.text = 'Is smaller';

        const comparatorOptionGreater = document.createElement('option');
        comparatorOptionGreater.value = '>';
        comparatorOptionGreater.text = 'Is greater';

        comparatorOptionList.appendChild(comparatorOptionNoFilter);
        comparatorOptionList.appendChild(comparatorOptionEqual);
        comparatorOptionList.appendChild(comparatorOptionNotEqual);

        if (currentTableFieldInfo['type'] === 'int') comparatorOptionList.appendChild(comparatorOptionSmaller);
        if (currentTableFieldInfo['type'] === 'int') comparatorOptionList.appendChild(comparatorOptionGreater);

        comparator.appendChild(comparatorOptionList);

        const filter = tableRow.insertCell();
        const filterValue = document.createElement('input');
        filterValue.type = 'text';
        filterValue.name = currentTableFieldInfo['field_id'] + 'filterValue';

        filter.appendChild(filterValue);
    }


    const applyFiltersButton = document.createElement('input');
    applyFiltersButton.type = 'submit';
    applyFiltersButton.classList.add('table-config--submit');
    applyFiltersButton.value = 'Apply filters';

    getFilteredDataForm.appendChild(filterOptionsTable);
    getFilteredDataForm.append(applyFiltersButton);


    $(applyFiltersButton).on('click', (e) => {
        e.preventDefault();
        $.ajax({
            url: '/getFilteredTableData',
            type: 'get',
            data: $(getFilteredDataForm).serialize(),
            success: function (response) {
                const filteredDataJson = JSON.parse(response);
                reloadDataFiltered(tableId, filteredDataJson);

            }
        });
    });
}

function loadAddDataMenu(tableId, tableFields) {
    const addDataForm = document.getElementById('add-data--form');

    clearForm(addDataForm);

    const addDataMenuTable = document.createElement('table');
    addDataMenuTable.style.borderSpacing = '10px';

    for (let i = 0; i < tableFields.length; i++) {
        const currentTableFieldInfo = JSON.parse(tableFields[i]);

        const fieldName = currentTableFieldInfo['name'];

        const tableRow = addDataMenuTable.insertRow();

        const tdFieldName = tableRow.insertCell();
        tdFieldName.appendChild(document.createTextNode(fieldName));

        const tdFieldValue = tableRow.insertCell();
        const fieldValue = document.createElement('input');

        fieldValue.type = 'text';
        fieldValue.name = fieldName;
        tdFieldValue.appendChild(fieldValue);

    }

    const addDataButton = document.createElement('input');

    addDataButton.type = 'submit';
    addDataButton.value = 'Add new data';
    addDataButton.classList.add('table-config--submit');

    addDataForm.appendChild(addDataMenuTable);
    addDataForm.appendChild(addDataButton);

    $(addDataButton).on('click', (e) => {
        e.preventDefault();
        $.ajax({
            url: '/addData',
            type: 'POST',
            data: $(addDataForm).serialize(),
            success: function () {
                reloadData(tableId);
            }
        });
    });
}

function loadUserSavedData(userSavedData, tableId) {
    const deleteSavedDataForm = document.getElementById('delete-saved-data--form');


    clearForm(deleteSavedDataForm);

    const userSavedDataTable = document.createElement('table');
    userSavedDataTable.id = 'user-saved-data-table';

    const headers = [
        'Link',
        'Delete'
    ];

    const headerRow = userSavedDataTable.insertRow();
    for (let i = 0; i < headers.length; i++) {
        const headerCell = headerRow.insertCell();
        headerCell.appendChild(document.createTextNode(headers[i]));
    }

    for (let i = 0; i < userSavedData.length; i++) {
        const savedDataRow = userSavedDataTable.insertRow();
        const dataId = userSavedData[i];
        savedDataRow.id = 'saved-datarow-' + dataId;

        const getSavedDataButton = document.createElement('button');
        getSavedDataButton.textContent = 'Saved data number: ' + dataId;
        getSavedDataButton.classList.add('saved-data-link');
        getSavedDataButton.type = 'button';

        getSavedDataButton.addEventListener('click', () => {
            window.location = 'http://localhost:8080/getSharedData?dataApiId=' + dataId;
        });

        const getSavedDataButtonCell = savedDataRow.insertCell();
        getSavedDataButtonCell.appendChild(getSavedDataButton);

        const deleteSavedDataButton = document.createElement('button');
        deleteSavedDataButton.textContent = 'DELETE';
        deleteSavedDataButton.classList.add('delete-data-button');
        deleteSavedDataButton.type = 'button';
        deleteSavedDataButton.value = dataId;

        const deleteSavedDataButtonCell = savedDataRow.insertCell();
        deleteSavedDataButtonCell.appendChild(deleteSavedDataButton);


        deleteSavedDataButton.addEventListener('click', (src) => {
            applySavedDataForDelete(src, tableId);
        });


        userSavedDataTable.appendChild(savedDataRow);

    }
    deleteSavedDataForm.appendChild(userSavedDataTable);
}

function loadData(freshData, tabId, tableFields) {
    const dataList = document.getElementById("data-list");

    while (dataList.firstChild) {
        dataList.removeChild(dataList.lastChild);
    }


    const newTable = document.createElement('table');
    newTable.classList.add('data-table');

    newTable.id = 'tableDataList';

    const tHeader = newTable.insertRow();

    //init tableHeaders
    for (let i = 0; i < tableFields.length; i++) {
        const currentTableFieldInfo = JSON.parse(tableFields[i]);

        const td = tHeader.insertCell();
        td.style.color = 'orange';

        td.addEventListener('mouseover', () => {
            if (controlPressed && currentTableFieldInfo['is_primary_key'] === false) {
                td.style.backgroundColor = 'red';
            }
        });

        td.addEventListener('mouseout', () => {
            if (!td.classList.contains('to-delete'))
                td.style.backgroundColor = 'transparent';

        });

        td.addEventListener('click', () => {

            if (controlPressed && currentTableFieldInfo['is_primary_key'] === false) {
                const deleteFieldForm = document.getElementById('delete-field--form');
                td.classList.toggle('to-delete');

                if (td.classList.contains('to-delete')) {

                    td.style.backgroundColor = 'red';
                    const fieldName = document.createElement('input');
                    fieldName.type = 'hidden';
                    fieldName.value = currentTableFieldInfo['name'];
                    fieldName.name = currentTableFieldInfo['name'];
                    fieldName.id = 'field-' + currentTableFieldInfo['name'];

                    const deleteFieldSubmit = document.createElement('input');
                    deleteFieldSubmit.type = 'submit';
                    deleteFieldSubmit.value = 'Commit field delete';
                    deleteFieldSubmit.id = 'delete-field--submit';

                    if (!deleteFieldForm.contains(document.querySelector('#delete-field--submit')))
                        deleteFieldForm.appendChild(deleteFieldSubmit);

                    deleteFieldForm.appendChild(fieldName)

                } else {
                    td.style.backgroundColor = 'transparent';
                    deleteFieldForm.removeChild(deleteFieldForm.querySelector('#field-' + currentTableFieldInfo['name']));
                    if (deleteFieldForm.contains(deleteFieldForm.querySelector('#delete-field--submit')) && deleteFieldForm.children.length === 2)
                        deleteFieldForm.removeChild(deleteFieldForm.querySelector('#delete-field--submit'));
                }

                $(deleteFieldForm).on('click', (e) => {
                    e.preventDefault();
                    $.ajax({
                        type: 'POST',
                        url: '/deleteField',
                        data: $(deleteFieldForm).serialize(),
                        success: function () {
                            //we remove the save button after successful saving
                            clearForm(deleteFieldForm);
                            reloadAllContents(tabId, dbId);

                        }
                    });
                });
            } else if (!controlPressed)
                sortTable(i);

        });


        const tableFieldName = document.createTextNode(currentTableFieldInfo['name']);

        if (currentTableFieldInfo['is_primary_key'] === true)
            td.style.color = 'gold';
        td.appendChild(tableFieldName);

    }

    //inserting mockup field
    //representing delete button
    //const mockupTd = tHeader.insertCell();

    for (let i = 0; i < freshData.length; i++) {

        const tRow = newTable.insertRow();
        tRow.id = 'datarow-' + freshData[i][0];

        const currentValues = JSON.parse(freshData[i][1]);

        for (let j = 0; j < tableFields.length; j++) {
            const currentTableFieldInfo = JSON.parse(tableFields[j]);

            const currentField = currentTableFieldInfo['name'];
            const currentValue = currentValues[currentField];

            const td = tRow.insertCell();
            td.classList.add(freshData[i][0]);

            td.addEventListener("dblclick", () => {
                const inputField = document.createElement('input');
                inputField.type = 'text';
                inputField.value = td.textContent;
                td.removeChild(td.lastChild);
                td.appendChild(inputField);
                inputField.focus();
            });

            td.addEventListener("keydown", () => {
                if (event.keyCode === 13) {
                    applyChangesToDataList(tableFields, tabId);
                }
            });

            td.appendChild(document.createTextNode(currentValue));
        }

        const deleteButtonTd = tRow.insertCell();
        const deleteButton = document.createElement('button');

        deleteButton.value = freshData[i][0];
        deleteButton.textContent = 'DELETE';
        deleteButton.classList.add('delete-data-button');

        deleteButton.addEventListener('click', (src) => {
            applyDataForDelete(src, tabId);
        });

        deleteButtonTd.appendChild(deleteButton);
    }

    dataList.appendChild(newTable);
}

function applyChangesToDataList(tableFields, tableId) {
    const table = document.getElementById("tableDataList");

    const modifyDataForm = document.getElementById('modify-data--form');

    clearForm(modifyDataForm);

    let rows = table.rows;

    for (let i = 1; i < rows.length; i++) {
        for (let j = 0; j < rows[i].cells.length - 1; j++) {
            const currentCell = rows[i].cells[j];
            if (currentCell.lastChild.nodeName !== "#text") {
                let text = currentCell.lastElementChild.value;
                let textDoc = document.createTextNode(text);

                currentCell.removeChild(currentCell.lastChild);
                currentCell.appendChild(textDoc);
            }
        }
    }

    let madeChanges = false;

    for (let i = 1; i < rows.length; i++) {
        for (let j = 0; j < rows[i].cells.length - 1; j++) {
            const currentTableFieldInfo = JSON.parse(tableFields[j]);

            const currentCell = rows[i].cells[j];
            const tableFieldName = currentTableFieldInfo['name'];

            if (currentCell.innerText !== loadedData[i - 1][tableFieldName]) {
                const cellValue = document.createElement('input');
                madeChanges = true;

                cellValue.type = 'hidden';
                cellValue.value = currentCell.innerText;
                cellValue.name = currentCell.className + tableFieldName;
                modifyDataForm.appendChild(cellValue);
            }

        }
    }

    if (madeChanges) {
        const saveChangesButton = document.createElement('input');

        saveChangesButton.type = 'submit';
        saveChangesButton.value = 'Save Changes';
        saveChangesButton.classList.add("table-config--submit")
        saveChangesButton.id = 'save-changes-button';

        if (!modifyDataForm.contains(document.querySelector('#save-changes-button')))
            modifyDataForm.appendChild(saveChangesButton);

        $(saveChangesButton).on('click', (e) => {
            e.preventDefault();
            $.ajax({
                type: 'POST',
                url: '/modifyData',
                data: $(modifyDataForm).serialize(),
                success: function () {

                    clearForm(modifyDataForm);
                    reloadData(tableId);

                }
            });
        });
    }


}

function applyDataForDelete(src, tableId) {

    const deleteDataForm = document.getElementById('delete-data--form');
    const sourceTarget = src.target;

    sourceTarget.classList.toggle('delete-button--active');

    const commitDeleteButton = document.createElement('input');

    commitDeleteButton.type = 'submit';
    commitDeleteButton.value = 'Commit data delete';
    commitDeleteButton.id = 'commit-delete--submit';

    const currentRow = document.getElementById('datarow-' + sourceTarget.value);

    if (sourceTarget.classList.contains('delete-button--active')) {

        currentRow.style.backgroundColor = 'red';
        const dataId = document.createElement('input');

        dataId.type = 'hidden';
        dataId.name = 'dataId-' + sourceTarget.value;
        dataId.id = 'dataId-' + sourceTarget.value;
        dataId.value = sourceTarget.value;

        deleteDataForm.appendChild(dataId);

        if (!deleteDataForm.contains(document.querySelector('#commit-delete--submit')))
            deleteDataForm.appendChild(commitDeleteButton);

        $(commitDeleteButton).on('click', (e) => {
            e.preventDefault();
            $.ajax({
                type: 'POST',
                url: 'deleteData',
                data: $(deleteDataForm).serialize(),
                success: function () {

                    clearForm(deleteDataForm);
                    reloadData(tableId);

                }
            });
        });

    } else {
        currentRow.style.backgroundColor = 'transparent';
        document.getElementById('dataId-' + sourceTarget.value).remove();
        if (deleteDataForm.contains(deleteDataForm.querySelector('#commit-delete--submit')) && deleteDataForm.children.length === 3)
            deleteDataForm.removeChild(deleteDataForm.querySelector('#commit-delete--submit'));
    }

}

function applySavedDataForDelete(src, tableId) {
    const deleteSavedDataForm = document.getElementById('delete-saved-data--form');
    const sourceTarget = src.target;

    sourceTarget.classList.toggle('delete-saved-data--active');
    const commitDeleteButton = document.createElement('input');

    commitDeleteButton.type = 'submit';
    commitDeleteButton.value = 'Commit data delete';
    commitDeleteButton.id = 'commit-saved-data-delete--submit';

    const currentRow = document.getElementById('saved-datarow-' + sourceTarget.value);

    if (sourceTarget.classList.contains('delete-saved-data--active')) {

        currentRow.style.backgroundColor = 'red';
        const savedDataId = document.createElement('input');

        savedDataId.type = 'hidden';
        savedDataId.name = 'savedDataId-' + sourceTarget.value;
        savedDataId.id = 'savedDataId-' + sourceTarget.value;
        savedDataId.value = sourceTarget.value;

        deleteSavedDataForm.appendChild(savedDataId);

        if (!deleteSavedDataForm.contains(document.querySelector('#commit-saved-data-delete--submit')))
            deleteSavedDataForm.appendChild(commitDeleteButton);

        $(commitDeleteButton).on('click', (e) => {
            e.preventDefault();
            $.ajax({
                type: 'POST',
                url: '/deleteSavedData',
                data: $(deleteSavedDataForm).serialize(),
                success: function () {
                    reloadSavedDataMenu(tableId);
                }
            });
        });

    } else {
        currentRow.style.backgroundColor = 'transparent';
        document.getElementById('savedDataId-' + sourceTarget.value).remove();
        if (deleteSavedDataForm.contains(deleteSavedDataForm.querySelector('#commit-saved-data-delete--submit')) && deleteSavedDataForm.children.length === 2)
            deleteSavedDataForm.removeChild(deleteSavedDataForm.querySelector('#commit-saved-data-delete--submit'));
    }


}

function sortTable(n) {
    let table, rows, switching, i, x, y, shouldSwitch, dir, switchCount = 0;
    table = document.getElementById("tableDataList");
    switching = true;
    dir = "asc";

    while (switching) {

        switching = false;
        rows = table.rows;

        for (i = 1; i < (rows.length - 1); i++) {
            shouldSwitch = false;

            x = rows[i].getElementsByTagName("TD")[n];
            y = rows[i + 1].getElementsByTagName("TD")[n];

            if (dir === "asc") {
                if (x.innerHTML.toLowerCase() > y.innerHTML.toLowerCase()) {
                    shouldSwitch = true;
                    break;
                }
            } else if (dir === "desc") {
                if (x.innerHTML.toLowerCase() < y.innerHTML.toLowerCase()) {
                    shouldSwitch = true;
                    break;
                }
            }
        }
        if (shouldSwitch) {

            rows[i].parentNode.insertBefore(rows[i + 1], rows[i]);
            switching = true;
            switchCount++;
        } else {
            if (switchCount === 0 && dir === "asc") {
                dir = "desc";
                switching = true;
            }
        }
    }
}

function clearForm(formToClear) {

    while (formToClear.firstChild) {

        if (formToClear.lastElementChild.name !== 'tableId' && formToClear.lastElementChild.name !== 'databaseId')
            formToClear.removeChild(formToClear.lastChild);

        else
            break;

    }

}

document.addEventListener('click', (e) => {
    if (e.target.className === 'table-manage-button'
        || e.target.className === 'data-manage-button'
        || e.target.classList.contains('table-content--active')) {

        const nextElement = e.target.nextElementSibling;
        e.target.classList.toggle('table-content--active');
        if (e.target.classList.contains('table-content--active')) {
            nextElement.style.maxHeight = nextElement.scrollHeight + 'px';

        } else {
            nextElement.style.maxHeight = 0 + 'px';
        }
    }
});
