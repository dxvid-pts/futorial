package de.davepe.futorial.tabs;

import android.app.Activity;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import de.davepe.futorial.R;

/**
 * Created by David on 30.11.2017.
 */

public class MainSubRecycleViewAdapter extends RecyclerView.Adapter<MainSubRecycleViewAdapter.ViewHolder> {
    private String[] time;
    String[] fucoin;
    String[] reason;
    Activity context;
    int length;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // public TextView mTime, mCoin, mReason;
        public ImageView info;
        public ConstraintLayout content;

        public ViewHolder(View view) {
            super(view);
            info = view.findViewById(R.id.fc_info);
            content = view.findViewById(R.id.module_box);
            //  mTime = view.findViewById(R.id.coin_time);
            //   mCoin = view.findViewById(R.id.coin);
            //    mReason = view.findViewById(R.id.reason);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public MainSubRecycleViewAdapter(int length, Activity context) {
        this.length = length;
        this.context = context;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public MainSubRecycleViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                                   int viewType) {
        // create a new view
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.module_forum_category, parent, false);
        // set the view's size, margins, paddings and layout parameters

        ViewHolder vh = new ViewHolder(itemView);
        return vh;
    }

    boolean dark = false;

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        if (dark) {
            holder.content.setBackgroundColor(context.getResources().getColor(R.color.colorPrimaryDark));
        }
        dark = !dark;
        holder.info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        // holder.mTime.setText(time[position]);
        //  holder.mReason.setText(reason[position]);
        //    holder.mCoin.setText(fucoin[position]);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return length;
    }
}