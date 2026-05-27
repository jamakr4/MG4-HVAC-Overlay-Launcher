# MG4 HVAC Overlay Launcher

Dieses Mini-Projekt startet die MG4-Klima-Overlay-Ansicht mit dem oberen Schliessen-Button.

## Was die App macht

Die App ist nur ein schlanker Launcher:

- sie bindet an `com.android.systemui.StartActivityService`
- sie verwendet die Binder-Schnittstelle `com.saicmotor.sdk.external.IPageService`
- sie ruft `openHvac()` auf

Dadurch wird nicht die normale HVAC-App als Vollansicht gestartet, sondern der `SystemUI`-eigene HVAC-Overlay-Pfad.

## Normaler OEM-Flow

Der normale Weg zur Overlay-Ansicht laeuft im MG4 nicht direkt ueber die HVAC-App selbst, sondern ueber `SystemUI`.

### Beteiligte Teile

- Vollansicht HVAC-App:
  - Paket: `com.saicmotor.hmi.hvac`
  - Activity: `com.saicmotor.hmi.hvac.HvacActivity`

- Overlay-Steuerung:
  - Paket: `com.android.systemui`
  - Service: `com.android.systemui.StartActivityService`
  - Binder-Implementierung: `com.android.systemui.PageManagerBinder`

- Overlay-View mit Close-Button:
  - `com.android.systemui.saicmotor.view.HVACPageView`
  - Layout: `layout_hvac_page.xml`
  - oberer Close-/Pickup-Button: `iv_pick_up`

## Wie das Overlay normalerweise entsteht

Der typische Ablauf ist:

1. Ein OEM-Teil in `SystemUI` oder ueber den `PageManager` fordert `openHvac()` an.
2. `PageManagerBinder.openHvac()` setzt den HVAC-Starttyp auf `0` (`HVAC_START_BY_HAND`).
3. `SystemUI` blendet Statusbar und Dock in den HVAC-Modus um.
4. `SystemUI` zeigt die Overlay-Ansicht `HVACPageView`.
5. In dieser View steckt oben der zusaetzliche Schliessen-Button.

Das bedeutet:

- die normale HVAC-App ist nicht selbst fuer diesen Close-Button zustaendig
- der Close-Button gehoert zur `SystemUI`-Overlay-Ansicht

## Unterschied zur normalen HVAC-App

Es gibt zwei verschiedene HVAC-Darstellungen:

- `HvacActivity`
  - volle Klima-App
  - keine `SystemUI`-Overlay-Leiste mit dem oberen Close-Button

- `HVACPageView`
  - `SystemUI`-Overlay
  - mit oberem Close-Button
  - genau diese Ansicht startet dieses Projekt

Zusatzlich existiert noch ein kleines Klima-Info-Dialogfenster in `SystemUI`:

- `HvacControlDialog`

Das ist aber nicht die grosse Overlay-Ansicht mit dem oberen Schliessen-Button.

## Was dieses Projekt konkret nachbaut

Dieses Projekt baut nur den Einstiegspunkt nach:

- App-Icon antippen
- an `StartActivityService` binden
- `openHvac()` aufrufen
- `SystemUI` zeigt das HVAC-Overlay

## Signierung

Damit die APK auf dem MG4 sinnvoll in das vorhandene OEM-Umfeld passt, sollte sie mit denselben Plattform-Keys signiert werden, die du auch schon im anderen Projekt verwendest.

Vorhandene Key-Dateien:

- `/Users/jan/Projekts/MG4-360-Camera-App/tools/platform.pk8`
- `/Users/jan/Projekts/MG4-360-Camera-App/tools/platform.x509.pem`

Das Projekt hier kopiert diese Keys nicht. Stattdessen verweist das Signier-Script direkt auf diesen bestehenden Ordner.

Wenn du spaeter bauen willst, ist der geplante Ablauf:

1. Release-APK erzeugen
2. `apksigner` mit `platform.pk8` und `platform.x509.pem` ausfuehren
3. die signierte APK ins Auto installieren

Das mitgelieferte Script dafuer ist:

- [tools/build_sign_release.sh](/Users/jan/Documents/Codex/2026-05-26/komplett-neues-mini-side-prokect-hier/tools/build_sign_release.sh)

## Wichtiger Hinweis

Das funktioniert nur, wenn das Zielsystem:

- `com.android.systemui.StartActivityService` exportiert
- die Binder-Schnittstelle unveraendert anbietet
- den Bind aus einer Drittanbieter-App nicht per Berechtigung blockiert

Falls der Bind auf einem anderen Softwarestand scheitert, ist das wahrscheinlich kein Fehler in dieser App, sondern eine OEM-/Firmware-Abweichung.
