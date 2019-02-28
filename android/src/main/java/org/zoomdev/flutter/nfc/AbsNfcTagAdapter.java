package org.zoomdev.flutter.nfc;


import android.nfc.tech.TagTechnology;

import java.io.IOException;

public abstract class AbsNfcTagAdapter<T> implements NfcTagAdapter<T> {
    private final TagTechnology tag;

    public AbsNfcTagAdapter(TagTechnology tag) {
        this.tag = tag;
    }

    public boolean isConnected() {
        return this.tag.isConnected();
    }

    public void connect() throws IOException {
        this.tag.connect();
    }

    public void close() {
        try {
            this.tag.close();
        } catch (Throwable e) {
            ;
        }

    }
}
