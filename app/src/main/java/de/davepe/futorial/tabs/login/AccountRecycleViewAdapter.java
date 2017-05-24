package de.davepe.futorial.tabs.login;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import de.davepe.futorial.R;

/**
 * Created by David on 30.11.2017.
 */

public class AccountRecycleViewAdapter extends RecyclerView.Adapter<AccountRecycleViewAdapter.ViewHolder> {
    private String[] time;
    String[] fucoin;
    String[] reason;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView mTime, mCoin, mReason;

        public ViewHolder(View view) {
            super(view);
            mTime = view.findViewById(R.id.coin_time);
            mCoin = view.findViewById(R.id.coin);
            mReason = view.findViewById(R.id.reason);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public AccountRecycleViewAdapter(String[] t, String[] f, String[] r) {
        time = t;
        fucoin = f;
        reason = r;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public AccountRecycleViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                                   int viewType) {
        // create a new view
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.module_fucoin, parent, false);
        // set the view's size, margins, paddings and layout parameters

        ViewHolder vh = new ViewHolder(itemView);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.mTime.setText(time[position]);
        holder.mReason.setText(reason[position]);
        holder.mCoin.setText(fucoin[position]);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return time.length;
    }
}