package de.davepe.futorial;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.InputStream;
import java.util.ArrayList;

import de.davepe.futorial.welcome.SocialMediaDialog;

public class UserDialog {
    RecyclerView grid;
    Dialog d;
    static Context c;
    TextView name, role;
    String web, yt, fac, sc;

    public UserDialog(final String url, final Context context) {
        d = new Dialog(context);//, R.style.DialogTheme android.R.style.Theme_Translucent_NoTitleBar
        d.requestWindowFeature(Window.FEATURE_NO_TITLE);
        d.setCancelable(true);
        d.setContentView(R.layout.user_dialog);
        d.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        d.show();
        this.c = context;
        grid = d.findViewById(R.id.grid);
        grid.setHasFixedSize(true);
        grid.setLayoutManager(new LinearLayoutManager(c));

        d.findViewById(R.id.button_comments).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.open("https://www.fl-studio-tutorials.de/forum?search=1&new=1&forum=all&value=" + url.split("/")[5] + "&type=4");
            }
        });
        d.findViewById(R.id.button_topics).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.open("https://www.fl-studio-tutorials.de/forum?search=1&new=1&forum=all&value=" + url.split("/")[5] + "&type=5");
            }
        });

        name = d.findViewById(R.id.name);
        role = d.findViewById(R.id.role);

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Document document = MainActivity.getHTML(url);
                    final Elements left = document.getElementsByClass("spColumnSection spProfileLeftCol");
                    final Elements right = document.getElementsByClass("spColumnSection spProfileRightCol");
                    String mProfilePicUrl = document.getElementsByClass("spAvatar").first().children().first().attr("abs:src");

                    final ArrayList<Drawable> icons = new ArrayList<>();
                    final ArrayList<String> data = new ArrayList<>();
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            boolean hasRole = false;
                            for (int i = 0; i < left.size(); i++) {
                                if (left.get(i).text() == null)
                                    continue;
                                if (right.get(i).text() == null)
                                    continue;
                                String l = left.get(i).text(), r = right.get(i).text();
                                switch (l) {
                                    case "Ort:":
                                        icons.add(context.getResources().getDrawable(R.drawable.ic_location));
                                        data.add(l + " " + r);
                                        break;
                                    case "Zuletzt besucht:":
                                        icons.add(context.getResources().getDrawable(R.drawable.ic_eye_dialog));
                                        data.add(l + " " + r);
                                        break;
                                    case "Beiträge:":
                                        icons.add(context.getResources().getDrawable(R.drawable.ic_book));
                                        data.add(l + " " + r);
                                        break;
                                    case "Mitglied seit:":
                                        icons.add(context.getResources().getDrawable(R.drawable.ic_member));
                                        data.add(l + " " + r);
                                        break;
                                    case "Nutzername:":
                                        name.setText(r);
                                        break;
                                    case "Rolle:":
                                        hasRole = true;
                                        role.setText(r);
                                        break;
                                    case "Website:":
                                        web = r;
                                        break;
                                    case "YouTube:":
                                        yt = r;
                                        break;
                                    case "Facebook:":
                                        fac = r;
                                        break;
                                    case "SoundCloud:":
                                        sc = r;
                                        break;
                                    default:
                                        break;
                                }
                            }
                            if (!hasRole)
                                role.setText("Member");
                            grid.setAdapter(new DataAdapter(icons, data));
                            d.findViewById(R.id.social_media).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    new SocialMediaDialog(c, web, yt, fac, sc);
                                }
                            });
                        }
                    });
                    new DownloadImageTask((ImageView) d.findViewById(R.id.userProfilePicture)).execute(mProfilePicUrl);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
            d.findViewById(R.id.user_dialog).setVisibility(View.VISIBLE);
        }
    }

    private final class DataAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        LayoutInflater inflater;
        ArrayList<Drawable> icons = new ArrayList<>();
        ArrayList<String> data = new ArrayList<>();

        DataAdapter(ArrayList<Drawable> icons, ArrayList<String> data) {
            this.icons = icons;
            this.data = data;
            this.inflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = inflater.inflate(R.layout.module_user_dialog, null);
            RecyclerView.ViewHolder holder = new RowNewsViewHolder(view);
            return holder;

        }

        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
            ((RowNewsViewHolder) holder).desc.setText(data.get(position));
            ((RowNewsViewHolder) holder).icon.setImageDrawable(icons.get(position));
        }

        @Override
        public int getItemCount() {
            return icons.size();
        }
    }

    public static class RowNewsViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView desc;

        public RowNewsViewHolder(View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.icon);
            desc = itemView.findViewById(R.id.desc);
        }
    }
}
