package com.android.navada.donit.activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.android.navada.donit.R;
import com.android.navada.donit.fragments.AddEventFragment;
import com.android.navada.donit.fragments.AddRequirementFragment;
import com.android.navada.donit.fragments.HomeFragment;
import com.android.navada.donit.fragments.MyDeliveriesFragment;
import com.android.navada.donit.fragments.MyDonationsFragment;
import com.android.navada.donit.fragments.ProfileFragment;
import com.android.navada.donit.fragments.RequirementsFragment;
import com.android.navada.donit.threads.GetUserData;
import com.google.firebase.auth.FirebaseAuth;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;
    private TextView mNavHeaderUserName;
    private TextView mNavHeaderUserEmail;
    private static final String TAG_HOME = "Home";
    private static final String TAG_PROFILE = "Profile";
    private static final String TAG_MY_DONATIONS = "My Donations";
    private static final String TAG_MY_DELIVERIES = "My Deliveries";
    private static final String TAG_ADD_EVENT = "Add Event";
    private static final String TAG_ADD_REQUIREMENT = "Add Requirement";
    private static final String TAG_REQUIREMENT = "Requirements";
    private FirebaseAuth mAuth;
    private static final int HOME_ID = 1;
    private static final int PROFILE_ID = 2;
    private static final int MY_DONATIONS_ID = 3;
    private static final int MY_DELIVERIES_ID = 4;
    private static final int ADD_EVENT_ID = 5;
    private static final int ADD_REQUIREMENT_ID = 6;
    private static final int REQUIREMENTS_ID = 7;
    private String CURRENT_TAG;
    private int navItemId;
    final Handler handler = new Handler();
    public static String userType;
    public ProgressDialog progressDialog;
    public static HashMap<String, Object> user;
    private AlertDialog.Builder exitBuilder;
    public static boolean isRequirementActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initialize();

    }

    private void initialize(){

        mAuth = FirebaseAuth.getInstance();
        isRequirementActivity = false;

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mDrawerLayout = findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this,mDrawerLayout,toolbar,R.string.navigation_drawer_open,R.string.navigation_drawer_close);
        mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        mNavigationView = findViewById(R.id.nav_view);

        final View mNavHeader = mNavigationView.getHeaderView(0);
        mNavHeaderUserName = mNavHeader.findViewById(R.id.nav_header_user_name);
        mNavHeaderUserEmail = mNavHeader.findViewById(R.id.nav_header_user_email);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            exitBuilder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
        else
            exitBuilder = new AlertDialog.Builder(this);

        exitBuilder.setTitle("Exit")
                .setMessage("Are you sure you want to exit?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        moveTaskToBack(true);
                        android.os.Process.killProcess(android.os.Process.myPid());
                        System.exit(1);
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) { }
                });

        userType = "";

        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setMessage("Please Wait...");

        CURRENT_TAG = TAG_HOME;
        navItemId = 0;

    }

    @Override
    protected void onResume() {
        super.onResume();

            if(!isRequirementActivity) {

                progressDialog.show();
                GetUserData getUserData = new GetUserData(this);
                getUserData.start();
                checkIfDone(getUserData);
            }

    }

    private void checkIfDone(final GetUserData getUserData){

        Runnable runnable = new Runnable() {
            @Override
            public void run() {

                if(getUserData.done){

                    userType = user.get("typeOfUser").toString();

                    mNavHeaderUserEmail.setText(user.get("email").toString());
                    mNavHeaderUserName.setText(user.get("name").toString());

                    addNavMenuItems();

                    mNavigationView.setNavigationItemSelectedListener(MainActivity.this);

                }
                else
                    handler.postDelayed(this, 1000);

            }
        };

        handler.postDelayed(runnable, 1000);

    }

    private void makeToast(String message){

        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()) {

            case R.id.main_menu_sign_out:
                mAuth.signOut();
                makeToast("Signed Out");
                finish();
                break;

            case R.id.main_menu_exit:
                exitBuilder.show();

        }

        return super.onOptionsItemSelected(item);
    }

    private void addNavMenuItems(){

        Menu menu =  mNavigationView.getMenu();
        menu.clear();
        menu.add(0, HOME_ID, 100, TAG_HOME);

        switch (userType){

            case "donor" :

            case "volunteer" :
                                   menu.add(0, PROFILE_ID, 200, TAG_PROFILE);
                                   menu.add(0, MY_DONATIONS_ID, 300, TAG_MY_DONATIONS);
                                   menu.add(0, MY_DELIVERIES_ID, 400, TAG_MY_DELIVERIES);
                                   menu.add(0, REQUIREMENTS_ID, 500, TAG_REQUIREMENT);
                                   break;

            case "organization" :

                                   menu.add(0, ADD_REQUIREMENT_ID, 300, TAG_ADD_REQUIREMENT);
                                   menu.add(0, ADD_EVENT_ID, 400, TAG_ADD_EVENT);

        }

        loadFragment();
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START))
            mDrawerLayout.closeDrawer(GravityCompat.START);
        else
            exitBuilder.show();
    }

    private Fragment getFragment() {

        switch (navItemId) {

            case HOME_ID : return new HomeFragment();

            case PROFILE_ID : return new ProfileFragment();

            case MY_DONATIONS_ID : return new MyDonationsFragment();

            case MY_DELIVERIES_ID : return new MyDeliveriesFragment();

            case ADD_EVENT_ID : return new AddEventFragment();

            case ADD_REQUIREMENT_ID : return new AddRequirementFragment();

            case REQUIREMENTS_ID : return new RequirementsFragment();

            default : return new HomeFragment();
        }

    }

    private void setToolBarTitle() {
        getSupportActionBar().setTitle(CURRENT_TAG);
    }

    private void loadFragment(){

        setToolBarTitle();

        if (getSupportFragmentManager().findFragmentByTag(CURRENT_TAG) != null) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
            return;
        }

        Runnable mPendingRunnable = new Runnable() {
            @Override
            public void run() {
                Fragment fragment = getFragment();
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.setCustomAnimations(android.R.anim.fade_in,
                        android.R.anim.fade_out);
                fragmentTransaction.replace(R.id.frame, fragment, CURRENT_TAG);
                fragmentTransaction.commitAllowingStateLoss();
            }
        };

        handler.post(mPendingRunnable);
        mDrawerLayout.closeDrawers();

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

        navItemId = menuItem.getItemId();

        switch (menuItem.getItemId()){

            case HOME_ID : CURRENT_TAG =TAG_HOME;
                           break;

            case PROFILE_ID : CURRENT_TAG = TAG_PROFILE;
                              break;

            case MY_DONATIONS_ID : CURRENT_TAG = TAG_MY_DONATIONS;
                                   break;

            case MY_DELIVERIES_ID : CURRENT_TAG = TAG_MY_DELIVERIES;
                                    break;

            case ADD_EVENT_ID : CURRENT_TAG = TAG_ADD_EVENT;
                                break;

            case ADD_REQUIREMENT_ID : CURRENT_TAG = TAG_ADD_REQUIREMENT;
                                      break;

            case REQUIREMENTS_ID : CURRENT_TAG = TAG_REQUIREMENT;
                                   break;

            default : navItemId = HOME_ID;
                      CURRENT_TAG = TAG_HOME;

        }

        loadFragment();

        return true;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {

        super.onConfigurationChanged(newConfig);

    }
}
