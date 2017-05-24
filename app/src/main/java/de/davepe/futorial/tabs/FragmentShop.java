package de.davepe.futorial.tabs;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import org.jsoup.nodes.Document;

import java.util.Random;

import de.davepe.futorial.MainActivity;
import de.davepe.futorial.R;

/**
 * Created by David on 10.11.2017.
 */

public class FragmentShop extends Fragment {

    WebView webView;
    SharedPreferences sp;
    SharedPreferences.Editor editor;
    String mUrl = "https://www.fl-studio-tutorials.de/preisliste";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_shop, container, false);

        sp = MainActivity.getMainactivity().getSharedPreferences("shop_cache", 0);
        editor = sp.edit();

        webView = rootView.findViewById(R.id.web);
        MainActivity.getMainactivity().settup(webView);
        if (sp.contains("html"))
            webView.loadDataWithBaseURL(mUrl, sp.getString("html", "about:blank"), "text/html", "UTF-8", null);
        else webView.loadUrl("file:///android_asset/shop.html");

        if (new Random().nextInt(20) < 5)
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        load(mUrl);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }, 4000);

        return rootView;
    }

    Document document = null;

    public void load(final String mUrl) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    document = MainActivity.getHTML(mUrl);
                    document.getElementsByClass("spHeadContainer").remove();
                    document.getElementsByClass("spFootContainer").remove();
                    document.getElementsByClass("entry-header").remove();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (document == null) return;
                //document.getElementsByTag("div").attr("style", "background-color: #263238");
                String html = document.html();
                if (html.contains("<br>"))
                    html = html.replaceFirst("<br>", "");
                editor.putString("html", html).commit();
            }
        });
    }
}