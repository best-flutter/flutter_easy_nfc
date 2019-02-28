# flutter_easy_nfc

<p align="center">
    <a href="https://pub.dartlang.org/packages/flutter_easy_nfc">
        <img src="https://img.shields.io/pub/v/flutter_easy_nfc.svg" alt="pub package" />
    </a>
    <a target="_blank" href="https://shang.qq.com/wpa/qunwpa?idkey=a71a2504cda4cc9ace3320f2dc588bdae928abc671e903463caeb71ec9302c2c"><img border="0" src="https://pub.idqqimg.com/wpa/images/group.png" alt="best-flutter" title="best-flutter"></a>
</p>

flutter nfc库

## 安装

依赖:

```
flutter_easy_nfc: any
```

## 使用


#### 导入库

import 'package:flutter_easy_nfc/flutter_easy_nfc.dart';

#### 判断是否有nfc

```
await FlutterEasyNfc.isAvailable();
```

#### 判断当前nfc是否打开

```
await FlutterEasyNfc.isEnabled();
```

#### 启用nfc监听

```
await FlutterEasyNfc.startup();
```

#### 停止nfc监听
```
await FlutterEasyNfc.shutdown();
```


#### 监听nfc事件:

```
 FlutterEasyNfc.onNfcEvent((NfcEvent event) async {
       /// 目前支持两种标签: IsoDep和MifareClassic
      if (event.tag is IsoDep) {
        IsoDep isoDep = event.tag;
        await isoDep.connect();
        await isoDep.transceive("00a40000023f00");
        Uint8List file05 = await isoDep.transceive("00b0850000");
        await isoDep.transceive("00a40000023f01");
        Uint8List file15 = await isoDep.transceive("00b0950000");
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
```
