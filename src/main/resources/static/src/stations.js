/**
 * Global variables
 */
var stationsTable = undefined;
var stationMessagesTable = undefined;
var stationsMap = undefined;
var drawControl = undefined;
var drawnItems = undefined;
var stompClient = null;

/**
 * The Stations Table Column Definitions
 * @type {Array}
 */
var stationsColumnDefs = [{
    data: "id",
    title: "ID",
    type: "hidden",
    visible: false,
    searchable: false
}, {
    data: "geometry",
    title: "Geometry",
    type: "hidden",
    visible: false,
    searchable: false
}, {
    data: "name",
    title: "Name",
    hoverMsg: "Name of the station",
    placeholder: "Name of the station",
    required: true,
}, {
    data: "ipAddress",
    title: "IP Address",
    hoverMsg: "IP Address of the station",
    placeholder: "IP Address of the station",
    required: true,
}, {
    data: "type",
    title: "Type",
    type: "select",
    options: ["VDES_1000","GNU_RADIO"],
    hoverMsg: "Type of the stations",
    placeholder: "Type of the station",
    required: true
}, {
    data: "channel",
    title: "Channel",
    type: "select",
    options: ["A","B", "NONE", "BOTH"],
    hoverMsg: "AIS Channel preference of the station",
    placeholder: "AIS Channel preference of the station",
    required: true
}, {
    data: "port",
    title: "Port",
    hoverMsg: "Port of the station",
    placeholder: "Port of the station",
    required: true
}, {
     data: "broadcastPort",
     title: "Broadcast Port",
     hoverMsg: "Broadcast port of the station",
     placeholder: "Broadcast port of the station"
 }, {
    data: "fwdIpAddress",
    title: "Forward IP Address",
    hoverMsg: "Forward IP Address for the station messages",
    placeholder: "Forward IP Address for the station messages"
 }, {
    data: "fwdPort",
    title: "Forward Port",
    hoverMsg: "Forward port for the station messages",
    placeholder: "Forward port for the station messages"
}, {
    data: "mmsi",
    title: "MMSI",
    hoverMsg: "MMSI of the station",
    placeholder: "MMSI of the station",
    required: true
}];

/**
 * The Station Nodes Table Column Definitions
 * @type {Array}
 */
var messageColumnDefs = [{
    data: "atonUID",
    title: "Message UID",
    hoverMsg: "The Message UID",
    placeholder: "The Message UID",
    width: "25%"
}, {
    data: "blacklisted",
    title: "Blacklisted",
    hoverMsg: "Whether the message is blacklisted",
    placeholder: "Whether the message is blacklisted",
    width: "15%",
    className: 'dt-body-center',
    render: function ( data, type, row ) {
        return (data ?
            `<i class="fa-solid fa-circle-check" style="color:red"></i>`:
            `<i class="fa-solid fa-circle-xmark" style="color:green"></i>`);
    },
 },{
    data: "content",
    title: "Content",
    type: "textarea",
    hoverMsg: "The Message Content",
    placeholder: "The Message Content",
    width: "60%",
    render: function (data, type, row) {
        return "<textarea style=\"width: 100%; max-height: 300px\" readonly>" + data + "</textarea>";
    }
}];

