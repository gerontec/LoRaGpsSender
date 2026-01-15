# LoRa GPS Sender - Installationsanleitung

## Einfache Installation auf Android

### Schritt 1: APK auf das Handy übertragen

**Option A - Per USB-Kabel:**
1. Handy per USB an PC anschließen
2. Datei `LoRaGpsSender.apk` auf das Handy kopieren (z.B. in Downloads-Ordner)

**Option B - Per Email/Cloud:**
1. APK-Datei per Email verschicken oder in Cloud (Google Drive, Dropbox) hochladen
2. Auf dem Handy Email öffnen oder Datei aus Cloud herunterladen

**Option C - Per QR-Code:**
1. APK auf Webserver hochladen
2. QR-Code mit Link zur APK erstellen
3. Mit Handy-Kamera QR-Code scannen

### Schritt 2: Installation erlauben

Beim ersten Mal Installation einer APK von außerhalb des Play Stores:

1. Android fragt: **"Aus dieser Quelle installieren?"**
2. Auf **"Einstellungen"** tippen
3. **"Aus dieser Quelle zulassen"** aktivieren
4. Zurück zur Installation

### Schritt 3: App installieren

1. APK-Datei auf dem Handy öffnen (z.B. über Datei-Manager oder Downloads)
2. Auf **"Installieren"** tippen
3. Warten bis Installation abgeschlossen ist
4. Auf **"Öffnen"** tippen

### Schritt 4: Berechtigungen erteilen

Die App fragt automatisch nach folgenden Berechtigungen:

#### GPS-Berechtigung (für Notfall-Funktion):
- Beim ersten Start erscheint Dialog
- Auf **"Zulassen"** tippen
- GPS auf dem Handy aktivieren (in Android-Einstellungen)

#### USB-Berechtigung (für LoRa-Adapter):
- CH341 USB-Adapter per OTG-Kabel anschließen
- Android zeigt automatisch Dialog: **"USB-Gerät verwenden?"**
- **"OK"** oder **"Zulassen"** tippen
- Häkchen bei **"Standardmäßig für dieses USB-Gerät verwenden"** setzen
- Ab jetzt verbindet sich die App automatisch beim Anschließen!

## Voraussetzungen

✅ **Android-Version:** Mindestens Android 7.0 (API 24)
✅ **USB OTG:** Handy muss USB-Host-Modus unterstützen (die meisten modernen Handys)
✅ **Hardware:** USB-OTG-Kabel oder USB-C auf USB-A Adapter
✅ **LoRa-Adapter:** CH341 USB-Serial-Adapter (VID: 0x1A86, PID: 0x7523)

## Funktionen

### 1. LoRa-Konfiguration
- Dropdown-Menü: `netid00` oder `netid10` auswählen
- Button **"Send"** drücken
- Sendet Raw-Konfiguration an LoRa-Modul

### 2. Kurznachricht senden
- Text eingeben (max. 100 Zeichen)
- Button **"Senden"** drücken
- Nachricht wird als ASCII über LoRa gesendet

### 3. Notfall-GPS (roter Button)
- GPS muss aktiviert sein
- Roten Button **"NOTFALL GPS"** drücken
- Sendet aktuelle GPS-Position im Format:
  ```
  EMERGENCY LAT:50.123456 LON:8.654321
  ```

## Troubleshooting

### "App wurde nicht installiert"
- Alte Version deinstallieren
- Neustart des Handys
- Erneut versuchen

### "USB-Gerät nicht erkannt"
- USB-OTG-Unterstützung prüfen (z.B. mit "USB OTG Checker" App)
- Anderes OTG-Kabel testen
- CH341-Treiber auf dem Adapter überprüfen

### "Keine GPS-Position verfügbar"
- GPS in Android-Einstellungen aktivieren
- Im Freien testen (GPS-Empfang)
- Warten bis GPS-Fix (kann 30-60 Sekunden dauern)

### "Nicht verbunden"
- USB-Adapter anschließen
- USB-Berechtigung erteilt? (Dialog beim Anschließen)
- App neu starten

## Support

Bei Problemen:
1. Log-Ausgabe im unteren Bereich der App prüfen
2. Screenshot des Logs machen
3. Fehlermeldung notieren

---

**Version:** 1.0
**Entwickler:** Gerontec
**Lizenz:** Open Source
