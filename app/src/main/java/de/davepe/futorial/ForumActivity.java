package de.davepe.futorial;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ActionMenuView;
import android.support.v7.widget.Toolbar;
import android.view.Display;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.Target;

import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

import java.util.ArrayList;

import de.davepe.futorial.notification.FirebaseIDService;
import de.davepe.futorial.tabs.forum.Comment;
import de.davepe.futorial.tabs.forum.Page;
import github.chenupt.dragtoplayout.AttachUtil;
import github.chenupt.dragtoplayout.DragTopLayout;

public class ForumActivity extends AppCompatActivity {

    public static ForumActivity instance;
    ActionBar actionBar;
    String mUrl;
    ActionMenuView amvMenu;
    String mAuthor;
    String mTitle;

    static boolean isLoaded = false; //für glocke
    static String originalUrl;
    static MenuItem badge;
    static MenuItem notifications;

    static FloatingActionButton fab;
    static FloatingActionButton f;
    static WebView fabWebView;

    static WebView mWebView;
    int startPage;
    boolean loadNewPageAllowed = false;
    boolean loadNewPageAllowedTop = false;
    ScrollView scrollView;
    DragTopLayout dragLayout;

    ArrayList<Page> pages;
    ArrayList<Page> pages_below;
    ArrayList<Page> pages_above;

