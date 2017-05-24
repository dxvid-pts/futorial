package de.davepe.futorial;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Base64;
import android.webkit.CookieManager;

import java.io.ByteArrayOutputStream;
import java.net.URL;

/**
 * Created by David on 06.03.2018.
 */

public class UserData {
    static MainActivity m = MainActivity.getMainactivity();

    private static void saveProfilePicture(Bitmap bitmap) {
        SharedPreferences shre = m.getSharedPreferences("user_data", 0);
        SharedPreferences.Editor edit = shre.edit();
        edit.putString("profile_picture", bitmapToString(bitmap));
        edit.commit();
    }

    public static Bitmap getProfilePicture() {
        try {
            return stringToBitmap(m.getSharedPreferences("user_data", 0).getString("profile_picture", ""));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String bitmapToString(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] b = baos.toByteArray();

        return Base64.encodeToString(b, Base64.DEFAULT);
    }

    public static Bitmap stringToBitmap(String previouslyEncodedImage) {
        if (!previouslyEncodedImage.equalsIgnoreCase("")) {
            byte[] b = Base64.decode(previouslyEncodedImage, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(b, 0, b.length);
            return bitmap;
        } else {
            return null;
        }
    }

    public static void setUserName(String name) {
        SharedPreferences shre = m.getSharedPreferences("user_data", 0);
        SharedPreferences.Editor edit = shre.edit();
        edit.putString("user_name", name);
        edit.commit();
    }

    public static String getUserName(Context c) {
        return c.getSharedPreferences("user_data", 0).getString("user_name", "");
    }public static String getUserName() {
        return getUserName(m);
    }

    public static void setFucoins(String name) {
        SharedPreferences shre = m.getSharedPreferences("user_data", 0);
        SharedPreferences.Editor edit = shre.edit();
        edit.putString("fucoins", name);
        edit.commit();
    }

    public static String getFucoins() {
        return m.getSharedPreferences("user_data", 0).getString("fucoins", "");
    }

    public static void setEmail(String name) {
        SharedPreferences shre = m.getSharedPreferences("user_data", 0);
        SharedPreferences.Editor edit = shre.edit();
        edit.putString("email", name);
        edit.commit();
    }

    public static String getEmail() {
        return m.getSharedPreferences("user_data", 0).getString("email", "");
    }

    public static void loadProfilePicture(String url) {
        new DownloadImageTask().execute(new String[]{url});
    }

    public static boolean isLoggedIn() {
        CookieManager cookieManager = CookieManager.getInstance();
        String cookies = cookieManager.getCookie("https://www.fl-studio-tutorials.de");
        if (cookies != null) {
            String[] temp = cookies.split(";");
            for (String ar1 : temp) {
                System.out.println(ar1);

                if (ar1.contains("wordpress_logged_in")) {
                    return true;
                }
            }
        }
        return false;
    }

    private static class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        protected Bitmap doInBackground(String... urls) {
            Bitmap mIcon11 = null;
            try {
                mIcon11 = BitmapFactory.decodeStream(new URL(urls[0]).openStream());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            saveProfilePicture(result);
        }
    }
}
