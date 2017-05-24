package de.davepe.futorial;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.customtabs.CustomTabsIntent;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.webkit.CookieSyncManager;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;

public class LoginActivity extends AppCompatActivity {

    boolean loginAllowed = false;
    TextView mUser;
    TextView mPass;
    WebView mWeb;
    String mUsername;
    String mPasswort;
    boolean override = false;
    boolean finishedLoading = false;
    boolean wantsLogin = false;
    View mLogo;
    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mUser = findViewById(R.id.username);
        mPass = findViewById(R.id.password);
        mWeb = findViewById(R.id.loginWebView);
        mLogo = findViewById(R.id.loginLogo);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        findViewById(R.id.register).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               MainActivity.open("https://www.fl-studio-tutorials.de/wp-login.php?action=register");
            }
        });

        mUser.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                setLogginAllowed(mUser.getText().toString(), mPass.getText().toString());
            }
        });
        mPass.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                setLogginAllowed(mUser.getText().toString(), mPass.getText().toString());
            }
        });

        mWeb.getSettings().setJavaScriptEnabled(true);
        mWeb.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                System.out.println("LOGIN_PAGE_STARTED");
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                System.out.println("LOGIN_PAGE_OVERRIDE");
                override = true;

                MainActivity.getMainactivity().findViewById(R.id.toolbar_logged_in).setVisibility(View.VISIBLE);
                MainActivity.getMainactivity().findViewById(R.id.toolbar_login).setVisibility(View.GONE);

                Bundle params = new Bundle();
                params.putString("user_name", mUsername);
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.LOGIN, params);

                CookieSyncManager.createInstance(getApplicationContext());
                CookieSyncManager.getInstance().sync();

                MainActivity.restart(getApplicationContext(), new Intent(LoginActivity.this, MainActivity.class));

                return super.shouldOverrideUrlLoading(view, request);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                System.out.println("LOGIN_PAGE_FINISHED");
                if (override) {
                    CookieSyncManager.getInstance().sync();
                    System.out.println("LOGIN_PAGE_" + UserData.isLoggedIn());
                    super.onPageFinished(view, url);
                    return;
                }
                finishedLoading = true;
                if (wantsLogin) {
                    startJSLogin();
                    wantsLogin = false;
                }
                super.onPageFinished(view, url);
            }
        });

        findViewById(R.id.loginButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login(mUser.getText().toString(), mPass.getText().toString());
            }
        });

        mWeb.loadUrl("https://www.fl-studio-tutorials.de");
    }

    public void setLogginAllowed(String user, String pass) {
        if (user.matches("")) {//empty
            loginAllowed = false;
            findViewById(R.id.loginButton).setVisibility(View.GONE);
            findViewById(R.id.loginButtonGrey).setVisibility(View.VISIBLE);
            System.out.println(false);
            return;
        }
        if (pass.matches("")) {//empty
            loginAllowed = false;
            findViewById(R.id.loginButton).setVisibility(View.GONE);
            findViewById(R.id.loginButtonGrey).setVisibility(View.VISIBLE);
            System.out.println(false);
            return;
        }
        loginAllowed = true;
        findViewById(R.id.loginButton).setVisibility(View.VISIBLE);
        findViewById(R.id.loginButtonGrey).setVisibility(View.GONE);
        System.out.println(true);
    }

    public void login(String username, String password) {
        if (!loginAllowed)
            return;
        mUsername = username;
        mPasswort = password;
        System.out.println(mUsername + ":" + mPasswort);
        if (finishedLoading)
            startJSLogin();
        else
            wantsLogin = true;
    }

    public void startJSLogin() {
        System.out.println("USER_LOGIN");
        mWeb.loadUrl("javascript:(function(){document.getElementById('user_login').value ='" +
                mUsername + "';document.getElementById('user_pass').value ='" + mPasswort + "';document.getElementById('wp-submit').click();})()");
    }

}