    int heightBefore;
    View layout2;
    static String online = null;
    boolean onlineVisible = false;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forum);
        instance = this;
        this.pages = new ArrayList<>();
        this.pages_below = new ArrayList<>();
        this.pages_above = new ArrayList<>();
        this.scrollView = findViewById(R.id.scroll);
        dragLayout = findViewById(R.id.drag_top_layout);

        mUrl = getIntent().getStringExtra("url");
        cleanUrl1();
        originalUrl = getIntent().getStringExtra("url");
        startPage = getPage();
        cleanUrl2();

        if (getIntent().hasExtra("title"))
            mTitle = getIntent().getStringExtra("title");
        else
            extractTitle();

        amvMenu = findViewById(R.id.toolbar_forum_menu);
        amvMenu.setOnMenuItemClickListener(new ActionMenuView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                return onOptionsItemSelected(menuItem);
            }
        });

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar_forum));
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        if (mTitle != null)
            actionBar.setTitle(mTitle + "                                       ");

        profileToolbar();

        fab = findViewById(R.id.comment);
        f = findViewById(R.id.comment_grey);
        dragLayout.closeTopView(false);
        dragLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (!loadNewPageAllowedTop) {
                    dragLayout.setTouchMode(false);
                    return false;
                }

                loadNextPage(false);
                return false;
            }
        });
        mWebView = findViewById(R.id.forum_article);
        try {
            MainActivity.getMainactivity().settup(mWebView);
        } catch (Exception e) {
            e.printStackTrace();
            startActivity(new Intent(ForumActivity.this, MainActivity.class));
            return;
        }
        scrollView.getViewTreeObserver().addOnScrollChangedListener(
                new ViewTreeObserver.OnScrollChangedListener() {
                    @Override
                    public void onScrollChanged() {
                        int y = scrollView.getScrollY();
                        int height = (int) Math.floor(mWebView.getContentHeight() * mWebView.getScale());
                        int webViewHeight = scrollView.getHeight();
                        int cutoff = height - (webViewHeight - 15);
                        if (y >= cutoff) {
                            loadNextPage(true);
                        }
                    }
                });
        KeyboardVisibilityEvent.setEventListener(this, new KeyboardVisibilityEventListener() {
            @Override
            public void onVisibilityChanged(boolean isOpen) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mWebView.setVisibility(View.GONE);
                        mWebView.setVisibility(View.VISIBLE);
                    }
                });
            }
        });
        scrollView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (loadNewPageAllowedTop)
                    dragLayout.setTouchMode(AttachUtil.isScrollViewAttach(scrollView));
                else
                    dragLayout.setTouchMode(false);
                return false;
            }
        });

        mWebView.setWebChromeClient(new WebChromeClient());
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onLoadResource(WebView view, String url) {
                super.onLoadResource(view, url);
                MainActivity.onLoadResource(view, url);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                findViewById(R.id.forum_load).setVisibility(View.GONE);
                mWebView.setVisibility(View.VISIBLE);

                if (!firstLoad) return;
                if (pages_below.size() > 0) {
                    findViewById(R.id.forum_load_more).setVisibility(View.VISIBLE);
                    loadNewPageAllowed = true;
                }
                firstLoad = false;
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, final String url) {
                System.out.println("shouldOverrideUrlLoading: " + url);
                try {
                    if (url.contains("fl-studio-tutorials.de") && getId(url).equals(getId(mUrl))) {
                        System.out.println("reload");//spPostTrackrating
                        load();
                        return true;
                    }
                }catch (Exception e){
                  e.printStackTrace();
                    Toast.makeText(getApplicationContext(),"Ein Fehler ist aufgetreten!",Toast.LENGTH_LONG).show();
                }

                if (url.startsWith("https://") || url.startsWith("http://")) {
                    MainActivity.open(url, ForumActivity.this);
                    return true;
                } else return false;
            }
        });
        MainActivity.addFucoinListener(mWebView, findViewById(R.id.forum_activity));
        mWebView.setWebChromeClient(new WebChromeClient());
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            mWebView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        mWebView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return (event.getAction() == MotionEvent.ACTION_MOVE);
            }
        });
        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putBoolean("allow_forum", true).commit();
        load();
    }

    boolean firstLoad = true;

    public String getId(String url) {
        if (url.contains("/page-"))
            url = url.split("/page")[0];
        String id = url.split("-tutorials.de/forum/")[1];
        if (id.contains("/"))
            id = id.split("/")[id.split("/").length - 1];
        System.out.println(id);
        return id;
    }

    public void load() {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    cleanUrl3();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                final int max = getMaxPages();
                                for (int i = 1; i <= max; i++)
                                    pages.add(new Page(mUrl + "/page-" + i, i));

                                final String startUrl = pages.get(startPage - 1).getUrl();
                                System.out.println("StartUrl: " + startUrl + ", max: " + max + ", startPage: " + startPage);

                                for (int i = startPage; i < max; i++)
                                    pages_below.add(pages.get(i));

                                for (int i = startPage - 1; i > 0; i--)
                                    pages_above.add(pages.get(i - 1));

                                System.out.println("above: " + pages_above.size() + " | below: " + pages_below.size());
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (pages_above.size() > 0) {
                                            loadNewPageAllowedTop = true;
                                            findViewById(R.id.forum_load_more_top).setVisibility(View.VISIBLE);
                                        }

                                       /* if (pages_below.size() > 0){
                                            findViewById(R.id.forum_load_more).setVisibility(View.VISIBLE);
                                            loadNewPageAllowed = true;
                                        }*/ //Moved To OnPageFinished
                                    }
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            final Document document = getDocument(mUrl);

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    badge.setVisible(true);
                                    FirebaseIDService.draw(originalUrl, notifications, false);
                                    isLoaded = true;

                                    mWebView.loadDataWithBaseURL(mUrl, document.html(), "text/html", "UTF-8", mUrl);

                                    final ViewTreeObserver viewTreeObserver = getWindow().getDecorView().getViewTreeObserver();
                                    viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                                        @Override
                                        public void onGlobalLayout() {
                                            View menuButton = findViewById(R.id.toolbar_forum_views);
                                            if (menuButton != null) {
                                                final int[] location = new int[2];
                                                menuButton.getLocationInWindow(location);

                                                showShowCaseView(new Target() {
                                                    @Override
                                                    public Point getPoint() {
                                                        System.out.println(location[0]);
                                                        int x = location[0] - 70;
                                                        int y = actionBar.getHeight() / 2 + 50;
                                                        return new Point(x, y);
                                                    }
                                                }, "Benachrichtigungen", "Abonniere deine Lieblings-Threads und erhalte eine Benachrichtigung bei neuen Beiträgen.", 34);

                                                viewTreeObserver.removeGlobalOnLayoutListener(this);
                                            }
                                        }
                                    });
                                    loadFloatingButton(document.html());
                                }
                            });
                        }
                    }).start();
                    /*final int max = getMaxPages();
                    for (int i = 1; i <= max; i++)
                        pages.add(new Page(mUrl + "/page-" + i, i));

                    final String startUrl = pages.get(startPage - 1).getUrl();
                    System.out.println("StartUrl: " + startUrl + ", max: " + max + ", startPage: " + startPage);

                    for (int i = startPage; i < max; i++)
                        pages_below.add(pages.get(i));

                    for (int i = startPage - 1; i > 0; i--)
                        pages_above.add(pages.get(i - 1));

                    final Document document = getDocument(startUrl);
                    //online = document.getElementsByClass("spCurrentBrowsing").first().text();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            badge.setVisible(true);
                            FirebaseIDService.draw(originalUrl, notifications, false);
                            isLoaded = true;

                            if (pages_above.size() > 0) {
                                loadNewPageAllowedTop = true;
                                findViewById(R.id.forum_load_more_top).setVisibility(View.VISIBLE);
                            } else
                                findViewById(R.id.forum_load_more_top).setVisibility(View.GONE);

                            if (pages_below.size() == 0)
                                findViewById(R.id.forum_load_more).setVisibility(View.GONE);
                            else {
                                findViewById(R.id.forum_load_more).setVisibility(View.VISIBLE);
                                loadNewPageAllowed = true;
                            }
                            mWebView.loadDataWithBaseURL(startUrl, document.html(), "text/html", "UTF-8", startUrl);

                            final ViewTreeObserver viewTreeObserver = getWindow().getDecorView().getViewTreeObserver();
                            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                                @Override
                                public void onGlobalLayout() {
                                    View menuButton = findViewById(R.id.toolbar_forum_views);
                                    if (menuButton != null) {
                                        final int[] location = new int[2];
                                        menuButton.getLocationInWindow(location);

                                        showShowCaseView(new Target() {
                                            @Override
                                            public Point getPoint() {
                                                System.out.println(location[0]);
                                                int x = location[0] - 70;
                                                int y = actionBar.getHeight() / 2 + 50;
                                                return new Point(x, y);
                                            }
                                        }, "Benachrichtigungen", "Abonniere deine Lieblings-Threads und erhalte eine Benachrichtigung bei neuen Beiträgen.", 34);

                                        viewTreeObserver.removeGlobalOnLayoutListener(this);
                                    }
                                }
                            });
                            loadFloatingButton(document.html());
                        }
                    });*/
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        try {
            menu = amvMenu.getMenu();
            getMenuInflater().inflate(R.menu.toolbar_forum, menu);
            badge = menu.findItem(R.id.toolbar_forum_views);
            badge.setVisible(false);
            notifications = menu.findItem(R.id.toolbar_forum_notification);
            FirebaseIDService.draw(mUrl, notifications, true);
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        System.out.println(item.getItemId());
        switch (item.getItemId()) {
            case R.id.toolbar_forum_more:
                Intent share = new Intent(Intent.ACTION_SEND);
                share.setType("text/plain");
                String sub = "sub";
                share.putExtra(Intent.EXTRA_SUBJECT, sub);
                share.putExtra(Intent.EXTRA_TEXT, originalUrl);
                startActivity(Intent.createChooser(share, "Diesen Thread teilen:"));
                break;
            case R.id.toolbar_forum_notification:
                if (!isLoaded)
                    return true;
                String url = mUrl;
                if (url.contains("#"))
                    url = url.split("#")[0];
                if (url.contains("/page-"))
                    url = url.split("page-")[0];
                FirebaseIDService.toggle(url, mTitle, mAuthor, item);
                break;
            case R.id.toolbar_forum_views:
                if (online == null) {
                    System.out.println("null");
                    break;
                }
                System.out.println("afasf: " + online);
                if (onlineVisible) hideOnline();
                else showOnline(item);
                onlineVisible = !onlineVisible;
                ((TextView) findViewById(R.id.online)).setText(online);
                break;
            case android.R.id.home:
                onBackPressed();
                break;
            default:
                break;
        }
        return true;
    }

    public void showOnline(MenuItem item) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Display display = getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            int width = size.x;
            layout2 = findViewById(R.id.online_layout);
            int x = width / 3 + width / 3;
            int y = 0;

            int startRadius = 0;
            int endRadius = (int) Math.hypot(width, size.y);

            Animator anim = ViewAnimationUtils.createCircularReveal(layout2, x, y, startRadius, endRadius);
            //anim.setDuration(700);

            layout2.setVisibility(View.VISIBLE);
            anim.start();
        } else {
            findViewById(R.id.online_layout).setVisibility(View.VISIBLE);
            Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), android.R.anim.fade_in);
            animation.setDuration(200);
            findViewById(R.id.online_layout).startAnimation(animation);
        }
    }

    public void hideOnline() {
        Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), android.R.anim.fade_out);
        animation.setDuration(200);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                findViewById(R.id.online_layout).setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        findViewById(R.id.online_layout).startAnimation(animation);
    }

    public void profileToolbar() {
        if (UserData.isLoggedIn()) {
            ImageView pb = findViewById(R.id.profilePicture_toolbar_forum);
            pb.setVisibility(View.VISIBLE);
            Bitmap bitmap = UserData.getProfilePicture();
            if (bitmap != null)
                pb.setImageBitmap(bitmap);
            findViewById(R.id.button_toolbar_login_forum).setVisibility(View.GONE);
        } else {
            findViewById(R.id.profilePicture_toolbar_forum).setVisibility(View.GONE);
            findViewById(R.id.button_toolbar_login_forum).setVisibility(View.VISIBLE);
            findViewById(R.id.button_toolbar_login_forum).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(ForumActivity.this, LoginActivity.class));
                }
            });
        }
    }

    public void cleanUrl1() {
        if (mUrl.contains("#"))
            mUrl = mUrl.split("#")[0];
    }

    public int getMaxPages() {
        try {
            Document rss = Jsoup.parse(Jsoup.connect(mUrl + "/rss").get().html(), "", Parser.xmlParser());
            String maxPages = rss.html().split("<link>")[2].split("</link>")[0].replaceAll(" ", "");
            if (maxPages.contains("#"))
                maxPages = maxPages.split("#")[0];
            return getPage(maxPages);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public int getPage() {
        return getPage(mUrl);
    }

    public int getPage(String url) {
        if (!url.contains("/page-"))
            return 1;
        return Integer.parseInt(url.split("/page-")[1]);
    }

    public void cleanUrl2() {
        if (!mUrl.contains("/page-"))
            return;
        mUrl = mUrl.split("/page-")[0];
    }

    public void cleanUrl3() {
        if (!mUrl.endsWith("/"))
            return;
        mUrl = mUrl.substring(0, mUrl.length() - 1);
    }

    public void extractTitle() {
        try {
            mTitle = mUrl.split("fl-studio-tutorials.de/forum/")[1].split("/")[1].replaceAll("-", " ");
            mTitle = mTitle.replaceFirst(String.valueOf(mTitle.charAt(0)), String.valueOf(Character.toUpperCase(mTitle.charAt(0))));
        } catch (Exception e) {
            e.printStackTrace();
            mTitle = mUrl;
        }

    }

    public void loadFloatingButton(final String html) {
        if (UserData.isLoggedIn()) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    final Dialog dialog = new Dialog(ForumActivity.this, android.R.style.Theme_Translucent_NoTitleBar);
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.setContentView(R.layout.section_comment);
                    Window window = dialog.getWindow();
                    WindowManager.LayoutParams wlp = window.getAttributes();
                    wlp.gravity = Gravity.CENTER;
                    wlp.flags &= ~WindowManager.LayoutParams.FLAG_BLUR_BEHIND;
                    window.setAttributes(wlp);
                    dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
                    fabWebView = dialog.findViewById(R.id.preview);

                    fabWebView.getSettings().setJavaScriptEnabled(true);
                    fabWebView.setWebViewClient(new WebViewClient() {
                        @Override
                        public void onPageFinished(WebView view, String url) {
                            super.onPageFinished(view, url);
                            MainActivity.getMainactivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    fab.setVisibility(View.VISIBLE);
                                    fab.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            new Comment(ForumActivity.this, dialog);
                                        }
                                    });
                                }
                            });
                        }
                    });
                    fabWebView.loadDataWithBaseURL(originalUrl, html, "text/html", "UTF-8", originalUrl);
                }
            });
        } else {
            f.setVisibility(View.VISIBLE);
            f.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Snackbar.make(findViewById(R.id.forum_activity), "Melde dich an, um kommentieren zu können", 5000).setAction("LOGIN", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ForumActivity.this.startActivity(new Intent(ForumActivity.this, LoginActivity.class));
                        }
                    }).show();
                }
            });
        }
    }

    public static Document getDocument(String url) {
        Document document = MainActivity.getHTML(url);
        settup(document);
        return document;
    }

    public static void settup(Document document) {
        online = document.getElementsByClass("spCurrentBrowsing").first().text();
        document.getElementsByClass("entry-header").first().remove();
        document.getElementsByClass("spHeadContainer").first().remove();
        document.getElementsByClass("spPlainSection spLeft").first().remove();
        if (document.getElementsByClass("spPlainSection").first() != null)
            document.getElementsByClass("spPlainSection").first().remove();
        document.getElementById("spMainContainer").attr("style", "padding: 0px;");
        document.getElementsByClass("page-content").attr("style", "padding: 0px;");
        document.getElementsByClass("spListSection").attr("style", "padding: 0px;");
        //document.getElementsByClass("spListSection").first().children().select("div").attr("style", "margin-left: 0px;margin-right: 0px;");
        if (document.getElementById("spTopicHeaderRSSButton") != null)
            document.getElementById("spTopicHeaderRSSButton").remove();
        document.getElementsByClass("spPostTrackrating").attr("style", "margin: 0px;padding: 5px;"); //makes trackrating box bigger
        document.getElementsByClass("spTopicViewSection").attr("style", "margin: 0px; padding: 0px;");
        document.getElementsByClass("spTopicPostSection").attr("style", "margin: 0px;");

        document.getElementsByClass("spButton spRight spQuotePost").remove();//Zitieren entfernt
        document.getElementsByClass("spRight spGoToTop").remove();
        document.getElementsByClass("spButton spRight spThankPost").attr("style", "margin-right: 10px;");

        document.getElementsByClass("spFootContainer").first().remove();
        document.getElementsByClass("spPlainSection").attr("style", "visibility: hidden;");
        //pico

        Elements links = document.getElementsByTag("a");
        for (Element l : links) {
            if (!l.hasAttr("href")) continue;
            else if (l.attr("abs:href").startsWith("http://picosong.com/")) {
                String link = l.attr("abs:href");
                l.tagName("div");
                for (Attribute a : l.attributes()) l.removeAttr(a.getKey());
                l.wrap("<div style='width: 100%; height: 190px; overflow:hidden; position: relative;'>\n" +
                        "<iframe src='" + link + "' scrolling='no' style ='position: absolute; top: -270px; width: 100%; height: 800px;'></iframe>\n" +
                        "</div>");
                System.out.println(l.html());
            }
        }
    }

    public static void reloadPage() {
        mWebView.setVisibility(View.GONE);
        ForumActivity.instance.findViewById(R.id.forum_load).setVisibility(View.VISIBLE);
        ForumActivity.instance.load();
    }

    public void loadNextPage(boolean down) {
        if (down) {
            if (!loadNewPageAllowed)
                return;
            loadNewPageAllowed = false;
            System.out.println("ON END SCROLL");
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        Document inject = MainActivity.getHTML(pages_below.get(0).getUrl(), false);//getDocument(pages_below.get(0).getUrl());
                        pages_below.remove(pages_below.get(0));
                        System.out.println("pages.left: " + pages_below.size());
                        final Elements comments = inject.getElementsByClass("spTopicPostSection");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (pages_below.size() == 0)
                                    findViewById(R.id.forum_load_more).setVisibility(View.GONE);
                                else
                                    loadNewPageAllowed = true;

                                for (Element comment : comments) {
                                    mWebView.loadUrl("javascript:(\n" +
                                            "function(){\n" +
                                            "function create(htmlStr) {\n" +
                                            "var div = document.createElement('div');\n" +
                                            "div.innerHTML = htmlStr.trim();\n" +
                                            "return div.firstChild;}" +
                                            "var container = document.getElementsByClassName('spTopicPostContainer')[0];container.appendChild(create('" + comment.outerHtml() + "'));\n" +
                                            "})()");
                                }
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } else {
            if (!loadNewPageAllowedTop)
                return;
            loadNewPageAllowedTop = false;
            System.out.println("ON TOP SCROLL"); //differenz mWebView.getContentHeight() davor und dannach -> nach dem laden scrollview (height)
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        //TODO: FIX
                        Document document = MainActivity.getHTML(pages_above.get(0).getUrl(), false);//getDocument(pages_above.get(0).getUrl());
                        // System.out.println(inject.html());
                        //Document document = Jsoup.parse(inject.body().outerHtml());
                        pages_above.remove(pages_above.get(0));
                        System.out.println("pages.left: " + pages_above.size());
                        final Elements comments = document.getElementsByClass("spTopicPostContainer").first().children();//inject.getElementsByClass("spTopicPostSection");
                        final Elements orderReturned = new Elements();
                        for (int i = comments.size() - 1; i >= 0; i--) {
                            orderReturned.add(comments.get(i));
                        }
                        System.out.println(orderReturned.size() + " " + comments.size());
                        // System.out.println(comments.html());
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                heightBefore = mWebView.getContentHeight();
                                dragLayout.closeTopView(true);
                                for (Element comment : orderReturned) {
                                    if (!comment.outerHtml().startsWith("<div class=\"spTopicPostSection")) {
                                        //System.out.println(Integer.valueOf(comment.attr("id").replaceAll("post", "")));
                                        // System.out.println(comment.outerHtml());
                                        System.out.println("---------------------------------------------");
                                    }
                                    mWebView.loadUrl("javascript:(\n" +
                                            "function(){\n" +
                                            "\tfunction create(htmlStr) {\n" +
                                            "\tvar div = document.createElement('div');\n" +
                                            "\tdiv.innerHTML = htmlStr.trim();\n" +
                                            "\treturn div.firstChild; \n" +
                                            "\t}\n" +
                                            "\tvar container = document.getElementsByClassName('spTopicPostContainer')[0];container.insertBefore(create('" + (comment.outerHtml().startsWith("<div class=\"spTopicPostSection") ? comment.outerHtml() : comment.html()) + "'),container.firstChild);\n" +
                                            "}\n" +
                                            ")()");
                                }
                                if (pages_above.size() == 0)
                                    findViewById(R.id.forum_load_more_top).setVisibility(View.GONE);
                                else
                                    loadNewPageAllowedTop = true;
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    public void showShowCaseView(Target t, String title, String desc, int id) {
        new ShowcaseView.Builder(this).withMaterialShowcase().setTarget(t).setContentTitle(title).setContentText(desc).setStyle(R.style.ShowCaseViewStyle).singleShot(id).build();
    }

   /* public static ForumActivity instance;
    ActionBar actionBar;
    String mUrl;
    ActionMenuView amvMenu;
    String mAuthor;
    String mTitle;

    static boolean isLoaded = false; //für glocke
    static String originalUrl;
    static MenuItem badge;
    static MenuItem notifications;

    static FloatingActionButton fab;
    static WebView fabWebView;

    int currentPage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forum);
        instance = this;

        mUrl = getIntent().getStringExtra("url");
        cleanUrl1();
        originalUrl = getIntent().getStringExtra("url");
        currentPage = getPage();
        cleanUrl2();

        if (getIntent().hasExtra("title"))
            mTitle = getIntent().getStringExtra("title");
        else
            extractTitle();

        final RecyclerView mRecyclerView = findViewById(R.id.recycler_forum);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));


        amvMenu = findViewById(R.id.toolbar_forum_menu);
        amvMenu.setOnMenuItemClickListener(new ActionMenuView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                return onOptionsItemSelected(menuItem);
            }
        });

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar_forum));
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        if (mTitle != null)
            actionBar.setTitle(mTitle + "                                       ");

        profileToolbar();

        fab = findViewById(R.id.comment);
        fabWebView = findViewById(R.id.preview);

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    final int max = getMaxPages();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mRecyclerView.setAdapter(new ForumRecycleViewAdapter(mUrl, mRecyclerView, max, currentPage, findViewById(R.id.forum_activity)));
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        try {
            menu = amvMenu.getMenu();
            getMenuInflater().inflate(R.menu.toolbar_forum, menu);
            badge = menu.findItem(R.id.toolbar_forum_views);
            badge.setVisible(false);
            notifications = menu.findItem(R.id.toolbar_forum_notification);
            FirebaseIDService.draw(mUrl, notifications, true);
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
        return true;
    }

    public static void onFinished() {
        badge.setVisible(true);
        isLoaded = true;
        FirebaseIDService.draw(originalUrl, notifications, false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        System.out.println(item.getItemId());
        switch (item.getItemId()) {
            case R.id.toolbar_forum_more:
                Intent share = new Intent(Intent.ACTION_SEND);
                share.setType("text/plain");
                String sub = "sub";
                share.putExtra(Intent.EXTRA_SUBJECT, sub);
                share.putExtra(Intent.EXTRA_TEXT, originalUrl);
                startActivity(Intent.createChooser(share, "Diesen Thread teilen:"));
                break;
            case R.id.toolbar_forum_notification:
                if (!isLoaded)
                    return true;
                String url = mUrl;
                if (url.contains("#"))
                    url = url.split("#")[0];
                if (url.contains("/page-"))
                    url = url.split("page-")[0];
                FirebaseIDService.toggle(url, mTitle, mAuthor, item);
                break;
            case R.id.toolbar_forum_views:
                //  if (onlineVisible)
                //      hideOnline();
                //   else
                //        showOnline(item);
                //     onlineVisible = !onlineVisible;
                //      ((TextView) findViewById(R.id.online)).setText(online);

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
            ImageView pb = findViewById(R.id.profilePicture_toolbar_forum);
            pb.setVisibility(View.VISIBLE);
            Bitmap bitmap = UserData.getProfilePicture();
            if (bitmap != null)
                pb.setImageBitmap(bitmap);
            findViewById(R.id.button_toolbar_login_forum).setVisibility(View.GONE);
        } else {
            findViewById(R.id.profilePicture_toolbar_forum).setVisibility(View.GONE);
            findViewById(R.id.button_toolbar_login_forum).setVisibility(View.VISIBLE);
            findViewById(R.id.button_toolbar_login_forum).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(ForumActivity.this, LoginActivity.class));
                }
            });
        }
    }

    public void cleanUrl1() {
        if (mUrl.contains("#"))
            mUrl = mUrl.split("#")[0];
    }

    public int getMaxPages() throws Exception {
        Document rss = Jsoup.parse(Jsoup.connect(mUrl + "/rss").get().html(), "", Parser.xmlParser());
        String maxPages = rss.html().split("<link>")[2].split("</link>")[0].replaceAll(" ", "");
        if (maxPages.contains("#"))
            maxPages = maxPages.split("#")[0];
        return getPage(maxPages);
    }

    public int getPage() {
        return getPage(mUrl);
    }

    public int getPage(String url) {
        if (!url.contains("/page-"))
            return 1;
        return Integer.parseInt(url.split("/page-")[1]);
    }

    public void cleanUrl2() {
        if (!mUrl.contains("/page-"))
            return;
        mUrl = mUrl.split("/page-")[0];
    }

    public void extractTitle() {
        mTitle = mUrl.split("fl-studio-tutorials.de/forum/")[1].split("/")[1].replaceAll("-", " ");
        mTitle = mTitle.replaceFirst(String.valueOf(mTitle.charAt(0)), String.valueOf(Character.toUpperCase(mTitle.charAt(0))));
    }

    public static void loadFloatingButton(final String html) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                fabWebView.getSettings().setJavaScriptEnabled(true);
                fabWebView.setWebViewClient(new WebViewClient() {
                    @Override
                    public void onPageFinished(WebView view, String url) {
                        super.onPageFinished(view, url);
                        MainActivity.getMainactivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                fab.setVisibility(View.VISIBLE);
                                fab.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        new Comment(fabWebView, instance).show();
                                    }
                                });
                            }
                        });
                    }
                });
                fabWebView.loadDataWithBaseURL(originalUrl, html, "text/html", "UTF-8", originalUrl);
            }
        });
    }*/





    /*  RecyclerView mRecyclerView;
    RecyclerView.Adapter mAdapter;
    RecyclerView.LayoutManager mLayoutManager;
    public static ForumActivity instance = null;

    String mTitile;
    String mAuthor;
    String mHTML;
    String mProfilePictureLink;

    TextView textCartItemCount;
    int mCartItemCount = 0;
    String online;
    String comment;

    int pages;
    int page = 1;
    ArrayList<String> pages_ = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forum);
        instance = this;

        amvMenu = (ActionMenuView) findViewById(R.id.toolbar_forum_menu);
        amvMenu.setOnMenuItemClickListener(new ActionMenuView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                return onOptionsItemSelected(menuItem);
            }
        });

        mUrl = getIntent().getStringExtra("url");
        mUrl = getOrginalUrl(mUrl);

        if (mUrl.contains("/page-")) {
            page = Integer.parseInt(mUrl.split("/page-")[1].contains("/") ? mUrl.split("/page-")[1].split("/")[0] : mUrl.split("/page-")[1]);
        }

        if (getIntent().getStringExtra("html") == null) {
            load(mUrl);
        } else {
            mHTML = getIntent().getStringExtra("html");
            mTitile = getIntent().getStringExtra("title");
            mAuthor = getIntent().getStringExtra("author");
            ui();
        }
    }



    public void load(final String url) {
        try {
            Toast.makeText(getApplicationContext(), url, Toast.LENGTH_LONG).show();
            io.fabric.sdk.android.services.concurrency.AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        Document document = MainActivity.getHTML(url);
                        Document comm = document.clone();
                        comm.getElementsByTag("iframe").remove();
                        comment = comm.html();
                        Elements user = document.getElementsByClass("spCurrentBrowsing").first().getElementsByClass("spOnlineUser spType-User spRank-mitglied spUsergroup-members");
                        mCartItemCount = user.size();
                        if (user.size() == 0)
                            System.out.println("member: 0");
                        else
                            System.out.println("member: " + user.size() + " : " + user);

                        online = document.getElementsByClass("spCurrentBrowsing").first().text();

                        settup(document);

                        pages = 1;
                        if (document.getElementsByClass("spPageLinks").last() != null)
                            pages = Integer.parseInt(document.getElementsByClass("spPageLinks").last().attr("abs:href").split("/page-")[1]);
                        System.out.println("LAST_PAGE;: " + pages);
                        for (int i = 0; i < pages; i++) {
                            if ((i + 1) == page)
                                pages_.add(document.html());
                            else
                                pages_.add("");
                        }
                        // System.out.println("pages: : " + pages_);

                        //document.getElementsByClass("").first().remove();

                        mHTML = document.html();
                        mTitile = document.getElementById("spTopicHeaderName").text();
                        mAuthor = document.getElementsByClass("spPostUserName").first().text();
                        ui();
                    } catch (Exception e) {
                        startActivity(new Intent(ForumActivity.this, MainActivity.class));
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            startActivity(new Intent(this, MainActivity.class));
            e.printStackTrace();
        }
    }

    public void ui() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_forum);
                setSupportActionBar(toolbar);

                ActionBar a = getSupportActionBar();
                a.setTitle(mTitile);
                a.setDisplayHomeAsUpEnabled(true);

                if (UserData.isLoggedIn()) {
                    ImageView pb = findViewById(R.id.profilePicture_toolbar_forum);
                    pb.setVisibility(View.VISIBLE);
                    Bitmap bitmap = UserData.getProfilePicture();
                    if (bitmap != null)
                        pb.setImageBitmap(bitmap);
                    findViewById(R.id.button_toolbar_login_forum).setVisibility(View.GONE);

                    final WebView c = (WebView) findViewById(R.id.preview);
                    c.getSettings().setJavaScriptEnabled(true);
                    c.setWebViewClient(new WebViewClient() {
                        @Override
                        public void onPageFinished(WebView view, String url) {
                            super.onPageFinished(view, url);
                            MainActivity.getMainactivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    FloatingActionButton f = (FloatingActionButton) findViewById(R.id.comment);
                                    f.setVisibility(View.VISIBLE);
                                    f.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            new Comment(c, ForumActivity.this).show();
                                        }
                                    });
                                }
                            });
                        }
                    });
                    c.loadDataWithBaseURL(mUrl, comment, "text/html", "UTF-8", mUrl);//postitem_ifr
                } else {
                    findViewById(R.id.profilePicture_toolbar_forum).setVisibility(View.GONE);
                    findViewById(R.id.button_toolbar_login_forum).setVisibility(View.VISIBLE);
                    findViewById(R.id.button_toolbar_login_forum).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            startActivity(new Intent(ForumActivity.this, LoginActivity.class));
                        }
                    });
                    FloatingActionButton f = (FloatingActionButton) findViewById(R.id.comment_grey);
                    f.setVisibility(View.VISIBLE);
                    f.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Snackbar.make(findViewById(R.id.forum_activity), "Melde dich an, um kommentieren zu können", 5000).setAction("LOGIN", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    startActivity(new Intent(ForumActivity.this, LoginActivity.class));
                                }
                            }).show();
                        }
                    });
                }
                mRecyclerView = (RecyclerView) findViewById(R.id.recycler_forum);
                mRecyclerView.setHasFixedSize(true);
                mLayoutManager = new LinearLayoutManager(getApplicationContext());
                mRecyclerView.setLayoutManager(mLayoutManager);
                mAdapter = new ForumRecycleViewAdapter(mUrl.contains("/page-") ? mUrl.split("/page-")[0] : mUrl, findViewById(R.id.forum_activity), ForumActivity.this, pages_.toArray(new String[pages_.size()]));
                mRecyclerView.setAdapter(mAdapter);
                //c.loadDataWithBaseURL(mUrl, mHTML,
                //         "text/html", "UTF-8", mUrl);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        try {
            menu = amvMenu.getMenu();
            getMenuInflater().inflate(R.menu.toolbar_forum, menu);
            FirebaseIDService.draw(mUrl, menu.findItem(R.id.toolbar_forum_notification));

            final MenuItem menuItem = menu.findItem(R.id.toolbar_forum_views);

            View actionView = MenuItemCompat.getActionView(menuItem);
            textCartItemCount = (TextView) actionView.findViewById(R.id.cart_badge);

            setupBadge();

            actionView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onOptionsItemSelected(menuItem);
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            Intent i = new Intent(ForumActivity.this, ForumActivity.class);
            i.putExtra("url", mUrl);
            startActivity(i);
        }


        return true;
    }

    boolean onlineVisible = false;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        System.out.println(item.getItemId());
        switch (item.getItemId()) {
            // action with ID action_refresh was selected
            case R.id.toolbar_forum_more:
                Intent share = new Intent(Intent.ACTION_SEND);
                share.setType("text/plain");
                String sub = "sub";
                share.putExtra(Intent.EXTRA_SUBJECT, sub);
                share.putExtra(Intent.EXTRA_TEXT, mUrl);
                startActivity(Intent.createChooser(share, "Diesen Thread teilen:"));
                break;
            case R.id.toolbar_forum_notification:
                String url = mUrl;
                if (url.contains("#"))
                    url = url.split("#")[0];
                if (url.contains("/page-"))
                    url = url.split("page-")[0];
                FirebaseIDService.toggle(url, mTitile, mAuthor, item);
                break;
            case R.id.toolbar_forum_views:
                if (onlineVisible)
                    hideOnline();
                else
                    showOnline(item);
                onlineVisible = !onlineVisible;
                ((TextView) findViewById(R.id.online)).setText(online);

                break;
            case android.R.id.home:
                onBackPressed();
            default:
                break;
        }
        return true;
    }

    View layout2;
    View layout1;
    View layoutMain;

    public void showOnline(MenuItem item) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            layoutMain = findViewById(R.id.forum_activity);
            layout1 = findViewById(R.id.recycler_forum);
            layout2 = findViewById(R.id.online_layout);
            int x = layoutMain.getWidth() / 3 + layoutMain.getWidth() / 3;
            int y = 0;

            int startRadius = 0;
            int endRadius = (int) Math.hypot(layoutMain.getWidth(), layoutMain.getHeight());

            Animator anim = ViewAnimationUtils.createCircularReveal(layout2, x, y, startRadius, endRadius);
            //anim.setDuration(700);

            layout2.setVisibility(View.VISIBLE);
            anim.start();
        } else {
            findViewById(R.id.online_layout).setVisibility(View.VISIBLE);
            Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), android.R.anim.fade_in);
            animation.setDuration(200);
            findViewById(R.id.online_layout).startAnimation(animation);
        }
    }

    public void hideOnline() {
        Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), android.R.anim.fade_out);
        animation.setDuration(200);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                findViewById(R.id.online_layout).setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        findViewById(R.id.online_layout).startAnimation(animation);
    }

    private void setupBadge() {
        if (textCartItemCount != null) {
            if (mCartItemCount == 0) {
                if (textCartItemCount.getVisibility() != View.GONE) {
                    textCartItemCount.setVisibility(View.GONE);
                }
            } else {
                textCartItemCount.setText(String.valueOf(Math.min(mCartItemCount, 99)));
                if (textCartItemCount.getVisibility() != View.VISIBLE) {
                    textCartItemCount.setVisibility(View.VISIBLE);
                }
            }
        }
    }


*/
}