package org.zoomdev.flutter.nfc;

import android.app.Activity;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.tech.IsoDep;
import android.nfc.tech.MifareClassic;

import org.zoomdev.flutter.nfc.adapters.IsoDepTagAdapter;
import org.zoomdev.flutter.nfc.adapters.MifareOneTagAdapter;
import org.zoomdev.flutter.nfc.adapters.NfcTagAdapter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry;
import io.flutter.plugin.common.PluginRegistry.Registrar;

/**
 * FlutterEasyNfcPlugin
 */
public class FlutterEasyNfcPlugin implements MethodCallHandler, NfcAdapterListener,
        PluginRegistry.NewIntentListener {


    /**
     * Plugin registration.
     */
    public static void registerWith(Registrar registrar) {
        final MethodChannel channel = new MethodChannel(registrar.messenger(), "flutter_easy_nfc");
        FlutterEasyNfcPlugin plugin = new FlutterEasyNfcPlugin(registrar, channel);
        channel.setMethodCallHandler(plugin);

    }

    private NfcModel model;

    private final Activity activity;

    private final MethodChannel channel;

    private final Registrar registrar;

    private NfcTagAdapter tagAdapter;


    public static final String NOT_AVALIABLE = "1";
    private static final String NOT_ENABLED = "2";
    public static final String NOT_INITIALIZED = "3";
    public static final String IO = "4";
    public static final String IN_CORRECT_METHOD = "5";

    public FlutterEasyNfcPlugin(Registrar registrar, MethodChannel channel) {
        this.activity = registrar.activity();
        this.channel = channel;
        this.registrar = registrar;
    }

    void test(byte[] bytes){
    }

    @Override
    public synchronized void onMethodCall(MethodCall call, Result result) {
        String method = call.method;



        if ("transceive".equals(method)) {
            transceive((byte[]) call.arguments, result);
        } else if ("authenticateSectorWithKeyA".equals(method)) {
            Map map = (Map) call.arguments;
            int sectorIndex = (int) map.get("sectorIndex");
            byte[] key = (byte[]) map.get("key");
            authenticateSectorWithKeyA(sectorIndex,key, result);
        } else if ("authenticateSectorWithKeyB".equals(method)) {
            Map map = (Map) call.arguments;
            int sectorIndex = (int) map.get("sectorIndex");
            byte[] key = (byte[]) map.get("key");
            authenticateSectorWithKeyB(sectorIndex,key,result);
        } else if ("readBlock".equals(method)) {
            Map map = (Map) call.arguments;
            int block = (int) map.get("block");
            readBlock(block,result);
        } else if ("writeBlock".equals(method)) {
            Map map = (Map) call.arguments;
            int block = (int) map.get("block");
            byte[] data = (byte[])map.get("data");
            writeBlock(block,data,result);
        } else if ("transfer".equals(method)) {
            Map map = (Map) call.arguments;
            int block = (int) map.get("block");
            transfer(block,result);
        } else if ("restore".equals(method)) {
            Map map = (Map) call.arguments;
            int block = (int) map.get("block");
            restore(block,result);
        } else if ("increment".equals(method)) {
            Map map = (Map) call.arguments;
            int block = (int) map.get("block");
            int value = (int) map.get("value");
            increment(block,value,result);
        } else if ("decrement".equals(method)) {
            Map map = (Map) call.arguments;
            int block = (int) map.get("block");
            int value = (int) map.get("value");
            decrement(block,value,result);
        } else if ("getBlockCount".equals(method)) {
            getBlockCount(result);
        } else if ("getSectorCount".equals(method)) {
            getSectorCount(result);
        } else if ("connect".equals(method)) {
            connect(result);
        } else if ("close".equals(method)) {
            close(result);
        } else if ("isAvailable".equals(method)) {
            result.success(isAvailable());
        } else if ("isEnabled".equals(method)) {
            result.success(isEnabled());
        } else if ("startup".equals(method)) {
            startup(result);
        } else if ("shutdown".equals(method)) {
            shutdown();
            result.success(new HashMap<String, Object>());
        } else if ("resume".equals(method)) {
            resume();
            result.success(new HashMap<String, Object>());
        } else if ("pause".equals(method)) {
            pause();
            result.success(new HashMap<String, Object>());
        } else {
            result.notImplemented();
        }
    }

    abstract class IsoDepNfcExector extends NfcExector<IsoDep> {

        @Override
        IsoDep get(NfcTagAdapter tagAdapter) {
            if (tagAdapter.getTag() instanceof IsoDep) {
                return (IsoDep) tagAdapter.getTag();
            }
            return null;
        }
    }

    abstract class MifareClassicNfcExecutor extends NfcExector<MifareClassic> {
        @Override
        MifareClassic get(NfcTagAdapter tagAdapter) {
            if (tagAdapter.getTag() instanceof MifareClassic) {
                return (MifareClassic) tagAdapter.getTag();
            }
            return null;
        }
    }

    abstract class NfcExector<T> {

        abstract T get(NfcTagAdapter tagAdapter);


        void handle(Result result) {
            if (tagAdapter == null) {
                processError(NOT_INITIALIZED, "", result);
                return;
            }

            T tag = get(tagAdapter);
            if (tag == null) {
                processError(IN_CORRECT_METHOD, "", result);
            } else {
                try {
                    Object data = execute(tag);
                    Map<String,Object> res = new HashMap<>();
                    res.put("data",data);
                    result.success(res);
                } catch (IOException e) {
                    processError(IO, "", result);
                }
            }
        }

        abstract Object execute(T tag) throws IOException;
    }

    private void transceive(final byte[] data, final Result result) {
        new IsoDepNfcExector() {
            @Override
            Object execute(IsoDep isoDep) throws IOException {
                return isoDep.transceive(data);
            }
        }.handle(result);
    }


    public boolean isAvailable() {
        return NfcAdapter.getDefaultAdapter(activity) != null;
    }

    public boolean isEnabled() {
        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(activity);
        if (nfcAdapter == null) {
            return false;
        }
        return nfcAdapter.isEnabled();
    }

    public synchronized void startup(Result promise) {
        shutdown();
        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(activity);
        if (nfcAdapter == null) {
            processError(NOT_AVALIABLE, "", promise);
            return;
        }
        if (!nfcAdapter.isEnabled()) {
            processError(NOT_ENABLED, "", promise);
            return;
        }
        model = new NfcModel(activity, new IsoDepTagAdapter.Factory(), new MifareOneTagAdapter.Factory());
        model.setAdapterListener(this);
        model.onResume(activity);
        model.onNewIntent(activity.getIntent());
        registrar.addNewIntentListener(this);

        promise.success(new HashMap<String, Object>());
    }

    public synchronized void shutdown() {
        if (model != null) {
            model.onPause(activity);
            model.destroy();
            model = null;
        }
    }

    public synchronized void resume() {
        if (model != null) {
            model.onResume(activity);
        }
    }

    public synchronized void pause() {
        if (model != null) {
            model.onPause(activity);
        }
    }

    @Override
    public void onNfcAdapter(NfcTagAdapter tag) {
        tagAdapter = tag;
        Map<String, Object> data = new HashMap<>();
        data.put("tech", tag.getTech());
        channel.invokeMethod("nfc", data);
    }

    public void close(Result promise) {
        if (tagAdapter != null) {
            tagAdapter.close();
            tagAdapter = null;
            processSuccess(promise);
        } else {
            processError(NOT_INITIALIZED, "", promise);
        }
    }

    private void processSuccess(Result promise) {
        promise.success(new HashMap<>());
    }

    public void connect(Result promise) {
        if (tagAdapter != null) {
            try {
                tagAdapter.connect();
                processSuccess(promise);
            } catch (IOException e) {
                processError(IO, "", promise);
            }

        } else {
            processError(NOT_INITIALIZED, "", promise);
        }
    }


    @Override
    public boolean onNewIntent(Intent intent) {
        return model.onNewIntent(intent);
    }


    public void authenticateSectorWithKeyA(final int sectorIndex, final byte[] key, Result result) {
        new MifareClassicNfcExecutor() {
            @Override
            Object execute(MifareClassic tag) throws IOException {
                return tag.authenticateSectorWithKeyA(sectorIndex, key);
            }
        }.handle(result);

    }

    public void authenticateSectorWithKeyB(final int sectorIndex, final byte[] key, Result result) {
        new MifareClassicNfcExecutor() {
            @Override
            Object execute(MifareClassic tag) throws IOException {
                return tag.authenticateSectorWithKeyB(sectorIndex, key);
            }
        }.handle(result);

    }

    public void readBlock(final int block, Result result) {
        new MifareClassicNfcExecutor() {
            @Override
            Object execute(MifareClassic tag) throws IOException {
                return tag.readBlock(block);
            }
        }.handle(result);
    }

    public void getBlockCount(Result result) {
        new MifareClassicNfcExecutor() {
            @Override
            Object execute(MifareClassic tag) throws IOException {
                return tag.getBlockCount();
            }
        }.handle(result);

    }

    public void getSectorCount(Result result) {
        new MifareClassicNfcExecutor() {
            @Override
            Object execute(MifareClassic tag) throws IOException {
                return tag.getSectorCount();
            }
        }.handle(result);

    }

    public void writeBlock(final int block, final byte[] bytes, Result result) {
        new MifareClassicNfcExecutor() {
            @Override
            Object execute(MifareClassic tag) throws IOException {
                tag.writeBlock(block, bytes);
                return null;
            }
        }.handle(result);

    }


    public void transfer(final int block, Result result) {
        new MifareClassicNfcExecutor() {
            @Override
            Object execute(MifareClassic tag) throws IOException {
                tag.transfer(block);
                return null;
            }
        }.handle(result);

    }

    public void restore(final int block, Result result) {
        new MifareClassicNfcExecutor() {
            @Override
            Object execute(MifareClassic tag) throws IOException {
                tag.restore(block);
                return null;
            }
        }.handle(result);

    }

    public void increment(final int block, final int value, Result result) {
        new MifareClassicNfcExecutor() {
            @Override
            Object execute(MifareClassic tag) throws IOException {
                tag.increment(block, value);
                return null;
            }
        }.handle(result);

    }


    public void decrement(final int block, final int value, Result result) {
        new MifareClassicNfcExecutor() {
            @Override
            Object execute(MifareClassic tag) throws IOException {
                tag.decrement(block, value);
                return null;
            }
        }.handle(result);

    }


    private void processError(String code, String message, Result promise) {
        Map<String, Object> data = new HashMap<>();
        data.put("code", code);
        data.put("message", message);

        promise.success(data);
    }


}
