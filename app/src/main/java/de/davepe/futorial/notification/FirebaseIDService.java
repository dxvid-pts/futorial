package de.davepe.futorial.notification;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.Calendar;

import de.davepe.futorial.ForumActivity;
import de.davepe.futorial.MainActivity;
import de.davepe.futorial.R;
import de.davepe.futorial.tabs.social.SubscribedTopicsFragment;

/**
 * Created by David on 11.11.2017.
 */

public class FirebaseIDService extends FirebaseInstanceIdService {

    final String tokenPreferenceKey = "fcm_token";
    final static String trennung = "-;-";

    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        final String token = FirebaseInstanceId.getInstance().getToken();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.edit().putString(tokenPreferenceKey, token).apply();

    }

    static SharedPreferences sp = MainActivity.getMainactivity().getSharedPreferences("notification_by_link", 0);
    static SharedPreferences.Editor editor = sp.edit();

    static SharedPreferences topics = MainActivity.getMainactivity().getSharedPreferences("subscribed_topics", 0);
    static SharedPreferences.Editor topicsEditor = topics.edit();

    public static boolean isEnabled(String link) {
        link = getOrginalUrl(link);
        if (sp.getString(link, "null").equals("null")) {
            return false;
        } else return true;
    }

    public static void toggle(String link, String name, String author, MenuItem item) {
        link = getOrginalUrl(link);
        String id = link.split("-tutorials.de/forum/")[1];
        if (id.contains("/"))
            id = id.split("/")[id.split("/").length - 1];
        System.out.println(id);
        if (!isEnabled(link)) {
            FirebaseMessaging.getInstance().subscribeToTopic(id);

            topicsEditor.putString(id, link).commit();
            editor.putString(link, name + trennung + getDate() + trennung + author).commit();

            draw(link, item);
            Toast.makeText(MainActivity.getMainactivity(), "Beitragsbenachrichtigungen zu diesem Threat aktiviert.", Toast.LENGTH_LONG).show();
        } else {
            FirebaseMessaging.getInstance().unsubscribeFromTopic(id);

            topicsEditor.remove(id).commit();
            editor.remove(link).commit();

            draw(link, item);
            Toast.makeText(MainActivity.getMainactivity(), "Beitragsbenachrichtigungen zu diesem Threat deaktiviert.", Toast.LENGTH_LONG).show();
        }
        //SubscribedTopicsFragment.refresh();
    }

    public static void draw(final String link, final MenuItem item) {
        draw(link, item, false);
    }

    public static void draw(final String link, final MenuItem item, final boolean disabled) {
        ForumActivity.instance.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isEnabled(getOrginalUrl(link))) {
                    if (disabled)
                        item.setIcon(R.drawable.ic_notifications_forum_dark);
                    else
                        item.setIcon(R.drawable.ic_notifications_active_black_24dp);
                    //img.setImageResource(R.drawable.ic_notifications_active_black_24dp);
                } else {
                    if (disabled)
                        item.setIcon(R.drawable.ic_notifications_none_forum_dark);
                    else
                        item.setIcon(R.drawable.ic_notifications_none_black_24dp);
                    //img.setImageResource(R.drawable.ic_notifications_none_black_24dp);
                }
            }
        });
    }

    public static String getOrginalUrl(String link) {
        if (link.contains("#"))
            return link.split("#")[0];
        else
            return link;
    }

    public static String getDate() {
        Calendar cal = Calendar.getInstance();
        return cal.get(Calendar.DAY_OF_MONTH) < 10 ? "0" : "" + cal.get(Calendar.DAY_OF_MONTH) + "." + ((cal.get(Calendar.MONTH) + 1) < 10 ? "0" : "") + (cal.get(Calendar.MONTH) + 1) + "." + cal.get(Calendar.YEAR);
    }
}