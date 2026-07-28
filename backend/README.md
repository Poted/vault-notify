# Backend — Google Apps Script

Jeden plik (`Code.gs`), wklejany bezpośrednio do edytora Apps Script powiązanego
z docelowym arkuszem. Nie jest to projekt do budowania ani deployowania z CLI —
`clasp` celowo pominięty, bo to jednorazowa konfiguracja.

## Wdrożenie

1. Otwórz (lub utwórz) arkusz Google Sheets, który ma być archiwum wydatków.
2. `Rozszerzenia` → `Apps Script`.
3. Wklej całą zawartość `Code.gs` do edytora (zastąp domyślny `Code.gs`).
4. `Ustawienia projektu` (ikona zębatki) → sekcja `Właściwości skryptu` →
   dodaj właściwość `TOKEN` z losową, długą wartością (np. wygenerowaną
   `openssl rand -hex 32`). To jedyny sekret po stronie backendu — nie trafia
   do kodu źródłowego.
5. `Wdróż` → `Nowe wdrożenie` → typ `Aplikacja internetowa`:
   - `Wykonaj jako`: Ja (twoje konto)
   - `Kto ma dostęp`: Każdy — **to konieczne**, bo aplikacja Android woła ten
     endpoint bez logowania Google. Jedynym zabezpieczeniem jest token, nie
     autoryzacja Google. Traktuj URL wdrożenia i token jak hasło.
6. Skopiuj wygenerowany `URL aplikacji internetowej` — to jest `SHEETS_URL`
   w `local.properties` aplikacji Android. Wartość `TOKEN` z kroku 4 to
   `SHEETS_TOKEN`.
7. Przy każdej zmianie `Code.gs` trzeba utworzyć **nowe wdrożenie** (albo
   zarządzać wersją przez `Zarządzaj wdrożeniami` → edytuj → nowa wersja),
   inaczej Apps Script będzie nadal serwować starą wersję kodu pod tym samym
   URL-em.

## Format żądania

```json
POST <SHEETS_URL>
{
  "token": "...",
  "pozycje": [
    { "czas": 1753701960000, "kwota": 15.48, "zrodlo": "IKO", "surowyTekst": "..." },
    { "czas": 1753701960000, "kwota": null, "zrodlo": "IKO", "surowyTekst": "..." }
  ]
}
```

`kwota: null` trafia do arkusza jako pusta komórka (patrz specyfikacja —
to celowe, sygnalizuje zmianę formatu powiadomień banku).

## Odpowiedź

```json
{ "status": "ok", "zapisano": 2 }
{ "status": "error", "blad": "nieprawidlowy_token" }
```

## Ważna pułapka: Apps Script Web Apps zawsze zwraca HTTP 200

Dopóki `doPost` nie rzuci nieobsłużonego wyjątku, Apps Script odpowiada HTTP
200 niezależnie od tego, czy operacja się powiodła. Błędy (zły token, zły
JSON, zła pozycja) też przychodzą jako HTTP 200 z `status: "error"` w ciele.

**To znaczy, że warstwa Android (WorkManager) musi sprawdzać HTTP 200 ORAZ
`status == "ok"` w ciele odpowiedzi, zanim oznaczy wpisy jako `WYSLANY`.**
Samo sprawdzenie kodu HTTP nie wystarczy. Zaimplementuję to tak w etapie 5 —
zaznaczam to już teraz, bo to różni się od typowego REST API i łatwo to
przeoczyć.

## Sekrety

- Jedyny sekret backendu: właściwość skryptu `TOKEN` (krok 4). Nie ma go w
  `Code.gs` ani w tym repozytorium.
- URL wdrożenia zawiera losowy identyfikator, ale sam w sobie nie jest
  tajny — nie polegaj na jego nieodgadnięciu, token jest właściwym
  zabezpieczeniem.
