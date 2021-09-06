/**
 * Global variables
 */
var stationsTable = undefined;
var stationsNodesTable = undefined;
var stationsMap = undefined;
var drawControl = undefined;
var drawnItems = undefined;

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
    options: ["A","B"],
    hoverMsg: "AIS Channel of the station",
    placeholder: "AIS Channel of the station",
    required: true
}, {
    data: "port",
    title: "Port",
    hoverMsg: "Port of the station",
    placeholder: "Port of the station",
    required: true
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
var nodesColumnDefs = [{
    data: "atonUID",
    title: "AtoN UID",
    visible: true,
    hoverMsg: "The Node AtoN UID",
    placeholder: "The Node AtoN UID",
    required: true,
    width: "33%"
}, {
    data: "content",
    title: "Content",
    type: "textarea",
    hoverMsg: "The Node Content",
    placeholder: "The Node Content",
    required: true
}];

// Run when the document is ready
$(document).ready( function () {
    stationsTable = $('#stations_table').DataTable({
        "processing": true,
        "language": {
            processing: '<i class="fa fa-spinner fa-spin fa-3x fa-fw"></i><span class="sr-only">Loading...</span>',
        },
        "serverSide": true,
        ajax: {
            "type": "POST",
            "url": "/api/stations/dt",
            "contentType": "application/json",
            "data": function (d) {
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
        }, {
           extend: 'selected', // Bind to Selected row
           text: '<i class="fas fa-map-marked-alt"></i>',
           titleAttr: 'Define Station Area',
           name: 'stationArea', // do not change name
           className: 'station-area-toggle',
           action: (e, dt, node, config) => {
               loadStationGeometry(e, dt, node, config);
           }
        }, {
            extend: 'selected', // Bind to Selected row
            text: '<i class="fas fa-table"></i>',
            titleAttr: 'Station Nodes',
            name: 'stationNodes', // do not change name
            className: 'station-nodes-toggle',
            action: (e, dt, node, config) => {
                loadStationNodes(e, dt, node, config);
            }
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
                success: success,
                error: error
            });
        },
        onEditRow: function (datatable, rowdata, success, error) {
            // The geometry is not read correctly so we need to access it in-direclty
            var idx = stationsTable.cell('.selected', 0).index();
            var data = stationsTable.rows(idx.row).data();
            var geometry = data[0].geometry;
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
                    geometry: geometry,
                    piSeqNo: 1
                }),
                success: success,
                error: error
            });
        }
    });

    // We also need to link the station areas toggle button with the the modal
    // panel so that by clicking the button the panel pops up. It's easier done with
    // jQuery.
    stationsTable.buttons('.station-area-toggle')
        .nodes()
        .attr({ "data-bs-toggle": "modal", "data-bs-target": "#stationAreasPanel" });

    // We also need to link the station nodes toggle button with the the modal
    // side panel so that by clicking the button the panel pops up. It's easier
    // done with jQuery.
    stationsTable.buttons('.station-nodes-toggle')
        .nodes()
        .attr({ "data-bs-toggle": "modal", "data-bs-target": "#stationNodesPanel" });

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
        addNonGroupLayers(L.geoJson(geometry), drawnItems);
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
 * This function will initialise the station_nodes_table DOM element and loads
 * the station nodes applicable for the provided row's station ID.
 *
 * @param {Event}         event         The event that took place
 * @param {DataTable}     table         The stations table
 * @param {Node}          button        The button node that was pressed
 * @param {Configuration} config        The table configuration
 */
function loadStationNodes(event, table, button, config) {
    var idx = table.cell('.selected', 0).index();
    var data = table.rows(idx.row).data();
    var stationId = data[0].id;

    // Destroy the table if it already exists
    if (stationsNodesTable) {
        stationsNodesTable.destroy();
        stationsNodesTable = undefined;
    }

    // And re-initialise it
    stationsNodesTable = $('#stations_nodes_table').DataTable({
        ajax: {
            "type": "GET",
            "url": `/api/stations/${stationId}/nodes`,
            "dataType": "json",
            "cache": false,
            "dataSrc": function (json) {
                // Place the content inside a textarea to escape the XML
                json.forEach(node => {
                    node["content"] = "<textarea style=\"width: 100%; max-height: 300px\" readonly>"
                     + node["content"]
                     + "</textarea>";
                });
                return json;
            },
            error: function (jqXHR, ajaxOptions, thrownError) {
                console.error(thrownError);
            }
        },
        columns: nodesColumnDefs,
        dom: "<'row'<'col-lg-3 col-md-4'B><'col-lg-2 col-md-4'l><'col-lg-7 col-md-4'f>><'row'<'col-md-12'rt>><'row'<'col-md-6'i><'col-md-6'p>>",
        select: 'single',
        lengthMenu: [10, 25, 50, 75, 100],
        responsive: true,
        altEditor: true, // Enable altEditor
        buttons: [{
            extend: 'selected', // Bind to Selected row
            text: '<i class="fas fa-trash-alt"></i>',
            titleAttr: 'Delete Node',
            name: 'delete' // do not change name
        }],
        onDeleteRow: function (datatable, rowdata, success, error) {
            $.ajax({
                url: `/api/snodes/uid/${rowdata["atonUID"]}`,
                type: 'DELETE',
                success: success,
                error: error
            });
        }
    });
}

/**
 * Saves the station geometry into the selected station entry from the stations
 * table. The current geometry selection is found in the global drawnItems
 * variable. The Leaflet Draw geometry object should first be translated into
 * GeoJSON and then be set as the selected station's geometry.
 */
function saveGeometry() {
    // Get the selected station geometry
    var idx = stationsTable.cell('.selected', 0).index();

    // If a selection has been made
    if(idx) {
        var data = stationsTable.rows(idx.row).data();
        var station = data[0];

        // Convert the feature collection to a geometry collection
        station.geometry = {
            type: "GeometryCollection",
            geometries: []
        };
        drawnItems.toGeoJSON().features.forEach(feature => {
            station.geometry.geometries.push(feature.geometry);
        });

        $.ajax({
            url: `/api/stations/${station.id}`,
            type: 'PUT',
            contentType: 'application/json; charset=utf-8',
            dataType: 'json',
            data: JSON.stringify(station),
            success: () => {console.log("success")},
            error: () => {console.error("error")}
        });
    }
}
