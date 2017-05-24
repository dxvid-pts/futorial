package de.davepe.futorial.tabs.login;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.percent.PercentFrameLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import de.davepe.futorial.LoginActivity;
import de.davepe.futorial.R;

/**
 * Created by David on 10.11.2017.
 */

public class FragmentLogin extends Fragment {

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
    View button;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.login, container, false);
        rootView.findViewById(R.id.loginLinkButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("start");
                startActivity(new Intent(getActivity(), LoginActivity.class));
               /* int x = layout1.getRight();
                int y = layout1.getBottom();

                int startRadius = 0;
                int endRadius = (int) Math.hypot(layoutMain.getWidth(), layoutMain.getHeight());

                Animator anim = ViewAnimationUtils.createCircularReveal(layout2, x, y, startRadius, endRadius);

                layout2.setVisibility(View.VISIBLE);
                anim.start();
                System.out.println("finish");*/

            }
        });

       /* mUser = (TextView) rootView.findViewById(R.id.username);
        mPass = (TextView) rootView.findViewById(R.id.password);
        mWeb = (WebView) rootView.findViewById(R.id.loginWebView);
        mLogo = rootView.findViewById(R.id.loginLogo);

        rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                try {
                    if (getContext() == null)
                        return;
                    int heightDiff = rootView.getRootView().getHeight() - rootView.getHeight();
                    if (heightDiff > dpToPx(getContext(), 200)) { // if more than 200 dp, it's probably a keyboard...
                        System.out.println("keyboard");
                        focusChange(true);
                    } else {
                        focusChange(false);
                        System.out.println("no keyboard");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        mUser.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // TODO Auto-generated method stub
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
                // TODO Auto-generated method stub
            }

            @Override
            public void afterTextChanged(Editable s) {
                setLogginAllowed(mUser.getText().toString(), mPass.getText().toString());
            }
        });

        mUser.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                focusChange(hasFocus);
            }
        });
        mPass.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                focusChange(hasFocus);
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

                FragmentTransaction transaction = getFragmentManager()
                        .beginTransaction();
                transaction.replace(R.id.root_frame, new FragmentAccount());
                transaction.commit();

                MainActivity.getMainactivity().findViewById(R.id.toolbar_logged_in).setVisibility(View.VISIBLE);
                MainActivity.getMainactivity().findViewById(R.id.toolbar_login).setVisibility(View.GONE);

                return super.shouldOverrideUrlLoading(view, request);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                System.out.println("LOGIN_PAGE_FINISHED");
                if (override) {
                    CookieSyncManager.getInstance().sync();
                    System.out.println("LOGIN_PAGE_" + isLoggedIn());
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

        rootView.findViewById(R.id.loginButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login(mUser.getText().toString(), mPass.getText().toString());
            }
        });

        mWeb.loadUrl("https://www.fl-studio-tutorials.de");

        */
        return rootView;
    }

    public void setLogginAllowed(String user, String pass) {
        if (user.matches("")) {//empty
            loginAllowed = false;
            System.out.println(false);
            return;
        }
        if (pass.matches("")) {//empty
            loginAllowed = false;
            System.out.println(false);
            return;
        }
        loginAllowed = true;
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

    public boolean isLoggedIn() {
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

    public void focusChange(boolean hasFocus) {
        if (hasFocus)
            mLogo.setVisibility(View.GONE);
        else {
            mLogo.setVisibility(View.VISIBLE);
        }
    }

    public static float dpToPx(Context context, float valueInDp) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, valueInDp, metrics);
    }
}
