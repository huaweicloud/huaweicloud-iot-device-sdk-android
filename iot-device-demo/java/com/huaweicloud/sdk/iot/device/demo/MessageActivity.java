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

import com.huaweicloud.sdk.iot.device.client.requests.Command;
import com.huaweicloud.sdk.iot.device.client.requests.CommandRsp;
import com.huaweicloud.sdk.iot.device.client.requests.DeviceMessage;
import com.huaweicloud.sdk.iot.device.client.requests.RawDeviceMessage;
import com.huaweicloud.sdk.iot.device.constant.BaseConstant;
import com.huaweicloud.sdk.iot.device.constant.IotDeviceIntent;
import com.huaweicloud.sdk.iot.device.utils.JsonUtil;

import java.util.HashMap;
import java.util.Map;


public class MessageActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MessageActivity";

    private EditText edtMessageContent;
    private EditText edtMessageName;
    private EditText edtMessageId;
    private Toast mToast;
    private EditText edtLog;
    private MessageBroadcastReceiver messageBroadcastReceiver = new MessageBroadcastReceiver();
    private EditText edtResponseName;
    private EditText edtParasKey;
    private EditText edtParasValue;
    private String requestId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        initViews();
        registerBroadcastReceiver();
    }


    private void registerBroadcastReceiver() {
        LocalBroadcastManager.getInstance(this).registerReceiver(messageBroadcastReceiver, new IntentFilter(IotDeviceIntent.ACTION_IOT_DEVICE_SYS_MESSAGES_UP));
        LocalBroadcastManager.getInstance(this).registerReceiver(messageBroadcastReceiver, new IntentFilter(IotDeviceIntent.ACTION_IOT_DEVICE_SYS_MESSAGES_DOWN));
        LocalBroadcastManager.getInstance(this).registerReceiver(messageBroadcastReceiver, new IntentFilter(IotDeviceIntent.ACTION_IOT_DEVICE_SYS_COMMANDS));
    }

    private void initViews() {
        edtMessageContent = findViewById(R.id.edt_message_content);
        edtMessageName = findViewById(R.id.edt_message_name);
        edtMessageId = findViewById(R.id.edt_message_id);
        edtLog = findViewById(R.id.edt_log);
        mToast = Toast.makeText(this, "", Toast.LENGTH_LONG);
        mToast.setGravity(Gravity.CENTER, 0, 0);
        findViewById(R.id.bt_message_report).setOnClickListener(this);
        findViewById(R.id.tv_log).setOnClickListener(this);

        edtResponseName = findViewById(R.id.edt_response_name);
        edtParasKey = findViewById(R.id.edt_paras_key);
        edtParasValue = findViewById(R.id.edt_paras_value);
        findViewById(R.id.bt_command_response).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_message_report:
                //属性上报
                messageProperty();
                break;
            case R.id.bt_command_response:
                //命令响应
                commandResponse();
                break;
            case R.id.tv_log:
                edtLog.setText("");
                break;
            default:
                Log.i(TAG, "onClick: default");
                break;
        }
    }

    private void commandResponse() {
        CommandRsp commandRsp = new CommandRsp(CommandRsp.SUCCESS);
        commandRsp.setResponseName(edtResponseName.getText().toString());
        Map<String, Object> paras = new HashMap<String, Object>();
        paras.put(edtParasKey.getText().toString(), edtParasValue.getText().toString());
        commandRsp.setParas(paras);
        Device.getDevice().getClient().respondCommand(requestId, commandRsp);
        edtLog.append("响应下发命令：" + JsonUtil.convertObject2String(commandRsp));
    }

    private void messageProperty() {
        String content = edtMessageContent.getText().toString();
        if (TextUtils.isEmpty(content)) {
            mToast.setText("消息内容为空！");
            mToast.show();
            return;
        }
        String name = edtMessageName.getText().toString();
        String id = edtMessageId.getText().toString();

        DeviceMessage deviceMessage = new DeviceMessage();
        deviceMessage.setContent(content);
        deviceMessage.setId(id);
        deviceMessage.setName(name);

        Device.getDevice().getClient().reportDeviceMessage(deviceMessage);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unRegisterBroadcastReceiver();
    }

    private void unRegisterBroadcastReceiver() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(messageBroadcastReceiver);
    }

    private class MessageBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "onReceive: " + intent.getAction());

            if (IotDeviceIntent.ACTION_IOT_DEVICE_SYS_MESSAGES_UP.equals(intent.getAction())) {
                int broadcastStatus = intent.getIntExtra(BaseConstant.BROADCAST_STATUS, BaseConstant.STATUS_FAIL);
                switch (broadcastStatus) {
                    case BaseConstant.STATUS_SUCCESS:
                        edtLog.append("消息上报成功！" + "\n");
                        break;
                    case BaseConstant.STATUS_FAIL:
                        String error = intent.getStringExtra(BaseConstant.COMMON_ERROR);
                        edtLog.append("消息上报失败！失败原因：" + error + "\n");
                        break;
                    default:
                        Log.i(TAG, "onReceive: broadcastStatus=" + broadcastStatus);
                        break;
                }
            } else if (IotDeviceIntent.ACTION_IOT_DEVICE_SYS_MESSAGES_DOWN.equals(intent.getAction())) {
                RawDeviceMessage deviceMessage = intent.getParcelableExtra(BaseConstant.SYS_DOWN_MESSAGES);
                Log.i(TAG, "平台下发的消息为：" + deviceMessage.toUTF8String());
                edtLog.append("平台下发的消息为：" + JsonUtil.convertObject2String(deviceMessage) + "\n");
            } else if (IotDeviceIntent.ACTION_IOT_DEVICE_SYS_COMMANDS.equals(intent.getAction())) {
                requestId = intent.getStringExtra(BaseConstant.REQUEST_ID);
                Command command = intent.getParcelableExtra(BaseConstant.SYS_COMMANDS);
                edtLog.append("平台下发的命令为:" + JsonUtil.convertObject2String(command));
            }
        }
    }


}
