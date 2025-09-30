package com.sczg.apple.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.sczg.apple.App;
import com.sczg.apple.AppContants;
import com.sczg.apple.bean.MqttMsgBean;
import com.sczg.apple.utils.AppMsgUtil;
import com.sczg.apple.utils.LogUtil;
import com.sczg.apple.utils.TaskTimeUtil;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import androidx.annotation.Nullable;


public class MqttService extends Service {

    public static final String TAG = MqttService.class.getSimpleName();

    private static MqttAndroidClient client;
    private MqttConnectOptions conOpt;

    private String host = AppContants.MQTT_BASE_URL;
    private String userName = "device";
    private String passWord = "123456";
    private static String myTopic = "jima/taskTime/" + AppMsgUtil.getIMEI(App.getAppContext());
    private String clientId = AppMsgUtil.getIMEI(App.getAppContext());

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "--------->onStartCommand: ");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "==mqttService---Start---topic=" + myTopic);
        init();
    }

    public static void publish(String msg) {
        String topic = myTopic;
        Integer qos = 2;
        Boolean retained = false;
        try {
            client.publish(topic, msg.getBytes(), qos.intValue(), retained.booleanValue());
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private void init() {
        // 服务器地址（协议+地址+端口号）
        String uri = host;
        client = new MqttAndroidClient(this, uri, clientId);
        // 设置MQTT监听并且接受消息
        client.setCallback(mqttCallback);

        conOpt = new MqttConnectOptions();
        // 清除缓存
        conOpt.setCleanSession(true);
        // 设置超时时间，单位：秒
        conOpt.setConnectionTimeout(10);
        // 心跳包发送间隔，单位：秒
        conOpt.setKeepAliveInterval(20);
        // 用户名
        conOpt.setUserName(userName);
        // 密码
        conOpt.setPassword(passWord.toCharArray());
        //断开后，是否自动连接
        conOpt.setAutomaticReconnect(true);

        // last will message
        boolean doConnect = true;
        String message = "outline";
        String topic = myTopic;
        Integer qos = 2;
        Boolean retained = false;
        if ((!message.equals("")) || (!topic.equals(""))) {
            // 最后的遗嘱
            try {
                conOpt.setWill(topic, message.getBytes(), qos.intValue(), retained.booleanValue());
            } catch (Exception e) {
                Log.i(TAG, "Exception Occured", e);
                doConnect = false;
                iMqttActionListener.onFailure(null, e);
            }
        }

        if (doConnect) {
            doClientConnection();
        }

    }

    @Override
    public void onDestroy() {
        Log.e(TAG, "--------->onDestroy: ");
        try {
            client.disconnect();
            client.unregisterResources();
            client.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    /**
     * 连接MQTT服务器
     */
    private void doClientConnection() {
        LogUtil.i("MQTT连接开始");
        if (!client.isConnected() && isConnectIsNomarl()) {
            try {
                client.connect(conOpt, null, iMqttActionListener);
            } catch (MqttException e) {
                LogUtil.i("MQTT连接失败");
                e.printStackTrace();
            }
        }

    }

    // MQTT是否连接成功
    private IMqttActionListener iMqttActionListener = new IMqttActionListener() {

        @Override
        public void onSuccess(IMqttToken arg0) {
            Log.i(TAG, "连接成功 ");
            try {
                // 订阅myTopic话题
                client.subscribe(myTopic, 2);
                publish("连接成功");
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onFailure(IMqttToken arg0, Throwable arg1) {
            arg1.printStackTrace();
            // 连接失败
        }
    };

    // MQTT监听并且接受消息
    private MqttCallback mqttCallback = new MqttCallback() {

        @Override
        public void messageArrived(String topic, MqttMessage message) throws Exception {

            String str1 = new String(message.getPayload());
            String str2 = topic + ";qos:" + message.getQos() + ";==retained:" + message.isRetained();
            Log.i(TAG, "==收到消息id:" + message.getId() + ",messageArrived:" + str1);
            Log.i(TAG, str2);
            dealMsg(str1);
            client.messageArrivedComplete(message.getId(), message.getQos());
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken arg0) {
            LogUtil.i("==deliveryComplete");
        }

        @Override
        public void connectionLost(Throwable arg0) {
            // 失去连接
            LogUtil.i("==connectionLost：" + arg0.getMessage());
        }
    };

    //处理收到的消息
    private void dealMsg(String msg) {
        if (TextUtils.isEmpty(msg)) {
            return;
        }
        try {
            MqttMsgBean msgBean = new Gson().fromJson(msg, MqttMsgBean.class);
            if (!msgBean.getTimes().isEmpty()) {
                TaskTimeUtil.saveTaskTime(msgBean.getTimes().get(0));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 判断网络是否连接
     */
    private boolean isConnectIsNomarl() {
        /*ConnectivityManager connectivityManager = (ConnectivityManager) this.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();
        if (info != null && info.isAvailable()) {
            String name = info.getTypeName();
            Log.i(TAG, "MQTT当前网络名称：" + name);
            return true;
        } else {
            Log.i(TAG, "MQTT 没有可用网络");
            return false;
        }*/
        //4g卡用检检测不到有网络，屏蔽网络检测直接连接
        return true;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
