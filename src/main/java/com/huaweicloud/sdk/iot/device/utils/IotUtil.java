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

package com.huaweicloud.sdk.iot.device.utils;

import android.content.Context;
import android.util.Log;

import com.huaweicloud.sdk.iot.device.client.ClientConf;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import java.security.KeyStore;
import java.security.SecureRandom;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicLong;


/**
 * IOT工具类
 */
public class IotUtil {


    private static final String TLS_V_1_2 = "TLSv1.2";
    private static final String TAG = "IotUtil";
    private static AtomicLong requestId = new AtomicLong(0);

    /**
     * 从topic里解析出requestId
     *
     * @param topic topic
     * @return requestId
     */
    public static String getRequestId(String topic) {
        String[] tmp = topic.split("request_id=");
        return tmp[1];
    }

    /**
     * 从deviceid解析nodeId
     *
     * @param deviceId 设备id
     * @return 设备物理标识
     */
    public static String getNodeIdFromDeviceId(String deviceId) {

        try {
            return deviceId.substring(deviceId.indexOf("_") + 1);
        } catch (Exception e) {
            Log.e(TAG, ExceptionUtil.getBriefStackTrace(e));
            return null;
        }

    }

    /**
     * 根据请求topic构造响应topic
     *
     * @param topic 请求topic
     * @return 响应topic
     */
    public static String makeRspTopic(String topic) {

        try {
            String[] tmp = topic.split("request_id");
            return tmp[0] + "response/" + "request_id" + tmp[1];
        } catch (Exception e) {
            Log.e(TAG, ExceptionUtil.getBriefStackTrace(e));
            return null;
        }
    }

    /**
     * 获取当前时间戳
     *
     * @return 当前时间戳
     */
    public static String getTimeStamp() {

        String msgTimestampFormat = "yyyyMMdd'T'HHmmss'Z'";

        SimpleDateFormat df = new SimpleDateFormat(msgTimestampFormat);
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        return df.format(new Date(System.currentTimeMillis()));

    }

    /**
     * 生成requestId
     *
     * @return requestId
     */
    public static String generateRequestId() {


        return Long.toString(requestId.incrementAndGet());

    }

    /**
     * HmacSHA256
     *
     * @param str       输入字符串
     * @param timeStamp 时间戳
     * @return hash后的字符串
     */
    public static String sha256_mac(String str, String timeStamp) {
        String passWord = null;
        try {
            Mac sha256Hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(timeStamp.getBytes("UTF-8"), "HmacSHA256");
            sha256Hmac.init(secretKey);
            byte[] bytes = sha256Hmac.doFinal(str.getBytes("UTF-8"));
            passWord = byteArrayToHexString(bytes);
        } catch (Exception e) {
            Log.e(TAG, ExceptionUtil.getBriefStackTrace(e));
        }
        return passWord;
    }

    /**
     * bytes转十六进制字符串
     *
     * @param b bytes
     * @return 十六进制字符串
     */
    public static String byteArrayToHexString(byte[] b) {
        StringBuilder sb = new StringBuilder();
        String temp;
        for (int i = 0; b != null && i < b.length; i++) {
            temp = Integer.toHexString(b[i] & 0XFF);
            if (temp.length() == 1) {
                sb.append('0');
            }
            sb.append(temp);
        }
        return sb.toString().toLowerCase(Locale.US);
    }


    private static TrustManager[] getTrustManager(Context mContext) throws Exception {
        KeyStore keyStore = KeyStore.getInstance("BKS");
        keyStore.load(mContext.getAssets().open("DigiCertGlobalRootCA.bks"), null);

        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(keyStore);
        TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();

        return trustManagers;
    }

    private static SSLContext getSSLContextWithKeystore(Context mContext, KeyStore keyStore, String keyPassword) throws Exception {
        SSLContext sslContext = SSLContext.getInstance(TLS_V_1_2);

        KeyManagerFactory managerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        managerFactory.init(keyStore, keyPassword.toCharArray());
        sslContext.init(managerFactory.getKeyManagers(), getTrustManager(mContext), null);
        return sslContext;
    }

    /**
     * 根据配置获取ssl上下文
     *
     * @param clientConf 客户端配置
     * @return ssl上下文
     * @throws Exception ssl相关异常
     */
    public static SSLContext getSSLContext(Context mContext, ClientConf clientConf) throws Exception {


        if (clientConf.getKeyStore() != null) {
            return getSSLContextWithKeystore(mContext, clientConf.getKeyStore(), clientConf.getKeyPassword());
        } else {
            SSLContext sslContext = SSLContext.getInstance(TLS_V_1_2);
            sslContext.init(null, getTrustManager(mContext), new SecureRandom());
            return sslContext;
        }
    }

}
