package de.davepe.futorial;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.miguelcatalan.materialsearchview.MaterialSearchView;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class SearchActivity extends AppCompatActivity {

    // EditText editText;
    MaterialSearchView searchView;
    ListView listView;
    static SharedPreferences suggestions = MainActivity.getMainactivity().getSharedPreferences("search_suggestions", 0);
    static SharedPreferences.Editor suggestionsEditor = suggestions.edit();
    ArrayList<String> resultLinks = new ArrayList<>();
    boolean start = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        Toolbar toolbar = findViewById(R.id.toolbar_search);
        toolbar.setTitle("Suchergebnisse");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        searchView = findViewById(R.id.search_view);
        searchView.setHint("Auf Futorial suchen");
        searchView.showSearch(false);
        searchView.showSuggestions();
        searchView.setSuggestionIcon(getResources().getDrawable(R.drawable.ic_restore_search));
        updateSuggestions();
        listView = findViewById(R.id.listView_search);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                MainActivity.open(resultLinks.get(position));
            }
        });


        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                setVisible(findViewById(R.id.search_state));
                suggestionsEditor.putString("" + new Random().nextLong(), query).commit();
                updateSuggestions();
                final String searchLink = "https://www.fl-studio-tutorials.de/forum?search=1&new=1&forum=all&value=" + query.replaceAll(" ", "+") + "&type=3&include=2";
                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        Document document = MainActivity.getHTML(searchLink);
                        Elements results = document.getElementsByClass("spTopicListSection");
                        if (results.size() == 0)
                            setVisible(findViewById(R.id.no_search_results));
                        else {
                            setVisible(findViewById(R.id.listView_search));
                            String[] list = new String[results.size()];

                            String topicBefore = "";
                            for (int i = 0; i < results.size(); i++) {
                                Element titleElement = results.get(i).getElementsByClass("spListTopicRowName").first();
                                Element topicElement = results.get(i).getElementsByClass("spListForumRowName").first();
                                String topic;
                                if (topicElement == null)
                                    topic = topicBefore;
                                else {
                                    topic = results.get(i).getElementsByClass("spListForumRowName").first().text();
                                    topicBefore = topic;
                                }
                                String title = titleElement.text();
                                String accentColor = String.format("#%06X", (0xFFFFFF & getResources().getColor(R.color.search_category_font)));
                                System.out.println(accentColor);
                                list[i] = "<font color='" + accentColor + "'>[" + topic + "]</font>  " + title;
                                resultLinks.add(titleElement.getElementsByClass("spLink").first().attr("abs:href"));
                            }
                            setListAdapter(list);
                        }
                    }
                });
                System.out.println("Search: " + searchLink);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return true;
            }

        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_menu, menu);
        MenuItem item = menu.findItem(R.id.action_search);
        searchView.setMenuItem(item);
        return true;
    }

    public void updateSuggestions() {
        ArrayList<String> list = new ArrayList(suggestions.getAll().values());
        Set<String> hs = new HashSet<>();
        hs.addAll(list);
        list.clear();
        list.addAll(hs);
        searchView.setSuggestions(list.toArray(new String[list.size()]));
    }

    public void setListAdapter(final String[] results) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                        SearchActivity.this, android.R.layout.simple_list_item_1, results) {

                    @Override
                    public View getView(int position, View convertView, ViewGroup parent) {
                        View view = super.getView(position, convertView, parent);

                        TextView textView = view.findViewById(android.R.id.text1);

                        textView.setTextColor(getResources().getColor(R.color.dark_font));
                        textView.setText(Html.fromHtml(results[position]), TextView.BufferType.SPANNABLE);
                        int padding = 50;
                        textView.setPadding(padding, padding, padding, padding);

                        return view;
                    }
                };

                listView.setAdapter(adapter);
            }
        });
    }

    public void setVisible(final View v) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                findViewById(R.id.search_state).setVisibility(View.GONE);
                findViewById(R.id.no_search_results).setVisibility(View.GONE);
                findViewById(R.id.listView_search).setVisibility(View.GONE);

                v.setVisibility(View.VISIBLE);
            }
        });
    }
}
