package cn.zjamss.pp.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeoutException;

import cn.zjamss.pp.MainActivity;
import cn.zjamss.pp.R;
import cn.zjamss.pp.net.PictureService;
import cn.zjamss.pp.util.BitmapUtil;
import cn.zjamss.pp.util.MQUtil;
import cn.zjamss.pp.widget.AppWidget;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class UpdateService extends Service {

    public static final String EXCHANGE_NAME = "pp_exc";
    private static final String TAG = "UpdateService";

    public UpdateService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel("notice", "运行通知", NotificationManager.IMPORTANCE_DEFAULT);
                manager.createNotificationChannel(channel);
            }
            Intent intent = new Intent(this, MainActivity.class);
            PendingIntent pd = PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_IMMUTABLE);

            Notification notification = new NotificationCompat.Builder(this,"notice")
                    .setContentTitle("PP")
                    .setContentText("运行中")
                    .setSmallIcon(R.mipmap.ic_launcher_round)
                    .setLargeIcon(BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher_foreground))
                    .setContentIntent(pd)
                    .setAutoCancel(true)
                    .build();
            startForeground(1,notification);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new Thread(() -> {
            Channel channel;
            try {
                SharedPreferences pp = getSharedPreferences("pp", Context.MODE_PRIVATE);
                channel = MQUtil.getChannel();
                String queueName = pp.getString("queue_name","???");
                channel.queueDeclare(queueName,false,false   ,true,null);
                channel.queueBind(queueName, EXCHANGE_NAME, "");
                channel.basicConsume(queueName, false, new DefaultConsumer(channel) {
                    @Override
                    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                        Log.d(TAG, "handleDelivery");
                        super.handleDelivery(consumerTag, envelope, properties, body);
                        long deliveryTag = envelope.getDeliveryTag();
                        channel.basicAck(deliveryTag, false);
                        updatePic();
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            } catch (TimeoutException e) {
                e.printStackTrace();
            }
        }).start();
        return super.onStartCommand(intent, flags, startId);
    }

    private void updatePic() {
        PictureService.service.getPic().
                enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (response.isSuccessful()) {
                            if (response.body() != null) {
                                InputStream inputStream = response.body().byteStream();
                                RemoteViews rv = new RemoteViews(UpdateService.this.getPackageName(), R.layout.app_widget);
                                ComponentName cn = new ComponentName(UpdateService.this, AppWidget.class);
                                rv.setImageViewBitmap(R.id.widget_image, BitmapUtil.bimapRound(BitmapUtil.adjustPhotoRotation(BitmapUtil.centerSquareScaleBitmap(BitmapFactory.decodeStream(inputStream), 500)),40));
                                AppWidgetManager manager = AppWidgetManager.getInstance(UpdateService.this);
                                manager.updateAppWidget(cn, rv);
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        t.printStackTrace();
                        System.out.println("失败");
                    }
                });
    }
}