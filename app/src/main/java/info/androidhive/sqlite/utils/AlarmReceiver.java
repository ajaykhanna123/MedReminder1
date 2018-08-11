package info.androidhive.sqlite.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.os.Build;
import android.provider.Settings;
//import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.webkit.URLUtil;
import android.widget.EditText;
import android.widget.TextView;

import info.androidhive.sqlite.database.DatabaseHelper;
import info.androidhive.sqlite.view.MainActivity;
import info.androidhive.sqlite.database.model.Task;
import info.androidhive.sqlite.R;



public class AlarmReceiver extends BroadcastReceiver {

    public static String NOTIFICATION_ID = "notification_id";
    public static String NOTIFICATION = "notification";
    DatabaseHelper dbHelp;
    int found=0;
    private Context mContext;


    @Override
    public void onReceive(Context context, Intent intent) {
        dbHelp=new DatabaseHelper(context);
        Cursor cursor = dbHelp.fetchData();
        while (cursor.moveToNext())
        {
            String taskName = cursor.getString(cursor.getColumnIndex("note"));
            String taskStatus = cursor.getString(cursor.getColumnIndex("timestamp"));
            String taskDate = cursor.getString(cursor.getColumnIndex("time"));
            String taskTime = cursor.getString(cursor.getColumnIndex("date"));
            Task task=new Task(taskName,taskStatus,taskDate,taskTime);
            if(taskName.equalsIgnoreCase(intent.getStringExtra("task")))
            {
                Log.d("TAG", "onReceive:found "+"1");
                found=1;
                break;
            }
        }
        Log.d("TAG", "onReceive:found "+found);


        Log.d("notif_task", "onReceive: " + intent.getStringExtra("task"));

        if(found==1) {

            MediaPlayer mediaPlayer=MediaPlayer.create(context, Settings.System.DEFAULT_ALARM_ALERT_URI);
            mediaPlayer.start();
            Notification.Builder builder = new Notification.Builder(context);
            builder.setContentText(intent.getStringExtra("task")).setContentTitle("Medicine Reminder")
                    .setContentText("Its time for your Medicine ").setOngoing(true);
            builder.setSmallIcon(R.drawable.md);

            int notifyId = intent.getIntExtra("millis", 0);
            Intent intent2 = new Intent(context, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, notifyId
                    , intent2, 0);


            builder.setContentIntent(pendingIntent);
            builder.setAutoCancel(true);
            builder.setOngoing(false);


            Notification notification = builder.build();
            // this is the main thing to do to make a non removable notification
            // 2nd method for permanent notification: is to add flags..(for API levels < 11)
            //  notification.flags |=Notification.FLAG_NO_CLEAR;
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            //manager.notify(sPref.getInt("size",-1), notification);
            manager.notify((int) System.currentTimeMillis(), notification);
            //int random= (int) Math.random();

        }

    }
    public static void showNotification(Context c, Intent intent,
                                        int notificationId, String title, String message, int largeIcon,
                                        int smallIcon) {

    }
}
