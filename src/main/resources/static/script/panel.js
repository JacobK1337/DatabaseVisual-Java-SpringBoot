
///////////////////////////////////////
const dbId = document.getElementById('databaseId').getAttribute('value');

const variableColorMapping = new Map([
    ['int', 'darkorange'],
    ['varchar', 'darkblue']
]);


class Relation {
    constructor(constraintFieldId, linkedFieldId) {
        this.constraintFieldId = constraintFieldId;
        this.linkedFieldId = linkedFieldId;
    }

    get fieldId() {
        return this.constraintFieldId;
    }

    get linkFieldId() {
        return this.linkedFieldId;
    }
}

$('#new-table--submit').on('click', (e) => {
    e.preventDefault();
    $.ajax({
        url: '/createNewTable',
        type: 'POST',
        data: $('#new-table--form').serialize(),
        success: function () {
            location.reload();
        }
    });
});


window.onload = async () => {
    await drawAllTables(dbId);
    await drawAndSaveForeignKeys(dbId);
}

async function fetchPrimaryKeys() {
    let response = await fetch("/getPrimaryKeys?databaseId=" + dbId);
    return await response.json();
}

async function fetchForeignKeys() {
    let response = await fetch("/getForeignKeys?databaseId=" + dbId);
    return await response.json();
}

async function fetchTableDetails(databaseId) {
    let response = await fetch("/getDatabaseTableDetails?databaseId=" + databaseId);
    return await response.json();
}

async function fetchTableFields(tableId) {

    let response = await fetch("/getTableFields?tableId=" + tableId);
    return await response.json();

}

async function drawAllTables(databaseId) {
    const tableDetails = await fetchTableDetails(databaseId);

    for (let i = 0; i < tableDetails.length; i++) {
        const tableDetailsJson = JSON.parse(tableDetails[i]);

        let tableId = tableDetailsJson['table_id'];
        let tableName = tableDetailsJson['table_name'];
        let pageX = tableDetailsJson['page_x'];
        let pageY = tableDetailsJson['page_y'];
        let color = tableDetailsJson['color'];
        await drawTable(pageX, pageY, tableName, tableId, color);
    }

    await printPrimaryKeys(databaseId);
}

async function printPrimaryKeys(databaseId) {
    const primaryKeys = await fetchPrimaryKeys(databaseId);

    for (let i = 0; i < primaryKeys.length; i++) {
        const currPrimaryKey = JSON.parse(primaryKeys[i]);
        const primaryKeyId = currPrimaryKey['fieldId'];

        const primaryKeyField = document.getElementById('tf' + primaryKeyId);
        const primaryKeyCell = primaryKeyField.insertCell();
        primaryKeyCell.appendChild(document.createTextNode("PRIMARY KEY"));
        primaryKeyCell.style.color = 'gold';

    }

}

let relationList = [];

async function drawAndSaveForeignKeys(databaseId) {
    const foreignKeys = await fetchForeignKeys(databaseId);

    for (let i = 0; i < foreignKeys.length; i++) {

        const currentForeignKeyInfo = JSON.parse(foreignKeys[i][2]);

        const constraintFieldId = foreignKeys[i][0];
        const fieldName = foreignKeys[i][1];
        const referencedFieldId = currentForeignKeyInfo['linkedFieldId'];

        relationList.push(new Relation(constraintFieldId, referencedFieldId));

        const newLine = document.createElementNS('http://www.w3.org/2000/svg', 'line');

        newLine.setAttribute('stroke', 'violet');
        newLine.setAttribute('visibility', 'visible');
        newLine.setAttribute('stroke-width', '7');
        newLine.id = constraintFieldId + '-' + referencedFieldId;

        const newLineLabel = document.createElementNS('http://www.w3.org/2000/svg', 'text');
        newLineLabel.style.fontFamily = "Orbitron, sans-serif";
        newLineLabel.style.fontSize = "40px";
        newLineLabel.style.fill = 'gold';
        newLineLabel.appendChild(document.createTextNode(fieldName));

        newLineLabel.id = 'label--' + newLine.id;

        document.getElementById('relationSvg').appendChild(newLineLabel);
        document.getElementById('relationSvg').appendChild(newLine);
    }

    redrawRelations(relationList);

}

