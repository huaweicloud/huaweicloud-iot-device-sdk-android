/*Copyright 2020 Huawei Technologies Co., Ltd
 *Licensed under the Apache License, Version 2.0 (the "License");
 *you may not use this file except in compliance with the License.
 *You may obtain a copy of the License at
 *
 *http://www.apache.org/licenses/LICENSE-2.0
 *
 *Unless required by applicable law or agreed to in writing, software
 *distributed under the License is distributed on an "AS IS" BASIS,
 *WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *See the License for the specific language governing permissions and
 *limitations under the License.
 *
 * */

package com.huaweicloud.sdk.iot.gateway.demo;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import com.huaweicloud.sdk.iot.device.gateway.SubDevicesPersistence;
import com.huaweicloud.sdk.iot.device.gateway.requests.DeviceInfo;
import com.huaweicloud.sdk.iot.device.gateway.requests.SubDevicesInfo;
import com.huaweicloud.sdk.iot.device.utils.JsonUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 将子设备信息保存到json文件。用户可以自己实现SubDevicesPersistence接口来进行替换
 */
public class SubDevicesFilePersistence implements SubDevicesPersistence {

    private static final String TAG = "SubDevicesPersistence";
    private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private final Lock readLock = readWriteLock.readLock();
    private final Lock writeLock = readWriteLock.writeLock();
    private SubDevInfo subDevInfoCache;
    private Context mContext;

    public SubDevicesFilePersistence(Context mContext) {
        this.mContext = mContext;
        String content = readFromSP();
        this.subDevInfoCache = JsonUtil.convertJsonStringToObject(content, SubDevInfo.class);
        if (this.subDevInfoCache == null) {
            subDevInfoCache = new SubDevInfo();
            Map<String, DeviceInfo> subdevices = new HashMap<>();
            subDevInfoCache.setSubdevices(subdevices);
            subDevInfoCache.setVersion(-1);
        }
        Log.i(TAG, "subDevInfo:" + subDevInfoCache.toString());
    }


    @Override
    public DeviceInfo getSubDevice(String nodeId) {

        readLock.lock();
        try {
            return subDevInfoCache.getSubdevices().get(nodeId);
        } finally {
            readLock.unlock();
        }
    }


    public List<DeviceInfo> getAllSubDevices() {
        readLock.lock();
        try {
            return new ArrayList<DeviceInfo>(subDevInfoCache.getSubdevices().values());
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public int addSubDevices(SubDevicesInfo subDevicesInfo) {

        writeLock.lock();
        try {
            if (subDevicesInfo.getVersion() > 0 && subDevicesInfo.getVersion() < subDevInfoCache.getVersion()) {
                Log.i(TAG, "version too low: " + subDevicesInfo.getVersion());
                return -1;
            }

            if (addSubDeviceToFile(subDevicesInfo) != 0) {
                Log.i(TAG, "write file fail ");
                return -1;
            }

            if (subDevInfoCache.getSubdevices() == null) {
                subDevInfoCache.setSubdevices(new HashMap<String, DeviceInfo>());
            }

            List<DeviceInfo> deviceInfoList = subDevicesInfo.getDevices();
            for (int i = 0; i < deviceInfoList.size(); i++) {
                subDevInfoCache.getSubdevices().put(deviceInfoList.get(i).getNodeId(), deviceInfoList.get(i));
                Log.i(TAG, "add subdev: " + deviceInfoList.get(i).getNodeId());
            }

            subDevInfoCache.setVersion(subDevicesInfo.getVersion());
            Log.i(TAG, "version update to " + subDevInfoCache.getVersion());

        } finally {
            writeLock.unlock();
        }
        return 0;
    }

    @Override
    public int deleteSubDevices(SubDevicesInfo subDevicesInfo) {

        if (subDevicesInfo.getVersion() > 0 && subDevicesInfo.getVersion() < subDevInfoCache.getVersion()) {
            Log.i(TAG, "version too low: " + subDevicesInfo.getVersion());
            return -1;
        }

        if (subDevInfoCache.getSubdevices() == null) {
            return -1;
        }

        if (rmvSubDeviceToFile(subDevicesInfo) != 0) {
            Log.i(TAG, "remove from file fail ");
            return -1;
        }

        List<DeviceInfo> deviceInfoList = subDevicesInfo.getDevices();
        for (int i = 0; i < deviceInfoList.size(); i++) {
            subDevInfoCache.getSubdevices().remove(deviceInfoList.get(i).getNodeId());
            Log.i(TAG, "rmv subdev :" + deviceInfoList.get(i).getNodeId());
        }

        subDevInfoCache.setVersion(subDevicesInfo.getVersion());
        Log.i(TAG, "local version update to " + subDevicesInfo.getVersion());

        return 0;
    }

    @Override
    public long getVersion() {
        return subDevInfoCache.getVersion();
    }


    private int addSubDeviceToFile(SubDevicesInfo subDevicesInfo) {
        String content = readFromSP();

        SubDevInfo subDevInfo = JsonUtil.convertJsonStringToObject(content, SubDevInfo.class);

        if (subDevInfo == null) {
            subDevInfo = new SubDevInfo();
        }

        if (subDevInfo.getSubdevices() == null) {
            subDevInfo.setSubdevices(new HashMap<String, DeviceInfo>());
        }

        List<DeviceInfo> deviceInfoList = subDevicesInfo.getDevices();
        for (int i = 0; i < deviceInfoList.size(); i++) {
            subDevInfo.getSubdevices().put(deviceInfoList.get(i).getNodeId(), deviceInfoList.get(i));
        }
        subDevInfo.setVersion(subDevicesInfo.getVersion());

        if (writeToSP(JsonUtil.convertObject2String(subDevInfo))) {
            return 0;
        } else {
            return -1;
        }
    }

    private int rmvSubDeviceToFile(SubDevicesInfo subDevicesInfo) {
        String content = readFromSP();
        if (TextUtils.isEmpty(content)) {
            return -1;
        }

        SubDevInfo subDevInfo = JsonUtil.convertJsonStringToObject(content, SubDevInfo.class);

        if (subDevInfo.getSubdevices() == null) {
            return 0;
        }

        List<DeviceInfo> deviceInfoList = subDevicesInfo.getDevices();
        for (int i = 0; i < deviceInfoList.size(); i++) {
            subDevInfo.getSubdevices().remove(deviceInfoList.get(i).getNodeId());
        }
        subDevInfo.setVersion(subDevicesInfo.getVersion());

        if (writeToSP(JsonUtil.convertObject2String(subDevInfo))) {
            return 0;
        } else {
            return -1;
        }
    }

    private String readFromSP() {
        SharedPreferences sp = mContext.getSharedPreferences("subdevices", Context.MODE_PRIVATE);
        return sp.getString("sub", null);
    }

    private boolean writeToSP(String message) {
        SharedPreferences sp = mContext.getSharedPreferences("subdevices", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("sub", message);
        editor.commit();

        return true;
    }
}
