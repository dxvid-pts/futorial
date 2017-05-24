package de.davepe.futorial.tabs.login;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.mikhaellopez.circularimageview.CircularImageView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URL;
import java.util.ArrayList;

import de.davepe.futorial.MainActivity;
import de.davepe.futorial.R;
import de.davepe.futorial.UserData;

/**
 * Created by David on 10.11.2017.
 */

public class FragmentAccount extends Fragment {

    String mName, mProfileLink, mProfilePictureLink, mCoins;
    View rootView;
    RecyclerView mRecyclerView;
    RecyclerView.Adapter mAdapter;
    SwipeRefreshLayout mSwipeRefreshLayout;
    ArrayList<String> time = new ArrayList<>();
    ArrayList<String> coin = new ArrayList<>();
    ArrayList<String> reason = new ArrayList<>();

    private FirebaseAnalytics mFirebaseAnalytics;

    private ImageView[] bmImage;
    Bitmap b;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.section_logged_in, container, false);

        //init UI
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getActivity());
        mSwipeRefreshLayout = rootView.findViewById(R.id.swipeRefreshLayout_acc);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadData();
            }
        });
        mRecyclerView = rootView.findViewById(R.id.recycler_view_acc);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        //load saved Data
        String s = UserData.getUserName(getContext());
        if (s != null)
            ((TextView) rootView.findViewById(R.id.name)).setText(s);
        s = UserData.getFucoins();
        ((TextView) rootView.findViewById(R.id.fucoins)).setText(s);

        rootView.findViewById(R.id.logout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CookieManager cookieManager = CookieManager.getInstance();
                cookieManager.removeAllCookie(); //logout

                Bundle params = new Bundle();
                params.putString("user_name", mName);
                mFirebaseAnalytics.logEvent("futorial_logout", params);
                MainActivity.restart(getActivity(), new Intent(getActivity(), MainActivity.class));
            }
        });

        bmImage = new ImageView[]{(CircularImageView) rootView.findViewById(R.id.profilePicture),
                (CircularImageView) MainActivity.getMainactivity().findViewById(R.id.profilePicture_toolbar)};
        b = UserData.getProfilePicture();
        if (b != null)
            render(b);

        loadData();

        return rootView;
    }

    public void loadData() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(true);
            }
        });
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    CookieSyncManager.createInstance(getActivity());
                    CookieSyncManager.getInstance().sync();
                    String cookie = CookieManager.getInstance().getCookie("https://www.fl-studio-tutorials.de/");
                    Document document = Jsoup.connect("https://www.fl-studio-tutorials.de/mitgliedschaft").header("Cookie", cookie).get();
                    try {
                        mName = document.getElementsByClass("widget-title").first().text().replaceAll("Willkommen ", "");
                        UserData.setUserName(mName);
                        mProfileLink = document.getElementsByClass("profil-link").first().children().first().attr("abs:href");
                        UserData.setEmail(mProfileLink.replaceAll("https://www.", "").replaceAll("fl-studio-tutorials.de", "futorial"));
                        mProfilePictureLink = document.getElementsByClass("avatar_container").first().children().first().attr("abs:src");
                        mCoins = document.getElementsByClass("myCRED-balance mycred_default").first().text();
                        UserData.setFucoins(mCoins);

                        for (Element row : document.getElementsByTag("tr")) {
                            if (row.text().contains("Date FuCoins Entry"))
                                continue;
                            Elements info = row.getElementsByTag("td");
                            time.add(info.get(0).text());
                            coin.add(info.get(1).text());
                            reason.add(info.get(2).text().replaceAll("FuCoins", ""));
                        }

                        setData();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void setData() {
        new Handler(Looper.getMainLooper()).post(new Runnable() {

            @Override
            public void run() {
                ((TextView) rootView.findViewById(R.id.name)).setText(mName);
                ((TextView) rootView.findViewById(R.id.fucoins)).setText(mCoins);

                mAdapter = new AccountRecycleViewAdapter(time.toArray(new String[time.size()]), coin.toArray(new String[coin.size()]), reason.toArray(new String[reason.size()]));
                mRecyclerView.setAdapter(mAdapter);
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });
        System.out.println("profPigbig: " + mProfilePictureLink);

        if (b == null) {
            new DownloadImageTask().execute(new String[]{mProfilePictureLink});
        }
        UserData.loadProfilePicture(mProfilePictureLink);

        // ((LinearLayout) rootView.findViewById(R.id.coins)).removeAllViews();
        //  loadFuCoinVerlauf();
    }

    public void render(final Bitmap b) {
        for (final ImageView v : bmImage) {
            if (v != null) {
                MainActivity.getMainactivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        v.setImageBitmap(b);
                    }
                });
            }
        }
        MainActivity.getMainactivity().settupNavigationDrawer();
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
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
            render(result);
        }
    }
}
