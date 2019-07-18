# LocateLibrary

>一个帮助 Android App 进行终端定位的辅助aar库

**Demo下载**
[demo apk下载](https://github.com/youyutorch/LocateLibrary/blob/master/LocateLibrary/demo/app-debug.apk)、[最新aar包下载](https://github.com/youyutorch/LocateLibrary/blob/master/LocateLibrary/demo/CTLocateLib_V1.0.0_20190711.aar)


## 功能介绍
1. 集成高德定位API，用于公网环境下获取终端经纬度信息及具体地址信息。
2. 使用GPS、AGPS模块，获取终端经纬度信息。
3. 使用SIM卡通信模块，获取终端基站相关信息。
4. 使用wifi通信模块，获取终端wifi相关信息。
5. 使用OrmLite存储定位相关信息，支持离线获取最新定位信息，支持定位信息收集。

## 基础使用
1. 下载最新**aar包**，放到工程**libs**文件夹下，**build.gradle**文件下添加如下配置
```
 android {
     repositories {
        flatDir {
            dirs 'libs'
        }
    }
    ...
 }
 
 dependencies {
    compile(name: 'CTLocateLib_V1.0.0_20190711', ext: 'aar')
 }
```

2. **AndroidManifast.xml**文件中声明定位所需的权限
**TIP:** 若Android系统在6.0以上，需在代码中申请相关权限
```
<!-- 获取运营商信息，用于支持提供运营商信息相关的接口 -->
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <!-- 用于访问wifi网络信息，wifi信息会用于进行网络定位 -->
    <uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
    <!-- 这个权限用于获取wifi的获取权限，wifi信息会用来进行网络定位 -->
    <uses-permission android:name="android.permission.CHANGE_WIFI_STATE" />
    <!-- 请求网络 -->
    <uses-permission android:name="android.permission.INTERNET" />

    <!-- 需要运行时注册的权限 -->
    <!-- 用于进行网络定位 -->
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
    <!-- 用于访问GPS定位 -->
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
    <!-- 用于提高GPS定位速度 -->
    <uses-permission android:name="android.permission.ACCESS_LOCATION_EXTRA_COMMANDS" />
    
     <!-- 设置key -->
     <meta-data
         android:name="com.amap.api.v2.apikey"
         android:value="3baf7b5913982107bf555370f7d9e826" />

     <!-- 高德定位需要的服务 -->
     <service android:name="com.amap.api.location.APSService" />
```

3. 初始化定位manager
```
public class LibLocateApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        CTLocateManager.init(this);
        //测试模式下打印输出定位相关调试信息
        CTLocateManager.setDebugMode(true);
    }

}
```

4. 获取终端定位信息
```
    /**
     * 网络定位测试
     * @param view
     */
    public void netWorkTest(View view) {
        contentView.setText("开始网络定位..");
        CTLocateOption option = new CTLocateOption(CTLocateConstant.TYPE_NETWORK_LOCATE);
        CTLocateManager.getInstance().getLocation(option, mLocationListener);
    }
    
       private CTGetLocationListener mLocationListener = new CTGetLocationListener() {
        @Override
        public void onFind(CTLocateInfo ctLocateInfo) {
            if (ctLocateInfo == null) {
                contentView.setText("获取位置异常!!");
                return;
            }

            contentView.setText(ctLocateInfo.toString());
        }
    };
```

5. 添加混淆规则（如使用Proguard）
```
#lib_locate模块混淆
-dontwarn com.centerm.lib.locate.**
-keep class com.centerm.lib.locate.** { *; }

#ormlite混淆
-keep class com.j256.**{*;}
-keepclassmembers class com.j256.** { *; }
-keep enum com.j256.**{*;}
-keepclassmembers enum com.j256.** { *; }
-keep interface com.j256.**{*;}
-keepclassmembers interface com.j256.** { *; }
-dontwarn com.j256.ormlite.**
```

## 进阶用法

### 获取定位
用于获取单次终端定位信息

1. 可通过getLastLocation获取最新一次更新的位置信息
>原理: 当进行终端定位时，会将终端最新定位信息通过Ormlite保存到数据库中，调用此接口就是直接从数据库中获取定位信息
```
//获取最新的一次高德定位信息
CTLocateManager.getInstance().getLastLocation(CTLocateConstant.TYPE_NETWORK_LOCATE);
```

2. 获取定位选项可通过CTLocateOption参数进行配置，常用配置如下
```
public class CTLocateOption {
    //定位类型：0-网络定位，1-wifi定位，2-gps定位，3-基站定位，4-所有
    private int locateType;

    //是否使用最近的一次定位结果
    private boolean useLastUpdate;

    //单次超时时间（秒）
    private long requestTimeout = 60;
    
    ...
}
```

### 收集定位
当单次终端定位信息不准确时，可能需要通过在不同时间多次获取定位，来提高定位信息准确性。
此处常用于终端基站信息的获取

1. 调用startCollectLocation来收集终端定位信息
```
   /**
     * 基站定位收集测试
     * @param view
     */
    public void stationCollectTest(View view) {
        contentView.setText("开始基站定位收集..");
        stringBuffer = new StringBuffer();
        CTLocateOption option = new CTLocateOption(CTLocateConstant.TYPE_BASE_STATATION_LOCATE);
        option.setInterval(5);
        option.setTotalTime(30);
        option.setBaseMaxCount(5);
        option.setFilterInvalidSignal(false);
        CTLocateManager.getInstance().setCollectLocationListener(mCollectLocationListener);
        CTLocateManager.getInstance().startCollectLocation(option);
    }
    
        private CTCollectLocationListener mCollectLocationListener = new CTCollectLocationListener() {
        @Override
        public void onTick(int currCount, CTLocateInfo ctLocateInfo) {
            if (ctLocateInfo == null) {
                contentView.setText("第" + currCount + "次获取位置异常!!");
                return;
            }
            stringBuffer.append("第" + currCount + "次收集结果：" + ctLocateInfo.toString() + "\n");
            contentView.setText(stringBuffer.toString());
        }

        @Override
        public void onFinish(int totalCount, CTLocateInfo ctLocateInfo) {
            if (ctLocateInfo == null) {
                contentView.setText("最终收集结果异常");
                return;
            }
            stringBuffer.append("最终收集结果：" + ctLocateInfo.toString() + "\n");
            contentView.setText(stringBuffer.toString());
        }
    };
```

2. 获取定位收集选项可通过CTLocateOption参数进行配置，常用配置如下
```
public class CTLocateOption {
    //定位类型：0-网络定位，1-wifi定位，2-gps定位，3-基站定位，4-所有
    private int locateType;
    
    ...
    
    //间隔时间（秒），调用收集接口时使用
    private long interval;

    //查询总时间(秒)，调用收集接口时使用
    private long totalTime;

    //是否只获取一次,获取到就结束,调用收集接口时使用
    private boolean getOnce;

    //是否清除原有收集数据，调用收集接口时使用
    private boolean clearOldInfo = true;

    //是否强制收集（直至查询总时间结束时才停止收集），调用收集接口是使用
    private boolean forceCollect;
    
    
     //************* 以下为base station类型定位参数 ******************//

    //要获取的最大基站个数（回调返回的最大基站信息个数）
    private int baseMaxCount = Integer.MAX_VALUE;

    //最小的信号强度，0到5格
    private int minSignalStrength;

    //最小收集的基站个数（收集到此个数后就停止收集），调用收集接口时使用
    private int minCollectCount;

    //是否过滤无效信号
    private boolean filterInvalidSignal = true;

```

### 混合定位
在需要同时获取多种类型终端信息时使用，目前只支持同时获取所有类型**CTLocateConstant.TYPE_ALL_LOCATE**

1. 调用getLocation获取多种类型定位
```
    public void mulLocateTest(View view) {
        contentView.setText("开始多种类型定位..");
        CTLocateManager.setFormatInfoVersion(CTLocateConstant.FORMAT_VERSION_EPAY);

        CTLocateOption option = new CTLocateOption(CTLocateConstant.TYPE_ALL_LOCATE);
        option.setBaseMaxCount(8);
        option.setUseGpsFirst(true);
        option.setRequestTimeout(120);
        option.setClearOldInfo(true);
        option.setInitMode(true);
        option.setForceCollect(true);
        CTLocateManager.getInstance().getMulLocation(option, mGetMulLocationListener);
    }
    
    private CTGetMulLocationListener mGetMulLocationListener = new CTGetMulLocationListener() {
        @Override
        public void onFind(List<CTLocateInfo> ctLocateInfoList) {
            if (ctLocateInfoList ==null || ctLocateInfoList.isEmpty()) {
                contentView.setText("获取位置异常!!");
                return;
            }
            StringBuffer buffer = new StringBuffer();
            buffer.append("获取多种类型定位结果：\n");
            for (CTLocateInfo locateInfo: ctLocateInfoList) {
                buffer.append(locateInfo.formatInfo() + "\n");
            }
            contentView.setText(buffer.toString());
        }
    };
```

2. 获取多种定位选项可通过CTLocateOption参数进行配置，常用配置如下
```
public class CTLocateOption {
    ...
    
    //************* 以下为all type类型定位参数 ******************//

    //是否是初始定位模式
    private boolean initMode;

    //优先使用GPS获取经纬度
    private boolean useGpsFirst;
}
```
混合定位时支持两种定位模式：
* 初始定位模式：一般时间较长，此时基站信息使用收集方式获取
* 实时定位模式：一般时间较短，此时直接获取基站信息，不再调用收集接口

## 组件化开发
当需要使用终端定位library做二次定位开发时，可下载源码，进行二次组件化开发。

在工程的**gradle.properties**文件中，增加**isModule**属性用于集成模式和组件模式的快捷切换
```
# 每次更改“isModule”的值后，需要点击 "Sync Project" 按钮
# isModule是“集成开发模式”和“组件开发模式”的切换开关
isModule=false
```

当需要把libary单独作为组件开发时，只需将isModule属性改为true即可。




