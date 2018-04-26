package org.openhds.mobile.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import org.openhds.mobile.R;
import org.openhds.mobile.fragment.FieldWorkerLoginFragment;
import org.openhds.mobile.fragment.SupervisorLoginFragment;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.openhds.mobile.utilities.ConfigUtils.getAppFullName;

public class LoginActivity extends AppCompatActivity {

    public static final String SELECTED_LOGIN_KEY = "selected_login";
    public static final byte FIELD_WORKER_IDX = 0;
    public static final byte SUPERVISOR_IDX = 1;

    private final Map<String, Fragment> fragments = new LinkedHashMap<>();
    private ViewPager pager;
    private ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setTitle(getAppFullName(this));
        setContentView(R.layout.login);

        fragments.put(getString(R.string.fieldworker_login), new FieldWorkerLoginFragment());
        fragments.put(getString(R.string.supervisor_login), new SupervisorLoginFragment());

        pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(new PagerAdapter(getSupportFragmentManager()));

        actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        byte tabIndex = 0, selectedIndex = getIntent().getByteExtra(SELECTED_LOGIN_KEY, FIELD_WORKER_IDX);
        for (Map.Entry<String, Fragment> fragment : fragments.entrySet()) {
            ActionBar.Tab t = actionBar.newTab().setText(fragment.getKey()).setTabListener(new TabListener());
            boolean tabSelected = tabIndex == selectedIndex;
            actionBar.addTab(t, tabSelected);
            tabIndex++;
        }

        pager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.login_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.configure_server:
                startActivity(new Intent(this, PreferenceActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    class TabListener implements ActionBar.TabListener {

        @Override
        public void onTabSelected(ActionBar.Tab tab, android.support.v4.app.FragmentTransaction ft) {
            pager.setCurrentItem(tab.getPosition(), true);
        }

        @Override
        public void onTabUnselected(ActionBar.Tab tab, android.support.v4.app.FragmentTransaction ft) {
        }

        @Override
        public void onTabReselected(ActionBar.Tab tab, android.support.v4.app.FragmentTransaction ft) {
        }
    }

    class PagerAdapter extends FragmentPagerAdapter {

        private String[] fragmentKeys = fragments.keySet().toArray(new String[]{});

        public PagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(fragmentKeys[position]);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return fragmentKeys[position];
        }
    }
}
