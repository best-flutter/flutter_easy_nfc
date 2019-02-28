import 'dart:async';
import 'dart:io';

import 'package:flutter/services.dart';
import 'package:flutter/widgets.dart';

class NfcError extends Error{

  final String code;

  NfcError({this.code});

  @override
  String toString() {
    return "NfcError: ${code} ";
  }

}

class NfcEvent<T extends BasicTagTechnology>{
  final T tag;

  NfcEvent(this.tag);
}

typedef void OnNfcEvent(NfcEvent event);

class BasicTagTechnology{

  MethodChannel _channel;

  BasicTagTechnology(MethodChannel  channel): _channel = channel;

  //// Base
  Future connect() async{
    await _channel.invokeMethod('connect');
  }

  Future close() async{
    await _channel.invokeMethod('close');
  }

}

class IsoDep extends BasicTagTechnology{
  IsoDep(MethodChannel channel) : super(channel);

  Future<String> transceive(String data) async{
    return await _channel.invokeMethod('transceive',data);
  }
}


class MifareClassic extends BasicTagTechnology{
  MifareClassic(MethodChannel channel) : super(channel);




  /// For MifareClassic
  Future authenticateSectorWithKeyA(int sectorIndex, String key){

  }

  Future authenticateSectorWithKeyB(int sectorIndex, String key){

  }

  Future<String> readBlock(int block){

  }

  Future writeBlock(int block,String data){

  }

  Future transfer(int block){

  }

  Future restore(int block){

  }

  Future increment(int block,int value){

  }

  Future decrement(int block,int value){

  }
}


class AppLifecycleStateObserver extends WidgetsBindingObserver{
  MethodChannel _channel;
  AppLifecycleStateObserver(MethodChannel channel):_channel = channel{
    WidgetsBinding.instance.addObserver(this);
  }

  void didChangeAppLifecycleState(AppLifecycleState state) {
    if(FlutterEasyNfc._isStartup){
      if(state == AppLifecycleState.resumed){
        _channel.invokeMethod("resume");
      }else if(state == AppLifecycleState.paused){
        _channel.invokeMethod("pause");
      }
    }

  }
}

class FlutterEasyNfc {
  static const MethodChannel _channel =
      const MethodChannel('flutter_easy_nfc');

  static OnNfcEvent _event;

  static bool _inited = false;

  static bool _isStartup = false;

  static AppLifecycleStateObserver _observer = new AppLifecycleStateObserver(_channel);

  static void onNfcEvent(OnNfcEvent event){
    _event = event;
  }

  /// is nfc available ?
  static Future<bool> isAvailable(){
    if(Platform.isAndroid){
      return _channel.invokeMethod('isAvailable');
    }
    return new Future.value(false);
  }

  /// is nfc is available and is enabled ?
  static Future<bool> isEnabled(){
    if(Platform.isAndroid){
      return _channel.invokeMethod('isEnabled');
    }
    return new Future.value(false);
  }

  /// startup the nfc system listener
  static Future startup() async  {
    if(!_inited){
      _channel.setMethodCallHandler(handler);
    }
    var res = await _channel.invokeMethod('startup');
    if(res['code'] != null){
      throw new NfcError(code: res['code']);
    }else{
      _isStartup = true;
    }

  }


  /// stop listen nfc events
  static Future shutdown() async{
    await _channel.invokeMethod('shutdown');
    _isStartup = false;
  }




  static Future handler(MethodCall call) {
    String name = call.method;
    var data = call.arguments;
    switch (name) {
      case "nfc":
        print(data);
        if(_event!=null){
          String tech = data['tech'];
          NfcEvent event;
          if(tech == 'IsoDep'){
            event = new NfcEvent(new IsoDep(_channel));
          }else if(tech == "MifareClassic"){
            event = new NfcEvent(new MifareClassic(_channel));
          }
          _event(event);

        }
        break;
    }
  }

}
