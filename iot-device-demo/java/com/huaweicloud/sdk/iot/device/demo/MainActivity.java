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

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.huaweicloud.sdk.iot.device.bootstrap.BootstrapClient;
import com.huaweicloud.sdk.iot.device.bootstrap.BootstrapMessage;
import com.huaweicloud.sdk.iot.device.client.requests.ShadowData;
import com.huaweicloud.sdk.iot.device.constant.BaseConstant;
import com.huaweicloud.sdk.iot.device.constant.IotDeviceIntent;
import com.huaweicloud.sdk.iot.device.log.LogService;
import com.huaweicloud.sdk.iot.device.timesync.TimeSyncMessage;
import com.huaweicloud.sdk.iot.device.timesync.TimeSyncService;
import com.huaweicloud.sdk.iot.device.utils.JsonUtil;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity";
    private EditText editTextDeviceId;
    private EditText editTextDevicePassword;
    private EditText editTextServerUri;
    private Button btPasswordCreate;
    private ConnectBroadcastReceiver connectBroadcastReceiver = new ConnectBroadcastReceiver();
    private Toast mToast;
    private Spinner functionSelector;
    private List<String> dataList;
    private ArrayAdapter<String> arrayAdapter;
    private Button btShowFunction;
    private EditText editTextLog;
    private int currentpos = 0;
    private CheckBox smokeDetector;
    private boolean isSmokeDetectorSelected = false;
    private SmokeDetectorService smokeDetectorService;
    private static final String SMOKE_DETECTOR_ID = "smokeDetector";
    private boolean btConectFlag;
    private Button btConnect;
    private String serverUri;
    private BootstrapClient bootstrapClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        initData(getIntent());
        listener();
        registerBroadcastReceiver();
    }

    private void listener() {
        smokeDetector.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    isSmokeDetectorSelected = true;
                } else {
                    isSmokeDetectorSelected = false;
                }
            }
        });
    }

    private void initData(Intent intent) {
        dataList = new ArrayList<String>();
        dataList.add("设备属性");
        dataList.add("设备命令和消息");
        dataList.add("软固件升级");
        dataList.add("文件上传/下载管理");
        dataList.add("自定义topic");
        dataList.add("V3接口");
        dataList.add("设备时间同步");

        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, dataList);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        functionSelector.setAdapter(arrayAdapter);
        functionSelector.setDropDownVerticalOffset(80);
        functionSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.i(TAG, "onItemSelected: " + position);
                currentpos = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //开启关闭连接按钮，默认显示开启连接
        btConectFlag = true;

        //设置获取到的信息
        String bootDeviceId = intent.getStringExtra("deviceId");
        String address = intent.getStringExtra("address");

        if (!TextUtils.isEmpty(address)) {
            serverUri = address;
        } else {
            Log.i(TAG, "initData: default serverUri");
            serverUri = "ssl://iot-mqtts.cn-north-4.myhuaweicloud.com:8883";
        }

        if (!TextUtils.isEmpty(bootDeviceId)) {
            editTextDeviceId.setText(bootDeviceId);
        }

    }

    private void registerBroadcastReceiver() {
        LocalBroadcastManager.getInstance(this).registerReceiver(connectBroadcastReceiver, new IntentFilter(IotDeviceIntent.ACTION_IOT_DEVICE_CONNECT));
        LocalBroadcastManager.getInstance(this).registerReceiver(connectBroadcastReceiver, new IntentFilter(Constant.SMOKE_DETECTOR_PROPERTY));
        LocalBroadcastManager.getInstance(this).registerReceiver(connectBroadcastReceiver, new IntentFilter(Constant.SMOKE_DETECTOR_COMMAND));
        LocalBroadcastManager.getInstance(this).registerReceiver(connectBroadcastReceiver, new IntentFilter(Constant.SMOKE_SHADOW_ACTION));
        LocalBroadcastManager.getInstance(this).registerReceiver(connectBroadcastReceiver, new IntentFilter(IotDeviceIntent.ACTION_IOT_DEVICE_TIME_SYNC_RESPONSE));
        LocalBroadcastManager.getInstance(this).registerReceiver(connectBroadcastReceiver, new IntentFilter(IotDeviceIntent.ACTION_IOT_DEVICE_BOOTSTRAP_REQUEST_TRIGGER));
        LocalBroadcastManager.getInstance(this).registerReceiver(connectBroadcastReceiver, new IntentFilter(IotDeviceIntent.ACTION_IOT_DEVICE_BOOTSTRAP));
    }

    private void initViews() {
        editTextDeviceId = findViewById(R.id.editText_mqtt_device_connect_deviceId);
        editTextDevicePassword = findViewById(R.id.editText_mqtt_device_connect_password);
        editTextServerUri = findViewById(R.id.editText_mqtt_device_server_uri);
        btPasswordCreate = findViewById(R.id.bt_password_create);
        btPasswordCreate.setOnClickListener(this);
        mToast = Toast.makeText(this, "", Toast.LENGTH_LONG);
        mToast.setGravity(Gravity.CENTER, 0, 0);
        functionSelector = findViewById(R.id.function_selector);
        btShowFunction = findViewById(R.id.bt_show_function);
        btShowFunction.setOnClickListener(this);
        findViewById(R.id.textView_log).setOnClickListener(this);
        findViewById(R.id.bt_connect).setOnClickListener(this);
        findViewById(R.id.bt_keystore_create).setOnClickListener(this);
        editTextLog = findViewById(R.id.editText_log);

        smokeDetector = findViewById(R.id.smoke_detector);
        btConnect = findViewById(R.id.bt_connect);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_password_create:
                String deviceId = editTextDeviceId.getText().toString();
                String deviceSecret = editTextDevicePassword.getText().toString();
                serverUri = editTextServerUri.getText().toString();
                if (TextUtils.isEmpty(deviceId) || TextUtils.isEmpty(deviceSecret)) {
                    Log.e(TAG, "设备Id或者密码为空");
                    mToast.setText("设备Id或者密码为空");
                    mToast.show();
                    return;
                }

                editTextLog.append("使用密码创建设备:" + "serverUri=" + serverUri + "\n"
                        + "deviceId=" + deviceId + "\n"
                        + "deviceSecret=" + deviceSecret + "\n");

                Device.init(getApplicationContext(), serverUri, deviceId, deviceSecret);
                if (isSmokeDetectorSelected) {
                    smokeDetectorService = new SmokeDetectorService(this);
                    Device.getDevice().addService(SMOKE_DETECTOR_ID, smokeDetectorService);
                }
                break;
            case R.id.bt_show_function:
                selectFunction();
                break;
            case R.id.textView_log:
                editTextLog.setText("");
                break;
            case R.id.bt_connect:
                if (btConectFlag) {
                    Device.connect();
                    btConnect.setText("关闭连接");
                    btConectFlag = false;
                    editTextLog.append("开启设备连接\n");
                } else {
                    Device.close();
                    btConnect.setText("打开连接");
                    btConectFlag = true;
                    editTextLog.append("关闭设备连接\n");
                }

                break;
            case R.id.bt_keystore_create:
                try {
                    keystoreCreate();
                } catch (KeyStoreException e) {
                    Log.e(TAG, e.getMessage());
                } catch (CertificateException e) {
                    Log.e(TAG, e.getMessage());
                } catch (NoSuchAlgorithmException e) {
                    Log.e(TAG, e.getMessage());
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage());
                }
                editTextLog.append("创建设备使用keystore\n");
                break;
            default:
                Log.i(TAG, "onClick: default");
                break;
        }
    }

    private void keystoreCreate() throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException {
        String keyPassword = "123456";

        KeyStore keyStore = CommonUtils.getKeyStore(this, keyPassword, "deviceCert.pem", "deviceCert.key");
        Log.i(TAG, "keystoreCreate: keyStore=" + keyStore);

        String deviceId = editTextDeviceId.getText().toString().trim();

        Device.init(getApplicationContext(), serverUri, deviceId, keyStore, keyPassword);
    }

    private void selectFunction() {
        switch (currentpos) {
            case 0:
                Intent propertyIntent = new Intent(this, PropertyActivity.class);
                startActivity(propertyIntent);
                break;
            case 1:
                Intent messageIntent = new Intent(this, MessageActivity.class);
                startActivity(messageIntent);
                break;
            case 2:
                Intent otaIntent = new Intent(this, OTAActivity.class);
                startActivity(otaIntent);
                break;
            case 3:
                Intent fileManagerIntent = new Intent(this, FileManagerActivity.class);
                startActivity(fileManagerIntent);
                break;
            case 4:
                Intent topicIntent = new Intent(this, TopicActivity.class);
                startActivity(topicIntent);
                break;
            case 5:
                Intent v3Intent = new Intent(this, V3Activity.class);
                v3Intent.putExtra("deviceId", editTextDeviceId.getText().toString());
                startActivity(v3Intent);
                break;
            case 6:
                //设备时间同步
                fncTimeSync();
                break;
            default:
                Log.e(TAG, "selectFunction: currentpos = " + currentpos);
                break;
        }
    }

    /**
     * 设备时间同步操作
     */
    private void fncTimeSync() {
        editTextLog.append("设备时间同步\n");
        TimeSyncService timeSyncService = Device.getDevice().getTimeSyncService();
        timeSyncService.requestTimeSync();
        //测试日志上报
        LogService logService = Device.getDevice().getLogService();
        logService.reportLog(logService.getLogContent());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unRegisterBroadcastReceiver();
        Device.close();
        if (smokeDetectorService != null) {
            Device.getDevice().delService(SMOKE_DETECTOR_ID);
            smokeDetectorService = null;
        }
        Log.i(TAG, "onDestroy: MainActivity destory!");
    }

    private void unRegisterBroadcastReceiver() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(connectBroadcastReceiver);
    }

    private class ConnectBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "onReceive: " + intent.getAction());

            if (IotDeviceIntent.ACTION_IOT_DEVICE_CONNECT.equals(intent.getAction())) {
                int broadcastStatus = intent.getIntExtra(BaseConstant.BROADCAST_STATUS, BaseConstant.STATUS_FAIL);
                switch (broadcastStatus) {
                    case BaseConstant.STATUS_SUCCESS:
                        //测试上报日志
                        editTextLog.append("设备创建成功!" + "\n");
                        if (isSmokeDetectorSelected) {
                            //启动自动周期上报
                            smokeDetectorService.enableAutoReport(10000);
                        }
                        break;
                    case BaseConstant.STATUS_FAIL:
                        //提示连接异常
                        String error = intent.getStringExtra(BaseConstant.COMMON_ERROR);
                        editTextLog.append("设备创建失败!失败原因：" + error + "\n");
                        break;
                    default:
                        Log.i(TAG, "onReceive: broadcastStatus=" + broadcastStatus);
                        break;
                }
            } else if (Constant.SMOKE_DETECTOR_PROPERTY.equals(intent.getAction())) {
                int smokeAlarm = intent.getIntExtra(Constant.SMOKE_PARAM_PROPERTY, -1);
                if (smokeAlarm != -1) {
                    editTextLog.append("物模型平台设置属性:" + "smokeAlarm=" + smokeAlarm + "\n");
                }
            } else if (Constant.SMOKE_DETECTOR_COMMAND.equals(intent.getAction())) {
                int duration = intent.getIntExtra(Constant.SMOKE_COMMAND_PROPERTY, -1);
                if (duration != -1) {
                    editTextLog.append("物模型命令设置属性:" + "smokeAlarm=" + duration + "\n");
                }
            } else if (Constant.SMOKE_SHADOW_ACTION.equals(intent.getAction())) {
                ShadowData shadowData = intent.getParcelableExtra(Constant.SMOKE_SHADOW_PARAM);
                if (shadowData != null) {
                    editTextLog.append("物模型影子数据:" + "shadowData=" + JsonUtil.convertObject2String(shadowData) + "\n");
                }
            } else if (IotDeviceIntent.ACTION_IOT_DEVICE_TIME_SYNC_RESPONSE.equals(intent.getAction())) {
                TimeSyncMessage timeSyncMessage = intent.getParcelableExtra(BaseConstant.DEVICE_TIME_SYNC_MESSAGE);
                long deviceSendTime = timeSyncMessage.getDeviceSendTime();
                long serverRecvTime = timeSyncMessage.getServerRecvTime();
                long serverSendTime = timeSyncMessage.getServerSendTime();
                editTextLog.append("设备发送时间戳:" + deviceSendTime + "\n");
                editTextLog.append("平台收到时间戳:" + serverRecvTime + "\n");
                editTextLog.append("平台发送时间戳:" + serverSendTime + "\n");
                long deviceRecvTime = System.currentTimeMillis();
                long now = (serverRecvTime + serverSendTime + deviceRecvTime - deviceSendTime) / 2;
                editTextLog.append("现在的时间是:" + new Date(now) + "\n");
            } else if (IotDeviceIntent.ACTION_IOT_DEVICE_BOOTSTRAP_REQUEST_TRIGGER.equals(intent.getAction())) {
                //断开已有设备并重引导
                Device.close();
                btConnect.setText("打开连接");
                btConectFlag = true;
                editTextLog.append("关闭设备连接\n");
                String bootstrapUri = "ssl://iot-bs.cn-north-4.myhuaweicloud.com:8883";
                String deviceId = editTextDeviceId.getText().toString().trim();
                String deviceSecret = editTextDevicePassword.getText().toString().trim();
                bootstrapClient = new BootstrapClient(MainActivity.this, bootstrapUri, deviceId, deviceSecret);
                bootstrapClient.bootstrap();
            } else if (IotDeviceIntent.ACTION_IOT_DEVICE_BOOTSTRAP.equals(intent.getAction())) {
                int status = intent.getIntExtra(BaseConstant.BROADCAST_STATUS, BaseConstant.STATUS_FAIL);
                switch (status) {
                    case BaseConstant.STATUS_SUCCESS:
                        //引导成功后，关闭引导连接
                        bootstrapClient.close();
                        BootstrapMessage bootstrapMessage = intent.getParcelableExtra(BaseConstant.BOOTSTRAP_MESSAGE);
                        String address = bootstrapMessage.getAddress();
                        serverUri = address;
                        editTextLog.append("重引导成功，IOT平台地址是：\n");
                        editTextLog.append(address + "\n");
                        break;
                    case BaseConstant.STATUS_FAIL:
                        String errorMsg = intent.getStringExtra(BaseConstant.COMMON_ERROR);
                        editTextLog.append("重引导失败，失败原因是:\n");
                        editTextLog.append(errorMsg + "\n");
                        break;
                    default:
                        break;
                }
            }
        }
    }
}
