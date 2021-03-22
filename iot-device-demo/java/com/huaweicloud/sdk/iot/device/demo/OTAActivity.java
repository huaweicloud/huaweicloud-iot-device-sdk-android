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
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.huaweicloud.sdk.iot.device.constant.BaseConstant;
import com.huaweicloud.sdk.iot.device.constant.IotDeviceIntent;
import com.huaweicloud.sdk.iot.device.ota.OTAPackage;
import com.huaweicloud.sdk.iot.device.ota.OTAService;
import com.huaweicloud.sdk.iot.device.utils.ExceptionUtil;
import com.huaweicloud.sdk.iot.device.utils.JsonUtil;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import androidx.annotation.NonNull;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import androidx.appcompat.app.AppCompatActivity;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;


public class OTAActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "OTAActivity";

    private EditText edtSwVersion;
    private EditText edtFwVersion;
    private EditText edtDescription;
    private EditText edtVersion;
    private EditText edtResultCode;
    private EditText edtProgress;
    private EditText edtLog;
    private Toast mToast;
    private UpgradeBroadcastReceiver upgradeBroadcastReceiver = new UpgradeBroadcastReceiver();
    private OTAPackage otaPackage;
    private OkHttpClient okHttpClient;

    private static final int UPGRADE_STATUS_SUCCESS = 0;
    private static final int UPGRADE_STATUS_FAILURE = 1;

    class MessageHandler extends Handler {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case UPGRADE_STATUS_SUCCESS://升级成功
                    //校验下载的升级包
                    if (checkPackage(otaPackage, (String) msg.obj) != 0) {
                        return;
                    }

                    //安装升级包
                    if (installPackage() != 0) {
                        return;
                    }

                    //上报升级成功，注意版本号要携带更新后的版本号，否则平台会认为升级失败
                    String version = otaPackage.getVersion();
                    Device.getDevice().getOtaService().reportOtaStatus(OTAService.OTA_CODE_SUCCESS, 100, version, "upgrade success");
                    edtLog.append("升级成功\n");
                    break;
                case UPGRADE_STATUS_FAILURE://升级失败
                    /**
                     * 当使用软件升级时，这里的版本号取edtSwVersion的值
                     */
                    String oldVersion = edtFwVersion.getText().toString();
                    Device.getDevice().getOtaService().reportOtaStatus(OTAService.OTA_CODE_DOWNLOAD_TIMEOUT, 0, oldVersion, "upgrade failed");
                    edtLog.append("升级失败\n");
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 安装升级包，需要用户实现
     * 如果安装失败，上报OTA_CODE_INSTALL_FAIL
     * otaService.reportOtaStatus(OTAService.OTA_CODE_INSTALL_FAIL, 0, version,null);
     */
    private int installPackage() {
        //TODO
        Log.i(TAG, "installPackage ok");

        return 0;
    }


    /**
     * 检验升级包
     *
     * @param otaPackage
     * @param md5
     * @return
     */
    private int checkPackage(OTAPackage otaPackage, String md5) {
        if (!md5.equalsIgnoreCase(otaPackage.getSign())) {
            Log.e(TAG, "md5 check fail");
            /**
             * 当使用软件升级时，这里的版本号取edtSwVersion的值
             */
            String version = edtFwVersion.getText().toString();
            Device.getDevice().getOtaService().reportOtaStatus(OTAService.OTA_CODE_CHECK_FAIL, 0, version, "md5 check fail");

            return -1;
        }

        //TODO 增加其他校验
        return 0;
    }

    private Handler mHandler = new MessageHandler();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ota);
        initViews();
        initData();
        registerBroadcastReceiver();
    }

    private void initData() {
        this.okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(600, TimeUnit.SECONDS)
                .sslSocketFactory(createSSLSocketFactory()).hostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String hostname, SSLSession session) {
                        return true;
                    }
                })
                .build();
    }


    private void registerBroadcastReceiver() {
        LocalBroadcastManager.getInstance(this).registerReceiver(upgradeBroadcastReceiver,
                new IntentFilter(IotDeviceIntent.ACTION_IOT_DEVICE_UPGRADE_EVENT));
        LocalBroadcastManager.getInstance(this).registerReceiver(upgradeBroadcastReceiver,
                new IntentFilter(IotDeviceIntent.ACTION_IOT_DEVICE_VERSION_QUERY_EVENT));
    }

    private void initViews() {
        edtSwVersion = findViewById(R.id.edt_sw_version);
        edtFwVersion = findViewById(R.id.edt_fw_version);
        edtDescription = findViewById(R.id.edt_description);
        edtVersion = findViewById(R.id.edt_version);
        edtResultCode = findViewById(R.id.edt_result_code);
        edtProgress = findViewById(R.id.edt_progress);
        edtLog = findViewById(R.id.edt_log);
        mToast = Toast.makeText(this, "", Toast.LENGTH_LONG);
        mToast.setGravity(Gravity.CENTER, 0, 0);
        findViewById(R.id.bt_version_report).setOnClickListener(this);
        findViewById(R.id.bt_upgrade_response).setOnClickListener(this);
        findViewById(R.id.tv_log).setOnClickListener(this);
        findViewById(R.id.bt_download_upgrade).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_version_report:
                versionReport();
                break;
            case R.id.bt_download_upgrade:
                downloadUpgrade();
                break;
            case R.id.bt_upgrade_response:
                upgradeResponse();
                break;
            case R.id.tv_log:
                edtLog.setText("");
                break;
            default:
                Log.i(TAG, "onClick: default");
                break;
        }
    }

    /**
     * 模拟下载升级软固件升级
     */
    private void downloadUpgrade() {
        if (TextUtils.isEmpty(otaPackage.getUrl()) || TextUtils.isEmpty(otaPackage.getToken())) {
            edtLog.append("no url or token for download!\n");
            return;
        }

        Request request = new Request.Builder()
                .url(otaPackage.getUrl()).header("Authorization", "Bearer " + otaPackage.getToken())
                .build();
        Call call = okHttpClient.newCall(request);

        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "onFailure: " + ExceptionUtil.getExceptionCause(e));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.i(TAG, "onResponse: " + response.toString());

                if (call.isCanceled()) {
                    return;
                }

                if (!response.isSuccessful()) {
                    Message message = Message.obtain();
                    message.what = UPGRADE_STATUS_FAILURE;
                    mHandler.sendMessage(message);
                    return;
                }

                BufferedInputStream bufferedInputStream = null;
                FileOutputStream fileOutputStream = null;

                try {
                    ResponseBody responseBody = response.body();
                    bufferedInputStream = new BufferedInputStream(responseBody.byteStream());
                    /**
                     * 升级文件下载路径，运行时请自定义
                     */
                    String downloadPath = "upgrade.download";
                    fileOutputStream = openFileOutput(downloadPath, Context.MODE_PRIVATE);

                    MessageDigest digest = MessageDigest.getInstance("SHA-256");

                    long total = responseBody.contentLength();
                    byte[] bytes = new byte[1024 * 10];
                    int len;
                    long current = 0;
                    while ((len = bufferedInputStream.read(bytes)) != -1) {
                        fileOutputStream.write(bytes, 0, len);
                        fileOutputStream.flush();
                        current += len;

                        //计算进度
                        int progress = (int) (100 * current / total);
                        Log.i(TAG, "progress = " + progress);
                        //发送进度到主线程

                        //计算md5
                        digest.update(bytes, 0, len);

                    }

                    if (current == total) {
                        String md5 = CommonUtils.bytesToHexString(digest.digest());
                        Log.i(TAG, "md5 = " + md5);
                        Message message = Message.obtain();
                        message.what = UPGRADE_STATUS_SUCCESS;
                        message.obj = md5;
                        mHandler.sendMessage(message);
                    }

                } catch (Exception e) {
                    Log.e(TAG, "onResponse: " + ExceptionUtil.getBriefStackTrace(e));
                    Message message = Message.obtain();
                    message.what = UPGRADE_STATUS_FAILURE;
                    mHandler.sendMessage(message);
                } finally {
                    if (fileOutputStream != null) {
                        fileOutputStream.close();
                    }
                    if (bufferedInputStream != null) {
                        bufferedInputStream.close();
                    }
                }
            }
        });
    }

    private SSLSocketFactory createSSLSocketFactory() {
        SSLSocketFactory ssfFactory = null;
        try {

            SSLContext sc = SSLContext.getInstance("TLSv1.2");
            sc.init(null, new TrustManager[]{new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

                }

                @Override
                public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            } }, new SecureRandom());
            ssfFactory = sc.getSocketFactory();
        } catch (Exception ignored) {
            Log.e(TAG, "createSSLSocketFactory: " + ExceptionUtil.getBriefStackTrace(ignored));
        }

        return ssfFactory;
    }

    private void upgradeResponse() {
        Integer resultCode = null;
        Integer progress = null;
        String version = null;
        String description = null;

        try {
            resultCode = Integer.parseInt(edtResultCode.getText().toString());
        } catch (NumberFormatException e) {
            mToast.setText("输入的结果码不是整数");
            return;
        }

        String strProgress = edtProgress.getText().toString();
        if (!TextUtils.isEmpty(strProgress)) {
            try {
                progress = Integer.parseInt(strProgress);
            } catch (NumberFormatException e) {
                mToast.setText("输入的升级进度不是整数");
                return;
            }
        }

        version = edtVersion.getText().toString();
        if (TextUtils.isEmpty(version)) {
            mToast.setText("输入的版本号不能为空");
            return;
        }

        description = edtDescription.getText().toString();
        if (Device.getDevice().getOtaService() == null) {
            Log.i(TAG, "upgradeResponse: no otaService");
            return;
        }

        Device.getDevice().getOtaService().reportOtaStatus(resultCode, progress, version, description);

    }

    private void versionReport() {
        String swVersion = edtSwVersion.getText().toString();
        String fwVersion = edtFwVersion.getText().toString();
        Device.getDevice().getOtaService().reportVersion(fwVersion, swVersion);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unRegisterBroadcastReceiver();
    }

    private void unRegisterBroadcastReceiver() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(upgradeBroadcastReceiver);
    }

    private class UpgradeBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "onReceive: " + intent.getAction());

            if (IotDeviceIntent.ACTION_IOT_DEVICE_VERSION_QUERY_EVENT.equals(intent.getAction())) {
                edtLog.append("平台下发获取版本信息通知！" + "\n");
            } else if (IotDeviceIntent.ACTION_IOT_DEVICE_UPGRADE_EVENT.equals(intent.getAction())) {
                OTAPackage pkg = intent.getParcelableExtra(BaseConstant.OTAPACKAGE_INFO);
                otaPackage = pkg;
                edtLog.append("平台下发升级通知:" + JsonUtil.convertObject2String(pkg) + "\n");
            }
        }
    }


}