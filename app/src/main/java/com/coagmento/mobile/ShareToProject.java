package com.coagmento.mobile;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v7.widget.ShareActionProvider;

import com.coagmento.mobile.data.EndpointsInterface;
import com.coagmento.mobile.models.ProjectListResponse;
import com.coagmento.mobile.models.Result;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit.Call;
import retrofit.Callback;
import retrofit.GsonConverterFactory;
import retrofit.Response;
import retrofit.Retrofit;

public class ShareToProject extends AppCompatActivity {

    // Menu References
    private ShareActionProvider shareActionProvider;
    private AlertDialog alertDialog;
    private ProgressDialog progressDialog;

    // User Data Variables
    private String host, email, password;
    private List<Result> projectList = new ArrayList<>();
    private CharSequence[] projectTitleList;
    private Result selectedProject = null;

    // Data recieved from share
    private String data = null;
    private Boolean isURL = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Fetch host, email, password, and names
        SharedPreferences prefs = getSharedPreferences("Login", 0);
        host = getString(com.coagmento.mobile.R.string.server_base_url);
        email = prefs.getString("email", null);
        password = prefs.getString("password", null);

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                String recievedText = intent.getStringExtra(Intent.EXTRA_TEXT);
                if (recievedText != null) {
                    data = recievedText;
                }
            }
        }

        //Set up Dialogs
        alertDialog = new AlertDialog.Builder(ShareToProject.this).create();
        progressDialog = ProgressDialog.show(this, "", "Loading...", true);

        if(email == null || password == null) {
            alertDialog.setTitle("User Not Logged In");
            alertDialog.setMessage("No user credentials were found. Please open Coagmento and sign in to enable sharing from other apps.");
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            finish();
                        }
                    });
            alertDialog.show();
        }

        isURL = Patterns.WEB_URL.matcher(data).matches();

        if(isURL && email!= null && password != null) {

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
                                         for(Result project : projectListResponse.getResult()) {
                                             if (project.getLevel().equals("w") || project.getLevel().equals("o")) {
                                                 projectList.add(project);
                                             }
                                         }
                                         progressDialog.dismiss();
                                         getProject();
                                     }
                                 } else {
                                     progressDialog.dismiss();

                                     alertDialog.setTitle("Error.");
                                     try {
                                         alertDialog.setMessage(response.errorBody().string());
                                     } catch (IOException e) {
                                         e.printStackTrace();
                                     }
                                     alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                                             new DialogInterface.OnClickListener() {
                                                 public void onClick(DialogInterface dialog, int which) {
                                                     dialog.dismiss();
                                                     finish();
                                                 }
                                             });
                                     alertDialog.show();
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
        } else if(email != null && password != null){
            alertDialog.setTitle("Invalid URL");
            alertDialog.setMessage("Coagmento doesn't currently support sharing plain text. Please use the share function of your preferred browser to add items to your project.");
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(com.coagmento.mobile.R.menu.activity_edit_snippet, menu);

        MenuItem item = menu.findItem(com.coagmento.mobile.R.id.add_snippet);

        shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
        if (shareActionProvider != null) {
            shareActionProvider.setShareIntent(shareIntent());
        }

        return false;
    }

    public Intent shareIntent() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("text/plain");
        return intent;
    }

    public void getProject() {

        projectTitleList = new CharSequence[projectList.size()];

        for(int i=0; i<projectList.size(); i++) {
            Result tmp = projectList.get(i);
            projectTitleList[i] = tmp.getTitle();
        }

        AlertDialog.Builder projectDialogBuilder = new AlertDialog.Builder(ShareToProject.this);
        projectDialogBuilder.setTitle("Select A Project");
        projectDialogBuilder.setItems(projectTitleList, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                selectedProject = projectList.get(which);
                selectType();
                dialog.dismiss();
            }
        });
        AlertDialog alert1 = projectDialogBuilder.create(); // The error log points to this line
        alert1.show();

    }

    public void selectType() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ShareToProject.this);
        builder.setTitle("Save As");
        builder.setItems(com.coagmento.mobile.R.array.quickActions, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                String[] array = getResources().getStringArray(com.coagmento.mobile.R.array.quickActions);

                switch (array[which]) {
                    case "Bookmark":
                        Intent inten1 = new Intent(ShareToProject.this, EditBookmark.class);
                        inten1.putExtra("project_id", selectedProject.getId());
                        inten1.putExtra("INTENT_ACTION", "SHARE");
                        if(data != null) inten1.putExtra("data", data);
                        startActivity(inten1);
                        finish();
                        break;
                    case "Snippet":
                        Intent intent = new Intent(ShareToProject.this, EditSnippet.class);
                        intent.putExtra("project_id", selectedProject.getId());
                        intent.putExtra("INTENT_ACTION", "SHARE");
                        if(data != null) intent.putExtra("data", data);
                        startActivity(intent);
                        finish();
                        break;
                    default:
                        finish();
                        break;
                }
            }
        });
        AlertDialog alert2 = builder.create(); // The error log points to this line
        alert2.show();
    }
}