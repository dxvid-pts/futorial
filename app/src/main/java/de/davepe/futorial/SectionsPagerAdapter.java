package de.davepe.futorial;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import de.davepe.futorial.tabs.FragmentHome;
import de.davepe.futorial.tabs.FragmentShop;
import de.davepe.futorial.tabs.forum.FragmentForum;
import de.davepe.futorial.tabs.login.RootFragment;
import de.davepe.futorial.tabs.social.SocialFragment;

/**
 * Created by David on 10.11.2017.
 */

public class SectionsPagerAdapter extends FragmentPagerAdapter {

    public SectionsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public int getItemPosition(Object object) {
// POSITION_NONE makes it possible to reload the PagerAdapter
        System.out.println("test");
        return POSITION_NONE;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new FragmentHome();
            case 1:
                return new FragmentForum();
            case 2:
                return new SocialFragment();
            case 3:
                return new FragmentShop();
            case 4:
                return new RootFragment();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 5;
    }
}