// Run when the document is ready
$(function () {
    stationsTable = $('#stations_table').DataTable({
        processing: true,
        language: {
            processing: '<i class="fa fa-spinner fa-spin fa-3x fa-fw"></i><span class="sr-only">Loading...</span>',
        },
        serverSide: true,
        ajax: {
            type: "POST",
            url: "./api/stations/dt",
            contentType: "application/json",
            data: function (d) {
                return JSON.stringify(d);
            },
            error: function (jqXHR, ajaxOptions, thrownError) {
                console.error(thrownError);
            }
        },
        columns: stationsColumnDefs,
        dom: "<'row'<'col-lg-3 col-md-4'B><'col-lg-3 col-md-4'l><'col-lg-6 col-md-4'f>><'row'<'col-md-12'rt>><'row'<'col-md-6'i><'col-md-6'p>>",
        select: 'single',
        lengthMenu: [10, 25, 50, 75, 100],
        responsive: true,
        altEditor: true, // Enable altEditor
        buttons: [{
            text: '<i class="fa-solid fa-plus"></i>',
            titleAttr: 'Add Station',
            name: 'add' // do not change name
        }, {
            extend: 'selected', // Bind to Selected row
            text: '<i class="fa-solid fa-pen-to-square"></i>',
            titleAttr: 'Edit Station',
            name: 'edit' // do not change name
        }, {
            extend: 'selected', // Bind to Selected row
            text: '<i class="fa-solid fa-trash"></i>',
            titleAttr: 'Delete Station',
            name: 'delete' // do not change name
        }, {
           extend: 'selected', // Bind to Selected row
           text: '<i class="fa-solid fa-map-location-dot"></i>',
           titleAttr: 'Define Station Area',
           name: 'stationArea', // do not change name
           className: 'station-area-toggle',
           action: (e, dt, node, config) => {
               loadStationGeometry(e, dt, node, config);
           }
        }, {
            extend: 'selected', // Bind to Selected row
            text: '<i class="fa-solid fa-table"></i>',
            titleAttr: 'Station Nodes',
            name: 'stationNodes', // do not change name
            className: 'station-messages-toggle',
            action: (e, dt, node, config) => {
                loadStationMessages(e, dt, node, config);
            }
        }, {
            extend: 'selected', // Bind to Selected row
            text: '<i class="fa-solid fa-terminal"></i>',
            titleAttr: 'Station Console',
            name: 'stationConsole', // do not change name
            className: 'station-console-toggle',
            action: (e, dt, node, config) => {
                disconnectConsole();
            }
        }],
        onAddRow: function (datatable, rowdata, success, error) {
            $.ajax({
                url: './api/stations',
                type: 'POST',
                contentType: 'application/json; charset=utf-8',
                crossDomain: true,
                dataType: 'json',
                data: JSON.stringify({
                    id: rowdata["id"],
                    name: rowdata["name"],
                    ipAddress: rowdata["ipAddress"],
                    type: rowdata["type"],
                    channel: rowdata["channel"],
                    port: rowdata["port"],
                    broadcastPort: rowdata["broadcastPort"],
                    fwdIpAddress: rowdata["fwdIpAddress"],
                    fwdPort: rowdata["fwdPort"],
                    mmsi: rowdata["mmsi"],
                    geometry: null
                }),
                success: success,
                error: error
            });
        },
        onDeleteRow: function (datatable, selectedRows, success, error) {
            selectedRows.every(function (rowIdx, tableLoop, rowLoop) {
                $.ajax({
                    url: `./api/stations/${this.data()["id"]}`,
                    type: 'DELETE',
                    crossDomain: true,
                    success: success,
                    error: error
                });
            });
        },
        onEditRow: function (datatable, rowdata, success, error) {
            // The geometry is not read correctly so we need to access it in-direclty
            var idx = stationsTable.cell('.selected', 0).index();
            var data = stationsTable.rows(idx.row).data();
            var geometry = data[0].geometry;
            $.ajax({
                url: `./api/stations/${rowdata["id"]}`,
                type: 'PUT',
                contentType: 'application/json; charset=utf-8',
                crossDomain: true,
                dataType: 'json',
                data: JSON.stringify({
                    id: rowdata["id"],
                    name: rowdata["name"],
                    ipAddress: rowdata["ipAddress"],
                    type: rowdata["type"],
                    channel: rowdata["channel"],
                    port: rowdata["port"],
                    broadcastPort: rowdata["broadcastPort"],
                    fwdIpAddress: rowdata["fwdIpAddress"],
                    fwdPort: rowdata["fwdPort"],
                    mmsi: rowdata["mmsi"],
                    geometry: geometry
                }),
                success: success,
                error: error
            });
        }
    });

    // We also need to link the station areas toggle button with the the modal
    // panel so that by clicking the button the panel pops up. It's easier done
    // with jQuery.
    stationsTable.buttons('.station-area-toggle')
        .nodes()
        .attr({ "data-bs-toggle": "modal", "data-bs-target": "#stationAreasPanel" });

    // We also need to link the station nodes toggle button with the the modal
    // side panel so that by clicking the button the panel pops up. It's easier
    // done with jQuery.
    stationsTable.buttons('.station-messages-toggle')
        .nodes()
        .attr({ "data-bs-toggle": "modal", "data-bs-target": "#stationMessagesPanel" });

    // We also need to link the station console toggle button with the the modal
    // panel so that by clicking the button the panel pops up. It's easier done
    // with jQuery.
    stationsTable.buttons('.station-console-toggle')
        .nodes()
        .attr({ "data-bs-toggle": "modal", "data-bs-target": "#stationConsolePanel" });

    // Now also initialise the station map before we need it
    stationsMap = L.map('stationMap').setView([54.910, -3.432], 5);
    L.tileLayer('http://{s}.tile.osm.org/{z}/{x}/{y}.png', {
        attribution: '&copy; <a href="http://osm.org/copyright">OpenStreetMap</a> contributors'
    }).addTo(stationsMap);

    // FeatureGroup is to store editable layers
    drawnItems = new L.FeatureGroup();
    stationsMap.addLayer(drawnItems);

    // Add the draw toolbar
    drawControl = new L.Control.Draw({
        draw: {
            marker: false,
            polyline: false,
            polygon: true,
            rectangle: true,
            circle: false,
            circlemarker: false,
        },
        edit: {
            featureGroup: drawnItems,
            remove: true
        }
    });

    stationsMap.on('draw:created', function (e) {
        var type = e.layerType;
        var layer = e.layer;

        // Do whatever else you need to. (save to db, add to map etc)
        drawnItems.addLayer(layer);
    });

    // Invalidate the map size on show to fix the presentation
    $('#stationAreasPanel').on('shown.bs.modal', function() {
        setTimeout(function() {
            stationsMap.invalidateSize();
        }, 10);
    });
});

