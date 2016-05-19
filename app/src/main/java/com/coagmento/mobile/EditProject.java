package com.coagmento.mobile;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.StrictMode;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.coagmento.mobile.adapter.ProjectUsersAdapter;
import com.coagmento.mobile.data.EndpointsInterface;
import com.coagmento.mobile.models.NullResponse;
import com.coagmento.mobile.models.ProjectResponse;
import com.coagmento.mobile.models.Result;
import com.coagmento.mobile.models.UserListResponse;
import com.coagmento.mobile.models.UserResponse;
import org.solovyev.android.views.llm.DividerItemDecoration;
import org.solovyev.android.views.llm.LinearLayoutManager;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import retrofit.Call;
import retrofit.Callback;
import retrofit.GsonConverterFactory;
import retrofit.Response;
import retrofit.Retrofit;

public class EditProject extends AppCompatActivity {

    // UI References
    private ImageView mInfoButtonView;
    private Button addPersonButton, renameButton, removeButton;
    private TextView titleView, descriptionView, createdView, modifiedView, accessView;
    private LinearLayout buttonsLayout;
    private View divider1;
    private View rootView;

    // User Data
    private Result project = null;
    String host, email, password;
    char userPermission = 'r';
    private Bundle userInfo = new Bundle();
    List<Result> userList = new ArrayList<Result>();
    private String formattedCreatedDate;

