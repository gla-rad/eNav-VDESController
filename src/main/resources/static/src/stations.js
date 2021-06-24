/**
 * The Stations Table Column Definitions
 * @type {Array}
 */
var columnDefs = [{
    data: "id",
    title: "ID",
    type: "hidden",
    visible: false,
    searchable: false
},{
    data: "geometry",
    title: "Geometry",
    type: "hidden",
    visible: false,
    searchable: false
}, {
    data: "name",
    title: "Name",
    hoverMsg: "Name of the station",
    required: true,
    placeholder: "Name of the station"
}, {
    data: "ipAddress",
    title: "IP Address",
    hoverMsg: "IP Address of the station",
    placeholder: "IP Address of the station"
}, {
    data: "type",
    title: "Type",
    type: "select",
    options: ["VDES_1000","GNU_RADIO"],
    hoverMsg: "Type of the stations",
    placeholder: "Type of the station"
}, {
    data: "channel",
    title: "Channel",
    type: "select",
    options: ["A","B"],
    hoverMsg: "AIS Channel of the station",
    placeholder: "AIS Channel of the station"
}, {
    data: "port",
    title: "Port",
    hoverMsg: "Port of the station",
    placeholder: "Port of the station"
}, {
    data: "mmsi",
    title: "MMSI",
    hoverMsg: "MMSI of the station",
    placeholder: "MMSI of the station"
}];

$(document).ready( function () {
    table = $('#stations_table').DataTable({
        ajax: {
            "type": "GET",
            "url": "/api/stations",
            "dataType": "json",
            "cache": false,
            "dataSrc": function (json) {
                return json;
            },
            error: function (jqXHR, ajaxOptions, thrownError) {
                console.error(thrownError);
            }
        },
        columns: columnDefs,
        dom: 'Bfrltip',
        select: 'single',
        lengthMenu: [10, 25, 50, 75, 100],
        responsive: true,
        altEditor: true, // Enable altEditor
        buttons: [{
            text: '<i class="fas fa-plus-circle"></i>',
            titleAttr: 'Add Station',
            name: 'add' // do not change name
        }, {
            extend: 'selected', // Bind to Selected row
            text: '<i class="fas fa-edit"></i>',
            titleAttr: 'Edit Station',
            name: 'edit' // do not change name
        }, {
            extend: 'selected', // Bind to Selected row
            text: '<i class="fas fa-trash-alt"></i>',
            titleAttr: 'Delete Station',
            name: 'delete' // do not change name
        }],
        onAddRow: function (datatable, rowdata, success, error) {
            $.ajax({
                url: '/api/stations',
                type: 'POST',
                contentType: 'application/json; charset=utf-8',
                dataType: 'json',
                data: JSON.stringify({
                    id: rowdata["id"],
                    name: rowdata["name"],
                    ipAddress: rowdata["ipAddress"],
                    type: rowdata["type"],
                    channel: rowdata["channel"],
                    port: rowdata["port"],
                    mmsi: rowdata["mmsi"],
                    geometry: null,
                    piSeqNo: 1
                }),
                success: success,
                error: error
            });
        },
        onDeleteRow: function (datatable, rowdata, success, error) {
            $.ajax({
                url: `/api/stations/${rowdata["id"]}`,
                type: 'DELETE',
                contentType: 'application/json; charset=utf-8',
                dataType: 'json',
                data: JSON.stringify(rowdata),
                success: success,
                error: error
            });
        },
        onEditRow: function (datatable, rowdata, success, error) {
            $.ajax({
                url: `/api/stations/${rowdata["id"]}`,
                type: 'PUT',
                contentType: 'application/json; charset=utf-8',
                dataType: 'json',
                data: JSON.stringify({
                    id: rowdata["id"],
                    name: rowdata["name"],
                    ipAddress: rowdata["ipAddress"],
                    type: rowdata["type"],
                    channel: rowdata["channel"],
                    port: rowdata["port"],
                    mmsi: rowdata["mmsi"],
                    geometry: null,
                    piSeqNo: 1
                }),
                success: success,
                error: error
            });
        }
    });
} );