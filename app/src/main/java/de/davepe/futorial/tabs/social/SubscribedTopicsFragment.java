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

public class SubscribedTopicsFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.section_sunscribed, container, false);

        SharedPreferences sp = getActivity().getSharedPreferences("notification_by_link", 0);
        ArrayList<String> name = new ArrayList<>();
        ArrayList<String> date = new ArrayList<>();

        for (String m : sp.getAll().keySet()) {
            String value = sp.getString(m, "");
            name.add(value.equals("") ? "" : value.contains("-;-") ? value.split("-;-")[0] : "null");
            date.add("Abonniert am: " + (value.equals("") ? "" : value.contains("-;-") ? value.split("-;-")[1] : "null"));
        }
        if (name.size() > 0)
            rootView.findViewById(R.id.empty_state_subscribed).setVisibility(View.GONE);
        else return rootView;


        Fragment fragment = new FragmentList();
        Bundle b = new Bundle();
        b.putStringArrayList("title", name);
        b.putStringArrayList("desc", date);
        b.putStringArrayList("links", new ArrayList<>(sp.getAll().keySet()));
        try {
            fragment.setArguments(b);
            FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment, fragment);
            transaction.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rootView;
    }
}
