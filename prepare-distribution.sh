#!/bin/bash
# LoRa GPS Sender - Distribution Preparation Script

set -e

VERSION="1.0"
APP_NAME="LoRaGpsSender"
OUTPUT_DIR="distribution"

echo "======================================"
echo "  LoRa GPS Sender - Distribution"
echo "  Version: $VERSION"
echo "======================================"
echo ""

# Check if APK exists
if [ ! -f "app/build/outputs/apk/debug/app-debug.apk" ]; then
    echo "❌ APK not found! Building first..."
    echo ""
    ./gradlew assembleDebug
fi

# Create distribution directory
echo "📦 Creating distribution package..."
mkdir -p "$OUTPUT_DIR"

# Copy and rename APK
cp app/build/outputs/apk/debug/app-debug.apk "$OUTPUT_DIR/${APP_NAME}-v${VERSION}.apk"
echo "✅ APK copied: ${APP_NAME}-v${VERSION}.apk"

# Copy installation guide
cp INSTALLATION.md "$OUTPUT_DIR/"
echo "✅ Installation guide copied"

# Create README for distribution
cat > "$OUTPUT_DIR/README.txt" << 'EOF'
╔════════════════════════════════════════════════════════════╗
║           LoRa GPS Sender - Installationspaket            ║
╚════════════════════════════════════════════════════════════╝

Inhalt dieses Pakets:
├── LoRaGpsSender-v1.0.apk    (Android App)
├── INSTALLATION.md            (Detaillierte Anleitung)
└── README.txt                 (Diese Datei)

═══════════════════════════════════════════════════════════
  SCHNELLSTART
═══════════════════════════════════════════════════════════

1. APK auf Android-Gerät übertragen
   → Per USB-Kabel in Download-Ordner kopieren
   → Oder per Email/Cloud senden

2. Auf Android-Gerät:
   → Downloads-App öffnen
   → LoRaGpsSender-v1.0.apk antippen
   → "Aus dieser Quelle installieren" erlauben
   → "Installieren" drücken

3. App starten und Berechtigungen erteilen:
   → GPS-Berechtigung: "Zulassen"
   → USB-Berechtigung: Beim Anschließen des CH341 "OK"

4. Fertig! 🎉

═══════════════════════════════════════════════════════════
  VORAUSSETZUNGEN
═══════════════════════════════════════════════════════════

✓ Android 7.0 oder höher
✓ USB OTG Support (bei modernen Handys vorhanden)
✓ USB-OTG-Kabel oder USB-C Adapter
✓ CH341 USB-Serial-Adapter für LoRa-Modul

═══════════════════════════════════════════════════════════
  FUNKTIONEN
═══════════════════════════════════════════════════════════

🔧 LoRa-Konfiguration
   → netid00 oder netid10 auswählen und senden

📝 Kurznachricht
   → Text eingeben und über LoRa verschicken

🆘 Notfall-GPS (roter Button)
   → Sendet GPS-Position für Notfälle

═══════════════════════════════════════════════════════════

Ausführliche Anleitung: Siehe INSTALLATION.md

Support: gh@gerontec.de
Version: 1.0
Lizenz: Open Source

═══════════════════════════════════════════════════════════
EOF

echo "✅ README created"

# Create ZIP package
if command -v zip &> /dev/null; then
    cd "$OUTPUT_DIR"
    zip -q "${APP_NAME}-v${VERSION}.zip" *.apk *.md *.txt
    cd ..
    echo "✅ ZIP package created: ${APP_NAME}-v${VERSION}.zip"
fi

# Calculate file sizes
APK_SIZE=$(du -h "$OUTPUT_DIR/${APP_NAME}-v${VERSION}.apk" | cut -f1)

echo ""
echo "======================================"
echo "  ✅ Distribution package ready!"
echo "======================================"
echo ""
echo "📂 Location: $OUTPUT_DIR/"
echo "📦 APK: ${APP_NAME}-v${VERSION}.apk ($APK_SIZE)"
echo ""
echo "📤 Verteilungsmöglichkeiten:"
echo "   1. Per USB auf Android-Gerät kopieren"
echo "   2. Per Email als Anhang versenden"
echo "   3. Auf Cloud hochladen (Drive, Dropbox)"
echo "   4. Per QR-Code teilen"
echo ""
echo "📖 Anleitung für User: $OUTPUT_DIR/INSTALLATION.md"
echo ""

# Show QR code information
echo "📱 QR-Code Download verfügbar:"
echo "   🔗 https://heissa.de/web1/app-debug.pkg"
echo ""
echo "   QR-Code wird automatisch in INSTALLATION.md und README.md angezeigt!"
echo "   User können direkt mit Handy-Kamera scannen und installieren."
echo ""

# Optional: Generate QR code if qrencode is available
if command -v qrencode &> /dev/null; then
    echo "💡 Lokalen QR-Code generieren:"
    qrencode -o "$OUTPUT_DIR/qr-code.png" "https://heissa.de/web1/app-debug.pkg" 2>/dev/null && \
    echo "   ✅ QR-Code erstellt: $OUTPUT_DIR/qr-code.png" || \
    echo "   ℹ️  qrencode -o qr-code.png 'https://heissa.de/web1/app-debug.pkg'"
    echo ""
fi

# Show next steps
echo "═══════════════════════════════════════"
echo "  NÄCHSTE SCHRITTE"
echo "═══════════════════════════════════════"
echo ""
echo "Option A - Direktverteilung:"
echo "  $ cp $OUTPUT_DIR/${APP_NAME}-v${VERSION}.apk /path/to/phone/"
echo ""
echo "Option B - Cloud-Upload:"
echo "  → Datei auf Google Drive/Dropbox hochladen"
echo "  → Link teilen"
echo ""
echo "Option C - Email:"
echo "  → APK + INSTALLATION.md als Anhang"
echo ""

exit 0
