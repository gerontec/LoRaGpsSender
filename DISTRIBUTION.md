# APK-Verteilung für End-User

## APK vorbereiten

Nach dem Build die APK umbenennen für bessere Erkennbarkeit:

```bash
cd ~/android-studio/LoRaGpsSender
cp app/build/outputs/apk/debug/app-debug.apk ./LoRaGpsSender-v1.0.apk
```

## Verteilungsmethoden

### 1. Per USB-Kabel (einfachste Methode)

```bash
# APK direkt auf Handy kopieren via ADB
adb push LoRaGpsSender-v1.0.apk /sdcard/Download/

# Oder manuell:
# - Handy per USB an PC
# - Datei in Download-Ordner kopieren
# - Auf Handy: Downloads-App öffnen → APK antippen
```

### 2. Per Email

```bash
# APK als Email-Anhang versenden
# Empfänger lädt APK herunter und installiert
```

### 3. Per Cloud-Storage

**Google Drive:**
1. APK auf Google Drive hochladen
2. Link teilen (Zugriff: "Jeder mit dem Link")
3. User öffnet Link → Download → Installieren

**Dropbox / OneDrive:** Analog

### 4. Per QR-Code (professionell)

**Option A - Lokaler Webserver:**
```bash
# Python Webserver starten
cd ~/android-studio/LoRaGpsSender
python3 -m http.server 8080

# QR-Code erstellen mit:
# http://YOUR_IP:8080/LoRaGpsSender-v1.0.apk
# User scannt QR-Code → Download → Installieren
```

**Option B - Cloud + QR:**
1. APK auf Cloud hochladen
2. QR-Code generieren (z.B. auf qr-code-generator.com)
3. QR-Code ausdrucken oder als Bild teilen
4. User scannt → Download → Installieren

### 5. Per Firmen-MDM (für Unternehmen)

Falls Sie ein Mobile Device Management System haben:
- APK über MDM verteilen
- Automatische Installation auf allen Firmengeräten möglich

## Signierte Release-APK erstellen (Optional)

Für professionelle Verteilung eine signierte Release-APK erstellen:

### Schritt 1: Keystore erstellen

```bash
keytool -genkey -v -keystore loragps-release.keystore \
  -alias loragps -keyalg RSA -keysize 2048 -validity 10000
```

Eingaben:
- Passwort: (sicher merken!)
- Name, Organisation, etc. eingeben

### Schritt 2: Release-APK bauen

`app/build.gradle` ergänzen:

```gradle
android {
    signingConfigs {
        release {
            storeFile file("../loragps-release.keystore")
            storePassword "YOUR_PASSWORD"
            keyAlias "loragps"
            keyPassword "YOUR_PASSWORD"
        }
    }

    buildTypes {
        release {
            signingConfig signingConfigs.release
            minifyEnabled false
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
    }
}
```

Dann bauen:

```bash
./gradlew assembleRelease
```

Signierte APK: `app/build/outputs/apk/release/app-release.apk`

## Empfohlene Methode für Ihre Nutzer

**Am einfachsten:**

1. **APK auf Cloud hochladen** (Google Drive, Dropbox)
2. **Link teilen** per Email/WhatsApp
3. **Installationsanleitung (INSTALLATION.md) mitschicken**

User-Schritte:
1. Link öffnen
2. APK herunterladen
3. Installation erlauben
4. APK installieren
5. Fertig!

## Checkliste vor Verteilung

✅ APK gebaut und getestet
✅ APK umbenannt (z.B. LoRaGpsSender-v1.0.apk)
✅ INSTALLATION.md bereitgestellt
✅ USB-Berechtigungen funktionieren (getestet)
✅ GPS-Berechtigungen funktionieren (getestet)
✅ Verteilungsweg gewählt
✅ Support-Kontakt bereitgestellt

## Automatische Updates (Zukunft)

Für automatische Updates in Zukunft:
- GitHub Releases nutzen
- Oder eigenen Update-Server einrichten
- Oder App in Play Store veröffentlichen (Gebühr: 25€ einmalig)

## Rechtliches

⚠️ **Hinweis:**
- Bei Verteilung außerhalb Play Store: Nutzer müssen "Installation aus unbekannten Quellen" erlauben
- Das ist normal und sicher, solange die APK von Ihnen stammt
- Alternative: App im Play Store veröffentlichen (dann keine manuelle Installation nötig)
