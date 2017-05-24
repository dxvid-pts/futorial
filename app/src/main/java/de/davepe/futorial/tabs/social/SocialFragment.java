package de.davepe.futorial.tabs.social;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.davepe.futorial.R;

/**
 * Created by David on 10.11.2017.
 */

public class SocialFragment extends Fragment {

    ViewPager mViewPager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.section_social, container, false);

        mViewPager = rootView.findViewById(R.id.social_viewpager);
        mViewPager.setAdapter(new PagerAdapter(getActivity().getSupportFragmentManager()));

        TabLayout mTabLayout = rootView.findViewById(R.id.social_tabs);
        mTabLayout.setupWithViewPager(mViewPager);
        return rootView;
    }

    public class PagerAdapter extends FragmentPagerAdapter {
        public PagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            int fragmentPos = position % 3;
            switch (fragmentPos) {
                case 0:
                    return new SubscribedTopicsFragment();
                case 1:
                    return new NotificationFragment();
                default:
                    return new SubscribedTopicsFragment();
            }
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            int fragmentPos = position % 3;
            switch (fragmentPos) {
                case 0:
                    return "Abonnements";
                case 1:
                    return "Benachrichtigungen";
                default:
                    return "Abonnements";
            }
        }
    }
}