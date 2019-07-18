package com.centerm.lib.locate;

/**
 * @desc 定位参数配置
 * @author tianyouyu
 * @date 2019/4/30 10:31
 */
public class CTLocateOption {
    //定位类型：0-网络定位，1-wifi定位，2-gps定位，3-基站定位，4-所有
    private int locateType;

    //是否使用最近的一次定位结果
    private boolean useLastUpdate;

    //单次超时时间（秒）
    private long requestTimeout = 60;

    //间隔时间（秒），调用收集接口时使用
    private long interval;

    //查询总时间(秒)，调用收集接口时使用
    private long totalTime;

    //是否只获取一次,获取到就结束,调用收集接口时使用
    private boolean getOnce;

    //是否清除原有收集数据，调用收集接口时使用
    private boolean clearOldInfo = true;

    //是否强制收集，调用收集接口是使用
    private boolean forceCollect;

    //************* 以下为network类型定位参数 ******************//

    //定位模式：0-低电耗，1-仅适用设备定位，2-高精度
    private int locateMode;

    //设置是否返回地址信息（默认返回地址信息）
    private boolean needAddress = true;

    //设置是否开启缓存
    private boolean cacheEnable = true;

    //网络定位使用的协议：0-http， 1-https
    private int protocol;


    //************* 以下为base station类型定位参数 ******************//

    //要获取的最大基站个数
    private int baseMaxCount = Integer.MAX_VALUE;

    //最小的信号强度，0到5格
    private int minSignalStrength;

    //最小收集的基站个数，调用收集接口时使用
    private int minCollectCount;

    //是否过滤无效信号
    private boolean filterInvalidSignal = true;

    //************* 以下为all type类型定位参数 ******************//

    //是否是初始定位模式
    private boolean initMode;

    //优先使用GPS获取经纬度
    private boolean useGpsFirst;

    public CTLocateOption(int locateType) {
        this.locateType = locateType;
    }

    public CTLocateOption(boolean getOnce, boolean useLastUpdate, long requestTimeout) {
        this.getOnce = getOnce;
        this.useLastUpdate = useLastUpdate;
        this.requestTimeout = requestTimeout;
    }

    public boolean isForceCollect() {
        return forceCollect;
    }

    public void setForceCollect(boolean forceCollect) {
        this.forceCollect = forceCollect;
    }

    public boolean isInitMode() {
        return initMode;
    }

    public void setInitMode(boolean initMode) {
        this.initMode = initMode;
    }

    public int getMinCollectCount() {
        return minCollectCount;
    }

    public void setMinCollectCount(int minCollectCount) {
        this.minCollectCount = minCollectCount;
    }

    public boolean isFilterInvalidSignal() {
        return filterInvalidSignal;
    }

    public void setFilterInvalidSignal(boolean filterInvalidSignal) {
        this.filterInvalidSignal = filterInvalidSignal;
    }

    public boolean isClearOldInfo() {
        return clearOldInfo;
    }

    public void setClearOldInfo(boolean clearOldInfo) {
        this.clearOldInfo = clearOldInfo;
    }

    public int getLocateType() {
        return locateType;
    }

    public void setLocateType(int locateType) {
        this.locateType = locateType;
    }

    public boolean isGetOnce() {
        return getOnce;
    }

    public void setGetOnce(boolean getOnce) {
        this.getOnce = getOnce;
    }

    public boolean isUseLastUpdate() {
        return useLastUpdate;
    }

    public void setUseLastUpdate(boolean useLastUpdate) {
        this.useLastUpdate = useLastUpdate;
    }

    public long getRequestTimeout() {
        return requestTimeout;
    }

    public void setRequestTimeout(long requestTimeout) {
        this.requestTimeout = requestTimeout;
    }

    public long getInterval() {
        return interval;
    }

    public void setInterval(long interval) {
        this.interval = interval;
    }

    public long getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(long totalTime) {
        this.totalTime = totalTime;
    }

    public int getLocateMode() {
        return locateMode;
    }

    public void setLocateMode(int locateMode) {
        this.locateMode = locateMode;
    }

    public boolean isNeedAddress() {
        return needAddress;
    }

    public void setNeedAddress(boolean needAddress) {
        this.needAddress = needAddress;
    }

    public boolean isCacheEnable() {
        return cacheEnable;
    }

    public void setCacheEnable(boolean cacheEnable) {
        this.cacheEnable = cacheEnable;
    }

    public int getProtocol() {
        return protocol;
    }

    public void setProtocol(int protocol) {
        this.protocol = protocol;
    }

    public int getBaseMaxCount() {
        return baseMaxCount;
    }

    public void setBaseMaxCount(int baseMaxCount) {
        this.baseMaxCount = baseMaxCount;
    }

    public int getMinSignalStrength() {
        return minSignalStrength;
    }

    public void setMinSignalStrength(int minSignalStrength) {
        this.minSignalStrength = minSignalStrength;
    }

    public boolean isUseGpsFirst() {
        return useGpsFirst;
    }

    public void setUseGpsFirst(boolean useGpsFirst) {
        this.useGpsFirst = useGpsFirst;
    }
}
