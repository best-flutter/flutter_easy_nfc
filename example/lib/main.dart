import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:flutter_easy_nfc/flutter_easy_nfc.dart';

import 'package:flutter_easy_nfc/flutter_easy_nfc.dart';

void main() => runApp(MyApp());

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> with WidgetsBindingObserver{


  @override
  void initState() {
    super.initState();
    FlutterEasyNfc.startup();
    WidgetsBinding.instance.addObserver(this);
    FlutterEasyNfc.onNfcEvent((NfcEvent event) async{
      if(event.tag is IsoDep){
        IsoDep isoDep = event.tag;
        await isoDep.transceive("00a40000023f00");
        String file05 = await isoDep.transceive("00b0850000");
        await isoDep.transceive("00a40000023f01");
        String file15 = await isoDep.transceive("00b0950000");

        print(file05);
        print(file15);
      }

    });

  }


  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Column(

        ),
      ),
    );
  }
}
