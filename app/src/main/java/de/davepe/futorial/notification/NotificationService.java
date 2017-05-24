package de.davepe.futorial.notification;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.jsoup.Jsoup;

import java.util.ArrayList;
import java.util.Random;

import de.davepe.futorial.MainActivity;
import de.davepe.futorial.NotificationOpenActivity;
import de.davepe.futorial.R;
import de.davepe.futorial.tabs.social.NotificationFragment;

/**
 * Created by David on 09.11.2017.
 */

public class NotificationService extends FirebaseMessagingService {
    static ArrayList<Integer> ids = new ArrayList<>();
    static SharedPreferences topics;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        System.out.println("FIREBASE: Message recived: " + remoteMessage);

        String topic = remoteMessage.getFrom();
        System.out.println("FIREBASE: Message recived from Topic: " + topic);
        try {
            if (topics == null)
                topics = MainActivity.getMainactivity().getSharedPreferences("subscribed_topics", 0);
            String user, comment, thread;

            user = remoteMessage.getData().get("title");
            comment = Jsoup.parse(remoteMessage.getData().get("content-text")).text();

            if (topic != null) {
                thread = topic.replaceAll("/topics/", "").replaceAll("-", " ");
                build(topics.getString((topic.contains("/topics/") ? topic.replaceAll("/topics/", "") : topic), ""), user + " hat in \""
                        + thread + "\" kommentiert", comment);

                MainActivity.getMainactivity().getSharedPreferences("received_notifications", 0).edit().putString(new Random().nextLong() + "",
                        thread + "';#" + comment + "';#" + user).commit();
                try {
                    NotificationFragment.refresh();
                }catch (Exception e){
                    e.printStackTrace();
                }
            } else
                build("", remoteMessage.getData().get("title"), remoteMessage.getData().get("content-text") == null ? remoteMessage.getData().get("content") : remoteMessage.getData().get("content-text"));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void build(final String link, final String title, final String text) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            buildOreo(link, title, text);
        else
            buildNonOreo(link, title, text);

    }

    public void buildOreo(final String link, final String title, final String text) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
            return;
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        String id = "Beitragsbenachrichtigung";
        CharSequence name = "Beitragsbenachrichtigung";
        String description = "Du erhälst eine Nachricht bei neuen Beiträgen eines abonnierten Threads";
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel mChannel = new NotificationChannel(id, name, importance);
        mChannel.setDescription(description);
        notificationManager.createNotificationChannel(mChannel);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getApplicationContext(), "Beitragsbenachrichtigung")
                .setContentTitle(title)
                .setContentText(text)
                .setSmallIcon(R.drawable.logo_vec)
                .setColor(getResources().getColor(R.color.colorAccent))
                .setVibrate(new long[]{0, 300, 300, 300})
                .setLights(Color.YELLOW, 1000, 5000)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
                .setBadgeIconType(R.drawable.logo_vec) //your app icon
                .setChannelId("Beitragsbenachrichtigung")
                .setAutoCancel(true)
                .setContentIntent(getPendingIntent(link))
                .setNumber(1);
        notificationManager.notify(getId(), notificationBuilder.build());
    }

    public void buildNonOreo(final String link, final String title, final String text) {
        NotificationCompat.Builder notification = new NotificationCompat.Builder(NotificationService.this)
                .setContentTitle(title)
                .setContentText(text)
                .setSmallIcon(R.drawable.logo_vec)
                .setColor(getResources().getColor(R.color.colorAccent))
                .setVibrate(new long[]{0, 300, 300, 300})
                .setLights(Color.YELLOW, 1000, 5000)
                .setContentIntent(getPendingIntent(link))
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(text));
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(getId(), notification.build());
    }

    public int getId() {
        int i = new Random().nextInt();

        while (ids.contains(i)) {
            i = new Random().nextInt();
        }
        ids.add(i);
        return i;
    }

    public PendingIntent getPendingIntent(String url) {
        Intent intent = new Intent(getApplicationContext(), NotificationOpenActivity.class);
        intent.putExtra("url", url);
        return PendingIntent.getActivity(getApplicationContext(), 123, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

}
