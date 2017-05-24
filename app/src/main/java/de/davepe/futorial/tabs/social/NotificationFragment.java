package de.davepe.futorial.tabs.social;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import de.davepe.futorial.FragmentList;
import de.davepe.futorial.R;

/**
 * Created by David on 30.11.2017.
 */

public class NotificationFragment extends Fragment {

    private static NotificationFragment nf;
    View rootView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.section_notification, container, false);

        nf = this;
        load();

        return rootView;
    }

    public void load() {
        SharedPreferences sp = getActivity().getSharedPreferences("received_notifications", 0);
        ArrayList<String> thread = new ArrayList<>();
        ArrayList<String> comment = new ArrayList<>();
        ArrayList<String> user = new ArrayList<>();

        for (String key : sp.getAll().keySet()) {
            String value = sp.getString(key, "");
            thread.add(value.split("';#")[0]);
            comment.add(value.split("';#")[1]);
            user.add(value.split("';#")[2]);
        }

        if (thread.size() > 0)
            rootView.findViewById(R.id.empty_state_subscribed).setVisibility(View.GONE);
        else return;

        Fragment fragment = new FragmentList();
        Bundle b = new Bundle();
        b.putStringArrayList("title", thread);
        b.putStringArrayList("desc", comment);

        try {
            fragment.setArguments(b);
            FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment, fragment);
            transaction.commit();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void refresh() {
        nf.load();
    }
}
