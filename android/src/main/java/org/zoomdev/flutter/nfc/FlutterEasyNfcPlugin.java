package org.zoomdev.flutter.nfc;

import android.app.Activity;
import android.content.Intent;
import android.nfc.NfcAdapter;

import org.zoomdev.flutter.nfc.adapters.IsoDepTagAdapter;
import org.zoomdev.flutter.nfc.adapters.MifareOneTagAdapter;

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

    private  NfcTagAdapter tagAdapter;


    public static final String NOT_AVALIABLE = "1";
    private static final String NOT_ENABLED = "2";
    public static final String NOT_INITIALIZED = "3";
    public static final String IO = "4";

    public FlutterEasyNfcPlugin(Registrar registrar, MethodChannel channel) {
        this.activity = registrar.activity();
        this.channel = channel;
        this.registrar = registrar;
    }

    @Override
    public synchronized void onMethodCall(MethodCall call, Result result) {
        String method = call.method;
        if ("close".equals(method)) {
            close(result);
        }else if ("connect".equals(method)) {
            connect(result);
        }else if ("isAvaliable".equals(method)) {
            result.success(isAvaliable());
        }else if ("isEnabled".equals(method)) {
            result.success(isEnabled());
        } else if ("startup".equals(method)) {
            startup(result);
        } else if("shutdown".equals(method)) {
            shutdown();
            result.success(new HashMap<String,Object>());
        }else if("resume".equals(method)) {
            resume();
            result.success(new HashMap<String,Object>());
        }else if("pause".equals(method)) {
            pause();
            result.success(new HashMap<String,Object>());
        }else {
            result.notImplemented();
        }
    }


    public boolean isAvaliable(){
        return NfcAdapter.getDefaultAdapter(activity)!=null;
    }

    public boolean isEnabled(){
        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(activity);
        if(nfcAdapter==null){
           return false;
        }
        return nfcAdapter.isEnabled();
    }

    public synchronized void startup(Result promise) {
        shutdown();
        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(activity);
        if(nfcAdapter==null){
            processError(NOT_AVALIABLE,"",promise);
            return;
        }
        if(nfcAdapter.isEnabled()){
            processError(NOT_ENABLED,"",promise);
            return;
        }
        model = new NfcModel(activity,new IsoDepTagAdapter.Factory(),new MifareOneTagAdapter.Factory());
        model.setAdapterListener(this);
        model.onResume(activity);
        model.onNewIntent(activity.getIntent());
        registrar.addNewIntentListener(this);

        promise.success(new HashMap<String,Object>());
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
        Map<String,Object> data = new HashMap<>();
        data.put("tech",tag.getTech());
        channel.invokeMethod("nfc", data);
    }

    public void close(Result promise){
        if(tagAdapter!=null){
            tagAdapter.close();
            tagAdapter = null;
            processSuccess(promise);
        }else{
            processError(NOT_INITIALIZED,"",promise);
        }
    }

    private void processSuccess(Result promise) {
        promise.success(new HashMap<>());
    }

    public void connect(Result promise) {
        if(tagAdapter!=null){
            try{
                tagAdapter.connect();
                processSuccess(promise);
            }catch (IOException e){
                processError(IO,"",promise);
            }

        }else{
            processError(NOT_INITIALIZED,"",promise);
        }
    }


    @Override
    public boolean onNewIntent(Intent intent) {
        return model.onNewIntent(intent);
    }


//    public void apdu(String command,Result promise) {
//        if(isoDepTagAdapter!=null){
//            try {
//                isoDepTagAdapter.connect();
//                NfcResponse response = isoDepTagAdapter.send(command);
//                Map<String,Object> data = new HashMap<>();
//                data.put("res",response.getStr());
//                promise.success(data);
//            } catch (IOException e) {f
//                processError("io","io",promise);
//            } catch (NfcException e) {
//                processError("nfc","nfc",promise);
//            }
//        }else{
//            processError("init","Not supported",promise);
//        }
//    }


//    public void authenticateSectorWithKeyA(int sectorIndex, String key) throws IOException {
//        this.mifareOneTagAdapter.authenticateSectorWithKeyA(sectorIndex, HexUtil.decodeHex(key));
//    }
//
//    public void authenticateSectorWithKeyB(int sectorIndex, byte[] key) throws IOException {
//        this.mifareOneTagAdapter.authenticateSectorWithKeyB(sectorIndex, key);
//    }
//
//    public byte[] readBlock(int block) throws IOException {
//        return this.mifareOneTagAdapter.readBlock(block);
//    }
//
//    public int getBlockCount() {
//        return this.mifareOneTagAdapter.getBlockCount();
//    }
//
//    public int getSectorCount() {
//        return this.mifareOneTagAdapter.getSectorCount();
//    }
//
//    public void writeBlock(int block, byte[] bytes) throws IOException {
//        this.mifareOneTagAdapter.writeBlock(block, bytes);
//    }
//
//
//    public void transfer(int block) throws IOException {
//        this.mifareOneTagAdapter.transfer(block);
//    }
//
//    public void restore(int block) throws IOException {
//        this.mifareOneTagAdapter.restore(block);
//    }
//
//    public void increment(int block, int value) throws IOException {
//        this.mifareOneTagAdapter.increment(block, value);
//    }
//
//
//    public void decrement(int block, int value) throws IOException {
//        this.mifareOneTagAdapter.decrement(block, value);
//    }


    private void processError(String code,String message,Result promise){
        Map<String,Object> data = new HashMap<>();
        data.put("code",code);
        data.put("message",message);

        promise.success(data);
    }




}
