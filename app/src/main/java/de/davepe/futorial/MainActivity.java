package de.davepe.futorial;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.customtabs.CustomTabsIntent;
import android.support.design.internal.BottomNavigationItemView;
import android.support.design.internal.BottomNavigationMenuView;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.amlcurran.showcaseview.OnShowcaseEventListener;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.Target;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import de.davepe.futorial.tabs.forum.FragmentForum;
import de.davepe.futorial.welcome.WelcomeActivity;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static MainActivity instance;
    public static boolean block = false;
    public static boolean start = false;
    boolean webInForum = false;
    boolean forum = false;
    String loadedUrl = "";
    String loadedForumUrl = "";
    HashMap<String, Document> saved = new HashMap<>();
    public static ProgressBar prgs;
    public static LinearLayout user_online;
    View onlineUserView;
    SharedPreferences sharedPreferences;

    View slideUpView;
    SlidingUpPanelLayout slidingUpPanelLayout;

    private FirebaseAnalytics mFirebaseAnalytics;

    ViewPager viewPager;
    BottomNavigationView navigation;

    public void setOnlineVisible(final boolean b) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((SlidingUpPanelLayout) findViewById(R.id.sliding_layout)).setPanelHeight(b ? Math.round(getResources().getDimension(R.dimen.panel_height)) : 0);
                if (b) {
                    // onlineUserView.setVisibility(View.INVISIBLE);
                    // slidingVisibility(true);
                    Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_up_online);
                    animation.setDuration(200);
                    animation.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {
                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            onlineUserView.setVisibility(View.VISIBLE);
                            slidingVisibility(true);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });
                    if (onlineUserView.getVisibility() == View.VISIBLE) {
                        slidingVisibility(false);
                        slideUpView.startAnimation(animation);
                    } else slidingVisibility(true);
                    onlineUserView.startAnimation(animation);
                } else {
                    if (onlineUserView.getVisibility() == View.GONE)
                        return;
                    Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_down_online);
                    animation.setDuration(200);
                    animation.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            onlineUserView.setVisibility(View.GONE);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });
                    onlineUserView.startAnimation(animation);
                    slidingVisibility(false);
                }
            }
        });
    }

    private void slidingVisibility(boolean visible) {
        SlidingUpPanelLayout.LayoutParams loparams = (SlidingUpPanelLayout.LayoutParams) slideUpView.getLayoutParams();
        loparams.width = visible ? SlidingUpPanelLayout.LayoutParams.MATCH_PARENT : 0;
        slideUpView.setLayoutParams(loparams);
    }

    private ViewPager.OnPageChangeListener onPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            switch (position) {
                case 0:
                    setOnlineVisible(true);
                    navigation.setSelectedItemId(R.id.navigation_news);
                    break;
                case 1:
                    setOnlineVisible(true);
                    navigation.setSelectedItemId(R.id.navigation_forum);
                    break;
                case 2:
                    setOnlineVisible(false);
                    navigation.setSelectedItemId(R.id.navigation_notifications);
                    break;
                case 3:
                    setOnlineVisible(false);
                    navigation.setSelectedItemId(R.id.navigation_shop);
                    break;
                case 4:
                    setOnlineVisible(false);
                    navigation.setSelectedItemId(R.id.navigation_account);
                    break;
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    private BottomNavigationView.OnNavigationItemSelectedListener onNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(MenuItem item) {
            if (block)
                return false;
            switch (item.getItemId()) {
                case R.id.navigation_news:
                    viewPager.setCurrentItem(0);
                    break;
                case R.id.navigation_forum:
                    viewPager.setCurrentItem(1);
                    break;
                case R.id.navigation_notifications:
                    viewPager.setCurrentItem(2);
                    break;
                case R.id.navigation_shop:
                    viewPager.setCurrentItem(3);
                    break;
                case R.id.navigation_account:
                    viewPager.setCurrentItem(4);
                    break;
            }
            slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            allowedSlidePanel = false;
            return true;
        }

    };
    boolean allowedSlidePanel = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        instance = this;

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        System.out.println("FIREBASE: " + sharedPreferences.getString("fcm_token", "null"));
        if (sharedPreferences.getBoolean("first_start", true)) {
            sharedPreferences.edit().putString("update_location", "https://dl.dropbox.com/s/gwssfvzpbg3aldp/futorial.apk?dl=0").commit();
            startActivity(new Intent(this, WelcomeActivity.class));
            finish();
        }
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        //FirebaseMessaging.getInstance().subscribeToTopic("test-channel");

        onlineUserView = findViewById(R.id.main_online);
        slideUpView = findViewById(R.id.slide_up);

        viewPager = findViewById(R.id.viewPager);
        SectionsPagerAdapter adapter = new SectionsPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(onPageChangeListener);
        viewPager.setOffscreenPageLimit(4);

        slidingUpPanelLayout = findViewById(R.id.sliding_layout);
        slidingUpPanelLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                System.out.println("touch");
                allowedSlidePanel = true;
                return false;
            }
        });
        slidingUpPanelLayout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {

            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
                if (!allowedSlidePanel)
                    slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            }
        });
        settupNavigationDrawer();

        navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener);
        disableShiftMode(navigation);

        if (UserData.isLoggedIn()) {
            findViewById(R.id.toolbar_logged_in).setVisibility(View.VISIBLE);
            findViewById(R.id.toolbar_login).setVisibility(View.GONE);
        } else {
            findViewById(R.id.toolbar_logged_in).setVisibility(View.GONE);
            findViewById(R.id.toolbar_login).setVisibility(View.VISIBLE);

            findViewById(R.id.button_toolbar_login).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                }
            });
        }

        findViewById(R.id.search).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, SearchActivity.class));
            }
        });
        findViewById(R.id.search2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, SearchActivity.class));
            }
        });
        findViewById(R.id.menu).setOnClickListener(openNav);
        findViewById(R.id.menu2).setOnClickListener(openNav);
        System.out.println("token: " + PreferenceManager.getDefaultSharedPreferences(this).getString("fcm_token", "failed"));

        prgs = findViewById(R.id.progress_online_user);
        user_online = findViewById(R.id.user_online);

        findViewById(R.id.trackrating).setOnClickListener(FragmentForum.randomTrack); //random Track
        MainActivity.showShowCaseView(new Showcase(new Target() {
            @Override
            public Point getPoint() {
                int[] location = new int[2];
                View v = findViewById(R.id.widget_up);
                v.getLocationOnScreen(location);
                int x = v.getWidth() / 2;
                int y = location[1] + 20;
                return new Point(x, y);
            }
        }, "Random Tracks", "Swipe up to get a random track.", 27));

        //------------------------------------UPDATER------------------------------------//

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        database.getReference("download_location").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                sharedPreferences.edit().putString("update_location", dataSnapshot.getValue(String.class)).commit();
            }

            @Override
            public void onCancelled(DatabaseError error) {
            }
        });
        database.getReference("app_version").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    if (!(dataSnapshot.getValue(Long.class) == MainActivity.this.getPackageManager().getPackageInfo(getPackageName(), 0).versionCode)) {
                        Snackbar.make(findViewById(R.id.content), "Eine neue Version der App ist verfügbar. (Version: " + (dataSnapshot.getValue(Long.class)) + ")",
                                8000).setAction("UPDATE", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if (isStoragePermissionGranted()) downloadUpdate();
                            }
                        }).show();
                    }
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
            }
        });
    }

    public boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
                return false;
            }
        } else return true;

    }

    final int PERMISSION_REQUEST_CODE = 325;

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    downloadUpdate();
                break;
        }
    }

    ProgressDialog progressDialog;

    public void downloadUpdate() {
        System.out.println("updating... " + sharedPreferences.getString("update_location", ""));

        progressDialog = new ProgressDialog(this, R.style.progressDialog);
        progressDialog.setTitle("Downloading...");
        progressDialog.setMax(100);
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.show();

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(sharedPreferences.getString("update_location", ""));

                    HttpURLConnection c = (HttpURLConnection) url.openConnection();
                    c.setRequestMethod("GET");
                    c.setDoOutput(true);
                    c.connect();

                    int lenghtOfFile = c.getContentLength();

                    String PATH = Environment.getExternalStorageDirectory() + "/Futorial/";//Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
                    new File(PATH).mkdir();
                    File output = new File(new File(PATH), "futorial_update.apk");

                    System.out.println(output.getPath());
                    FileOutputStream fos = new FileOutputStream(output);

                    InputStream is = c.getInputStream();

                    byte[] buffer = new byte[1024];
                    int len1 = 0;

                    int total = 0;
                    while ((len1 = is.read(buffer)) != -1) {
                        total += len1;
                        progressDialog.setProgress((total * 100) / lenghtOfFile);
                        fos.write(buffer, 0, len1);
                    }
                    fos.close();
                    is.close();
                    System.out.println("Download complete!");
                    progressDialog.dismiss();

                    progressDialog.setProgress(111);
                    install(output);

                } catch (Exception e) {
                    Log.e("UpdateAPP", "Update error! " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }

    private void install(final File f) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Build.VERSION.SDK_INT >= 23 ? FileProvider.getUriForFile(getApplicationContext(),
                getApplicationContext().getPackageName() + ".provider", f) : Uri.fromFile(f), "application/vnd.android.package-archive");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);// without this flag android returned a intent error!
        startActivity(intent);
    }

    public static void open(String url, Context c) {
        System.out.println("open: " + url);
        if (!url.contains("fl-studio-tutorials.de") || url.contains("https://www.fl-studio-tutorials.de/wp-login.php?action=register")) {
            ChromeCustomTab(url);
            return;
        }
        if (url.contains("fl-studio-tutorials.de/forum/profile")) {
            new UserDialog(url, c);
            return;
        }
        if (url.contains("fl-studio-tutorials.de/forum/")) {
            String cleanedUrl = url;
            if (cleanedUrl.contains("/page-"))
                cleanedUrl = cleanedUrl.split("/page-")[0];
            if (cleanedUrl.contains("#"))
                cleanedUrl = cleanedUrl.split("#")[0];
            System.out.println(cleanedUrl.split("/").length);
            if (cleanedUrl.split("/").length == 5) {
                Intent i = new Intent(MainActivity.getMainactivity(), MainSubActivity.class);
                i.putExtra("url", url);
                MainActivity.getMainactivity().startActivity(i);
                return;
            } else {
                Intent i = new Intent(MainActivity.getMainactivity(), ForumActivity.class);
                i.putExtra("url", url);
                MainActivity.getMainactivity().startActivity(i);
                return;
            }
        }

        Intent i = new Intent(MainActivity.getMainactivity(), FutorialTabActivity.class);
        i.putExtra("url", url);
        MainActivity.getMainactivity().startActivity(i);
    }

    public static void open(String url) {
        open(url, getMainactivity());
    }

    public static void ChromeCustomTab(String url) {
        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
        builder.setToolbarColor(MainActivity.getMainactivity().getResources().getColor(R.color.colorPrimary));
        CustomTabsIntent customTabsIntent = builder.build();
        customTabsIntent.launchUrl(MainActivity.getMainactivity(), Uri.parse(url));
    }

    public void settupNavigationDrawer() {
        NavigationView navigationView = findViewById(R.id.drawer);
        Bitmap b = UserData.getProfilePicture();
        if (UserData.isLoggedIn() && b != null) {
            View hView = navigationView.getHeaderView(0);
            ((TextView) hView.findViewById(R.id.drawer_user_name)).setText(UserData.getUserName());
            ((TextView) hView.findViewById(R.id.drawer_email)).setText(UserData.getEmail());
            ((ImageView) hView.findViewById(R.id.drawer_profilePicture)).setImageBitmap(b);
            ((ImageView) hView.findViewById(R.id.drawer_background)).setImageBitmap(blur(b));
        } else {
            navigationView.removeHeaderView(navigationView.getHeaderView(0));
            navigationView.inflateHeaderView(R.layout.drawer_header_login);
            ((ImageView) navigationView.getHeaderView(0).findViewById(R.id.drawer_background)).setImageBitmap(blur(BitmapFactory.decodeResource(getApplication().getResources(), R.drawable.user_default)));
            navigationView.getHeaderView(0).findViewById(R.id.button_drawer_login).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                }
            });
        }
        navigationView.setCheckedItem(R.id.drawer_forum);
        navigationView.setNavigationItemSelectedListener(this);
    }

    public static Bitmap blur(Bitmap b) {
        Bitmap blured = Bitmap.createScaledBitmap(b, 2, 3, true);
        Canvas canvas = new Canvas(blured);
        canvas.drawARGB(80, 0, 0, 0);
        canvas.drawBitmap(blured, new Matrix(), new Paint());
        return blured;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        switch (item.getItemId()) {
            case R.id.drawer_impressum:
                open("https://www.fl-studio-tutorials.de/impressum");
                break;
            case R.id.drawer_news:
                open("https://www.fl-studio-tutorials.de/category/news");
                break;
            case R.id.drawer_futorial_records:
                open("https://www.fl-studio-tutorials.de/f-a-q");
                break;
            case R.id.drawer_reviews:
                open("https://www.fl-studio-tutorials.de/category/reviews");
                break;
            case R.id.drawer_feedback:
                open("https://www.fl-studio-tutorials.de/forum/dies-das/futorial-app/page-2"); //TODO /page-2
                break;
            //  case R.id.drawer_settings:
            //   startActivity(new Intent(MainActivity.this, SettingsActivity.class));
        }
        //close navigation drawer
        ((DrawerLayout) findViewById(R.id.main)).closeDrawer(Gravity.START);
        return true;
    }

    View.OnClickListener openNav = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (!block)
                ((DrawerLayout) findViewById(R.id.main)).openDrawer(Gravity.START);
        }
    };

    public void settup(final WebView webView) {
        settup(webView, findViewById(R.id.main));
    }


    Document document;

    public void settup(final WebView webView, final View root) {
        runOnUiThread(new Runnable() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public void run() {
                CookieSyncManager.createInstance(getApplicationContext());
                CookieManager.getInstance().setAcceptCookie(true);
                CookieSyncManager.getInstance().startSync();

                webView.getSettings().setJavaScriptEnabled(true);
                webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
                    webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
                else
                    webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);


                webView.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);
                String newUA = "Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.9.0.4) Gecko/20100101 Firefox/4.0";
                webView.getSettings().setUserAgentString(newUA);

                //Diable Horizontal Scrolling
                webView.setHorizontalScrollBarEnabled(false);
                webView.setOnTouchListener(new View.OnTouchListener() {
                    float m_downX;

                    public boolean onTouch(View v, MotionEvent event) {
                        if (event.getPointerCount() > 1) {
                            //Multi touch detected
                            return true;
                        }

                        switch (event.getAction()) {
                            case MotionEvent.ACTION_DOWN: {
                                // save the x
                                m_downX = event.getX();
                                break;
                            }
                            case MotionEvent.ACTION_MOVE:
                            case MotionEvent.ACTION_CANCEL:
                            case MotionEvent.ACTION_UP: {
                                // set x so that it doesn't move
                                event.setLocation(m_downX, event.getY());
                                break;
                            }
                        }
                        return false;
                    }
                });
                webView.addJavascriptInterface(new JavascriptWebInterface(root), "Android");
                webView.setWebViewClient(new WebViewClient() {
                    @Override
                    public boolean shouldOverrideUrlLoading(final WebView view, final String url) {
                        open(url);
                        return true;
                    }

                    @Override
                    public void onLoadResource(WebView view, String url) {
                        super.onLoadResource(view, url);
                        MainActivity.onLoadResource(view, url);
                    }

                 /*   @Override
                    public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
                        WebResourceResponse wrr = MainActivity.this.shouldInterceptRequest(url);
                        if (wrr == null)
                            return super.shouldInterceptRequest(view, url);
                        else return wrr;
                    }

                    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                    @Override
                    public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                        WebResourceResponse wrr = MainActivity.this.shouldInterceptRequest(request.getUrl().getEncodedPath());
                        if (wrr == null)
                            return super.shouldInterceptRequest(view, request);
                        else return wrr;
                    }*/
                });
            }
        });
    }

    public static void onLoadResource(WebView view, String url) {
        view.loadUrl("javascript:(function(){" +
                "var msg = document.getElementsByClassName('notice-wrap')[0]; msg.style.position='relative';" +
                "    if(msg.hasChildNodes()){" +
                "Android.sendInfoBox(msg.innerHTML);" +
                "    }else{" +
                "        console.log(\"no new messages...\");" +
                "    }" +
                "})()");
    }

    public WebResourceResponse shouldInterceptRequest(String url) {
        HashMap<String, InputStream> replace = new HashMap<>();
        replace.put("https://s.ytimg.com/yts/imgbin/player-cougar-vflcpT6bR.png", getResources().openRawResource(R.raw.yt));
        replace.put("https://widget.sndcdn.com/assets/images/logo-200x120-a1591e.png", getResources().openRawResource(R.raw.soundcloud));
        replace.put("https://www.fl-studio-tutorials.de/wp-content/sp-resources/forum-plugins/blog-linking/resources/images/sp_blog_link.png", getResources().openRawResource(R.raw.sp_blog_link));
        replace.put("https://www.fl-studio-tutorials.de/wp-content/sp-resources/forum-avatar-pool/User_Blue.png", getResources().openRawResource(R.raw.user_blue));
        replace.put("https://www.fl-studio-tutorials.de/wp-content/sp-resources/forum-avatar-pool/User_Red.png", getResources().openRawResource(R.raw.user_red));
        replace.put("https://www.fl-studio-tutorials.de/wp-content/sp-resources/forum-avatar-pool/User_Green.png", getResources().openRawResource(R.raw.user_green));
        replace.put("https://www.fl-studio-tutorials.de/wp-content/sp-resources/forum-avatars/defaults/userdefault.png", getResources().openRawResource(R.raw.userdefault));
        replace.put("https://www.fl-studio-tutorials.de/wp-content/sp-resources/forum-smileys/happy.png", getResources().openRawResource(+R.drawable.smiley_happy));
        replace.put("https://www.fl-studio-tutorials.de/wp-content/sp-resources/forum-smileys/lol.png", getResources().openRawResource(+R.drawable.lol));
        replace.put("https://www.fl-studio-tutorials.de/wp-content/sp-resources/forum-smileys/sad_2.png", getResources().openRawResource(+R.drawable.sad_2));
        replace.put("https://www.fl-studio-tutorials.de/wp-content/sp-resources/forum-smileys/shocked.png", getResources().openRawResource(+R.drawable.shocked));
        replace.put("https://www.fl-studio-tutorials.de/wp-content/sp-resources/forum-smileys/angry.png", getResources().openRawResource(+R.drawable.angry));
        replace.put("https://www.fl-studio-tutorials.de/wp-content/sp-resources/forum-smileys/cry.png", getResources().openRawResource(+R.drawable.cry));
        replace.put("https://www.fl-studio-tutorials.de/wp-content/sp-resources/forum-smileys/zinker.png", getResources().openRawResource(+R.drawable.zinker));
        replace.put("https://www.fl-studio-tutorials.de/wp-content/sp-resources/forum-smileys/zunge.png", getResources().openRawResource(+R.drawable.zunge));
        replace.put("https://www.fl-studio-tutorials.de/wp-content/sp-resources/forum-smileys/surprise.png", getResources().openRawResource(+R.drawable.surprise));
        replace.put("https://www.fl-studio-tutorials.de/wp-content/sp-resources/forum-smileys/love.png", getResources().openRawResource(+R.drawable.love));
        replace.put("https://www.fl-studio-tutorials.de/wp-content/sp-resources/forum-smileys/heart.png", getResources().openRawResource(+R.drawable.heart));
        replace.put("https://www.fl-studio-tutorials.de/wp-content/sp-resources/forum-smileys/cube.png", getResources().openRawResource(+R.drawable.cube));
        replace.put("https://www.fl-studio-tutorials.de/wp-content/sp-resources/forum-smileys/key.png", getResources().openRawResource(+R.drawable.key));
        replace.put("https://www.fl-studio-tutorials.de/wp-content/sp-resources/forum-smileys/eq.png", getResources().openRawResource(+R.drawable.eq));
        replace.put("https://www.fl-studio-tutorials.de/wp-content/sp-resources/forum-smileys/DJ.png", getResources().openRawResource(+R.drawable.dj));
        replace.put("https://www.fl-studio-tutorials.de/wp-content/sp-resources/forum-smileys/mixer.png", getResources().openRawResource(+R.drawable.mixer));
        replace.put("https://www.fl-studio-tutorials.de/wp-content/sp-resources/forum-smileys/vinyl.png", getResources().openRawResource(+R.drawable.vinyl));
        replace.put("https://www.fl-studio-tutorials.de/wp-content/sp-resources/forum-smileys/FuCoin.png", getResources().openRawResource(+R.drawable.fucoin));
        replace.put("https://www.fl-studio-tutorials.de/wp-content/sp-resources/forum-smileys/1.png", getResources().openRawResource(+R.drawable.eins));
        replace.put("https://www.fl-studio-tutorials.de/wp-content/sp-resources/forum-smileys/2.png", getResources().openRawResource(+R.drawable.zwei));
        replace.put("https://www.fl-studio-tutorials.de/wp-content/sp-resources/forum-smileys/3.png", getResources().openRawResource(+R.drawable.drei));
        replace.put("https://www.fl-studio-tutorials.de/wp-content/sp-resources/forum-smileys/4.png", getResources().openRawResource(+R.drawable.vier));
        replace.put("https://www.fl-studio-tutorials.de/wp-content/sp-resources/forum-smileys/5.png", getResources().openRawResource(+R.drawable.fuenf));
        replace.put("https://www.fl-studio-tutorials.de/wp-content/sp-resources/forum-smileys/6.png", getResources().openRawResource(+R.drawable.sechs));
        replace.put("https://www.fl-studio-tutorials.de/wp-content/sp-resources/forum-smileys/7.png", getResources().openRawResource(+R.drawable.sieben));
        replace.put("https://www.fl-studio-tutorials.de/wp-content/sp-resources/forum-smileys/8.png", getResources().openRawResource(+R.drawable.acht));
        replace.put("https://www.fl-studio-tutorials.de/wp-content/sp-resources/forum-smileys/9.png", getResources().openRawResource(+R.drawable.neun));
        replace.put("https://www.fl-studio-tutorials.de/wp-content/sp-resources/forum-smileys/10.png", getResources().openRawResource(+R.drawable.zehn));
        replace.put("https://www.fl-studio-tutorials.de/wp-content/sp-resources/forum-themes/futorial/images/sp_EditHistory.png", getResources().openRawResource(R.raw.sp_edit_history));
        replace.put("https://www.fl-studio-tutorials.de/wp-content/sp-resources/forum-themes/futorial/images/sp_Permalink.png", getResources().openRawResource(R.raw.sp_permalink));
        replace.put("https://www.fl-studio-tutorials.de/wp-content/sp-resources/forum-themes/futorial/images/sp_ThanksRank.png", getResources().openRawResource(R.raw.sp_thanks_rank));
        replace.put("https://www.fl-studio-tutorials.de/wp-content/sp-resources/forum-themes/futorial/images/sp_TopicIcon.png", getResources().openRawResource(R.raw.sp_topic_icon));
        //replace.put("https://www.fl-studio-tutorials.de/wp-content/cache/minify/4328a.css", getResources().openRawResource(R.raw.main));
        for (String key : replace.keySet()) {
            if (url == null)
                continue;
            if (url.contains(key)) {
                Log.v("WebView", "Replacing [" + url + "]");
                ContentResolver contentResolver = getContentResolver();
                return new WebResourceResponse(contentResolver.getType(Uri.parse(key)), "UTF-8", replace.get(key));
            }
        }
        return null;
    }

    public static void addFucoinListener(WebView webView, View root) {
        webView.addJavascriptInterface(new JavascriptWebInterface(root), "Android");
    }

    static Snackbar snackbar;

    public static class JavascriptWebInterface {
        String lastBox = "";
        View root;

        public JavascriptWebInterface(View rootView) {
            root = rootView;
        }

        @JavascriptInterface
        public void sendInfoBox(String html) {

            Document document = Jsoup.parse(html);
            final String info = document.getElementsByTag("p").first().text();
            final String coins = document.getElementsByTag("h1").first().text();
            final String data = coins + ":" + info;

            System.out.println(data);

            if (lastBox.contains(data))
                return;
            lastBox = data;

            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    snackbar = Snackbar
                            .make(root, coins.replaceAll("fc", "").replaceAll(" ", "") + " " + info, Snackbar.LENGTH_INDEFINITE)
                            .setAction("OK", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    snackbar.dismiss();
                                }
                            });

                    snackbar.show();
                }
            });
        }
    }

    public static Document getHTML(final String url) {
        return getHTML(url, true);
    }

    public static Document getHTML(final String url, boolean style) {
        Document document = null;
        try {
            if (UserData.isLoggedIn()) {
                final String cookie = CookieManager.getInstance().getCookie("https://www.fl-studio-tutorials.de/");
                if (cookie == null) document = getWithoutCookie(url);
                else document = getWithCookie(url, cookie);
            } else document = getWithoutCookie(url);

            if (style) {
                String[] ids = new String[]{"masthead", "site-header", "responsive-menu-button", "secondary", "content-sidebar", "colophon", "responsive-menu-container", "cookie-notice"};

                for (String id : ids) {
                    Element e = document.getElementById(id);
                    if (e != null)
                        e.remove();
                }
                try {
                    document.getElementsByClass("navigation paging-navigation").first().remove();
                } catch (Exception e) {
                }
            }
            String color = "#ffbf00";
            document.getElementsByTag("a").attr("style", "color: " + color + ";");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return document;
    }

    public static Document getWithCookie(String url, String cookie) {
        try {
            return Jsoup.connect(url).header("Cookie", cookie).get();
        } catch (Exception e) {
            e.printStackTrace();
            return getWithoutCookie(url);
        }
    }

    public static Document getWithoutCookie(String url) {
        try {
            return Jsoup.connect(url).get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static MainActivity getMainactivity() {
        return instance;
    }

    static boolean showcaseVisible = false;
    static ArrayList<Showcase> queue = new ArrayList<>();

    public static void showShowCaseView(Showcase s) {
        if (showcaseVisible) {
            queue.add(s);
            return;
        }
        showcaseVisible = true;
        new ShowcaseView.Builder(MainActivity.getMainactivity()).withMaterialShowcase().setTarget(s.getTarget()).setContentTitle(s.getTitle()).
                setContentText(s.getDescription()).setStyle(R.style.ShowCaseViewStyle).singleShot(s.getId()).setShowcaseEventListener(new OnShowcaseEventListener() {
            @Override
            public void onShowcaseViewHide(ShowcaseView showcaseView) {
                showcaseVisible = false;
                if (queue.size() != 0) {
                    showShowCaseView(queue.get(0));
                    queue.remove(0);
                }
            }

            @Override
            public void onShowcaseViewDidHide(ShowcaseView showcaseView) {

            }

            @Override
            public void onShowcaseViewShow(ShowcaseView showcaseView) {

            }

            @Override
            public void onShowcaseViewTouchBlocked(MotionEvent motionEvent) {

            }
        }).build();
    }

    public static void restart(Context context, Intent nextIntent) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(" ", nextIntent);
        context.startActivity(intent);
        if (context instanceof Activity) {
            ((Activity) context).finish();
        }

        Runtime.getRuntime().exit(0);
    }

    private void disableShiftMode(BottomNavigationView view) {
        BottomNavigationMenuView menuView = (BottomNavigationMenuView) view.getChildAt(0);
        try {
            Field shiftingMode = menuView.getClass().getDeclaredField("mShiftingMode");
            shiftingMode.setAccessible(true);
            shiftingMode.setBoolean(menuView, false);
            shiftingMode.setAccessible(false);
            for (int i = 0; i < menuView.getChildCount(); i++) {
                BottomNavigationItemView item = (BottomNavigationItemView) menuView.getChildAt(i);
                item.setShiftingMode(false);
                // set once again checked value, so view will be updated
                item.setChecked(item.getItemData().isChecked());
            }
        } catch (NoSuchFieldException e) {
            Log.e("BNVHelper", "Unable to get shift mode field", e);
        } catch (IllegalAccessException e) {
            Log.e("BNVHelper", "Unable to change value of shift mode", e);
        }
    }
}
