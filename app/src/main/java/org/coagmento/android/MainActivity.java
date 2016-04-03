package org.coagmento.android;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ShareActionProvider;
import android.widget.TextView;


import com.amulyakhare.textdrawable.TextDrawable;
import com.crashlytics.android.Crashlytics;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.squareup.okhttp.ResponseBody;
import org.coagmento.android.data.EndpointsInterface;
import org.coagmento.android.fragment.ActivityFragment;
import org.coagmento.android.fragment.BookmarksFragment;
import org.coagmento.android.fragment.ChatFragment;
import org.coagmento.android.fragment.DocumentsFragment;
import org.coagmento.android.fragment.MainFragment;

import org.coagmento.android.fragment.SearchesFragment;
import org.coagmento.android.fragment.SnippetsFragment;
import org.coagmento.android.models.DocumentResult;
import org.coagmento.android.models.Errors;
import org.coagmento.android.models.ProjectListResponse;
import org.coagmento.android.models.Result;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import retrofit.Call;
import retrofit.Callback;
import retrofit.Converter;
import retrofit.GsonConverterFactory;
import retrofit.Response;
import retrofit.Retrofit;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, MainFragment.OnFragmentInteractionListener,
                    BookmarksFragment.BookmarksFragmentInteraction {

    //UI References
    private TextView mHeaderNameView;
    private TextView mHeaderEmailView;
    private AlertDialog alertDialog;
    private ProgressDialog progressDialog;
    private ImageView userAvatarView;

    // Menu References
    private Toolbar toolbar;
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private MenuItem previousItem;
    private NavigationView navigationView;
    private HashMap<MenuItem, Result> menuItemHashMap;
    private HashMap<Integer, MenuItem> projectIdHashMap;
    private FloatingActionsMenu floatingActionsMenu;
    private ShareActionProvider mShareActionProvider;
    private FloatingActionButton addSnippetButton, addBookmarkButton;

    // User Data Variables
    private int user_id;
    private String host, email, password, name;
    private List<Result> projectList, myProjectList, sharedProjectList;
    private Result currentProject = null;
    private Bundle userInfo = new Bundle();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Set up Dialogs
        alertDialog = new AlertDialog.Builder(MainActivity.this).create();
        progressDialog = ProgressDialog.show(this,"", "Loading...", true);

        // Fetch host, email, password, and names
        SharedPreferences prefs = getSharedPreferences("Login", 0);
        host = getString(R.string.server_base_url);
        user_id = prefs.getInt("user_id", 0);
        name = prefs.getString("name", null);
        email = prefs.getString("email", null);
        password = prefs.getString("password", null);
        userInfo.putString("host", host);
        userInfo.putString("email", email);
        userInfo.putString("password", password);

        // TODO: Move this to where you establish a user session
        logUser();

        Intent intent = getIntent();
        currentProject = (Result) intent.getSerializableExtra("current_project");

        if(name == null || email==null) {
            userInfo = intent.getBundleExtra("userInfo");
            host = userInfo.getString("host");
            name = userInfo.getString("name");
            email = userInfo.getString("email");
            password = userInfo.getString("password");
        }

        updateProjectList();
    }

    protected void updateProjectList() {
        // Fetch Project Titles, Id's and Ownership
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(host)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        EndpointsInterface apiService = retrofit.create(EndpointsInterface.class);

        Call<ProjectListResponse> call = apiService.getProjectList(email, password);

        call.enqueue(new Callback<ProjectListResponse>() {
                         @Override
                         public void onResponse(Response<ProjectListResponse> response, Retrofit retrofit) {
                             if (response.code() == 200) {
                                 ProjectListResponse projectListResponse = response.body();
                                 if (projectListResponse.getStatus().equals("ok")) {
                                     projectList = projectListResponse.getResult();
                                     progressDialog.dismiss();
                                     initializeUI();
                                 }
                             } else {
                                 Converter<ResponseBody, Errors> errorConverter =
                                         retrofit.responseConverter(Error.class, new Annotation[0]);
                                 // Convert the error body into our Error type.
                                 try {
                                     Errors error = errorConverter.convert(response.errorBody());
                                     List<String> errorList = error.getGeneral();
                                     String errorValue = errorList.get(0);

                                     progressDialog.dismiss();

                                     alertDialog.setTitle("Error.");
                                     alertDialog.setMessage(errorValue);
                                     alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                                             new DialogInterface.OnClickListener() {
                                                 public void onClick(DialogInterface dialog, int which) {
                                                     dialog.dismiss();
                                                     finish();
                                                 }
                                             });
                                     alertDialog.show();

                                 } catch (IOException e) {

                                 }
                             }
                         }

                         @Override
                         public void onFailure(Throwable t) {
                             progressDialog.dismiss();

                             alertDialog.setTitle("Network Failure.");
                             alertDialog.setMessage("Connection to host failed. Please check the Host URL or your network connection and try logging in again.");
                             alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                     new DialogInterface.OnClickListener() {
                                         public void onClick(DialogInterface dialog, int which) {
                                             dialog.dismiss();
                                             finish();
                                         }
                                     });
                             alertDialog.show();
                         }
                     }
        );
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.action_settings).setVisible(!(projectList.size() == 0));
        return true;
    }

    protected void initializeUI() {

        // Set tabs on toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Find the floating action button
        floatingActionsMenu = (FloatingActionsMenu) findViewById(R.id.fab);
        addBookmarkButton = (FloatingActionButton) findViewById(R.id.add_bookmark_fab);
        addSnippetButton = (FloatingActionButton) findViewById(R.id.add_snippet_fab);

        // Set User Name and Email in navigation header
        userAvatarView = (ImageView) findViewById(R.id.userAvatarView);
        TextDrawable userAvatar = TextDrawable.builder()
                .beginConfig()
                .width(150)
                .height(150)
                .endConfig()
                .buildRound(String.valueOf(name.charAt(0)), R.color.white);
        userAvatarView.setImageDrawable(userAvatar);

        mHeaderNameView = (TextView) findViewById(R.id.navview_header_name);
        mHeaderNameView.setText(name);

        mHeaderEmailView = (TextView) findViewById(R.id.navview_header_email);
        mHeaderEmailView.setText(email);

        // Set Up Navigation Drawer
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        // Add Projects to Nav Menu

        myProjectList = sortProjects(projectList, false);
        sharedProjectList = sortProjects(projectList, true);

        navigationView.inflateMenu(R.menu.activity_main_drawer);
        Menu menu = navigationView.getMenu();
        Menu myProjects = menu.findItem(R.id.myProjects).getSubMenu();
        Menu sharedProjects = menu.findItem(R.id.sharedProjects).getSubMenu();

        menuItemHashMap = new HashMap<>();
        projectIdHashMap = new HashMap<>();

        for (int i = 0; i < myProjectList.size(); i++) {
            MenuItem menuItem = myProjects.add(myProjectList.get(i).getTitle()).setIcon(R.drawable.ic_keyboard_arrow_right_black_24dp);
            menuItemHashMap.put(menuItem, myProjectList.get(i));
            projectIdHashMap.put(myProjectList.get(i).getProjectId(), menuItem);
        }

        for (int i=0; i<sharedProjectList.size(); i++) {
            MenuItem menuItem = sharedProjects.add(sharedProjectList.get(i).getTitle()).setIcon(R.drawable.ic_keyboard_arrow_right_black_24dp);
            menuItemHashMap.put(menuItem, sharedProjectList.get(i));
            projectIdHashMap.put(sharedProjectList.get(i).getProjectId(), menuItem);
        }

        MenuItem mi = menu.getItem(menu.size() - 1);
        mi.setTitle(mi.getTitle());

        if(projectList.size() != 0) {
            //Initialize the navigation view withe first project
            if (currentProject != null) {
                previousItem = projectIdHashMap.get(currentProject.getProjectId());
                if (previousItem != null) {
                    currentProject = menuItemHashMap.get(previousItem);
                    previousItem.setChecked(true);
                    toolbar.setTitle(previousItem.getTitle());
                } else {
                    loadFirstProject();
                }
            } else if (currentProject == null && (myProjectList.size() > 0 || sharedProjectList.size() > 0)) {
                loadFirstProject();
            }

            // Set Floating Action Button Visible only if user has write access
            if(currentProject.getLevel().equals("w") || currentProject.getLevel().equals("o")) {
                findViewById(R.id.fab).setVisibility(View.VISIBLE);
            } else {
                findViewById(R.id.fab).setVisibility(View.INVISIBLE);
            }

            addSnippetButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, EditSnippet.class);
                    startActivity(intent);
                }
            });

            addBookmarkButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, EditBookmark.class);
                    startActivity(intent);
                }
            });
        }

        // Set ViewPager
        // Get the ViewPager and set it's PagerAdapter so that it can display items
        viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setAdapter(new SectionsPagerAdapter(getSupportFragmentManager(),
                MainActivity.this));
        viewPager.setOffscreenPageLimit(4);

        // Give the TabLayout the ViewPager
        tabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
        tabLayout.setupWithViewPager(viewPager);

        if(projectList.size() == 0) {
            toolbar.setTitle(R.string.no_projects_found);
            tabLayout.setVisibility(View.GONE);
            floatingActionsMenu.setVisibility(View.GONE);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, EditProject.class);
            intent.putExtra("host", host);
            intent.putExtra("email", email);
            intent.putExtra("password", password);
            intent.putExtra("current_project", currentProject);
            startActivity(intent);
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_logout) {
            SharedPreferences prefs = getSharedPreferences("Login", 0);
            SharedPreferences.Editor editor = prefs.edit();
            editor.clear();
            editor.apply();

            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.nav_new_project) {
            Intent intent = new Intent(MainActivity.this, NewProjectActivity.class);
            intent.putExtra("host", host);
            intent.putExtra("email", email);
            intent.putExtra("password", password);
            startActivity(intent);

        } else if (id == R.id.nav_settings) {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);

        } else {
            if(previousItem != null) {
                previousItem.setChecked(false);
            }
            item.setChecked(true);
            currentProject = menuItemHashMap.get(item);
            previousItem = item;
            toolbar.setTitle(currentProject.getTitle());

            // Refresh data in fragments when project is switched
            List<Fragment> fragmentList = getSupportFragmentManager().getFragments();
            if(fragmentList != null) {
                for (Fragment fragment : fragmentList) {
                    if(fragment instanceof BookmarksFragment) {
                        BookmarksFragment bookmarksFragment = (BookmarksFragment) fragment;
                        bookmarksFragment.loadList(currentProject.getProjectId());
                    } else if(fragment instanceof ActivityFragment){
                        ActivityFragment activityFragment = (ActivityFragment) fragment;
                        activityFragment.loadList(currentProject.getProjectId());
                    } else if(fragment instanceof SearchesFragment) {
                        SearchesFragment searchesFragment = (SearchesFragment) fragment;
                        searchesFragment.loadList(currentProject.getProjectId());
                    } else if (fragment instanceof SnippetsFragment) {
                        SnippetsFragment snippetsFragment = (SnippetsFragment) fragment;
                        snippetsFragment.loadList(currentProject.getProjectId());
                    } else if (fragment instanceof DocumentsFragment) {
                        DocumentsFragment documentsFragment = (DocumentsFragment) fragment;
                        documentsFragment.loadList(currentProject.getProjectId());
                    } else if(fragment instanceof ChatFragment) {

                    }
                }
            }

            // Set Floating Action Button Visible only if user has write access
            if(currentProject.getLevel().equals("w") || currentProject.getLevel().equals("o")) {
                findViewById(R.id.fab).setVisibility(View.VISIBLE);
            } else {
                findViewById(R.id.fab).setVisibility(View.INVISIBLE);
            }
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

    }

    private List<Result> sortProjects(List<Result> result, boolean sharedQuery) {
        Result tmpProject;
        List<Result> tmpList = new ArrayList<Result>(result);
        if (sharedQuery) {
            for (int i = 0; i < tmpList.size(); i=i) {
                tmpProject = tmpList.get(i);
                if (tmpProject.getLevel().equals("o")) {
                    tmpList.remove(i);
                } else {
                    i++;
                }
            }

            return tmpList;
        } else {
            for (int i = 0; i < tmpList.size(); i=i) {
                tmpProject = tmpList.get(i);
                if (tmpProject.getLevel().equals("o")) {
                    i++;
                } else {
                    tmpList.remove(i);
                }
            }
            return tmpList;
        }

    }

    protected void loadFirstProject() {
        if(navigationView.getMenu().getItem(1).getSubMenu().size() > 0) previousItem = navigationView.getMenu().getItem(1).getSubMenu().getItem(0);
        else previousItem = navigationView.getMenu().getItem(2).getSubMenu().getItem(0);
        previousItem.setChecked(true);
        toolbar.setTitle(previousItem.getTitle());
        currentProject = menuItemHashMap.get(previousItem);
    }

    private void logUser() {
        // TODO: Use the current user's information
        // You can call any combination of these three methods
        Crashlytics.setUserIdentifier(String.valueOf(user_id));
        Crashlytics.setUserEmail(email);
        Crashlytics.setUserName(name);
    }


    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public List<Result> getProjectList() {
        return projectList;
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        final int PAGE_COUNT = 5;
        private String tabTitles[] = new String[]{"Pages", "Searches", "Bookmarks", "Snippets", "Documents"};
        private Context context;

        public SectionsPagerAdapter(FragmentManager fm, Context context) {
            super(fm);
            this.context = context;
        }

        @Override
        public int getCount() {
            if(projectList.size() > 0) {
                return PAGE_COUNT;
            } else {
                return 1;
            }
        }

        @Override
        public Fragment getItem(int position) {
            if (projectList.size() != 0 && currentProject != null) {
                userInfo.putInt("project_id", currentProject.getId());
                switch(position) {
                    case 0:
                        return ActivityFragment.newInstance(userInfo);
                    case 1:
                        return SearchesFragment.newInstance(userInfo);
                    case 2:
                        return BookmarksFragment.newInstance(userInfo, MainActivity.this);
                    case 3:
                        return SnippetsFragment.newInstance(userInfo);
                    case 4:
                        return DocumentsFragment.newInstance(userInfo);
                    default:
                        return MainFragment.newInstance();
                }
            } else {
                return MainFragment.newInstance();
            }
        }

        @Override
        public CharSequence getPageTitle(int position) {
            // Generate title based on item position
            return tabTitles[position];
        }
    }
}