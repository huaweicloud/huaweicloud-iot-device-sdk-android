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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.huaweicloud.sdk.iot.device.client.requests.CommandRspV3;
import com.huaweicloud.sdk.iot.device.client.requests.CommandV3;
import com.huaweicloud.sdk.iot.device.client.requests.DevicePropertiesV3;
import com.huaweicloud.sdk.iot.device.client.requests.ServiceData;
import com.huaweicloud.sdk.iot.device.constant.BaseConstant;
import com.huaweicloud.sdk.iot.device.constant.IotDeviceIntent;
import com.huaweicloud.sdk.iot.device.utils.JsonUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class V3Activity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "V3Activity";

    private Toast mToast;
    private EditText edtLog;
    private EditText edtMessageServiceId;
    private EditText edtPropertyKey;
    private EditText edtPropertyValue;
    private EditText edtResponseErrcode;
    private EditText edtParasKey;
    private EditText edtParasValue;
    private String deviceId;
    private V3Receiver v3Receiver = new V3Receiver();
    private CommandV3 commandV3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_v3);
        initViews();
        registerBroadcastReceiver();
        //订阅topic
        deviceId = getIntent().getStringExtra("deviceId");
        Device.getDevice().getClient().subscribeTopicV3("/huawei/v1/devices/" + deviceId + "/command/json", 0);
    }


    private void registerBroadcastReceiver() {
        LocalBroadcastManager.getInstance(this).registerReceiver(v3Receiver, new IntentFilter(IotDeviceIntent.ACTION_IOT_DEVICE_PROPERTIES_REPORT_V3));
        LocalBroadcastManager.getInstance(this).registerReceiver(v3Receiver, new IntentFilter(IotDeviceIntent.ACTION_IOT_DEVICE_SYS_COMMANDS_V3));
    }

    private void initViews() {
        edtLog = findViewById(R.id.edt_log);
        mToast = Toast.makeText(this, "", Toast.LENGTH_LONG);
        mToast.setGravity(Gravity.CENTER, 0, 0);
        findViewById(R.id.tv_log).setOnClickListener(this);

        edtMessageServiceId = findViewById(R.id.edt_message_service_id);
        edtPropertyKey = findViewById(R.id.edt_property_key);
        edtPropertyValue = findViewById(R.id.edt_property_value);
        edtResponseErrcode = findViewById(R.id.edt_response_errcode);
        edtParasKey = findViewById(R.id.edt_paras_key);
        edtParasValue = findViewById(R.id.edt_paras_value);

        findViewById(R.id.bt_message_report).setOnClickListener(this);
        findViewById(R.id.bt_command_response).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_log:
                edtLog.setText("");
                break;
            case R.id.bt_message_report:
                messageReport();
                break;
            case R.id.bt_command_response:
                commandResponse();
                break;
            default:
                Log.i(TAG, "onClick: default");
                break;
        }
    }

    private void commandResponse() {
        String errcode = edtResponseErrcode.getText().toString();
        if (TextUtils.isEmpty(errcode)) {
            mToast.setText("errcode is empty");
            mToast.show();
            return;
        }

        CommandRspV3 commandRspV3 = new CommandRspV3("deviceRsp", commandV3.getMid(), 0);
        Map<String, Object> json = new HashMap<String, Object>();
        String parasKey = edtParasKey.getText().toString();
        String parasValue = edtParasValue.getText().toString();
        json.put(parasKey, parasValue);
        commandRspV3.setBody(json);

        Device.getDevice().getClient().responseCommandV3(commandRspV3);
    }

    private void messageReport() {
        String serviceId = edtMessageServiceId.getText().toString();
        if (TextUtils.isEmpty(serviceId)) {
            mToast.setText("serviceId is empty");
            mToast.show();
            return;
        }

        DevicePropertiesV3 devicePropertiesV3 = new DevicePropertiesV3();
        devicePropertiesV3.setMsgType("deviceReq");

        ServiceData serviceData = new ServiceData();
        serviceData.setServiceId(serviceId);

        Map<String, Object> json = new HashMap<String, Object>();
        String propertyKey = edtPropertyKey.getText().toString();
        String propertyValue = edtPropertyValue.getText().toString();
        json.put(propertyKey, propertyValue);

        serviceData.setServiceData(json);

        List<ServiceData> list = new ArrayList<ServiceData>();
        list.add(serviceData);
        devicePropertiesV3.setServiceDatas(list);

        Device.getDevice().getClient().reportPropertiesV3(devicePropertiesV3);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unRegisterBroadcastReceiver();
    }

    private void unRegisterBroadcastReceiver() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(v3Receiver);
    }

    private class V3Receiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "onReceive: " + intent.getAction());
            if (IotDeviceIntent.ACTION_IOT_DEVICE_PROPERTIES_REPORT_V3.equals(intent.getAction())) {
                int broadcastStatus = intent.getIntExtra(BaseConstant.BROADCAST_STATUS, BaseConstant.STATUS_FAIL);
                switch (broadcastStatus) {
                    case BaseConstant.STATUS_SUCCESS:
                        edtLog.append("V3上报数据成功" + "\n");
                        break;
                    case BaseConstant.STATUS_FAIL:
                        String errorMessage = intent.getStringExtra(BaseConstant.PROPERTIES_REPORT_ERROR);
                        edtLog.append("V3上报数据失败: " + errorMessage + "\n");
                        break;
                    default:
                        Log.i(TAG, "onReceive: broadcastStatus=" + broadcastStatus);
                        break;
                }

            } else if (IotDeviceIntent.ACTION_IOT_DEVICE_SYS_COMMANDS_V3.equals(intent.getAction())) {
                commandV3 = intent.getParcelableExtra(BaseConstant.SYS_COMMANDS);
                edtLog.append("V3命令下发成功： " + JsonUtil.convertObject2String(commandV3) + "\n");
            }
        }
    }


}