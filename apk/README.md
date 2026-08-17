# Gebaute Staende

Versionierte APKs, damit ein aelterer Stand greifbar bleibt. Das erzeugte
Verteilungspaket samt Installationsanleitung baut `prepare-distribution.sh`
davon getrennt nach `distribution/`.

| Datei | Stand | Aenderung |
|---|---|---|
| `LoRaGpsSender-v1.1.apk` | 17.08.2026 | Kanal 18 statt 24 |

## v1.1 -- Kanal 18 statt 24

`REG2` stand auf `0x18`. Nach der Formel der 900er Reihe ist das
`850.125 + 24 = 874.125 MHz`; das Krisennetz liegt aber auf Kanal 18 =
**868.125 MHz**. Die App konfigurierte das Modul also auf eine Frequenz, auf
der niemand hoert.

Die uebrige Konfiguration passt zum am 17.08.2026 ausgemessenen Netz:

| Register | Wert | Bedeutung |
|---|---|---|
| ADDH/ADDL | `FF FF` | Broadcast -- als Empfaenger ungefiltert, als Sender von keiner Gegenstelle gefiltert |
| NETID | `00` | muss netzweit gleich sein |
| REG0 | `0x62` | 9600 8N1, Luftrate 2 = **SF11/BW500** |
| REG2 | `0x12` | Kanal 18 = 868.125 MHz |
| REG3 | `0x80` | RSSI-Byte an, transparent, kein Fixpunkt, kein Repeater |

**Die Luftratentabelle von Ebyte ist nominal.** Die gaengige Uebersetzung nach
BW125 ist falsch -- gemessen ist die Leiter durchgehend **BW500**. Ein
Empfaenger auf BW125 hoert nichts.

**Rahmen baut das Modul selbst.** Die App schickt ASCII ueber die serielle
Schnittstelle; im Transparentmodus verpackt das Modul das in seinen 8-Byte-
Rahmen mit Pruefsumme, NETID, Adresse und XOR-Weissung. Das Gateway loest ihn
auf und veroeffentlicht Absender und Klartext auf MQTT `lora/raw`.

Gebaut mit Gradle 8.14.3 und JDK 17, `compileSdk 34`, `minSdk 24`.
