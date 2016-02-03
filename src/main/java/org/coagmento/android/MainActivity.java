package org.coagmento.android;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.squareup.okhttp.ResponseBody;

import org.coagmento.android.data.EndpointsInterface;
import org.coagmento.android.fragment.MainFragment;
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
        implements NavigationView.OnNavigationItemSelectedListener, MainFragment.OnFragmentInteractionListener {

    //UI References
    private TextView mHeaderNameView;
    private TextView mHeaderEmailView;
    private AlertDialog alertDialog;
    private ProgressDialog progressDialog;

    // Menu References
    private Toolbar toolbar;
    private MenuItem previousItem;
    private NavigationView navigationView;
    private HashMap<MenuItem, Result> menuItemHashMap;
    private HashMap<Integer, MenuItem> projectIdHashMap;

    // User Data Variables
    private String host, email, password, name;
    private List<Result> projectList, myProjectList, sharedProjectList;
    private Result currentProject = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Set up Dialogs
        alertDialog = new AlertDialog.Builder(MainActivity.this).create();
        progressDialog = ProgressDialog.show(this,"", "Loading...", true);

        // Fetch host, email, password, and names
        SharedPreferences prefs = getSharedPreferences("Login", 0);
        host = prefs.getString("host", getString(R.string.server_base_url));
        name = prefs.getString("name", null);
        email = prefs.getString("email", null);
        password = prefs.getString("password", null);

        Intent intent = getIntent();
        currentProject = (Result) intent.getSerializableExtra("current_project");

        if(name == null || email==null) {
            Bundle b = intent.getBundleExtra("userInfo");
            host = b.getString("host");
            name = b.getString("name");
            email = b.getString("email");
            password = b.getString("password");
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

    protected void initializeUI() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Set User Name and Email in navigation header
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



//        // Add Projects to Nav Menu
//
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

        //Initialize the navigation view withe first project
        if (currentProject!= null) {
            previousItem = projectIdHashMap.get(currentProject.getProjectId());
            if(previousItem!=null) {
                currentProject = menuItemHashMap.get(previousItem);
                previousItem.setChecked(true);
                toolbar.setTitle(previousItem.getTitle());
            } else {
                loadFirstProject();
            }
        }
        else if(currentProject == null && (myProjectList.size() > 0 || sharedProjectList.size() > 0)) {
            loadFirstProject();
        }

        //Load Main Fragment

        // Check that the activity is using the layout version with
        // the fragment_container FrameLayout
        if (findViewById(R.id.fragment_container) != null) {

            // However, if we're being restored from a previous state,
            // then we don't need to do anything and should return or else
            // we could end up with overlapping fragments.

            // Create a new Fragment to be placed in the activity layout
            MainFragment firstFragment = new MainFragment();

            // In case this activity was started with special instructions from an
            // Intent, pass the Intent's extras to the fragment as arguments
            firstFragment.setArguments(getIntent().getExtras());

            // Add the fragment to the 'fragment_container' FrameLayout
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, firstFragment).commit();
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

        } else {
            if(previousItem != null) {
                previousItem.setChecked(false);
            }
            item.setChecked(true);
            currentProject = menuItemHashMap.get(item);
            previousItem = item;
            toolbar.setTitle(currentProject.getTitle());
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
        previousItem = navigationView.getMenu().getItem(1).getSubMenu().getItem(0);
        previousItem.setChecked(true);
        toolbar.setTitle(previousItem.getTitle());
        currentProject = menuItemHashMap.get(previousItem);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}