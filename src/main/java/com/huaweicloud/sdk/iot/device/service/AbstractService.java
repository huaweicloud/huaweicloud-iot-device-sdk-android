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

package com.huaweicloud.sdk.iot.device.service;

import android.util.Log;

import com.huaweicloud.sdk.iot.device.client.IotResult;
import com.huaweicloud.sdk.iot.device.client.requests.Command;
import com.huaweicloud.sdk.iot.device.client.requests.CommandRsp;
import com.huaweicloud.sdk.iot.device.client.requests.DeviceEvent;
import com.huaweicloud.sdk.iot.device.utils.ExceptionUtil;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Provides automatic properties read/write and command invoking capabilities. You can inherit this class and define your own services based on the product model.
 */
public abstract class AbstractService implements IService {

    private static final String TAG = "AbstractService";

    private AbstractDevice iotDevice;
    private Map<String, Method> commands = new HashMap<String, Method>();
    private Map<String, Field> writeableFields = new HashMap<String, Field>();
    private Map<String, FieldPair> readableFields = new HashMap<String, FieldPair>();
    private Timer timer;
    private String serviceId;

    private static class FieldPair {
        public String propertyName;
        public Field field;

        public FieldPair(String propertyName, Field field) {
            this.propertyName = propertyName;
            this.field = field;
        }
    }

    public AbstractService() {

        for (Field field : this.getClass().getDeclaredFields()) {

            Property property = field.getAnnotation(Property.class);
            if (property == null) {
                continue;
            }

            String name = property.name();
            if (name.isEmpty()) {
                name = field.getName();
            }
            if (property.writeable()) {
                writeableFields.put(name, field);
            }

            // Key indicates the field name, and the property name is stored in the pair.
            readableFields.put(field.getName(), new FieldPair(name, field));
        }

        for (Method method : this.getClass().getDeclaredMethods()) {
            DeviceCommand deviceCommand = method.getAnnotation(DeviceCommand.class);
            if (deviceCommand == null) {
                continue;
            }
            String name = deviceCommand.name();
            if (name.isEmpty()) {
                name = method.getName();
            }
            commands.put(name, method);
        }

    }

    private Object getFiledValue(String fieldName) {

        Field field = readableFields.get(fieldName).field;
        if (field == null) {
            Log.e(TAG, "field is null: " + fieldName);
            return null;
        }
        String getter = "get" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
        Method method;

        try {
            method = this.getClass().getDeclaredMethod(getter);
        } catch (NoSuchMethodException e) {
            Log.e(TAG, ExceptionUtil.getBriefStackTrace(e));
            return null;
        }

        if (method == null) {
            Log.e(TAG, "method is null: " + getter);
            return null;
        }

        try {
            Object value = method.invoke(this);
            return value;
        } catch (IllegalAccessException e) {
            Log.e(TAG, ExceptionUtil.getBriefStackTrace(e));
        } catch (InvocationTargetException e) {
            Log.e(TAG, ExceptionUtil.getBriefStackTrace(e));
        }

        return null;

    }


    /**
     * Called when a property read request is received.
     *
     * @param fields Indicates the names of fields to read. If it is set to NULL, all fields are read.
     * @return Returns the property values.
     */
    @Override
    public Map<String, Object> onRead(String... fields) {

        Map<String, Object> ret = new HashMap<String, Object>();

        // Reads specified fields.
        if (fields.length > 0) {
            for (String fieldName : fields) {

                if (readableFields.get(fieldName) == null) {
                    Log.e(TAG, "field is not readable:" + fieldName);
                    continue;
                }

                Object value = getFiledValue(fieldName);
                if (value != null) {
                    ret.put(readableFields.get(fieldName).propertyName, value);
                }
            }

            return ret;
        }

        // Reads all fields.
        for (Map.Entry<String, FieldPair> entry : readableFields.entrySet()) {
            Object value = getFiledValue(entry.getKey());
            if (value != null) {
                ret.put(entry.getValue().propertyName, value);
            }
        }
        return ret;

    }


