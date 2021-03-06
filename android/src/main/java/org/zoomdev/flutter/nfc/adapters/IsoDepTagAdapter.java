package org.zoomdev.flutter.nfc.adapters;


import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.nfc.tech.TagTechnology;

public class IsoDepTagAdapter extends AbsNfcTagAdapter<IsoDep> {


    @Override
    public String getTech() {
        return "IsoDep";
    }

    @Override
    public IsoDep getTag() {
        return isoDep;
    }

    public static class Factory implements TagAdapterFactory<IsoDepTagAdapter> {

        @Override
        public IsoDepTagAdapter createTagAdapter(Tag tag) {
            IsoDep tagTechnology = IsoDep.get(tag);
            if (tagTechnology == null) return null;
            return new IsoDepTagAdapter(tagTechnology);
        }

        @Override
        public Class<? extends TagTechnology> getTagTechnology() {
            return IsoDep.class;
        }

    }

    private final IsoDep isoDep;

    public IsoDepTagAdapter(IsoDep isoDep) {
        super(isoDep);
        this.isoDep = isoDep;
    }



}
