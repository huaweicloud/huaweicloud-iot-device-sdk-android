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

import com.huaweicloud.sdk.iot.device.bootstrap.BootstrapClient;
import com.huaweicloud.sdk.iot.device.bootstrap.BootstrapMessage;
import com.huaweicloud.sdk.iot.device.constant.BaseConstant;
import com.huaweicloud.sdk.iot.device.constant.IotDeviceIntent;
import com.huaweicloud.sdk.iot.device.utils.ExceptionUtil;

import java.security.KeyStore;

public class BootstrapActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "BootstrapActivity";

    private EditText edtBootstrapDeviceId;
    private EditText edtBootstrapPassword;
    private EditText editTextLog;
    private Toast mToast;
    private String deviceId;
    private String deviceSecret;

    private BroadcastReceiver bootstrapReceiver = new BootstrapReceiver();

    private BootstrapClient bootstrapClient;

    private String bootstrapUri = "ssl://iot-bs.cn-north-4.myhuaweicloud.com:8883";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bootstrap);
        initViews();
        registerBroadcastReceiver();
    }

    private void registerBroadcastReceiver() {
        LocalBroadcastManager.getInstance(this).registerReceiver(bootstrapReceiver, new IntentFilter(IotDeviceIntent.ACTION_IOT_DEVICE_BOOTSTRAP));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unRegisterBroadcastReceiver();
    }

    private void unRegisterBroadcastReceiver() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(bootstrapReceiver);
    }

    private void initViews() {
        edtBootstrapDeviceId = findViewById(R.id.edt_bootstrap_deviceId);
        edtBootstrapPassword = findViewById(R.id.edt_bootstrap_password);
        editTextLog = findViewById(R.id.edit_text_log);
        mToast = Toast.makeText(this, "", Toast.LENGTH_LONG);
        mToast.setGravity(Gravity.CENTER, 0, 0);
        findViewById(R.id.bt_password_create).setOnClickListener(this);
        findViewById(R.id.bt_keystore_create).setOnClickListener(this);
        findViewById(R.id.bt_no_bootstrap).setOnClickListener(this);
        findViewById(R.id.text_view_log).setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_password_create:
                passwordCreate();
                break;
            case R.id.bt_keystore_create:
                keystoreCreate();
                break;
            case R.id.bt_no_bootstrap:
                noBootstrap();
                break;
            case R.id.text_view_log:
                editTextLog.setText("");
                break;
            default:
                break;
        }
    }

    /**
     * 设备组引导方式bootstrapClient = new BootstrapClient(this, bootstrapUri, deviceId, keyStore, keyPassword, "xxxxx");
     */
    private void keystoreCreate() {
        try {
            deviceId = edtBootstrapDeviceId.getText().toString();
            if (TextUtils.isEmpty(deviceId)) {
                Log.e(TAG, "设备Id为空");
                mToast.setText("设备Id为空");
                mToast.show();
                return;
            }
            String keyPassword;
            KeyStore keyStore = CommonUtils.getKeyStore(this, keyPassword, "deviceCert.pem", "deviceCert.key");

            editTextLog.append("使用keystore创建设备:" + "bootstrapUri=" + bootstrapUri + "\n"
                    + "deviceId=" + deviceId + "\n");
            //创建引导客户端，发起引导
            bootstrapClient = new BootstrapClient(this, bootstrapUri, deviceId, keyStore, keyPassword);
            bootstrapClient.bootstrap();

        } catch (Exception e) {
            Log.e(TAG, "keystoreCreate: " + ExceptionUtil.getBriefStackTrace(e));
        }

    }

    private void noBootstrap() {
        Intent startIntent = new Intent(BootstrapActivity.this, MainActivity.class);
        startActivity(startIntent);
    }

    private void passwordCreate() {
        deviceId = edtBootstrapDeviceId.getText().toString();
        deviceSecret = edtBootstrapPassword.getText().toString();
        if (TextUtils.isEmpty(deviceId) || TextUtils.isEmpty(deviceSecret)) {
            Log.e(TAG, "设备Id或者密码为空");
            mToast.setText("设备Id或者密码为空");
            mToast.show();
            return;
        }

        editTextLog.append("使用密码创建设备:" + "bootstrapUri=" + bootstrapUri + "\n"
                + "deviceId=" + deviceId + "\n"
                + "deviceSecret=" + deviceSecret + "\n");

        //创建引导客户端，发起引导
        bootstrapClient = new BootstrapClient(this, bootstrapUri, deviceId, deviceSecret);
        bootstrapClient.bootstrap();
    }

    private class BootstrapReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (IotDeviceIntent.ACTION_IOT_DEVICE_BOOTSTRAP.equals(intent.getAction())) {
                int status = intent.getIntExtra(BaseConstant.BROADCAST_STATUS, BaseConstant.STATUS_FAIL);
                switch (status) {
                    case BaseConstant.STATUS_SUCCESS:
                        //引导成功后，关闭引导连接
                        bootstrapClient.close();
                        BootstrapMessage bootstrapMessage = intent.getParcelableExtra(BaseConstant.BOOTSTRAP_MESSAGE);
                        String address = bootstrapMessage.getAddress();
                        editTextLog.append("引导成功，IOT平台地址是：\n");
                        editTextLog.append(address + "\n");

                        Intent startIntent = new Intent(BootstrapActivity.this, MainActivity.class);
                        startIntent.putExtra("address", "ssl://" + address);
                        startIntent.putExtra("deviceId", deviceId);
                        startActivity(startIntent);
                        finish();
                        break;
                    case BaseConstant.STATUS_FAIL:
                        String errorMsg = intent.getStringExtra(BaseConstant.COMMON_ERROR);
                        editTextLog.append("设备引导失败，失败原因是:\n");
                        editTextLog.append(errorMsg + "\n");
                        break;
                    default:
                        break;
                }
            }
        }
    }
}