    // Retrofit References
    private Retrofit retrofit;
    private EndpointsInterface apiService;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_project);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        rootView = findViewById(android.R.id.content);

        // Set Action Bar
        getSupportActionBar().setTitle("Edit Project");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Retrieve User Data
        Intent intent = getIntent();
        host = intent.getStringExtra("host");
        email = intent.getStringExtra("email");
        password = intent.getStringExtra("password");

        userInfo.putString("host", host);
        userInfo.putString("email", email);
        userInfo.putString("password", password);

        project = (Result) intent.getSerializableExtra("current_project");
        userPermission = project.getLevel().charAt(0);

        retrofit = new Retrofit.Builder()
                .baseUrl(host)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(EndpointsInterface.class);


        // Set up users
        Call<UserListResponse> call = apiService.getUserList(project.getProjectId());

        call.enqueue(new Callback<UserListResponse>(){

            @Override
            public void onResponse(Response<UserListResponse> response, Retrofit retrofit) {
                if(response.code() == 200) {
                    userList = response.body().getResult();
                    final LinearLayoutManager layoutManager = new org.solovyev.android.views.llm.LinearLayoutManager(EditProject.this, LinearLayoutManager.VERTICAL, false);
                    final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.project_users_recycler_view);
                    recyclerView.setLayoutManager(layoutManager);
                    recyclerView.addItemDecoration(new DividerItemDecoration(EditProject.this, null));
                    recyclerView.setAdapter(new ProjectUsersAdapter(userInfo, project, userList));
                }
                else Log.e("Error", "Unable to retrieve list");
            }

            @Override
            public void onFailure(Throwable t) {
                Log.e("Network Failure", "Unable to connect to host");
            }
        });

        // UI Elements

        mInfoButtonView = (ImageView) findViewById(R.id.edit_project_info_button);
        mInfoButtonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog alertDialog = new AlertDialog.Builder(EditProject.this).create();
                alertDialog.setTitle("Permission Levels");
                alertDialog.setMessage("Users are allowed to add other users to collaborate in their projects. With this, the user can identify which users can hold what permissions.\n\n"
                        + "READ permission grants a user permission to view the project.\n\n"
                        + "WRITE permission allows a user to contribute to the project\n\n"
                        + "OWNER permission grants full permission to the user. Users with this permission level can add or remove other users in the project.");
                alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        dialog.dismiss();
                    }
                });
                alertDialog.show();
            }
        });

        addPersonButton = (Button) findViewById(R.id.add_person_button);
        addPersonButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog alertDialog = new AlertDialog.Builder(EditProject.this).create();
                alertDialog.setTitle("Add User To Project");
                final LinearLayout input = (LinearLayout) getLayoutInflater().inflate(R.layout.add_user_layout, null);
                final EditText editText = (EditText) input.findViewById(R.id.edit_title_view);

                final AppCompatSpinner spinner = (AppCompatSpinner) input.findViewById(R.id.permission_spinner);
                ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(EditProject.this, R.array.permissions, android.R.layout.simple_spinner_item);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(adapter);

                editText.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                alertDialog.setView(input);
                alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final String new_email = editText.getText().toString();

                        if(isEmailValid(new_email)) {
                            if (isValidUser(editText.getText().toString())) {
                                String permissionLevel = "r";
                                switch(spinner.getSelectedItem().toString()) {
                                    case "Read":
                                        permissionLevel = "r";
                                        break;
                                    case "Write":
                                        permissionLevel = "w";
                                        break;
                                    default:
                                        permissionLevel = "r";
                                        break;
                                }
                                addUserToProject(new_email, permissionLevel);
                                dialog.dismiss();
                            } else {
                                AlertDialog alertDialog2 = new AlertDialog.Builder(EditProject.this).create();
                                alertDialog2.setTitle("Error");
                                alertDialog2.setMessage("User does not exist in the Coagmento database. Please ask them to register and then try adding them to the project.");
                                alertDialog2.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                                alertDialog.show();
                                            }
                                        });
                                alertDialog2.show();
                            }
                        } else {
                            AlertDialog alertDialog2 = new AlertDialog.Builder(EditProject.this).create();
                            alertDialog2.setTitle("Error");
                            alertDialog2.setMessage("Inputted email is not a valid entry. Please check your formatting and try again.");
                            alertDialog2.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                            alertDialog.show();
                                            editText.setText(new_email);
                                        }
                                    });
                            alertDialog2.show();
                        }
                    }
                });
                alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                alertDialog.show();
            }
        });

        renameButton = (Button) findViewById(R.id.edit_title);
        renameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog alertDialog = new AlertDialog.Builder(EditProject.this).create();
                alertDialog.setTitle("Edit Title");
                final LinearLayout input = (LinearLayout) getLayoutInflater().inflate(R.layout.edit_title_textview, null);
                final EditText editText = (EditText) input.findViewById(R.id.edit_title_view);
                editText.setInputType(InputType.TYPE_CLASS_TEXT);
                editText.setText(project.getTitle());
                alertDialog.setView(input);
                alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "SAVE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, int which) {
                        Call<NullResponse> call = apiService.updateProjectTitle(project.getProjectId(), editText.getText().toString(), email, password);
                        call.enqueue(new Callback<NullResponse>() {
                            @Override
                            public void onResponse(Response<NullResponse> response, Retrofit retrofit) {
                                if (response.code() == 200) {
                                    titleView.setText(editText.getText());
                                    dialog.dismiss();
                                }
                            }

                            @Override
                            public void onFailure(Throwable t) {
                                AlertDialog alertDialog = new AlertDialog.Builder(EditProject.this).create();
                                alertDialog.setTitle("Network Failure.");
                                alertDialog.setMessage("Connection to host failed. Please check the Host URL or your network connection and try logging in again.");
                                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                            }
                                        });
                                alertDialog.show();
                            }
                        });
                    }
                });
                alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                alertDialog.show();
            }
        });

        removeButton = (Button) findViewById(R.id.remove_project);
        removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog alertDialog = new AlertDialog.Builder(EditProject.this).create();
                alertDialog.setTitle("Confirm Action");
                alertDialog.setMessage("Are you sure you want to delete this project?");
                alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Call<ProjectResponse> call = apiService.deleteProject(project.getId(), email, password);
                        call.enqueue(new Callback<ProjectResponse>() {

                            @Override
                            public void onResponse(Response<ProjectResponse> response, Retrofit retrofit) {
                                if (response.code() == 200) {
                                    finishActivity(false);
                                }
                            }

                            @Override
                            public void onFailure(Throwable t) {

                            }
                        });
                    }
                });
                alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                alertDialog.show();
            }
        });

        buttonsLayout = (LinearLayout) findViewById(R.id.edit_project_buttons_layout);
        divider1 = (View) findViewById(R.id.divider1);
        if(project.getLevel().equals("r")) {
            buttonsLayout.setVisibility(View.GONE);
            divider1.setVisibility(View.GONE);
        } else if(project.getLevel().equals("w")) {
            addPersonButton.setVisibility(View.GONE);
            removeButton.setVisibility(View.GONE);
        }

        titleView = (TextView) findViewById(R.id.edit_project_title_view);
        titleView.setText(project.getTitle());

        descriptionView = (TextView) findViewById(R.id.edit_project_description_view);
        if(project.getDescription().equals("") || project.getDescription() == null) {
            LinearLayout linearLayout = (LinearLayout) findViewById(R.id.description_view);
            linearLayout.setVisibility(View.GONE);
        } else {
            descriptionView.setText(project.getDescription());
        }

        formattedCreatedDate = "";
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
        try {
            Date date1 = dateFormat.parse(project.getcreated_at());
            formattedCreatedDate = new SimpleDateFormat("EEE, MMMM d").format(date1);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        createdView = (TextView) findViewById(R.id.edit_project_created_view);

        Call<UserResponse> call2 = apiService.getUser(project.getcreator_id());
        call2.enqueue(new Callback<UserResponse>() {

            @Override
            public void onResponse(Response<UserResponse> response, Retrofit retrofit) {
                if(response.code() == 200) {
                    String createdByView = formattedCreatedDate + " by " + response.body().getResult().getUser().getName();
                    createdView.setText(createdByView);
                }

            }

            @Override
            public void onFailure(Throwable t) {
                AlertDialog alertDialog = new AlertDialog.Builder(EditProject.this).create();
                alertDialog.setTitle("Network Failure.");
                alertDialog.setMessage("Connection to host failed. Please check the Host URL or your network connection and try logging in again.");
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alertDialog.show();
            }
        });

        String formattedModifiedDate = "";
        SimpleDateFormat dateFormat2 = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
        try {
            Date date2 = dateFormat2.parse(project.getupdated_at());
            formattedModifiedDate = new SimpleDateFormat("EEE, MMMM d").format(date2);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        modifiedView = (TextView) findViewById(R.id.edit_project_modified_view);
        modifiedView.setText(formattedModifiedDate);

        accessView = (TextView) findViewById(R.id.edit_project_privacy_view);
        switch (project.getPrivate()) {
            case 0:
                accessView.setText("Public");
                break;
            case 1:
                accessView.setText("Private");
                break;
            default:
                accessView.setText("Public");
                break;
        }

    }

    @Override
    public boolean onSupportNavigateUp() {
        boolean x = super.onNavigateUp();
        finishActivity(true);
        return x;
    }

    @Override
    public void onBackPressed() {
        super.onNavigateUp();
        finishActivity(true);
    }

    protected void finishActivity(boolean addProject) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("userInfo", userInfo);
        if(addProject) intent.putExtra("current_project", project);
        startActivity(intent);
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        finish();
    }

    public boolean isValidUser(String new_email) {
        Call<UserResponse> call = apiService.getUser(new_email);

        try {
            UserResponse response = call.execute().body();
            if(response != null) {
                Result user = response.getResult();
                if(user.getEmail() != null && user.getEmail().contains("@")) {
                    return true;
                }
            } else {
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return false;
    }

    public boolean isEmailValid(String email)
    {
        String regExpn =
                "^(([\\w-]+\\.)+[\\w-]+|([a-zA-Z]{1}|[\\w-]{2,}))@"
                        +"((([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                        +"[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\."
                        +"([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                        +"[0-9]{1,2}|25[0-5]|2[0-4][0-9])){1}|"
                        +"([a-zA-Z]+[\\w-]+\\.)+[a-zA-Z]{2,4})$";

        CharSequence inputStr = email;

        Pattern pattern = Pattern.compile(regExpn, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(inputStr);

        if(matcher.matches())
            return true;
        else
            return false;
    }

    void addUserToProject(String email, String permission) {
        Call<NullResponse> call = apiService.shareProject(project.getId(), email, permission, this.email, this.password);

        call.enqueue(new Callback<NullResponse>() {
            @Override
            public void onResponse(Response<NullResponse> response, Retrofit retrofit) {
                if(response.code() == 200) {
                    Snackbar snackbar = Snackbar
                            .make(rootView, "User Added To Project", Snackbar.LENGTH_SHORT);
                    snackbar.show();
                } else {
                    try {
                        String errorMsg = response.errorBody().string();

                        Snackbar snackbar = Snackbar
                                .make(rootView, "Error: " + errorMsg, Snackbar.LENGTH_SHORT);
                        snackbar.show();
                    } catch (IOException e) {
                        e.printStackTrace();

                        Snackbar snackbar = Snackbar
                                .make(rootView, "Error: " + response.code(), Snackbar.LENGTH_SHORT);
                        snackbar.show();
                    }
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Snackbar snackbar = Snackbar
                        .make(rootView, "Error: " + t.getMessage(), Snackbar.LENGTH_SHORT);
                snackbar.show();
            }
        });
    }
}
