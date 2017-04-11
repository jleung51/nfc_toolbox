# NFC Toolbox

Android application which provides a basic toolset for NFC functions.

## Usage

Remember to set the list of NFC types supported by the app in the file `app/src/main/res/xml/nfc_tech_filter.xml`. Common NFC types are Ndef, NfcA, NfcB, and NfcC.

## Currently Supported Functions
* Read data from an NDEF, mimetype `text/plain` NFC tag
