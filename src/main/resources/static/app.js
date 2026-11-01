// ===== FloodMonitor Webclient (jQuery) =====
const API = '/api/v1';
let refreshTimer = null;
let pendingRequests = 0;

// ---------- Hilfsfunktionen ----------

/** Accept-Header je nach gewähltem Format */
function acceptHeader() {
    return $('#format').val() === 'xml' ? 'application/xml' : 'application/json';
}

/** GET-Request an die API. Antwort wird als Text geholt und selbst ausgewertet (JSON oder XML). */
function apiGet(path, params) {
    // leere Parameter weglassen
    const clean = {};
    $.each(params || {}, (k, v) => { if (v !== '' && v !== null && v !== undefined) clean[k] = v; });

    pendingRequests++;
    $('#loading').prop('hidden', false);
    return $.ajax({
        url: API + path,
        data: clean,
        headers: { Accept: acceptHeader() },
        dataType: 'text'
    }).always(() => {
        pendingRequests--;
        if (pendingRequests === 0) $('#loading').prop('hidden', true);
    });
}

/** true, wenn die Antwort XML ist (laut Content-Type) */
function isXml(xhr) {
    return (xhr.getResponseHeader('Content-Type') || '').includes('xml');
}

/** Text eines Kind-Elements in XML lesen */
function txt(el, tag) {
    const node = el.getElementsByTagName(tag)[0];
    return node ? node.textContent : null;
}

function num(v) {
    return v === null || v === undefined || v === '' ? null : Number(v);
}

function formatTime(iso) {
    return iso ? new Date(iso).toLocaleString('de-AT') : '–';
}

/** datetime-local ("2026-11-01T18:00") -> ISO-8601 in UTC ("2026-11-01T17:00:00.000Z") */
function toIso(localValue) {
    return localValue ? new Date(localValue).toISOString() : '';
}

function badge(level) {
    return $('<span>').addClass('badge ' + (level || 'UNKNOWN')).text(level || 'UNKNOWN');
}

function showRaw(text) {
    $('#raw').text(text);
}

// ---------- JSON/XML -> gleiche JS-Objekte ----------

function parseStations(text, xhr) {
    if (isXml(xhr)) {
        const doc = $.parseXML(text);
        return Array.from(doc.getElementsByTagName('station')).map(el => ({
            id: txt(el, 'id'),
            name: txt(el, 'name'),
            river: txt(el, 'river'),
            status: txt(el, 'status'),
            warningLevel: txt(el, 'warningLevel'),
            isActive: txt(el, 'isActive') === 'true'
        }));
    }
    return JSON.parse(text).stations;
}

function measurementFromXml(el) {
    return {
        stationId: txt(el, 'stationId'),
        timestamp: txt(el, 'timestamp'),
        waterLevel: num(txt(el, 'waterLevel')),
        flowRate: num(txt(el, 'flowRate')),
        rainfall: num(txt(el, 'rainfall')),
        temperature: num(txt(el, 'temperature')),
        batteryLevel: num(txt(el, 'batteryLevel')),
        status: txt(el, 'status'),
        warningLevel: txt(el, 'warningLevel')
    };
}

function parseMeasurement(text, xhr) {
    return isXml(xhr) ? measurementFromXml($.parseXML(text).documentElement) : JSON.parse(text);
}

function parseMeasurements(text, xhr) {
    if (isXml(xhr)) {
        const doc = $.parseXML(text);
        return Array.from(doc.getElementsByTagName('measurement')).map(measurementFromXml);
    }
    return JSON.parse(text).measurements;
}

function parseAlerts(text, xhr) {
    if (isXml(xhr)) {
        const doc = $.parseXML(text);
        return Array.from(doc.getElementsByTagName('alert')).map(el => ({
            stationId: txt(el, 'stationId'),
            warningLevel: txt(el, 'warningLevel'),
            message: txt(el, 'message'),
            timestamp: txt(el, 'timestamp')
        }));
    }
    return JSON.parse(text).alerts;
}

// ---------- Fehleranzeige ----------

function errorMessage(xhr) {
    if (xhr.status === 0) {
        return 'Server nicht erreichbar. Läuft die Anwendung?';
    }
    let message = null;
    try {
        if (isXml(xhr)) {
            message = txt($.parseXML(xhr.responseText).documentElement, 'message');
        } else {
            message = JSON.parse(xhr.responseText).message;
        }
    } catch (e) {
        // Antwort war kein gültiges JSON/XML
    }
    return 'Fehler ' + xhr.status + ': ' + (message || xhr.statusText);
}

