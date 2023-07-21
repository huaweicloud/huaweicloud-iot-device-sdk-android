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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.huaweicloud.sdk.iot.device.constant.BaseConstant;
import com.huaweicloud.sdk.iot.device.constant.IotDeviceIntent;
import com.huaweicloud.sdk.iot.device.gateway.requests.DeviceInfo;
import com.huaweicloud.sdk.iot.device.gateway.requests.DeviceStatus;
import com.huaweicloud.sdk.iot.device.gateway.requests.SubDevicesAddInfo;
import com.huaweicloud.sdk.iot.device.gateway.requests.SubDevicesDeleteInfo;
import com.huaweicloud.sdk.iot.device.gateway.requests.SubDevicesInfo;
import com.huaweicloud.sdk.iot.device.utils.JsonUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String TAG = "MainActivity";

    private static final String HOST = "localhost";
    private static final int PORT = 20001;
    private SimpleGateway simpleGateway;
    private SubDevicesFilePersistence subDevicesPersistence;

    private GatewayBroadcastReceiver gatewayBroadcastReceiver = new GatewayBroadcastReceiver();


    /**
     * 用来记录网关子设备的<deviceId, nodeId>
     */
    private Map<String, String> subDevices;
    private EditText editTextLog;

    /**
     * 模拟在线子设备<nodeId, TcpDevice>
     */
    private Map<String, TcpDevice> tcpDeviceMap = new HashMap<>();

    private StringTcpServer stringTcpServer;

    private EditText edtAddNodeId;
    private EditText edtOnlineNodeId;
    private EditText edtOfflineNodeId;
    private EditText edtDeleteNodeId;
    private Toast mToast;

    class MessageHandler extends Handler {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    editTextLog.append((String) msg.obj);
                    editTextLog.append("\n\n");
                    break;
                default:
                    break;
            }
        }
    }

    private Handler mHandler = new MessageHandler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        initData();
        initBroadcasts();
    }

    private void initBroadcasts() {
        LocalBroadcastManager.getInstance(this).registerReceiver(gatewayBroadcastReceiver,
                new IntentFilter(IotDeviceIntent.ACTION_IOT_DEVICE_CONNECT));
        LocalBroadcastManager.getInstance(this).registerReceiver(gatewayBroadcastReceiver,
                new IntentFilter(IotDeviceIntent.ACTION_IOT_DEVICE_SYS_SUB_ADD_DEVICE_NOTIFY));
        LocalBroadcastManager.getInstance(this).registerReceiver(gatewayBroadcastReceiver,
                new IntentFilter(IotDeviceIntent.ACTION_IOT_DEVICE_SYS_SUB_DELETE_DEVICE_NOTIFY));
        LocalBroadcastManager.getInstance(this).registerReceiver(gatewayBroadcastReceiver,
                new IntentFilter(IotDeviceIntent.ACTION_IOT_DEVICE_SYS_SUB_ADD_DEVICE_RESPONSE));
        LocalBroadcastManager.getInstance(this).registerReceiver(gatewayBroadcastReceiver,
                new IntentFilter(IotDeviceIntent.ACTION_IOT_DEVICE_SYS_SUB_DELETE_DEVICE_RESPONSE));
        LocalBroadcastManager.getInstance(this).registerReceiver(gatewayBroadcastReceiver,
                new IntentFilter(IotDeviceIntent.ACTION_IOT_DEVICE_SYS_SUB_STATUSES_REPORT));
    }

    private void initData() {
        subDevices = new HashMap<String, String>();
        subDevicesPersistence = new SubDevicesFilePersistence(this);
        simpleGateway = new SimpleGateway(this, subDevicesPersistence,
                "ssl://xxxxx.st1.iotda-device.cn-north-4.myhuaweicloud.com:8883",
                "5eb4cd4049a5ab087d7d4861_demo", "secret");
        //同步网关信息
        List<DeviceInfo> allSubDevices = subDevicesPersistence.getAllSubDevices();
        for (int i = 0; i < allSubDevices.size(); i++) {
            subDevices.put(allSubDevices.get(i).getDeviceId(), allSubDevices.get(i).getNodeId());
        }
        stringTcpServer = new StringTcpServer(this, mHandler, simpleGateway);
        stringTcpServer.start();
    }

    private void initViews() {
        editTextLog = findViewById(R.id.editText_log);
        edtAddNodeId = findViewById(R.id.edt_add_nodeId);
        edtOnlineNodeId = findViewById(R.id.edt_online_nodeId);
        edtOfflineNodeId = findViewById(R.id.edt_offline_nodeId);
        edtDeleteNodeId = findViewById(R.id.edt_delete_nodeId);
        findViewById(R.id.start_gateway).setOnClickListener(this);
        findViewById(R.id.test_add_sub).setOnClickListener(this);
        findViewById(R.id.test_delete_sub).setOnClickListener(this);
        findViewById(R.id.textView_log).setOnClickListener(this);
        findViewById(R.id.close_gateway).setOnClickListener(this);
        findViewById(R.id.test_sub_online).setOnClickListener(this);
        findViewById(R.id.test_sub_offline).setOnClickListener(this);
        mToast = Toast.makeText(this, "", Toast.LENGTH_LONG);
        mToast.setGravity(Gravity.CENTER, 0, 0);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.start_gateway:
                simpleGateway.init();
                break;
            case R.id.test_add_sub:
                testAddSub();
                break;
            case R.id.test_delete_sub:
                testDeleteSub();
                break;
            case R.id.test_sub_online:
                testSubOnline();
                break;
            case R.id.test_sub_offline:
                testSubOffline();
                break;
            case R.id.close_gateway:
                closeGateway();
                break;
            case R.id.textView_log:
                editTextLog.setText("");
                break;
            default:
                break;
        }
    }

    private void testSubOffline() {
        String nodeId = edtOfflineNodeId.getText().toString();

        if (TextUtils.isEmpty(nodeId)) {
            mToast.setText("nodeId不能为空!");
            mToast.show();
            return;
        }

        Set<String> keySet = subDevices.keySet();
        String offlineDeviceId = null;
        for (String key : keySet) {
            if (nodeId.equals(subDevices.get(key))) {
                offlineDeviceId = key;
                break;
            }
        }

        if (TextUtils.isEmpty(offlineDeviceId)) {
            mToast.setText("nodeId对应的子设备不存在!");
            mToast.show();
            return;
        }

        simpleGateway.reportSubDeviceStatus(offlineDeviceId, "OFFLINE");
    }

    private void testSubOnline() {
        String nodeId = edtOnlineNodeId.getText().toString();

        if (TextUtils.isEmpty(nodeId)) {
            mToast.setText("nodeId不能为空!");
            mToast.show();
            return;
        }

        Set<String> keySet = subDevices.keySet();
        String onlineDeviceId = null;
        for (String key : keySet) {
            if (nodeId.equals(subDevices.get(key))) {
                onlineDeviceId = key;
                break;
            }
        }

        if (TextUtils.isEmpty(onlineDeviceId)) {
            mToast.setText("nodeId对应的子设备不存在!");
            mToast.show();
            return;
        }

        //启动子设备
        TcpDevice tcpDevice = new TcpDevice(nodeId, this, mHandler, HOST, PORT);
        tcpDevice.start();
        tcpDeviceMap.put(nodeId, tcpDevice);
    }

    private void closeGateway() {
        editTextLog.append("关闭网关\n");
        //下线所有子设备
        offlineSubs();
        simpleGateway.close();
    }

    private void offlineSubs() {
        Set<String> keySet = subDevices.keySet();
        ArrayList<DeviceStatus> deviceStatuses = new ArrayList<>();
        for (String deviceId : keySet) {
            DeviceStatus deviceStatus = new DeviceStatus();
            deviceStatus.setStatus("OFFLINE");
            deviceStatus.setDeviceId(deviceId);
            deviceStatuses.add(deviceStatus);
        }

        simpleGateway.reportSubDeviceStatus(deviceStatuses);
    }

    /**
     * 网关删除子设备
     */
    private void testDeleteSub() {
        String nodeId = edtDeleteNodeId.getText().toString();
        if (TextUtils.isEmpty(nodeId)) {
            mToast.setText("nodeId不能为空！");
            mToast.show();
            return;
        }

        String deleteDeviceId = null;

        Set<String> keySet = subDevices.keySet();
        for (String key : keySet) {
            if (nodeId.equals(subDevices.get(key))) {
                deleteDeviceId = key;
                break;
            }
        }

        if (TextUtils.isEmpty(deleteDeviceId)) {
            mToast.setText("不存在nodeId对应的子设备");
            mToast.show();
            return;
        }

        List<String> deviceIds = new ArrayList<>();
        deviceIds.add(deleteDeviceId);
        simpleGateway.reportSubDeviceDelete(deviceIds);
    }

    private void testAddSub() {
        String nodeId = edtAddNodeId.getText().toString();
        if (TextUtils.isEmpty(nodeId)) {
            mToast.setText("nodeId不能为空！");
            mToast.show();
            return;
        }

        editTextLog.append("网关新增子设备请求\n");
        List<DeviceInfo> deviceInfoList = new ArrayList<>();
        DeviceInfo deviceInfo = new DeviceInfo();
        deviceInfo.setNodeId(nodeId);
        deviceInfo.setProductId("5eb4cd4049a5ab087d7d4861");
        deviceInfo.setName(deviceInfo.getNodeId());
        deviceInfoList.add(deviceInfo);

        simpleGateway.reportSubDeviceAdd(deviceInfoList);
    }


    class GatewayBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (IotDeviceIntent.ACTION_IOT_DEVICE_CONNECT.equals(intent.getAction())) {
                int status = intent.getIntExtra(BaseConstant.BROADCAST_STATUS, BaseConstant.STATUS_FAIL);
                switch (status) {
                    case BaseConstant.STATUS_SUCCESS:
                        editTextLog.append("网关创建成功!\n");
                        break;
                    case BaseConstant.STATUS_FAIL:
                        editTextLog.append("网关创建失败!\n");
                        String errorMsg = intent.getStringExtra(BaseConstant.COMMON_ERROR);
                        editTextLog.append("失败原因：" + errorMsg + "\n");
                        break;
                    default:
                        break;
                }
            } else if (IotDeviceIntent.ACTION_IOT_DEVICE_SYS_SUB_ADD_DEVICE_NOTIFY.equals(intent.getAction())) {
                SubDevicesInfo subDevicesInfo = intent.getParcelableExtra(BaseConstant.SUB_DEVICE_LIST);
                subDevicesPersistence.addSubDevices(subDevicesInfo);
                editTextLog.append("平台平台通知网关子设备新增，信息为：\n");
                editTextLog.append(JsonUtil.convertObject2String(subDevicesInfo) + "\n");
                editTextLog.append("\n\n");
                List<DeviceInfo> devices = subDevicesInfo.getDevices();
                for (int i = 0; i < devices.size(); i++) {
                    String nodeId = devices.get(i).getNodeId();
                    String deviceId = devices.get(i).getDeviceId();
                    subDevices.put(deviceId, nodeId);
                }
            } else if (IotDeviceIntent.ACTION_IOT_DEVICE_SYS_SUB_DELETE_DEVICE_NOTIFY.equals(intent.getAction())) {
                SubDevicesInfo subDevicesInfo = intent.getParcelableExtra(BaseConstant.SUB_DEVICE_LIST);
                subDevicesPersistence.deleteSubDevices(subDevicesInfo);
                editTextLog.append("平台通知网关子设备删除，信息为：\n");
                editTextLog.append(JsonUtil.convertObject2String(subDevicesInfo) + "\n");
                editTextLog.append("\n\n");
                List<DeviceInfo> devices = subDevicesInfo.getDevices();
                for (int i = 0; i < devices.size(); i++) {
                    String deviceId = devices.get(i).getDeviceId();
                    String nodeId = subDevices.get(deviceId);
                    subDevices.remove(deviceId);
                    TcpDevice tcpDevice = tcpDeviceMap.get(nodeId);
                    if (tcpDevice == null) {
                        return;
                    }

                    tcpDevice.setStatus("OFFLINE");
                    tcpDeviceMap.remove(nodeId);
                }
            } else if (IotDeviceIntent.ACTION_IOT_DEVICE_SYS_SUB_ADD_DEVICE_RESPONSE.equals(intent.getAction())) {
                SubDevicesAddInfo subDevicesAddInfo = intent.getParcelableExtra(BaseConstant.SUB_DEVICE_ADD);
                editTextLog.append("网关新增子设备请求响应，信息为：\n");
                editTextLog.append(JsonUtil.convertObject2String(subDevicesAddInfo) + "\n");
                editTextLog.append("\n\n");
                List<DeviceInfo> successfulDevices = subDevicesAddInfo.getSuccessfulDevices();

                //添加进缓存
                SubDevicesInfo subDevicesInfo = new SubDevicesInfo();
                subDevicesInfo.setVersion(subDevicesPersistence.getVersion());
                subDevicesInfo.setDevices(successfulDevices);
                subDevicesPersistence.addSubDevices(subDevicesInfo);

                for (int i = 0; i < successfulDevices.size(); i++) {
                    String nodeId = successfulDevices.get(i).getNodeId();
                    String deviceId = successfulDevices.get(i).getDeviceId();
                    subDevices.put(deviceId, nodeId);
                }
            } else if (IotDeviceIntent.ACTION_IOT_DEVICE_SYS_SUB_DELETE_DEVICE_RESPONSE.equals(intent.getAction())) {
                SubDevicesDeleteInfo subDevicesDeleteInfo = intent.getParcelableExtra(BaseConstant.SUB_DEVICE_DELETE);
                editTextLog.append("网关删除子设备请求响应，信息为：\n");
                editTextLog.append(JsonUtil.convertObject2String(subDevicesDeleteInfo) + "\n");
                editTextLog.append("\n\n");


                List<String> successfulDevices = subDevicesDeleteInfo.getSuccessfulDevices();

                List<DeviceInfo> devices = new ArrayList<>();

                for (int i = 0; i < successfulDevices.size(); i++) {
                    String deviceId = successfulDevices.get(i);
                    //删除模拟子设备线程
                    String nodeId = subDevices.get(deviceId);
                    subDevices.remove(deviceId);
                    TcpDevice tcpDevice = tcpDeviceMap.get(nodeId);
                    if (tcpDevice == null) {
                        return;
                    }

                    tcpDevice.setStatus("OFFLINE");
                    tcpDeviceMap.remove(nodeId);

                    DeviceInfo subDevice = subDevicesPersistence.getSubDevice(nodeId);
                    devices.add(subDevice);
                }
                SubDevicesInfo subDevicesInfo = new SubDevicesInfo();
                subDevicesInfo.setVersion(subDevicesPersistence.getVersion());
                subDevicesInfo.setDevices(devices);

                //删除缓存
                subDevicesPersistence.deleteSubDevices(subDevicesInfo);
            } else if (IotDeviceIntent.ACTION_IOT_DEVICE_SYS_SUB_STATUSES_REPORT.equals(intent.getAction())) {
                int reportStatus = intent.getIntExtra(BaseConstant.BROADCAST_STATUS, BaseConstant.STATUS_FAIL);
                switch (reportStatus) {
                    case BaseConstant.STATUS_SUCCESS:
                        ArrayList<DeviceStatus> deviceStatusArrayList = intent.getParcelableArrayListExtra(BaseConstant.SUB_DEVICE_ID_LIST_STATUS);
                        editTextLog.append("子设备状态变化:\n");
                        editTextLog.append(JsonUtil.convertObject2String(deviceStatusArrayList) + "\n");
                        dealDeviceStatus(deviceStatusArrayList);
                        break;
                    default:
                        break;
                }
            }
        }

        private void dealDeviceStatus(ArrayList<DeviceStatus> deviceStatusArrayList) {
            for (int i = 0; i < deviceStatusArrayList.size(); i++) {
                DeviceStatus deviceStatus = deviceStatusArrayList.get(i);
                String status = deviceStatus.getStatus();
                String deviceId = deviceStatus.getDeviceId();
                if ("OFFLINE".equals(status)) {
                    //子设备下线
                    String nodeId = subDevices.get(deviceId);
                    if (TextUtils.isEmpty(nodeId)) {
                        editTextLog.append(nodeId + "子设备不存在!\n");
                        continue;
                    }

                    tcpDeviceMap.get(nodeId).setStatus("OFFLINE");
                    tcpDeviceMap.remove(nodeId);
                } else if ("ONLINE".equals(status)) {
                    //子设备上线
                    String nodeId = subDevices.get(deviceId);
                    if (TextUtils.isEmpty(nodeId)) {
                        editTextLog.append(nodeId + "子设备不存在!\n");
                        continue;
                    }

                    TcpDevice tcpDevice = tcpDeviceMap.get(nodeId);

                    if (tcpDevice == null) {
                        Log.e(TAG, "不存在对应的子设备:" + nodeId);
                        continue;
                    }

                    tcpDevice.setStatus("ONLINE");
                }
            }
        }

    }

}
