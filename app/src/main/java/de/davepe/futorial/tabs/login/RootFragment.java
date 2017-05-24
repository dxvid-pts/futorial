package de.davepe.futorial.tabs.login;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;

import de.davepe.futorial.R;

/**
 * Created by pineappslab.com
 * http://www.pineappslab.com/post/fragments-viewpager/
 */

public class RootFragment extends Fragment {

    private static final String TAG = "RootFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.root_fragment, container, false);

        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        if (isLoggedIn())
            transaction.replace(R.id.root_frame, new FragmentAccount());
        else
            transaction.replace(R.id.root_frame, new FragmentLogin());
        transaction.commit();

        return view;
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
}