/**
 * This function will load the station geometry onto the drawnItems variable
 * so that it is shown in the station maps layers.
 *
 * @param {Event}         event         The event that took place
 * @param {DataTable}     table         The AtoN type table
 * @param {Node}          button        The button node that was pressed
 * @param {Configuration} config        The table configuration
 */
function loadStationGeometry(event, table, button, config) {
    var idx = table.cell('.selected', 0).index();
    var data = table.rows(idx.row).data();
    var geometry = data[0].geometry;

    // Refresh the stations map control
    stationsMap.removeControl(drawControl);
    stationsMap.addControl(drawControl);

    // Recreate the drawn items feature group
    drawnItems.clearLayers();
    if(geometry) {
        var geomLayer = L.geoJson(geometry);
        addNonGroupLayers(geomLayer, drawnItems);
        stationsMap.setView(geomLayer.getBounds().getCenter(), 5);
    }
}

// Would benefit from https://github.com/Leaflet/Leaflet/issues/4461
function addNonGroupLayers(sourceLayer, targetGroup) {
    if (sourceLayer instanceof L.LayerGroup) {
        sourceLayer.eachLayer(function(layer) {
            addNonGroupLayers(layer, targetGroup);
        });
    } else {
        targetGroup.addLayer(sourceLayer);
    }
}

/**
 * This function will initialise the station_messages_table DOM element and
 * loads the station messages applicable for the provided row's station ID.
 *
 * @param {Event}         event         The event that took place
 * @param {DataTable}     table         The stations table
 * @param {Node}          button        The button node that was pressed
 * @param {Configuration} config        The table configuration
 */
function loadStationMessages(event, table, button, config) {
    var idx = table.cell('.selected', 0).index();
    var data = table.rows(idx.row).data();
    var stationId = data[0].id;

    // Destroy the table if it already exists
    if (stationMessagesTable) {
        stationMessagesTable.destroy();
        stationMessagesTable = undefined;
    }

    // And re-initialise it
    stationMessagesTable = $('#station_messages_table').DataTable({
        ajax: {
            type: "GET",
            url: `./api/stations/${stationId}/messages`,
            crossDomain: true,
            dataType: "json",
            cache: false,
            dataSrc: function (json) {
                return json;
            },
            error: function (jqXHR, ajaxOptions, thrownError) {
                console.error(thrownError);
            }
        },
        columns: messageColumnDefs,
        dom: "<'row'<'col-lg-2 col-md-2'B><'col-lg-2 col-md-2'l><'col-lg-8 col-md-8'f>><'row'<'col-md-12'rt>><'row'<'col-md-6'i><'col-md-6'p>>",
        select: 'single',
        lengthMenu: [10, 25, 50, 75, 100],
        responsive: true,
        altEditor: true, // Enable altEditor
        buttons: [{
           extend: 'selected', // Bind to Selected row
           text: '<i class="fa-solid fa-circle-play"></i>',
           titleAttr: 'Whitelist Message',
           name: 'whitelist', // do not change name
           action: (e, dt, node, config) => {
              toggleBlacklistUid(e, dt, node, config, false);
           }
       }, {
          extend: 'selected', // Bind to Selected row
          text: '<i class="fa-solid fa-circle-stop"></i>',
          titleAttr: 'Blacklist Message',
          name: 'blacklist', // do not change name
          action: (e, dt, node, config) => {
             toggleBlacklistUid(e, dt, node, config, true);
          }
       }],
       createdRow: (row, data, dataIndex) => {
           if( data["blacklisted"] ){
               $(row).addClass('table-danger');
           }
       }
    });
    stationMessagesTable.columns.adjust();
}

