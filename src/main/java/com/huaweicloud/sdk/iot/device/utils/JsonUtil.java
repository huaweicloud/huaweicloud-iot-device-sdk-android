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

import android.util.Log;

import com.google.gson.Gson;

import java.util.Map;

/**
 * Json工具类
 */
public class JsonUtil {

    private static final String TAG = "JsonUtil";

    private static Gson gson = new Gson();

    public static <T> String convertObject2String(T object) {
        if (null == object) {
            return null;
        } else {
            String rStr = null;
            rStr = gson.toJson(object);

            return rStr;
        }
    }

    public static <T> T convertJsonStringToObject(String jsonString, Class<T> cls) {
        if (jsonString == null) {
            return null;
        } else {
            T object = null;
            object = gson.fromJson(jsonString, cls);
            return object;
        }
    }

    public static <T> T convertMap2Object(Map<String, Object> paras, Class<T> cls) {
        if (null == paras || paras.isEmpty()) {
            return null;
        } else {
            String tempJson = gson.toJson(paras);
            Log.i(TAG, "convertMap2Object: " + tempJson);
            T object = null;
            object = gson.fromJson(tempJson, cls);

            return object;
        }
    }
}
