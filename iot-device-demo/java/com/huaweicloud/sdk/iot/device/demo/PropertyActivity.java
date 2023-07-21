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

import com.huaweicloud.sdk.iot.device.client.IotResult;
import com.huaweicloud.sdk.iot.device.client.requests.PropsSet;
import com.huaweicloud.sdk.iot.device.client.requests.ServiceProperty;
import com.huaweicloud.sdk.iot.device.client.requests.ShadowGet;
import com.huaweicloud.sdk.iot.device.client.requests.ShadowMessage;
import com.huaweicloud.sdk.iot.device.constant.BaseConstant;
import com.huaweicloud.sdk.iot.device.constant.IotDeviceIntent;
import com.huaweicloud.sdk.iot.device.utils.JsonUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PropertyActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "PropertyActivity";
    private EditText edtServiceId;
    private EditText edtPropertyKey;
    private EditText edtPropertyValue;
    private EditText edtLog;
    private Toast mToast;
    private BroadcastReceiver propertyBroadcastReceiver = new PropertyBroadcastReceiver();

    /**
     * 平台查询设备属性请求ID
     */
    private String requestId;

    /**
     * 平台查询设备的服务ID
     */
    private String serviceId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_property);
        initViews();
        registerBroadcastReceiver();
    }


    private void registerBroadcastReceiver() {
        LocalBroadcastManager.getInstance(this).registerReceiver(propertyBroadcastReceiver, new IntentFilter(IotDeviceIntent.ACTION_IOT_DEVICE_PROPERTIES_REPORT));
        LocalBroadcastManager.getInstance(this).registerReceiver(propertyBroadcastReceiver, new IntentFilter(IotDeviceIntent.ACTION_IOT_DEVICE_SYS_PROPERTIES_GET));
        LocalBroadcastManager.getInstance(this).registerReceiver(propertyBroadcastReceiver, new IntentFilter(IotDeviceIntent.ACTION_IOT_DEVICE_SYS_PROPERTIES_SET));
        LocalBroadcastManager.getInstance(this).registerReceiver(propertyBroadcastReceiver, new IntentFilter(IotDeviceIntent.ACTION_IOT_DEVICE_SYS_SHADOW_GET));
    }

    private void initViews() {
        edtServiceId = findViewById(R.id.edt_service_id);
        edtPropertyKey = findViewById(R.id.edt_property_key);
        edtPropertyValue = findViewById(R.id.edt_property_value);
        edtLog = findViewById(R.id.edt_log);
        findViewById(R.id.bt_property_report).setOnClickListener(this);
        findViewById(R.id.bt_property_response).setOnClickListener(this);
        findViewById(R.id.tv_log).setOnClickListener(this);
        findViewById(R.id.bt_shadow_get).setOnClickListener(this);
        mToast = Toast.makeText(this, "", Toast.LENGTH_LONG);
        mToast.setGravity(Gravity.CENTER, 0, 0);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_property_report:
                //属性上报
                reportProperty();
                break;
            case R.id.bt_property_response:
                //平台查询设备属性响应
                responseProperty();
                break;
            case R.id.bt_shadow_get:
                //获取影子数据
                getShadow();
                break;
            case R.id.tv_log:
                edtLog.setText("");
                break;
            default:
                Log.i(TAG, "onClick: default");
                break;
        }
    }

    private void getShadow() {
        ShadowGet shadowGet = new ShadowGet();
        edtLog.append("获取影子数据：" + "\n");
        Device.getDevice().getClient().getShadowMessage(shadowGet);
    }

    private void responseProperty() {
        List<ServiceProperty> serviceProperties = getServiceProperties();
        if (serviceProperties == null) {
            Log.i(TAG, "responseProperty: no serviceProperties");
            return;
        }
        edtLog.append("平台查询设备属性响应：" + JsonUtil.convertObject2String(serviceProperties) + "\n");
        Device.getDevice().getClient().respondPropsGet(requestId, serviceProperties);
    }

    private void reportProperty() {
        List<ServiceProperty> serviceProperties = getServiceProperties();
        if (serviceProperties == null) {
            Log.i(TAG, "reportProperty: no serviceProperties");
            return;
        }
        edtLog.append("上报属性：" + JsonUtil.convertObject2String(serviceProperties) + "\n");
        Device.getDevice().getClient().reportProperties(serviceProperties);
    }

    private List<ServiceProperty> getServiceProperties() {
        String serviceId = edtServiceId.getText().toString();
        String propertyKey = edtPropertyKey.getText().toString();
        String propertyValue = edtPropertyValue.getText().toString();
        if (TextUtils.isEmpty(serviceId)) {
            mToast.setText("设备的服务ID为空");
            mToast.show();
            return null;
        }
        if (TextUtils.isEmpty(propertyKey)) {
            mToast.setText("设备的属性为空");
            mToast.show();
            return null;
        }
        if (TextUtils.isEmpty(propertyValue)) {
            mToast.setText("设备的属性值为空");
            mToast.show();
            return null;
        }
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put(propertyKey, propertyValue);
        ServiceProperty serviceProperty = new ServiceProperty();
        serviceProperty.setServiceId(serviceId);
        serviceProperty.setProperties(properties);
        List<ServiceProperty> serviceProperties = new ArrayList<ServiceProperty>();
        serviceProperties.add(serviceProperty);
        return serviceProperties;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unRegisterBroadcastReceiver();
    }

    private void unRegisterBroadcastReceiver() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(propertyBroadcastReceiver);
    }

    private class PropertyBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "onReceive: " + intent.getAction());

            if (IotDeviceIntent.ACTION_IOT_DEVICE_PROPERTIES_REPORT.equals(intent.getAction())) {
                //上报属性结果
                int broadcastStatus = intent.getIntExtra(BaseConstant.BROADCAST_STATUS, BaseConstant.STATUS_FAIL);
                switch (broadcastStatus) {
                    case BaseConstant.STATUS_SUCCESS:
                        edtLog.append("上报属性成功!" + "\n");
                        break;
                    case BaseConstant.STATUS_FAIL:
                        String error = intent.getStringExtra(BaseConstant.PROPERTIES_REPORT_ERROR);
                        edtLog.append("上报属性失败!失败原因：" + error + "\n");
                        break;
                    default:
                        Log.i(TAG, "onReceive: broadcastStatus=" + broadcastStatus);
                        break;
                }
            } else if (IotDeviceIntent.ACTION_IOT_DEVICE_SYS_PROPERTIES_GET.equals(intent.getAction())) {
                //平台查询设备属性广播
                requestId = intent.getStringExtra(BaseConstant.REQUEST_ID);
                serviceId = intent.getStringExtra(BaseConstant.SERVICE_ID);
                edtLog.append("平台查询设备属性: " + "requestId=" + requestId + ",serviceId=" + serviceId + "\n");
                edtServiceId.setText(serviceId);
            } else if (IotDeviceIntent.ACTION_IOT_DEVICE_SYS_PROPERTIES_SET.equals(intent.getAction())) {
                //平台设置设备属性广播
                requestId = intent.getStringExtra(BaseConstant.REQUEST_ID);
                PropsSet propsSet = intent.getParcelableExtra(BaseConstant.SYS_PROPERTIES_SET);
                edtLog.append("平台设置设备属性: " + "requestId=" + requestId + ",PropsSet=" + JsonUtil.convertObject2String(propsSet) + "\n");
                //设备响应平台执行结果
                IotResult iotResult = new IotResult(0, "success");
                Device.getDevice().getClient().respondPropsSet(requestId, iotResult);
                edtLog.append("平台设置设备属性,设备响应结果: " + JsonUtil.convertObject2String(iotResult) + "\n");
            } else if (IotDeviceIntent.ACTION_IOT_DEVICE_SYS_SHADOW_GET.equals(intent.getAction())) {
                //设备影子广播
                requestId = intent.getStringExtra(BaseConstant.REQUEST_ID);
                ShadowMessage shadowMessage = intent.getParcelableExtra(BaseConstant.SHADOW_DATA);
                edtLog.append("设备侧获取平台的设备影子数据: " + "requestId=" + requestId + ",shadowMessage="
                        + JsonUtil.convertObject2String(shadowMessage));
            }
        }
    }


}
