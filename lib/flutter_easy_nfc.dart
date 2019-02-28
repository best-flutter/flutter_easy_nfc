import 'dart:async';

import 'package:flutter/services.dart';

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


class FlutterEasyNfc {
  static const MethodChannel _channel =
      const MethodChannel('flutter_easy_nfc');

  static OnNfcEvent _event;

  static void onNfcEvent(OnNfcEvent event){
    _event = event;
  }

  static Future  startup() async  {
    _channel.setMethodCallHandler(handler);
    await _channel.invokeMethod('startup');
  }

//
//  static Future<String> apdu(String command) async{
//    var data = await _channel.invokeMethod('apdu',command);
//    if(data['code']!=null){
//      throw new NfcError(code: data['code']);
//    }
//    return data['res'];
//  }



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
