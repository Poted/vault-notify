/**
 * vault-notify backend — Google Apps Script Web App.
 *
 * Receives batches of parsed bank-notification amounts from the Android app
 * and appends them to the bound spreadsheet in one setValues() call.
 *
 * Setup: see backend/README.md for deployment steps.
 */

var SHEET_NAME = 'Wydatki';
var HEADERS = ['Data', 'Kwota', 'Źródło', 'Surowy tekst'];

function doPost(e) {
  var lock = LockService.getScriptLock();
  try {
    lock.waitLock(30000);
  } catch (lockError) {
    return jsonResponse({ status: 'error', blad: 'zajety_zasob' });
  }

  try {
    var body = parseBody(e);
    if (body === null) {
      return jsonResponse({ status: 'error', blad: 'nieprawidlowy_json' });
    }

    var expectedToken = PropertiesService.getScriptProperties().getProperty('TOKEN');
    if (!expectedToken || body.token !== expectedToken) {
      return jsonResponse({ status: 'error', blad: 'nieprawidlowy_token' });
    }

    var pozycje = body.pozycje;
    if (!Array.isArray(pozycje)) {
      return jsonResponse({ status: 'error', blad: 'brak_pozycji' });
    }
    if (pozycje.length === 0) {
      return jsonResponse({ status: 'ok', zapisano: 0 });
    }

    var rows = [];
    for (var i = 0; i < pozycje.length; i++) {
      var pozycja = pozycje[i];
      if (!pozycja || typeof pozycja.czas !== 'number' || typeof pozycja.zrodlo !== 'string') {
        return jsonResponse({ status: 'error', blad: 'nieprawidlowa_pozycja', indeks: i });
      }
      rows.push([
        new Date(pozycja.czas),
        pozycja.kwota === null || pozycja.kwota === undefined ? '' : pozycja.kwota,
        pozycja.zrodlo,
        typeof pozycja.surowyTekst === 'string' ? pozycja.surowyTekst : ''
      ]);
    }

    var sheet = getOrCreateSheet();
    var startRow = sheet.getLastRow() + 1;
    sheet.getRange(startRow, 1, rows.length, HEADERS.length).setValues(rows);

    return jsonResponse({ status: 'ok', zapisano: rows.length });
  } catch (err) {
    return jsonResponse({ status: 'error', blad: 'wyjatek', komunikat: String(err) });
  } finally {
    lock.releaseLock();
  }
}

function parseBody(e) {
  try {
    if (!e || !e.postData || !e.postData.contents) return null;
    var parsed = JSON.parse(e.postData.contents);
    if (typeof parsed !== 'object' || parsed === null) return null;
    return parsed;
  } catch (parseError) {
    return null;
  }
}

function getOrCreateSheet() {
  var ss = SpreadsheetApp.getActiveSpreadsheet();
  var sheet = ss.getSheetByName(SHEET_NAME);
  if (!sheet) {
    sheet = ss.insertSheet(SHEET_NAME);
  }
  if (sheet.getLastRow() === 0) {
    sheet.getRange(1, 1, 1, HEADERS.length).setValues([HEADERS]);
  }
  return sheet;
}

function jsonResponse(payload) {
  return ContentService
    .createTextOutput(JSON.stringify(payload))
    .setMimeType(ContentService.MimeType.JSON);
}
