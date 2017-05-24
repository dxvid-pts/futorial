package de.davepe.futorial;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by David on 11.05.2018.
 */

public class FragmentList extends Fragment {

    RecyclerView recyclerView;
    ArrayList<String> title, desc, additionalText, links;
    boolean deleteRow = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_list, container, false);

        recyclerView = rootView.findViewById(R.id.recycler);

        Bundle args = getArguments();
        if (args == null) return rootView;
        title = args.getStringArrayList("title");
        desc = args.getStringArrayList("desc");
        if (args.containsKey("additional"))
            additionalText = args.getStringArrayList("additional");
        if (args.containsKey("links"))
            links = args.getStringArrayList("links");
        if (args.containsKey("deleteRow"))
            deleteRow = args.getBoolean("deleteRow");

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(new RecycleViewAdapter());
        return rootView;
    }

    public class RecycleViewAdapter extends RecyclerView.Adapter<RecycleViewAdapter.ViewHolder> {
        public class ViewHolder extends RecyclerView.ViewHolder {
            public TextView title, desc, additional;
            public LinearLayout holder, add_parent;

            public ViewHolder(View view) {
                super(view);
                title = view.findViewById(R.id.title);
                desc = view.findViewById(R.id.desc);
                holder = view.findViewById(R.id.holder);
                additional = view.findViewById(R.id.additional);
                add_parent = view.findViewById(R.id.add_parent);
            }
        }

        @Override
        public RecycleViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            ViewHolder vh = new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.module_list_item, parent, false));
            return vh;
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            holder.title.setText(title.get(position));
            holder.desc.setText(desc.get(position));
            if (additionalText != null) {
                holder.add_parent.setVisibility(View.VISIBLE);
                holder.additional.setText(additionalText.get(position));
            }
            if (links != null && links.size() > 0)
                holder.holder.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (PreferenceManager.getDefaultSharedPreferences(MainActivity.getMainactivity()).getBoolean("allow_forum", true)) {
                            PreferenceManager.getDefaultSharedPreferences(MainActivity.getMainactivity()).edit().putBoolean("allow_forum", false).commit();
                            final Intent i = new Intent(MainActivity.getMainactivity(), ForumActivity.class);
                            i.putExtra("url", links.get(position));
                            i.putExtra("title", title.get(position));

                            if (deleteRow) {
                                /*title.remove(position);
                                desc.remove(position);
                                links.remove(position);
                                if (additionalText != null)
                                    additionalText.remove(position);
                                recyclerView.setAdapter(new RecycleViewAdapter());
                            */
                                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        MainActivity.getMainactivity().startActivity(i);
                                    }
                                }, collapse(holder.holder));
                            } else MainActivity.getMainactivity().startActivity(i);
                        }
                    }
                });

        }

        // Return the size of your dataset (invoked by the layout manager)
        @Override
        public int getItemCount() {
            return title.size();
        }
    }

    public static int collapse(final View v) {
        final int initialHeight = v.getMeasuredHeight();

        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if (interpolatedTime == 1) {
                    v.setVisibility(View.GONE);
                } else {
                    v.getLayoutParams().height = initialHeight - (int) (initialHeight * interpolatedTime);
                    v.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // 1dp/ms
        int duration = (int)(initialHeight / v.getContext().getResources().getDisplayMetrics().density);
        a.setDuration(duration);
        v.startAnimation(a);
        return duration;
    }
}
