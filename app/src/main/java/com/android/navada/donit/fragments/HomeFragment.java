package com.android.navada.donit.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import com.android.navada.donit.R;
import com.android.navada.donit.activities.MainActivity;

public class HomeFragment extends Fragment implements BottomNavigationView.OnNavigationItemSelectedListener {

    private BottomNavigationView mBottomNavigationView;
    private Handler handler;
    private static final int DONATE_ID = 1;
    private static final int FEED_ID = 2;
    private static final int VALIDATE_ID = 3;
    private static final int STORIES_ID = 4;
    private static final int DONATIONS_ID = 5;
    private static final String TAG_DONATE = "Donate";
    private static final String TAG_FEED = "Feed";
    private static final String TAG_VALIDATE = "Validate";
    private static final String TAG_DONATIONS = "Donations";
    private static final String TAG_STORIES = "Stories";
    private String CURRENT_TAG;
    private Toolbar toolbar;
    private int navItemId;

    public HomeFragment() {
        // Required empty public constructor
    }

    private void initialize(){

        CURRENT_TAG = TAG_STORIES;
        navItemId = STORIES_ID;
        handler = new Handler();
        toolbar = getActivity().findViewById(R.id.toolbar);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        initialize();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        mBottomNavigationView = view.findViewById(R.id.bottom_nav_view);
        return view;

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setBottomNavBar();

    }

    private void setBottomNavBar(){

        Menu menu = mBottomNavigationView.getMenu();
        menu.clear();

        switch (MainActivity.userType){

            case "donor" :

                menu.add(Menu.NONE, STORIES_ID, Menu.NONE,"Stories").setIcon(R.drawable.ic_story);
                menu.add(Menu.NONE, DONATE_ID, Menu.NONE,"Donate").setIcon(R.drawable.ic_donate);
                break;

            case "volunteer" :

                menu.add(Menu.NONE, STORIES_ID, Menu.NONE,"Stories").setIcon(R.drawable.ic_story);
                menu.add(Menu.NONE, FEED_ID, Menu.NONE,"Feed").setIcon(R.drawable.ic_feed);
                menu.add(Menu.NONE, DONATE_ID, Menu.NONE,"Donate").setIcon(R.drawable.ic_donate);
                break;

            case "superadmin" :

                menu.add(Menu.NONE, STORIES_ID, Menu.NONE,"Stories").setIcon(R.drawable.ic_story);
                menu.add(Menu.NONE, VALIDATE_ID, Menu.NONE,"Validate").setIcon(R.drawable.ic_validate);
                break;

            case "organization" :

                menu.add(Menu.NONE, STORIES_ID, Menu.NONE,"Stories").setIcon(R.drawable.ic_story);
                menu.add(Menu.NONE, VALIDATE_ID, Menu.NONE,"Validate").setIcon(R.drawable.ic_validate);
                menu.add(Menu.NONE, DONATIONS_ID, Menu.NONE,"Donations").setIcon(R.drawable.ic_donate);
                break;

            default :

                menu.add(Menu.NONE, STORIES_ID, Menu.NONE,"Stories").setIcon(R.drawable.ic_story);
                menu.add(Menu.NONE, DONATE_ID, Menu.NONE,"Donate").setIcon(R.drawable.ic_donate);

        }

        loadFragment();

    }

    private Fragment getFragment() {

        switch (navItemId){

            case DONATE_ID : return new DonateFragment();

            case FEED_ID : return new FeedFragment();

            case VALIDATE_ID : if(MainActivity.userType.equals("organization"))
                                    return new OrgValidateFragment() ;
                               else
                                    return new SuperAdminValidateFragment();

            case DONATIONS_ID : return new DonationsFragment();

            case STORIES_ID :

            default : return new StoriesFragment();
        }

    }

    @Override
    public void onResume() {
        super.onResume();

        mBottomNavigationView.setOnNavigationItemSelectedListener(this);

    }

    private void loadFragment() {

        final FragmentManager fragmentManager = getActivity().getSupportFragmentManager();

        if(fragmentManager.getBackStackEntryCount()>0)
            fragmentManager.popBackStack();

        setToolBarTitle();

        Runnable mPendingRunnable = new Runnable() {

            public void run() {
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.replace(R.id.frame_container, getFragment() ,CURRENT_TAG);
                transaction.setCustomAnimations(android.R.anim.fade_in,
                        android.R.anim.fade_out);
                transaction.addToBackStack(null);
                transaction.commitAllowingStateLoss();
            }
        };

        handler.post(mPendingRunnable);
    }

    private void setToolBarTitle(){

        toolbar.setTitle(CURRENT_TAG);

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

        menuItem.setChecked(true);

        navItemId = menuItem.getItemId();

        switch(navItemId){

            case DONATE_ID : CURRENT_TAG = TAG_DONATE;
                             break;

            case FEED_ID : CURRENT_TAG = TAG_FEED;
                           break;

            case VALIDATE_ID : CURRENT_TAG = TAG_VALIDATE;
                               break;

            case DONATIONS_ID : CURRENT_TAG = TAG_DONATIONS;
                                break;

            case STORIES_ID :

            default : CURRENT_TAG = TAG_STORIES;

        }

        loadFragment();

        return true;
    }
}
