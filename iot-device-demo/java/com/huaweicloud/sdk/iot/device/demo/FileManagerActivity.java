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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.huaweicloud.sdk.iot.device.constant.BaseConstant;
import com.huaweicloud.sdk.iot.device.constant.IotDeviceIntent;
import com.huaweicloud.sdk.iot.device.filemanager.UrlParam;
import com.huaweicloud.sdk.iot.device.utils.ExceptionUtil;
import com.huaweicloud.sdk.iot.device.utils.JsonUtil;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import java.io.InputStream;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import javax.net.ssl.TrustManager;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class FileManagerActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "FileManagerActivity";
    private EditText edtLog;
    private Toast mToast;
    private EditText edtFileName;
    private EditText edtAttributeKey;
    private EditText edtAttributeValue;
    private EditText edtObjectName;
    private EditText edtResultCode;
    private EditText edtStatusCode;
    private EditText edtStatusDescription;
    private FileManagerBroadcastReceiver fileManagerBroadcastReceiver = new FileManagerBroadcastReceiver();

    private OkHttpClient okHttpClient;

    private String uploadUrl; //获取到的文件上传路径
    private String downloadUrl; //获取到的文件下载路径
    private String uploadFileName = "uploadfile";
    private String downloadFileName = "downloafile";

    private MessageHandler messageHandler = new MessageHandler();

    private static final int UPLOAD_STATUS_SUCCESS = 0;
    private static final int UPLOAD_STATUS_FAILURE = 1;
    private static final int DOWNLOAD_STATUS_SUCCESS = 2;
    private static final int DOWNLOAD_STATUS_FAILURE = 3;

    class MessageHandler extends Handler {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case UPLOAD_STATUS_SUCCESS:
                    edtLog.append("文件上传成功\n\n");
                    //上报上传成功
                    Map<String, Object> paras = new HashMap<String, Object>();
                    paras.put("object_name", uploadFileName);
                    paras.put("result_code", 0);
                    paras.put("status_code", 200);
                    paras.put("status_description", "upload success");
                    Device.getDevice().getFileManager().uploadResultReport(paras);
                    break;
                case UPLOAD_STATUS_FAILURE:
                    edtLog.append("文件上传失败\n\n");
                    //上报上传失败
                    Map<String, Object> uploadFailedParas = new HashMap<String, Object>();
                    uploadFailedParas.put("object_name", uploadFileName);
                    uploadFailedParas.put("result_code", 1);
                    uploadFailedParas.put("status_code", 1);
                    uploadFailedParas.put("status_description", "upload failed");
                    Device.getDevice().getFileManager().uploadResultReport(uploadFailedParas);
                    break;
                case DOWNLOAD_STATUS_SUCCESS:
                    edtLog.append("文件下载成功\n\n");
                    break;
                case DOWNLOAD_STATUS_FAILURE:
                    edtLog.append("文件下载失败\n\n");
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filemanager);
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
                    public boolean verify(String s, SSLSession sslSession) {
                        Log.i(TAG, "verify " + s);
                        return true;
                    }
                })
                .build();
    }

    private SSLSocketFactory createSSLSocketFactory() {
        SSLSocketFactory ssfFactory = null;
        try {

            SSLContext sc = SSLContext.getInstance("TLSv1.2");
            sc.init(null, new TrustManager[]{new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                    Log.i(TAG, "checkClientTrusted: do nothing");
                }

                @Override
                public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                    Log.i(TAG, "checkClientTrusted: do nothing");
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            }
            }, new SecureRandom());
            ssfFactory = sc.getSocketFactory();
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }

        return ssfFactory;
    }

    private void registerBroadcastReceiver() {
        LocalBroadcastManager.getInstance(this).registerReceiver(fileManagerBroadcastReceiver,
                new IntentFilter(IotDeviceIntent.ACTION_IOT_DEVICE_GET_UPLOAD_URL));
        LocalBroadcastManager.getInstance(this).registerReceiver(fileManagerBroadcastReceiver,
                new IntentFilter(IotDeviceIntent.ACTION_IOT_DEVICE_GET_DOWNLOAD_URL));
    }

    private void initViews() {
        edtFileName = findViewById(R.id.edt_file_name);
        edtAttributeKey = findViewById(R.id.edt_attribute_key);
        edtAttributeValue = findViewById(R.id.edt_attribute_value);
        edtObjectName = findViewById(R.id.edt_object_name);
        edtResultCode = findViewById(R.id.edt_result_code);
        edtStatusCode = findViewById(R.id.edt_status_code);
        edtStatusDescription = findViewById(R.id.edt_status_code);
        edtLog = findViewById(R.id.edt_log);
        mToast = Toast.makeText(this, "", Toast.LENGTH_LONG);
        mToast.setGravity(Gravity.CENTER, 0, 0);
        findViewById(R.id.tv_log).setOnClickListener(this);
        findViewById(R.id.bt_get_upload_url).setOnClickListener(this);
        findViewById(R.id.bt_get_download_url).setOnClickListener(this);
        findViewById(R.id.bt_upload_result_report).setOnClickListener(this);
        findViewById(R.id.bt_download_result_report).setOnClickListener(this);
        findViewById(R.id.bt_file_upload).setOnClickListener(this);
        findViewById(R.id.bt_file_download).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_get_upload_url:
                getUploadUrl();
                break;
            case R.id.bt_get_download_url:
                getDownloadUrl();
                break;
            case R.id.bt_file_upload:
                uploadFile();
                break;
            case R.id.bt_file_download:
                downloadFile();
                break;
            case R.id.bt_upload_result_report:
                uploadResultReport();
                break;
            case R.id.bt_download_result_report:
                downloadResultReport();
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
     * 获取文件的属性，demo只给出文件的hash值和size，用户需要自行扩展
     *
     * @param fileName 文件名
     * @return
     */
    private Map<String, Object> getFileAttributes(String fileName) {
        if (TextUtils.isEmpty(fileName)) {
            return null;
        }

        Map<String, Object> fileAttributes = new HashMap<String, Object>();
        fileAttributes.put("hash_code", CommonUtils.getHash(fileName));
        long fileLength = 0L;
        File file = new File(fileName);
        if (file.exists() && file.isFile()) {
            fileLength = file.length();
        }
        fileAttributes.put("size", fileLength);

        return fileAttributes;
    }

    private void downloadFile() {
        Request request = new Request.Builder()
                .url(downloadUrl)
                .get()
                .header("Content-Type", "text/plain")
                .build();

        Call call = okHttpClient.newCall(request);

        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "onFailure: " + ExceptionUtil.getBriefStackTrace(e));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.i(TAG, "onResponse: " + response.toString());

                if (call.isCanceled()) {
                    messageHandler.sendEmptyMessage(DOWNLOAD_STATUS_FAILURE);
                    return;
                }

                if (!response.isSuccessful()) {
                    messageHandler.sendEmptyMessage(DOWNLOAD_STATUS_FAILURE);
                    return;
                }

                BufferedInputStream bufferedInputStream = null;
                FileOutputStream fileOutputStream = null;

                try {
                    bufferedInputStream = new BufferedInputStream(response.body().byteStream());
                    fileOutputStream = openFileOutput(downloadFileName, Context.MODE_PRIVATE);

                    byte[] bytes = new byte[1024];
                    int length = 0;

                    while ((length = bufferedInputStream.read(bytes)) != -1) {
                        fileOutputStream.write(bytes, 0, length);
                        fileOutputStream.flush();
                    }

                } catch (Exception e) {
                    Log.e(TAG, "onResponse: " + ExceptionUtil.getBriefStackTrace(e));
                    messageHandler.sendEmptyMessage(DOWNLOAD_STATUS_FAILURE);
                } finally {
                    if (bufferedInputStream != null) {
                        bufferedInputStream.close();
                    }

                    if (fileOutputStream != null) {
                        fileOutputStream.close();
                    }
                }

                messageHandler.sendEmptyMessage(DOWNLOAD_STATUS_SUCCESS);
            }
        });
    }

    /**
     * 文件上传，用户可以根据自己需要修改
     */
    private void uploadFile() {
        InputStream inputStream = null;
        FileOutputStream fileOutputStream = null;
        try {
            inputStream = getResources().getAssets().open("DigiCertGlobalRootCA.bks");
            fileOutputStream = openFileOutput(uploadFileName, Context.MODE_PRIVATE);
            byte[] bytes = new byte[1024];
            int length = 0;
            while ((length = inputStream.read(bytes)) != -1) {
                fileOutputStream.write(bytes, 0, length);
            }

            fileOutputStream.flush();
        } catch (Exception e) {
            Log.e(TAG, "uploadFile: " + ExceptionUtil.getBriefStackTrace(e));
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    Log.e(TAG, "uploadFile: " + ExceptionUtil.getBriefStackTrace(e));
                }
            }

            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    Log.e(TAG, "uploadFile: " + ExceptionUtil.getBriefStackTrace(e));
                }
            }
        }

        //上传文件到平台给定url
        String filePath = getFilesDir().getAbsolutePath() + File.separator + uploadFileName;
        Log.i(TAG, "uploadFile: " + filePath);
        File uploadFile = new File(filePath);
        if (!uploadFile.exists()) {
            Log.e(TAG, "uploadFile: " + filePath + " no exists!");
            return;
        }

        final MediaType mediaType = MediaType.parse("text/plain");
        RequestBody requestBody = RequestBody.create(mediaType, uploadFile);
        Request request = new Request.Builder()
                .url(uploadUrl)
                .put(requestBody)
                .build();
        Call call = okHttpClient.newCall(request);

        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "onFailure: " + ExceptionUtil.getBriefStackTrace(e));
                messageHandler.sendEmptyMessage(UPLOAD_STATUS_FAILURE);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.i(TAG, "onResponse: " + response.toString());
                if (call.isCanceled()) {
                    messageHandler.sendEmptyMessage(UPLOAD_STATUS_FAILURE);
                    return;
                }

                if (!response.isSuccessful()) {
                    messageHandler.sendEmptyMessage(UPLOAD_STATUS_FAILURE);
                    return;
                }

                messageHandler.sendEmptyMessage(UPLOAD_STATUS_SUCCESS);
            }
        });
    }

    private void downloadResultReport() {
        Map<String, Object> paras = getStringObjectMap();
        if (paras == null) {
            Log.i(TAG, "downloadResultReport: no paras");
            return;
        }

        Device.getDevice().getFileManager().downloadResultReport(paras);
    }

    private void uploadResultReport() {
        Map<String, Object> paras = getStringObjectMap();
        if (paras == null) {
            Log.i(TAG, "uploadResultReport: no paras");
            return;
        }

        Device.getDevice().getFileManager().uploadResultReport(paras);
    }

    private Map<String, Object> getStringObjectMap() {
        String objectName = edtObjectName.getText().toString();
        if (TextUtils.isEmpty(objectName)) {
            mToast.setText("文件名称为空！");
            return null;
        }

        Integer resultCode = null;
        try {
            resultCode = Integer.parseInt(edtResultCode.getText().toString());
        } catch (NumberFormatException e) {
            mToast.setText("文件状态必须为整数！");
            return null;
        }

        Integer statusCode = null;
        String strStatusCode = edtStatusCode.getText().toString();
        if (!TextUtils.isEmpty(strStatusCode)) {
            try {
                statusCode = Integer.parseInt(strStatusCode);
            } catch (NumberFormatException e) {
                mToast.setText("状态码必须为整数！");
                return null;
            }
        }

        String statusDescription = edtStatusDescription.getText().toString();
        Map<String, Object> paras = new HashMap<String, Object>();
        paras.put("object_name", objectName);
        paras.put("result_code", resultCode);
        paras.put("status_code", statusCode);
        paras.put("status_description", statusDescription);
        return paras;
    }

    private void getDownloadUrl() {
        String fileName = edtFileName.getText().toString();
        if (TextUtils.isEmpty(fileName)) {
            mToast.setText("文件名称为空！");
            return;
        }

        Map<String, Object> fileAttributes = new HashMap<String, Object>();
        String attributeKey = edtAttributeKey.getText().toString();
        String attributeValue = edtAttributeValue.getText().toString();
        fileAttributes.put(attributeKey, attributeValue);

        Device.getDevice().getFileManager().getDownloadUrl(fileName, fileAttributes);
    }

    private void getUploadUrl() {
        String fileName = edtFileName.getText().toString();
        if (TextUtils.isEmpty(fileName)) {
            mToast.setText("文件名称为空！");
            return;
        }

        Map<String, Object> fileAttributes = new HashMap<String, Object>();

        String filePath = getFilesDir().getAbsolutePath() + File.separator + uploadFileName;
        Log.e(TAG, "getUploadUrl: " + filePath);
        fileAttributes.putAll(getFileAttributes(filePath));

        String attributeKey = edtAttributeKey.getText().toString();
        String attributeValue = edtAttributeValue.getText().toString();
        fileAttributes.put(attributeKey, attributeValue);

        Device.getDevice().getFileManager().getUploadUrl(fileName, fileAttributes);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unRegisterBroadcastReceiver();
    }

    private void unRegisterBroadcastReceiver() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(fileManagerBroadcastReceiver);
    }

    private class FileManagerBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "onReceive: " + intent.getAction());

            if (IotDeviceIntent.ACTION_IOT_DEVICE_GET_UPLOAD_URL.equals(intent.getAction())) {
                UrlParam urlParam = intent.getParcelableExtra(BaseConstant.URLPARAM_INFO);
                edtLog.append("平台下发文件上传临时URL:" + JsonUtil.convertObject2String(urlParam) + "\n");
                uploadUrl = urlParam.getUrl();

            } else if (IotDeviceIntent.ACTION_IOT_DEVICE_GET_DOWNLOAD_URL.equals(intent.getAction())) {
                UrlParam urlParam = intent.getParcelableExtra(BaseConstant.URLPARAM_INFO);
                edtLog.append("平台下发文件下载临时URL:" + JsonUtil.convertObject2String(urlParam) + "\n");
                downloadUrl = urlParam.getUrl();
            }
        }
    }


}