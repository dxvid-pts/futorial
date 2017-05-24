package de.davepe.futorial;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.Toast;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URL;

public class MainSubActivity extends AppCompatActivity {

    WebView w;
    Document html;
    // DragTopLayout dragLayout;
    String mUrl;
    String topicElement;
    String picUrl;
    Integer pic;
    String title;
    ActionBar actionBar;
    ImageView img;
    ImageView bg;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_sub);
       // if(1==1) return;
        // dragLayout = findViewById(R.id.drag_top_layout_main_sub);
        img = findViewById(R.id.main_backdrop);
        bg = findViewById(R.id.main_background);

        mUrl = getIntent().getStringExtra("url");
        if (mUrl == null)
            finish();
        topicElement = getIntent().getStringExtra("topic");
        title = getIntent().getStringExtra("title");
        pic = getIntent().getIntExtra("pic_url", 246424264);
        if (pic != 246424264)
            settup();

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar_m_s));
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        if (title != null)
            title(title);

        w = findViewById(R.id.main_sub_web);
        MainActivity.getMainactivity().settup(w);
        Toast.makeText(getApplicationContext(),"Dieses Feature ist noch in Arbeit!",Toast.LENGTH_LONG).show();
      /*  w.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                findViewById(R.id.scroll_m_s).onTouchEvent(event);
                return (event.getAction() == MotionEvent.ACTION_MOVE);
            }
        });
       /* w.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                System.out.println(w.getScrollY() + " " + dragLayout.getScrollY());
                if (w.getScrollY() == 0)
                    dragLayout.setTouchMode(true);
                else
                    dragLayout.setTouchMode(false);
                // dragLayout.setTouchMode(AttachUtil.isWebViewAttach(w));
                return false;
            }
        });//*/

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    html = MainActivity.getHTML(mUrl);
                    html.getElementsByClass("spGroupViewSection").attr("style", "padding: 0px; margin: 0px;  width: 100%;");//background: #24373e;
                    html.getElementsByClass("page-content").attr("style", "padding: 0px;");
                    html.getElementsByClass("spMainContainer").attr("style", "padding: 0px;");
                    html.getElementsByClass("spColumnSection spColumnSectionStats spRight").attr("style", "display: inline;");

                    Element topic;
                    if (topicElement == null)
                        topic = html.getElementsByClass("spForumViewSection").first();
                    else
                        topic = html.getElementById(topicElement);
                    topic.attr("style", "display: block; position: fixed; Z-Index: 10; width: 100%;height: 100%; top: 0px; left: 0px; overflow: auto;padding: 0px; margin: 0px;");

                    html.getElementsByClass("spColumnSection spColumnSectionLast spRight").remove();
                    topic.getElementsByClass("spHeaderName spGroupHeaderOpen").remove();
                    topic.getElementsByClass("spHeaderDescription").remove();
                    topic.getElementsByClass("spIcon spRight").remove();

                    Element titleElement = topic.getElementById("spForumHeaderName");
                    if (title == null)
                        title(titleElement.text());
                    if (titleElement != null) {
                        titleElement.remove();
                        topic.getElementsByClass("spColumnSection spColumnSectionTitle spLeft").first().remove();
                    }
                    Element picElement = topic.getElementsByClass("spHeaderIcon spLeft").first();
                    if (pic == 246424264) {
                        picUrl = picElement.attr("abs:src");
                        settup();
                    }
                    picElement.remove();

                    Element pages = html.getElementsByClass("spPageLinks").first();
                    String pagesHtml = "";
                    if (pages != null)
                        pagesHtml = pages.outerHtml();

                    Elements parents = topic.parents();
                    for (Element e : html.body().getAllElements()) {
                        if (e == null)
                            continue;
                        if (e.equals(topic))
                            continue;
                        if (parents.contains(e))
                            continue;
                        if (e.parents().contains(topic))
                            continue;
                        try {
                            e.remove();
                        } catch (Exception E) {
                            System.out.println(e.hasText());
                            E.printStackTrace();
                            continue;
                        }
                    }
                    if (pages != null)
                        topic.after(pagesHtml);
//                    html.getElementsByClass("spPageLinks").first().remove();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            w.loadDataWithBaseURL(null, html.html(), "text/html", "UTF-8", null);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void title(final String title) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                actionBar.setTitle(title);
            }
        });
    }

    public void settup() {
        if (pic == 246424264) {
            new DownloadImageTask().execute(new String[]{picUrl});
        } else {
            Drawable drawable = getResources().getDrawable(pic);
            img.setImageDrawable(drawable);
            bg.setImageDrawable(new BitmapDrawable(getResources(), MainActivity.blur(drawableToBitmap(drawable))));
            //bg.setImageDrawable(new BitmapDrawable(getResources(), MainActivity.blur(BitmapFactory.decodeResource(getApplicationContext().getResources(), pic))));
        }
    }

    public void settupDownloaded(final Bitmap b) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                img.setImageDrawable(new BitmapDrawable(getResources(), b));
                bg.setImageDrawable(new BitmapDrawable(getResources(), MainActivity.blur(b)));
            }
        });
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap = null;
        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if (bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
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
            settupDownloaded(result);
        }
    }
}
