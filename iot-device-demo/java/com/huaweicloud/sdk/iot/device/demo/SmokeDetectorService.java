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

package com.huaweicloud.sdk.iot.device.demo;


import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.huaweicloud.sdk.iot.device.client.requests.CommandRsp;
import com.huaweicloud.sdk.iot.device.service.AbstractService;
import com.huaweicloud.sdk.iot.device.service.DeviceCommand;
import com.huaweicloud.sdk.iot.device.service.Property;

import java.security.SecureRandom;
import java.util.Map;

/**
 * 此例用来演示面向物模型编程的方法。用户只需要根据物模型定义自己的设备服务类，就可以直接对设备服务进行读写操作，SDK会自动
 * 的完成设备属性的同步和命令的调用。本例中实现的设备服务为烟感服务
 * 烟感服务，支持属性：报警标志、烟雾浓度、温度、湿度
 * 支持的命令：响铃报警
 */
public class SmokeDetectorService extends AbstractService {

    private Context mContext;

    private final static String TAG = "SmokeDetectorService";

    //按照设备模型定义属性，注意属性的name和类型需要和模型一致，writeable表示属性知否可写，name指定属性名
    @Property(name = "alarm", writeable = true)
    int smokeAlarm = 1;

    @Property(name = "smokeConcentration", writeable = false)
    float concentration = 0.0f;

    @Property(writeable = false)
    int humidity;

    @Property(writeable = false)
    float temperature;

    public SmokeDetectorService(Context mContext) {
        this.mContext = mContext;
    }

    //定义命令，注意接口入参和返回值类型是固定的不能修改，否则会出现运行时错误
    @DeviceCommand(name = "ringAlarm")
    public CommandRsp alarm(Map<String, Object> paras) {
        Integer duration = (Integer) paras.get("duration");
        Log.i(TAG, "ringAlarm  duration = " + duration);
        Intent intent = new Intent(Constant.SMOKE_DETECTOR_COMMAND);
        intent.putExtra(Constant.SMOKE_COMMAND_PROPERTY, duration);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
        return new CommandRsp(0);
    }

    //setter和getter接口的命名应该符合java bean规范，sdk会自动调用这些接口
    public int getHumidity() {

        //模拟从传感器读取数据
        humidity = new SecureRandom().nextInt(100);
        return humidity;
    }

    public void setHumidity(int humidity) {
        //humidity是只读的，不需要实现
    }

    public float getTemperature() {

        //模拟从传感器读取数据
        temperature = new SecureRandom().nextInt(100);
        return temperature;
    }

    public void setTemperature(float temperature) {
        //只读字段不需要实现set接口
    }

    public float getConcentration() {

        //模拟从传感器读取数据
        concentration = new SecureRandom().nextFloat() * 100.0f;
        return concentration;
    }

    public void setConcentration(float concentration) {
        //只读字段不需要实现set接口
    }

    public int getSmokeAlarm() {
        return smokeAlarm;
    }

    public void setSmokeAlarm(int smokeAlarm) {

        this.smokeAlarm = smokeAlarm;
        if (smokeAlarm == 0) {
            Log.i(TAG, "alarm is cleared by app");
        }

        Intent intent = new Intent(Constant.SMOKE_DETECTOR_COMMAND);
        intent.putExtra(Constant.SMOKE_COMMAND_PROPERTY, smokeAlarm);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

}
