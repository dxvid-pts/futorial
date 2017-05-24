package de.davepe.futorial.welcome;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.io.InputStream;

import de.davepe.futorial.MainActivity;
import de.davepe.futorial.R;

public class WelcomeActivity2 extends AppCompatActivity {

    boolean disabled = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome2);

        findViewById(R.id.next).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!disabled) {
                    startActivity(new Intent(WelcomeActivity2.this, MainActivity.class));
                    PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putBoolean("first_start", false).commit();
                }
            }
        });

        final WebView webView = findViewById(R.id.web);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                webView.setVisibility(View.VISIBLE);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        ((Button) findViewById(R.id.next)).setTextColor(getResources().getColor(R.color.colorAccent));
                        disabled = false;
                    }
                }, 1000 * 4);
            }

            @Nullable
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
                System.out.println(url);
                return null;
            }

            @Nullable
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                return null;
            }
        });
        //MainActivity.getMainactivity().settup(webView);
        InputStream is = null;

        try {
            is = getAssets().open("welcome.html");
            Document doc = Jsoup.parse(is, "UTF-8", "futorial.de");
            doc.getElementsByTag("a").attr("style", "color: #ffbf00;");
            doc.getElementsByTag("p").attr("style", "color: #a6a6a6;");
            doc.getElementsByTag("div").attr("style","background-color: #263238");
            webView.loadDataWithBaseURL(null, doc.html(), "text/html", "UTF-8", null);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (is != null)
                    is.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
