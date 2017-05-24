package de.davepe.futorial;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ActionMenuView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.ArrayList;

import de.davepe.futorial.tabs.forum.FragmentForum;

public class UnreadPostsActivity extends AppCompatActivity {

    ActionMenuView amvMenu;
    ActionBar actionBar;
    WebView webView;
    boolean firstFinished = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unread_posts);

        if (!UserData.isLoggedIn()) return; //TODO DIALOG

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        //amvMenu = findViewById(R.id.toolbar_fu_tab_menu);
       /* amvMenu.setOnMenuItemClickListener(new ActionMenuView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                return onOptionsItemSelected(menuItem);
            }
        });*/
        actionBar.setTitle("Ungelesene Beiträge                                                                                                         ");
        profileToolbar();

        webView = findViewById(R.id.web);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.addJavascriptInterface(new JavascriptWebInterface(), "Android");
        if (FragmentForum.document == null)
            webView.loadUrl("https://www.fl-studio-tutorials.de/forum/f-a-q");
        else
            webView.loadDataWithBaseURL("https://www.fl-studio-tutorials.de/forum/f-a-q", FragmentForum.document.html(), "text/html", "UTF-8", "https://www.fl-studio-tutorials.de/forum/f-a-q");
        webView.setWebViewClient(new WebViewClient() {
           /* @Nullable
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
                System.out.println("x");
                if (!firstFinished) {
                    System.out.println("a");
                    webView.loadUrl("javascript:(function(){\n" +
                            "console.log(document.getElementsByClassName('ui-dialog ui-widget ui-widget-content ui-corner-all ui-front spDialogDefault ui-draggable ui-resizable')[0].innerHTML);\n" +
                            "})()");
                }
                return super.shouldInterceptRequest(view, url);
            }*/

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        webView.loadUrl("javascript:(function(){document.getElementById('spUnreadPostsLink').childNodes[0].click();})()");
                        check();
                    }
                });
            }
        });
    }

    public void check() {
        webView.loadUrl("javascript:(function(){\n" +
                "Android.containsUl(document.body.contains(document.getElementsByClassName('ui-dialog ui-widget ui-widget-content ui-corner-all ui-front spDialogDefault ui-draggable ui-resizable')[0]));\n" +
                "})()");
    }

    public class JavascriptWebInterface {
        @JavascriptInterface
        public void containsUl(final boolean b) {
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (b == false) check();
                    else webView.loadUrl("javascript:(function(){\n" +
                            "Android.printElement(document.getElementsByClassName('ui-dialog ui-widget ui-widget-content ui-corner-all ui-front spDialogDefault ui-draggable ui-resizable')[0].innerHTML);\n" +
                            "})()");
                }
            }, 1000 * 2);//1000 milliseconds= 1 second
          //  System.out.println(b);
        }

        @JavascriptInterface
        public void printElement(final String html) {
            Document document = Jsoup.parse(html);

            final ArrayList<String> titles = new ArrayList<>();
            final ArrayList<String> topic = new ArrayList<>();
            final ArrayList<String> links = new ArrayList<>();
            final ArrayList<String> date = new ArrayList<>();

            String genre = "";
            for (Element row : document.getElementsByClass("spListSection spListViewSection").first().children()) {
                System.out.println(row.text());
                if (row.getElementsByClass("spListForumRowName").size() > 0)
                    genre = row.getElementsByClass("spListForumRowName").get(0).text();

                Element topicElement = row.getElementsByClass("spListTopicRowName").first();
                titles.add(topicElement.text());
                links.add(topicElement.child(0).attr("abs:href"));
                topic.add(genre);
                date.add(row.getElementsByClass("spListPostLink").first().children().last().text());
            }

            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    Fragment fragment = new FragmentList();

                    Bundle b = new Bundle();
                    b.putStringArrayList("title", titles);
                    b.putStringArrayList("desc", topic);
                    b.putStringArrayList("links", links);
                    b.putStringArrayList("additional", date);
                    b.putBoolean("deleteRow", true);
                    fragment.setArguments(b);

                    try {
                        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                        transaction.replace(R.id.fragment, fragment);
                        transaction.commit();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    findViewById(R.id.progressBar).setVisibility(View.GONE);
                }
            });
        }
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
                    startActivity(new Intent(UnreadPostsActivity.this, LoginActivity.class));
                }
            });
        }
    }
}
