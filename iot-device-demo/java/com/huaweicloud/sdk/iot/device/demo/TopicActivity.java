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

import com.huaweicloud.sdk.iot.device.constant.BaseConstant;
import com.huaweicloud.sdk.iot.device.constant.IotDeviceIntent;
import com.huaweicloud.sdk.iot.device.transport.RawMessage;

import java.io.UnsupportedEncodingException;


public class TopicActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "TopicActivity";

    private EditText edtLog;
    private Toast mToast;
    private EditText edtSubcribeTopicName;
    private EditText edtPublishTopicName;
    private EditText edtMessageContent;
    private CustomizedTopicReceiver customizedTopicReceiver = new CustomizedTopicReceiver();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topic);
        initViews();
        registerBroadcastReceiver();
    }


    private void registerBroadcastReceiver() {
        LocalBroadcastManager.getInstance(this).registerReceiver(customizedTopicReceiver, new IntentFilter(IotDeviceIntent.ACTION_IOT_DEVICE_CUSTOMIZED_TOPIC_CONNECT));
        LocalBroadcastManager.getInstance(this).registerReceiver(customizedTopicReceiver, new IntentFilter(IotDeviceIntent.ACTION_IOT_DEVICE_CUSTOMIZED_TOPIC_MESSAGE));
        LocalBroadcastManager.getInstance(this).registerReceiver(customizedTopicReceiver, new IntentFilter(IotDeviceIntent.ACTION_IOT_DEVICE_CUSTOMIZED_TOPIC_REPORT));
    }

    private void initViews() {
        edtSubcribeTopicName = findViewById(R.id.edt_subcribe_topic_name);
        edtPublishTopicName = findViewById(R.id.edt_publish_topic_name);
        edtMessageContent = findViewById(R.id.edt_message_content);
        edtLog = findViewById(R.id.edt_log);
        findViewById(R.id.tv_log).setOnClickListener(this);
        findViewById(R.id.bt_subcribe_topic).setOnClickListener(this);
        findViewById(R.id.bt_publish_topic).setOnClickListener(this);
        mToast = Toast.makeText(this, "", Toast.LENGTH_LONG);
        mToast.setGravity(Gravity.CENTER, 0, 0);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_log:
                edtLog.setText("");
                break;
            case R.id.bt_subcribe_topic:
                String subcribeTopicName = edtSubcribeTopicName.getText().toString();
                if (TextUtils.isEmpty(subcribeTopicName)) {
                    mToast.setText("topicName不能为空");
                    return;
                }
                Device.getDevice().getClient().subscribeTopic(subcribeTopicName, 0);
                edtLog.append("订阅Topic:" + subcribeTopicName + "\n");
                break;
            case R.id.bt_publish_topic:
                String publishTopicName = edtPublishTopicName.getText().toString();
                if (TextUtils.isEmpty(publishTopicName)) {
                    mToast.setText("topicName不能为空");
                    return;
                }
                String message = edtMessageContent.getText().toString();
                Device.getDevice().getClient().publishTopic(publishTopicName, message, 0);
                edtLog.append("发布Topic:" + publishTopicName + "\n");
                edtLog.append("发布消息为：" + message + "\n");
                break;
            default:
                Log.i(TAG, "onClick: default");
                break;
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unRegisterBroadcastReceiver();
    }

    private void unRegisterBroadcastReceiver() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(customizedTopicReceiver);
    }

    private class CustomizedTopicReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (IotDeviceIntent.ACTION_IOT_DEVICE_CUSTOMIZED_TOPIC_CONNECT.equals(intent.getAction())) {
                int status = intent.getIntExtra(BaseConstant.BROADCAST_STATUS, BaseConstant.STATUS_FAIL);
                String topicName = intent.getStringExtra(BaseConstant.CUSTOMIZED_TOPIC_NAME);
                switch (status) {
                    case BaseConstant.STATUS_SUCCESS:
                        edtLog.append("订阅Topic成功：" + topicName + "\n");
                        break;
                    case BaseConstant.STATUS_FAIL:
                        String errorMessage = intent.getStringExtra(BaseConstant.COMMON_ERROR);
                        edtLog.append("订阅Topic失败：" + topicName + "\n");
                        edtLog.append("失败原因：" + errorMessage + "\n");
                        break;
                    default:
                        Log.i(TAG, "onReceive: status=" + status);
                        break;
                }
            } else if (IotDeviceIntent.ACTION_IOT_DEVICE_CUSTOMIZED_TOPIC_MESSAGE.equals(intent.getAction())) {
                String topicName = intent.getStringExtra(BaseConstant.CUSTOMIZED_TOPIC_NAME);
                RawMessage rawMessage = intent.getParcelableExtra(BaseConstant.CUSTOMIZED_TOPIC_MESSAGE);
                edtLog.append("订阅Topic下发消息：" + topicName + "\n");
                try {
                    edtLog.append("下发消息内容：" + new String(rawMessage.getPayload(), "UTF-8") + "\n");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

            } else if (IotDeviceIntent.ACTION_IOT_DEVICE_CUSTOMIZED_TOPIC_REPORT.equals(intent.getAction())) {
                int status = intent.getIntExtra(BaseConstant.BROADCAST_STATUS, BaseConstant.STATUS_FAIL);
                String topicName = intent.getStringExtra(BaseConstant.CUSTOMIZED_TOPIC_NAME);
                switch (status) {
                    case BaseConstant.STATUS_SUCCESS:
                        edtLog.append("发布Topic成功：" + topicName + "\n");
                        break;
                    case BaseConstant.STATUS_FAIL:
                        String errorMessage = intent.getStringExtra(BaseConstant.COMMON_ERROR);
                        edtLog.append("发布Topic失败：" + topicName + "\n");
                        edtLog.append("失败原因：" + errorMessage + "\n");
                        break;
                    default:
                        Log.i(TAG, "onReceive: status=" + status);
                        break;
                }
            }
        }
    }
}