    /**
     * Called when a property write request is received.
     * To add extra processing when writing properties, you can override this method.
     *
     * @param properties Indicates the desired properties.
     * @return Returns the operation result.
     */
    @Override
    public IotResult onWrite(Map<String, Object> properties) {


        List<String> changedProps = new ArrayList<String>();
        for (Map.Entry<String, Object> entry : properties.entrySet()) {

            Field field = writeableFields.get(entry.getKey());
            if (field == null) {
                Log.e(TAG, "field not found or not writeable " + entry.getKey());
                return new IotResult(-1, "field not found or not writeable " + entry.getKey());

            }

            Object value = entry.getValue();
            String setter = "set" + Character.toUpperCase(field.getName().charAt(0)) + field.getName().substring(1);
            Method method = null;

            try {
                method = this.getClass().getDeclaredMethod(setter, field.getType());
            } catch (NoSuchMethodException e) {
                Log.e(TAG, ExceptionUtil.getBriefStackTrace(e));
            }

            if (method == null) {
                Log.e(TAG, "method not found： " + setter);
                return new IotResult(-1, "method not found： " + setter);
            }

            try {
                method.invoke(this, value);
                Log.i(TAG, "write property ok:" + entry.getKey());
                changedProps.add(field.getName());
            } catch (Exception e) {
                Log.e(TAG, ExceptionUtil.getBriefStackTrace(e));
                return new IotResult(-1, e.getMessage());
            }
        }

        // Reports changed properties.
        if (changedProps.size() > 0) {
            firePropertiesChanged(changedProps.toArray(new String[changedProps.size()]));
        }

        return IotResult.SUCCESS;
    }

    /**
     * Called when an event delivered by the platform is received. The default implementation does nothing.
     *
     * @param deviceEvent Indicates the event.
     */
    @Override
    public void onEvent(DeviceEvent deviceEvent) {
        Log.i(TAG, "onEvent no op");
    }

    /**
     * Reports a property change.
     *
     * @param properties Indicates the properties changed. If it is set to NULL, changes of all readable properties are reported.
     */
    public void firePropertiesChanged(String... properties) {
        iotDevice.firePropertiesChanged(getServiceId(), properties);
    }

    /**
     * Called when a command delivered by the platform is received.
     *
     * @param command Indicates a command request.
     * @return Returns a command response.
     */
    @Override
    public CommandRsp onCommand(Command command) {

        Method method = commands.get(command.getCommandName());
        if (method == null) {
            Log.e(TAG, "command not found " + command.getCommandName());
            return new CommandRsp(CommandRsp.FAIL);
        }

        try {

            return (CommandRsp) method.invoke(this, command.getParas());

        } catch (IllegalAccessException e) {
            Log.e(TAG, ExceptionUtil.getBriefStackTrace(e));
            return new CommandRsp(CommandRsp.FAIL);
        } catch (InvocationTargetException e) {
            Log.e(TAG, ExceptionUtil.getBriefStackTrace(e));
            return new CommandRsp(CommandRsp.FAIL);
        } catch (Exception e) {
            Log.e(TAG, ExceptionUtil.getBriefStackTrace(e));
            return new CommandRsp(CommandRsp.FAIL);
        }
    }

    /**
     * Obtains a device instance.
     *
     * @return Returns the device instance.
     */
    public AbstractDevice getIotDevice() {
        return iotDevice;
    }

    /**
     * Sets a device instance.
     *
     * @param iotDevice Indicates the device instance to set.
     */
    public void setIotDevice(AbstractDevice iotDevice) {
        this.iotDevice = iotDevice;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    /**
     * Enables automatic, periodic property reporting.
     *
     * @param reportInterval Indicates the interval at which properties are reported, in units of ms.
     */
    public void enableAutoReport(int reportInterval) {
        if (timer != null) {
            Log.e(TAG, "timer is already enabled");
            return;
        }

        if (timer == null) {
            timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    firePropertiesChanged();
                }
            }, reportInterval, reportInterval);
        }
    }

    /**
     * Disables automatic, periodic property reporting. You can use firePropertiesChanged to trigger property reporting.
     */
    public void disableAutoReport() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }
}