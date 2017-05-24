package de.davepe.futorial;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ActionMenuView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;

import org.jsoup.nodes.Document;

public class FutorialTabActivity extends AppCompatActivity {

    String mUrl;
    ActionMenuView amvMenu;
    ActionBar actionBar;
    WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_futorial_tab);

        mUrl = getIntent().getStringExtra("url");

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar_fu_tab));
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        amvMenu = findViewById(R.id.toolbar_fu_tab_menu);
        amvMenu.setOnMenuItemClickListener(new ActionMenuView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                return onOptionsItemSelected(menuItem);
            }
        });
        actionBar.setTitle("                                                                                                         ");
        profileToolbar();

        webView = findViewById(R.id.fu_tab_web);
        MainActivity.getMainactivity().settup(webView);
        load();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        try {
            menu = amvMenu.getMenu();
            getMenuInflater().inflate(R.menu.toolbar_fu_tab, menu);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        System.out.println(item.getItemId());
        switch (item.getItemId()) {
            case R.id.toolbar_fu_tab_share:
                Intent share = new Intent(Intent.ACTION_SEND);
                share.setType("text/plain");
                String sub = "sub";
                share.putExtra(Intent.EXTRA_SUBJECT, sub);
                share.putExtra(Intent.EXTRA_TEXT, mUrl);
                startActivity(Intent.createChooser(share, "Diesen Thread teilen:"));
                break;

            case android.R.id.home:
                onBackPressed();
                break;
            default:
                break;
        }
        return true;
    }

    public void profileToolbar() {
        if (UserData.isLoggedIn()) {
            ImageView pb = findViewById(R.id.profilePicture_toolbar_fu_tab);
            pb.setVisibility(View.VISIBLE);
            Bitmap bitmap = UserData.getProfilePicture();
            if (bitmap != null)
                pb.setImageBitmap(bitmap);
            findViewById(R.id.button_toolbar_login_fu_tab).setVisibility(View.GONE);
        } else {
            findViewById(R.id.profilePicture_toolbar_fu_tab).setVisibility(View.GONE);
            findViewById(R.id.button_toolbar_login_fu_tab).setVisibility(View.VISIBLE);
            findViewById(R.id.button_toolbar_login_fu_tab).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(FutorialTabActivity.this, LoginActivity.class));
                }
            });
        }
    }

    String html;

    public void load() {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                final Document document = MainActivity.getHTML(mUrl);

                try {
                    document.getElementsByClass("spHeadContainer").remove();
                    document.getElementsByClass("spFootContainer").remove();
                    document.getElementsByClass("entry-header").remove();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                html = document.html();
                if (html.contains("<br>"))
                    html = html.replaceFirst("<br>", "");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        actionBar.setTitle(document.title() + "                                                   ");
                        webView.loadDataWithBaseURL(mUrl, html, "text/html", "UTF-8", null);
                    }
                });
            }
        });
    }
}
