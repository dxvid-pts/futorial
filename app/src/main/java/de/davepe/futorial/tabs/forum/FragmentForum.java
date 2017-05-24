package de.davepe.futorial.tabs.forum;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.amlcurran.showcaseview.targets.Target;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Random;

import de.davepe.futorial.ForumActivity;
import de.davepe.futorial.FragmentList;
import de.davepe.futorial.LoginDialog;
import de.davepe.futorial.MainActivity;
import de.davepe.futorial.R;
import de.davepe.futorial.Showcase;

/**
 * Created by David on 10.11.2017.
 */

public class FragmentForum extends Fragment {


    public static Document document = null;
    static boolean result = false;
    SwipeRefreshLayout mSwipeRefreshLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.section_forum, container, false);
        mSwipeRefreshLayout = rootView.findViewById(R.id.swipeRefreshLayout);
        loadData();
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });

        return rootView;
    }

    public void loadData() {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mSwipeRefreshLayout.setRefreshing(true);
                        }
                    });
                    CookieManager cm = CookieManager.getInstance();
                    final String cookie = cm.getCookie("https://www.fl-studio-tutorials.de/forum/f-a-q");

                    if (cookie == null)
                        document = Jsoup.connect("https://www.fl-studio-tutorials.de/forum/f-a-q").get();
                    else {
                        try {
                            document = Jsoup.connect("https://www.fl-studio-tutorials.de/forum/f-a-q").header("Cookie", cookie).get();
                        } catch (Exception e) {
                            try {
                                document = Jsoup.connect("https://www.fl-studio-tutorials.de/forum/f-a-q").get();
                            } catch (Exception e2) {
                                e2.printStackTrace();
                            }
                        }
                    }


                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mSwipeRefreshLayout.setRefreshing(false);
                        }
                    });
                    addNewstestPosts(document);
                    addCurrentOnline(document);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void refresh() {
        loadData();
    }

    public void addNewstestPosts(final Document document) {
        if (document == null)
            return;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Elements rows = document.getElementById("spRecentPostsTag").children();

                ArrayList<String> titles = new ArrayList<>();
                ArrayList<String> descs = new ArrayList<>();
                ArrayList<String> links = new ArrayList<>();

                for (Element row : rows) {
                    if (row.nodeName().equals("div")) continue;
                    titles.add(row.children().first().text());
                    descs.add(row.children().last().text());
                    links.add(row.children().first().attr("abs:href"));
                }
                Fragment fragment = new FragmentList();

                Bundle b = new Bundle();
                b.putStringArrayList("title", titles);
                b.putStringArrayList("desc", descs);
                b.putStringArrayList("links", links);
                fragment.setArguments(b);

                try {
                    FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
                    transaction.replace(R.id.fragment, fragment);
                    transaction.commit();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void addCurrentOnline(final Document document) { //TODO: Underline on click, Open Profile
        final Elements user = document.getElementsByClass("spOnlineUser");//spLink spProfilePage
        final ArrayList<String> us = new ArrayList<>();
        final ArrayList<String> links = new ArrayList<>();
        for (Element u : user) {
            try {
                if (us.contains(u.text()))
                    continue;
                if (u.text().contains("Gast/Gäste"))
                    continue;
                if (u == null)
                    continue;

                us.add(u.text());
                if (u.children().size() > 0 && u.children().first().hasAttr("href")) {
                    links.add(u.children().first().attr("abs:href"));
                    System.out.println(true);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LinearLayout linearLayout = MainActivity.getMainactivity().findViewById(R.id.user_online);
                linearLayout.removeAllViews();
                MainActivity.prgs.setVisibility(View.GONE);
                MainActivity.user_online.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                for (int i = 0; i < us.size(); i++) {
                    final int ii = i;
                    final TextView t = new TextView(getContext());
                    t.setText(us.get(i) + ", ");
                    t.setOnTouchListener(new View.OnTouchListener() {
                        public boolean onTouch(View view, MotionEvent event) {
                            if (event.getAction() == android.view.MotionEvent.ACTION_DOWN)
                                t.setText(Html.fromHtml("<u>" + t.getText() + "</u>"));
                            else t.setText(t.getText().toString());
                            return false;
                        }
                    });
                    t.setTextColor(getResources().getColor(R.color.colorAccent));
                    t.setMaxLines(1);
                    linearLayout.addView(t);
                    t.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (links.size() > 0) MainActivity.open(links.get(ii));
                            else
                                new LoginDialog(getActivity());
                        }
                    });

                    if (i == 0)
                        try {
                            MainActivity.showShowCaseView(new Showcase(new Target() {
                                @Override
                                public Point getPoint() {
                                    int[] location = new int[2];
                                    t.getLocationOnScreen(location);
                                    System.out.println(location[0]);
                                    int x = 160;
                                    int y = location[1];
                                    return new Point(x, y);
                                }
                            }, "Erhalte Informationen über User", "Tippe auf einen Benutzernamen für mehr.", 25));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                }
                String gäste = document.getElementsByClass("spCurrentOnline").first().text().split(" Gast/Gäste")[0];
                gäste = gäste.split(" ")[gäste.split(" ").length - 1];
                final TextView t = new TextView(getContext());
                t.setText(gäste + " Gast/Gäste");
                t.setTextColor(getResources().getColor(R.color.colorAccentDark));
                t.setMaxLines(1);
                linearLayout.addView(t);
            }
        });
    }

    public static View.OnClickListener randomTrack = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    String randomPage = "https://www.fl-studio-tutorials.de/forum/track-rating/page-" + (new Random().nextInt(165 - 1) + 1);
                    System.out.println("start: " + randomPage);
                    try {
                        Document document = Jsoup.connect(randomPage).get();

                        Elements tracks = document.getElementsByClass("spColumnSection spColumnSectionTitle spLeft");
                        tracks = tracks.select("a");

                        tracks.first().remove(); //remove Charts
                        System.out.println(tracks.html());

                        final String link = tracks.get(new Random().nextInt(tracks.size())).attr("abs:href");
                        System.out.println("link: " + link);

                        MainActivity.getMainactivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                result = true;
                                // (MainActivity.getMainactivity().findViewById(R.id.section_blur)).setVisibility(View.GONE);
                                // (MainActivity.getMainactivity().findViewById(R.id.section_random)).setVisibility(View.GONE);
                                if (abbruch) {
                                    abbruch = false;
                                    return;
                                }
                                closePopup();

                                Intent i = new Intent(MainActivity.getMainactivity(), ForumActivity.class);
                                i.putExtra("url", link);
                                MainActivity.getMainactivity().startActivity(i);

                                //showWeb();
                                // load(webView, link);
                                //MainActivity.getMainactivity().loadPage(link);
                                //MainActivity.getMainactivity().setVisible(new View[]{MainActivity.getMainactivity().findViewById(R.id.sectionWebView)});
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            ShowPopup(R.layout.random_search_popup);
        }
    };

    static Dialog myDialog;
    static boolean abbruch = false;

    public static Dialog ShowPopup(final int layout) {
        myDialog = new Dialog(MainActivity.getMainactivity());
        myDialog.setContentView(layout);
        myDialog.setCancelable(false);
        myDialog.findViewById(R.id.btnfollow).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                abbruch = true;
                myDialog.dismiss();
            }
        });
        myDialog.findViewById(R.id.txtclose).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                abbruch = true;
                myDialog.dismiss();
            }
        });
        myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        myDialog.show();

        return myDialog;
    }

    public static void closePopup() {
        if (myDialog == null)
            return;
        myDialog.dismiss();
    }
}