function showError(xhr) {
    $('#error').text(errorMessage(xhr)).prop('hidden', false);
}

// ---------- Daten laden und anzeigen ----------

function loadStations() {
    return apiGet('/stations').done((text, _s, xhr) => {
        const stations = parseStations(text, xhr);
        const select = $('#station');
        const selected = select.val();
        select.empty();
        stations.forEach(s => {
            select.append($('<option>').val(s.id)
                .text(s.id + ' – ' + s.name + ' (' + s.river + ') – ' + s.warningLevel));
        });
        if (selected) select.val(selected);
    }).fail(showError);
}

function loadLatest(stationId) {
    return apiGet('/stations/' + stationId + '/measurements/latest').done((text, _s, xhr) => {
        const m = parseMeasurement(text, xhr);
        const tiles = [
            [m.waterLevel + ' cm', 'Wasserstand'],
            [m.flowRate + ' m³/s', 'Durchfluss'],
            [m.rainfall + ' mm/h', 'Niederschlag'],
            [m.temperature + ' °C', 'Temperatur'],
            [m.batteryLevel + ' %', 'Akku'],
            [m.status, 'Status'],
            [formatTime(m.timestamp), 'Zeitpunkt']
        ];
        const box = $('#latest').empty();
        tiles.forEach(([value, label]) => {
            box.append($('<div class="tile">')
                .append($('<div class="value">').text(value))
                .append($('<div class="label">').text(label)));
        });
        box.append($('<div class="tile">')
            .append($('<div class="value">').append(badge(m.warningLevel)))
            .append($('<div class="label">').text('Warnstufe')));
    }).fail(xhr => {
        if (xhr.status === 404) {
            // z. B. ST-005 (OFFLINE) hat keine Messungen -> kein globaler Fehler
            $('#latest').empty().text(errorMessage(xhr));
        } else {
            showError(xhr);
        }
    });
}

function loadHistory(stationId) {
    const params = {
        from: toIso($('#from').val()),
        to: toIso($('#to').val()),
        warningLevel: $('#warningLevel').val(),
        limit: $('#limit').val()
    };
    return apiGet('/stations/' + stationId + '/measurements', params).done((text, _s, xhr) => {
        showRaw(text);
        const list = parseMeasurements(text, xhr);
        const tbody = $('#history').empty();
        if (list.length === 0) {
            tbody.append($('<tr>').append($('<td colspan="8">').text('Keine Messwerte für diese Filter.')));
            return;
        }
        list.forEach(m => {
            tbody.append($('<tr>')
                .append($('<td>').text(m.stationId))
                .append($('<td>').text(formatTime(m.timestamp)))
                .append($('<td>').text(m.waterLevel))
                .append($('<td>').text(m.flowRate))
                .append($('<td>').text(m.rainfall))
                .append($('<td>').text(m.batteryLevel))
                .append($('<td>').text(m.status))
                .append($('<td>').append(badge(m.warningLevel))));
        });
    }).fail(showError);
}

function loadAlerts() {
    return apiGet('/alerts').done((text, _s, xhr) => {
        const alerts = parseAlerts(text, xhr);
        const ul = $('#alerts').empty();
        if (alerts.length === 0) {
            ul.append($('<li>').text('Keine aktuellen Warnungen ✅'));
            return;
        }
        alerts.forEach(a => {
            ul.append($('<li>').append(badge(a.warningLevel)).append(' ' + a.message));
        });
    }).fail(showError);
}

/** Alles neu laden */
function refreshAll() {
    $('#error').prop('hidden', true);
    loadStations().always(() => {
        const id = $('#station').val();
        if (id) {
            loadLatest(id);
            loadHistory(id);
        }
        loadAlerts();
        $('#lastUpdate').text('Aktualisiert: ' + new Date().toLocaleTimeString('de-AT'));
    });
}

/** Auto-Refresh starten/stoppen */
function setupAutoRefresh() {
    clearInterval(refreshTimer);
    if ($('#autoRefresh').is(':checked')) {
        refreshTimer = setInterval(refreshAll, Number($('#interval').val()));
    }
}

// ---------- Events ----------

$(function () {
    $('#refreshBtn').on('click', refreshAll);
    $('#applyBtn').on('click', refreshAll);
    $('#resetBtn').on('click', () => {
        $('#from, #to').val('');
        $('#warningLevel').val('');
        $('#limit').val(50);
        refreshAll();
    });
    $('#station, #format').on('change', refreshAll);
    $('#autoRefresh, #interval').on('change', setupAutoRefresh);

    refreshAll();
    setupAutoRefresh();
});