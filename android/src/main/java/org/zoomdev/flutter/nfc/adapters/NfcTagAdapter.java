package org.zoomdev.flutter.nfc.adapters;

import android.nfc.Tag;
import android.nfc.tech.TagTechnology;

import java.io.IOException;

public interface NfcTagAdapter<BasicTagTechnology> {



    boolean isConnected();

    void connect() throws IOException;

    void close();


    interface TagAdapterFactory<T extends NfcTagAdapter> {
        T createTagAdapter(Tag tag);

        Class<? extends TagTechnology> getTagTechnology();

    }

    /// Tag class
    String getTech();

    BasicTagTechnology getTag();

}