/**
 * This function performs the blacklisting of the selected message UIDs.
 *
 * @param {Event}         event         The event that took place
 * @param {DataTable}     table         The stations table
 * @param {Node}          button        The button node that was pressed
 * @param {Configuration} config        The table configuration
 */
function toggleBlacklistUid(event, table, button, config, blacklist) {
    // Get the selected message UID
    let idx = table.cell('.selected', 0).index();
    let data = table.rows(idx.row).data();
    let atonUID = data[0].atonUID;

    // Get the selected station ID
    let stationIdx = stationsTable.cell('.selected', 0).index();
    let stationData = stationsTable.rows(stationIdx.row).data();
    let stationId = stationData[0].id;

    // And call the API to add the blacklist
    $.ajax({
        url: `./api/stations/${stationId}/messages/${atonUID}/blacklist`,
        type: blacklist ? 'PUT' : 'DELETE',
        contentType: 'application/json; charset=utf-8',
        crossDomain: true,
        success: () => {stationMessagesTable.ajax.reload();},
        error: () => {console.error("error");}
    });
}

/**
 * Saves the station geometry into the selected station entry from the stations
 * table. The current geometry selection is found in the global drawnItems
 * variable. The Leaflet Draw geometry object should first be translated into
 * GeoJSON and then be set as the selected station's geometry.
 */
function saveGeometry() {
    // Get the selected station
    var station = stationsTable.row({selected : true}).data();

    // If a selection has been made
    if(station) {
        // Convert the feature collection to a geometry collection
        station.geometry = {
            type: "GeometryCollection",
            geometries: []
        };
        drawnItems.toGeoJSON().features.forEach(feature => {
            station.geometry.geometries.push(feature.geometry);
        });

        $.ajax({
            url: `./api/stations/${station.id}`,
            type: 'PUT',
            contentType: 'application/json; charset=utf-8',
            dataType: 'json',
            data: JSON.stringify(station),
            success: () => {console.log("success")},
            error: () => {console.error("error")}
        });
    }
}

/**
 * Connects the station console popup dialog textarea with the output from
 * the respective web-socket.
 */
function connectConsole() {
    // Get the selected station
    var station = stationsTable.row({selected : true}).data();

    // If a selection has been made
    if(station) {
        // Do we need to open a new web-socket?
        if(stompClient == null) {
            var socket = new SockJS('/vdes-ctrl-websocket');
            stompClient = Stomp.over(socket);
            stompClient.connect({}, function (frame) {
                stompClient.subscribe('/topic/messages/' + station.ipAddress + ':' + station.broadcastPort, function (msg) {
                    $('#stationConsoleTextArea').val($('#stationConsoleTextArea').val() + msg.body);
                });
            });
        }

        // Now show only the disconnect button
        $('#consoleConnectButton').hide();
        $('#consoleDisconnectButton').show();
    }
}
/**
 * This function disconnects the station console web-socket and clears out the
 * currently displayed text area.
 */
function disconnectConsole() {
    // Do we have an open web-socket connection?
    if (stompClient !== null) {
        // Try to remove all the previous subscriptions
        for (const sub in stompClient.subscriptions) {
            if (this.stompClient.subscriptions.hasOwnProperty(sub)) {
                this.stompClient.unsubscribe(sub);
            }
        }
        stompClient = null;
        console.log("Disconnected");
    }

    // Clear the text area
    $('#stationConsoleTextArea').val("");

    // Now show only the connect button
    $('#consoleDisconnectButton').hide();
    $('#consoleConnectButton').show();
}
