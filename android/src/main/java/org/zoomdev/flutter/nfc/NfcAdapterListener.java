package org.zoomdev.flutter.nfc;


import org.zoomdev.flutter.nfc.adapters.NfcTagAdapter;

/**
 * This is a higher level of nfc
 */
public interface NfcAdapterListener {
    void onNfcAdapter(NfcTagAdapter tag);
}
