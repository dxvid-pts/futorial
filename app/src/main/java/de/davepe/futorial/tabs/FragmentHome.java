package de.davepe.futorial.tabs;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import org.jsoup.nodes.Document;

import de.davepe.futorial.LoginDialog;
import de.davepe.futorial.MainSubActivity;
import de.davepe.futorial.R;
import de.davepe.futorial.UnreadPostsActivity;
import de.davepe.futorial.UserData;

/**
 * Created by David on 10.11.2017.
 */

public class FragmentHome extends Fragment {

    WebView v;
    View rootView;
    Document page = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //rootView = inflater.inflate(R.layout.section_webview, container, false);
        rootView = inflater.inflate(R.layout.section_home, container, false);

        rootView.findViewById(R.id.home_top_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), MainSubActivity.class);
                i.putExtra("url", "https://www.fl-studio-tutorials.de/forum");
                i.putExtra("topic", "group1");
                i.putExtra("title", "Producer Forum");
                i.putExtra("pic_url", R.mipmap.producing_logo);
                startActivity(i);
            }
        });
        rootView.findViewById(R.id.home_top_right).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), MainSubActivity.class);
                i.putExtra("url", "https://www.fl-studio-tutorials.de/forum");
                i.putExtra("topic", "group3");
                i.putExtra("title", "Community Forum");
                i.putExtra("pic_url", R.mipmap.community);
                startActivity(i);
            }
        });
        rootView.findViewById(R.id.home_bottom_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), MainSubActivity.class);
                i.putExtra("url", "https://www.fl-studio-tutorials.de/forum");
                i.putExtra("topic", "group2");
                i.putExtra("title", "Futorial Support");
                i.putExtra("pic_url", R.mipmap.futorial_support);
                startActivity(i);
            }
        });
        rootView.findViewById(R.id.home_bottom_right).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (UserData.isLoggedIn())
                    startActivity(new Intent(getActivity(), UnreadPostsActivity.class));
                else new LoginDialog(getActivity());
            }
        });
       /* v = (WebView) rootView.findViewById(R.id.web);
        MainActivity.getMainactivity().settup(v);

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Document document;
                    if (page == null) {
                        document = MainActivity.getHTML("https://www.fl-studio-tutorials.de/news");
                        document.getElementsByClass("archive-header").first().remove();
                    } else
                        document = page;
                    page = document;
                    final String html = document.html();

                    MainActivity.getMainactivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            v.loadDataWithBaseURL("https://www.fl-studio-tutorials.de/news", html,
                                    "text/html", "UTF-8", "https://www.fl-studio-tutorials.de/news");
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });*/

        return rootView;
    }
}