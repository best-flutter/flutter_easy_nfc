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

class _MyAppState extends State<MyApp> with WidgetsBindingObserver {
  @override
  void initState() {
    super.initState();
    FlutterEasyNfc.startup();
    WidgetsBinding.instance.addObserver(this);
    FlutterEasyNfc.onNfcEvent((NfcEvent event) async {
      if (event.tag is IsoDep) {
        IsoDep isoDep = event.tag;
        await isoDep.connect();
        await isoDep.transceive("00a40000023f00");
        String file05 = await isoDep.transceive("00b0850000");
        await isoDep.transceive("00a40000023f01");
        String file15 = await isoDep.transceive("00b0950000");
        await isoDep.close();
        print(file05);
        print(file15);
      } else if (event.tag is MifareClassic) {
        MifareClassic m1 = event.tag;
        await m1.connect();
        await m1.authenticateSectorWithKeyA(0, "A0A1A2A3A4A5");
        print(await m1.readBlock(0));
        print(await m1.readBlock(1));
        print(await m1.readBlock(2));
        print(await m1.readBlock(3));
        await m1.close();
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
        body: new Center(
          child: new Text("请帖卡"),
        ),
      ),
    );
  }
}