async function drawTable(x, y, name, tabId, color) {
    let newTable = document.createElement('div');

    newTable.classList.add('table');
    newTable.style.top = y + 'px';
    newTable.style.left = x + 'px';
    newTable.style.backgroundColor = color;

    newTable.id = tabId;

    let tableContent = document.createElement('table');
    tableContent.classList.add('table-content');
    let tableNameHeader = document.createElement('header');
    tableNameHeader.textContent = name;
    tableNameHeader.classList.add('table-name-headers');
    tableNameHeader.addEventListener('dblclick', () => {
        alert("Clicked the header!");
    });

    newTable.appendChild(tableNameHeader);

    const tableFields = await fetchTableFields(tabId);

    for (let i = 0; i < tableFields.length; i++) {
        const currentTableFieldInfo = JSON.parse(tableFields[i]);

        const fieldName = currentTableFieldInfo['name'];
        const variableType = currentTableFieldInfo['type'];

        const fieldRow = tableContent.insertRow();

        const typeCell = fieldRow.insertCell();
        typeCell.appendChild(document.createTextNode(variableType));
        typeCell.style.color = 'darkorange';

        const fieldNameCell = fieldRow.insertCell();
        fieldNameCell.appendChild(document.createTextNode(fieldName));

        const tableFieldElement = document.createElement('p');
        const tableFieldVariable = document.createElement('span');


        tableFieldVariable.style.color = variableColorMapping.get(variableType);

        tableFieldVariable.appendChild(document.createTextNode(variableType + ": "));
        tableFieldElement.appendChild(tableFieldVariable);

        tableFieldElement.appendChild(document.createTextNode(fieldName));

        fieldRow.id = 'tf' + currentTableFieldInfo['field_id'];

        tableContent.appendChild(fieldRow);
    }

    newTable.appendChild(tableContent);
    let configButton = initConfigButton(tabId);
    let deleteButton = initDeleteButton(tabId);

    newTable.appendChild(configButton);

    newTable.appendChild(deleteButton);


    document.getElementById('usable-panel-area').appendChild(newTable);

    const currentTable = $(newTable);
    currentTable.appendTo("body");
    currentTable.draggable({
        containment: '#usable-panel-area',
        drag: () => {
            redrawRelations(relationList);
        },
        stop: (event, ui) => {
            let tableId = event.target.id;
            let newTop = event.target.style.top;
            let newLeft = event.target.style.left;
            saveTableLocation(tableId, newTop, newLeft);
        }

    });

}

function initConfigButton(tableId) {

    let configButton = document.createElement('button');
    configButton.textContent = 'Manage table';
    configButton.classList.toggle('config-table--button');
    configButton.addEventListener('click', () => {
        window.open('http://localhost:8080/manage_data?tableId=' + tableId + "&databaseId=" + dbId, 'Manage data window');
    });

    return configButton;
}

function initDeleteButton(tableId) {

    let deleteButton = document.createElement('button');
    deleteButton.textContent = 'Delete table';
    deleteButton.classList.toggle('delete-table--button');

    deleteButton.addEventListener('click', () => {

        $.ajax({
            type: 'post',
            url: '/deleteTable',
            data: {
                'tableId': tableId,
                'databaseId': dbId
            },
            success: () => {
                location.reload();
            }
        });
    });

    return deleteButton;
}

function saveTableLocation(tableId, top, left) {

    const newTop = top.substring(0, top.length - 2);
    const newLeft = left.substring(0, left.length - 2);

    $.ajax({
        type: 'post',
        url: '/updatePlacement',
        data: {
            'tableId': tableId,
            'pageX': newLeft,
            'pageY': newTop
        },

    });

}

function redrawRelations(relList) {

    for (let i = 0; i < relList.length; i++) {
        const constraintFieldId = relList[i].fieldId;
        const linkedFieldId = relList[i].linkFieldId;
        const fieldOne = $('#tf' + constraintFieldId).parents('.table')
        const fieldTwo = $('#tf' + linkedFieldId).parents('.table');

        const currentLine = document.getElementById(constraintFieldId + '-' + linkedFieldId);
        const currentLineLabel = document.getElementById('label--' + currentLine.id);
        const fOx1 = fieldOne.offset().left + fieldOne.width() / 2;
        const fOy1 = fieldOne.offset().top - fieldOne.height();

        const fOx2 = fieldTwo.offset().left + fieldTwo.width() / 2;
        const fOy2 = fieldTwo.offset().top - fieldTwo.height();

        $(currentLine)
            .attr('x1', fOx1)
            .attr('y1', fOy1)
            .attr('x2', fOx2)
            .attr('y2', fOy2);

        $(currentLineLabel)
            .attr('x', (fOx1 + fOx2) / 2)
            .attr('y', (fOy1 + fOy2) / 2);

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