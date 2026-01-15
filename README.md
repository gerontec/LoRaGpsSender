# LoRa GPS Sender

Android-App für LoRa-Kommunikation mit GPS-Notfallfunktion via CH341 USB-Serial-Adapter.

## 📲 Schnellinstallation

### Mit QR-Code:

<div align="center">

![QR-Code Download](https://api.qrserver.com/v1/create-qr-code/?size=400x400&data=https://heissa.de/web1/app-debug.pkg)

**🔗 Download-Link:** https://heissa.de/web1/app-debug.pkg

*Einfach QR-Code mit der Handy-Kamera scannen und installieren!*

</div>

---

## ✨ Funktionen

### 🔧 LoRa-Konfiguration
- **netid00** und **netid10** Konfigurationen
- Raw-Byte-Sequenzen direkt an LoRa-Modul senden

### 📝 Kurznachricht
- Textfeld für Nachrichten (max. 100 Zeichen)
- Sendet als ASCII über LoRa-Adapter

### 🆘 Notfall-GPS
- **Roter Button** für GPS-Notfall
- Sendet Position: `EMERGENCY LAT:xx.xxxxx LON:yy.yyyyy`
- Nur auf Knopfdruck (kein automatisches Tracking)

### 🔌 USB-Serial Support
- Automatische Erkennung des CH341-Adapters
- USB-Berechtigung wird automatisch angefragt
- Funktioniert mit USB-OTG-Kabel

## 📋 Voraussetzungen

- ✅ Android 7.0 oder höher
- ✅ USB OTG Support
- ✅ CH341 USB-Serial-Adapter (VID: 0x1A86, PID: 0x7523)
- ✅ USB-OTG-Kabel oder USB-C Adapter

## 📖 Installation

### Schnellstart (5 Schritte):

1. **QR-Code scannen** (oben) oder [Link öffnen](https://heissa.de/web1/app-debug.pkg)
2. **APK herunterladen**
3. **Installation erlauben** (beim ersten Mal: "Aus dieser Quelle zulassen")
4. **"Installieren" tippen**
5. **Fertig!** App öffnen und USB-Adapter anschließen

### Detaillierte Anleitung

Siehe [INSTALLATION.md](INSTALLATION.md) für ausführliche Installationsanleitung inkl. Troubleshooting.

## 🚀 Erste Schritte

1. **App starten**
2. **GPS-Berechtigung erteilen** (für Notfall-Funktion)
3. **CH341 USB-Adapter anschließen**
4. **USB-Berechtigung erteilen** (einmalig)
5. **Status prüfen:** "Connected to ttyUSB0"
6. **Fertig!** Alle Funktionen verfügbar

## 🎯 Verwendung

### LoRa-Konfiguration senden:
1. Konfiguration auswählen (netid00 oder netid10)
2. "Send" Button drücken
3. Bestätigung im Log prüfen

### Kurznachricht senden:
1. Text in "Kurznachricht" Feld eingeben
2. "Senden" Button drücken
3. Nachricht wird über LoRa versendet

### Notfall-GPS senden:
1. Sicherstellen, dass GPS aktiviert ist
2. Roten "NOTFALL GPS" Button drücken
3. Position wird sofort über LoRa gesendet

## 🔧 Technische Details

- **Sprache:** Kotlin
- **Min SDK:** 24 (Android 7.0)
- **Target SDK:** 34 (Android 14)
- **USB Serial Library:** usb-serial-for-android v3.5.1
- **Baud Rate:** 9600, 8N1
- **Berechtigungen:** USB, GPS (Fine + Coarse Location)

## 📦 Für Entwickler

### Repository klonen:
```bash
git clone https://github.com/gerontec/LoRaGpsSender.git
cd LoRaGpsSender
```

### App bauen:
```bash
./gradlew assembleDebug
```

### Distribution-Paket erstellen:
```bash
./prepare-distribution.sh
```

Siehe [DISTRIBUTION.md](DISTRIBUTION.md) für Details zur APK-Verteilung.

## 📱 Screenshots

```
┌─────────────────────────┐
│ LoRa GPS Sender         │
├─────────────────────────┤
│ Status: Connected       │
│ Device: CH341 (ttyUSB0) │
│                         │
│ Config: [netid00] [Send]│
│ Nachricht: [___] [Send] │
│                         │
│ ┌─────────────────────┐ │
│ │ Log Output:         │ │
│ │ [12:34] Connected   │ │
│ │ [12:35] TX: netid00 │ │
│ │ [12:36] GPS updated │ │
│ └─────────────────────┘ │
│                         │
│ [🆘 NOTFALL GPS]        │
└─────────────────────────┘
```

## 🐛 Troubleshooting

### USB-Gerät nicht erkannt
- USB-OTG-Unterstützung prüfen
- Anderes OTG-Kabel testen
- App neu starten

### Keine GPS-Position
- GPS in Android-Einstellungen aktivieren
- Im Freien testen (GPS-Empfang)
- 30-60 Sekunden auf GPS-Fix warten

### App stürzt ab
- Berechtigungen prüfen (USB + GPS)
- App-Daten löschen und neu starten
- Logcat prüfen für Details

## 📄 Lizenz

Open Source - Siehe LICENSE-Datei

## 👨‍💻 Entwickler

**Gerontec**
📧 gh@gerontec.de
🌐 https://gerontec.de

## 🙏 Credits

- [usb-serial-for-android](https://github.com/mik3y/usb-serial-for-android) - USB Serial Library

## 📝 Changelog

### Version 1.0 (2026-01-15)
- ✅ Initial Release
- ✅ CH341 USB-Serial Support
- ✅ LoRa-Konfiguration (netid00, netid10)
- ✅ Kurznachricht-Funktion
- ✅ GPS-Notfall-Button
- ✅ Automatische USB-Berechtigungen
- ✅ Real-time Logging

---

**Download:** [QR-Code scannen](#-schnellinstallation) oder https://heissa.de/web1/app-debug.pkg
