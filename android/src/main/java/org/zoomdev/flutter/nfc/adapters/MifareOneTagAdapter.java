package org.zoomdev.flutter.nfc.adapters;

import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.TagTechnology;

import org.zoomdev.flutter.nfc.AbsNfcTagAdapter;
import org.zoomdev.flutter.nfc.NfcTagAdapter;

import java.io.IOException;

public class MifareOneTagAdapter extends AbsNfcTagAdapter<MifareClassic> {

    private MifareClassic tagTechnology;

    public MifareOneTagAdapter(MifareClassic tagTechnology) {
        super(tagTechnology);
        this.tagTechnology = tagTechnology;
    }


    @Override
    public String getTech() {
        return "MifareClassic";
    }

    @Override
    public MifareClassic getTag() {
        return tagTechnology;
    }

    public static class Factory implements TagAdapterFactory<MifareOneTagAdapter> {

        @Override
        public MifareOneTagAdapter createTagAdapter(Tag tag) {
            MifareClassic tagTechnology = MifareClassic.get(tag);
            if (tagTechnology == null) return null;
            return new MifareOneTagAdapter(tagTechnology);
        }

        @Override
        public Class<? extends TagTechnology> getTagTechnology() {
            return MifareClassic.class;
        }

    }

}
