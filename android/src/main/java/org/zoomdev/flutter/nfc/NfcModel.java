package org.zoomdev.flutter.nfc;


import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Build;
import android.os.Parcelable;

import org.zoomdev.flutter.nfc.adapters.IsoDepTagAdapter;


public class NfcModel {


    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    private String[][] techLists;
    private IntentFilter[] filters;
    private NfcAdapterListener adapterListener;

    private NfcTagAdapter adapter;

    NfcTagAdapter.TagAdapterFactory[] factories;


    public static NfcTagAdapter.TagAdapterFactory createByName(String className) throws ClassNotFoundException {

        if ("android.nfc.tech.IsoDep".equals(className)) {
            return new IsoDepTagAdapter.Factory();
        }

        throw new ClassNotFoundException("Class name " + className + " is not supported!");
    }


    public <T extends NfcTagAdapter> T getAdapter() {
        return (T) adapter;
    }


    public NfcModel(Activity activity, NfcTagAdapter.TagAdapterFactory... factories) {
        if (Build.VERSION.SDK_INT > 10) {
            this.factories = factories;
            this.techLists = new String[factories.length][];
            for (int i = 0; i < factories.length; ++i) {
                this.techLists[i] = new String[]{factories[i].getTagTechnology().getName()};
            }
            try {
                this.filters = new IntentFilter[]{new IntentFilter("android.nfc.action.TECH_DISCOVERED", "*/*")};
            } catch (IntentFilter.MalformedMimeTypeException e) {
                throw new RuntimeException(e);
            }

            this.nfcAdapter = NfcAdapter.getDefaultAdapter(activity);
            this.pendingIntent = PendingIntent.getActivity(activity, 0,
                    (new Intent(activity, activity.getClass())), 0);
            this.onNewIntent(activity.getIntent());
        }
    }


    private Intent intent;

    public Intent getIntent() {
        return intent;
    }

    public boolean onNewIntent(Intent intent) {
        try {
            Parcelable p = intent.getParcelableExtra("android.nfc.extra.TAG");
            if (p != null && p instanceof Tag) {
                this.intent = intent;
                return this.load(p);

            }
            return false;
        } catch (Throwable e) {
            e.printStackTrace();
            return false;
        }
    }


    public NfcAdapterListener getAdapterListener() {
        return this.adapterListener;
    }

    public void setAdapterListener(NfcAdapterListener adapterListener) {
        this.adapterListener = adapterListener;
    }

    public boolean load(Parcelable parcelable) {
        Tag tag = (Tag) parcelable;
        //make sure ths last nfc is closed
        this.close();


        if (this.adapterListener != null) {
            for (NfcTagAdapter.TagAdapterFactory factory : factories) {
                if ((adapter = factory.createTagAdapter(tag)) != null) {
                    this.adapterListener.onNfcAdapter(adapter);
                    return true;
                }
            }
        }
        return false;
    }

    public void close() {
        if (this.adapter != null) {
            try {
                this.adapter.close();
            } catch (Throwable e) {

            }
            this.adapter = null;
        }
    }

    public void onResume(Activity context) {
        if (this.nfcAdapter != null) {
            this.nfcAdapter.enableForegroundDispatch(context, this.pendingIntent, this.filters, this.techLists);
        }

    }

    public void onPause(Activity context) {
        if (this.nfcAdapter != null) {
            this.nfcAdapter.disableForegroundDispatch(context);
        }

    }

    public void destroy() {
        close();
        this.intent = null;
        this.nfcAdapter = null;
    }


}
