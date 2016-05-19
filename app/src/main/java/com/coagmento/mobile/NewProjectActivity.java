package com.coagmento.mobile;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;

import com.coagmento.mobile.data.EndpointsInterface;
import com.coagmento.mobile.models.ProjectResponse;

import retrofit.Call;
import retrofit.Callback;
import retrofit.GsonConverterFactory;
import retrofit.Response;
import retrofit.Retrofit;

public class NewProjectActivity extends AppCompatActivity {

    // UI References
    private EditText mTitleView;
    private EditText mDescriptionView;
    private Switch mPrivateView;
    private Button mCreateProjectButton;
    private AlertDialog alertDialog;


    // User Variables
    private String host, email, password;
    private Bundle userInfo = new Bundle();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.coagmento.mobile.R.layout.activity_new_project);

        getSupportActionBar().setTitle("Create A New Project");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        alertDialog = new AlertDialog.Builder(NewProjectActivity.this).create();

        Intent intent = getIntent();
        host = intent.getStringExtra("host");
        email = intent.getStringExtra("email");
        password = intent.getStringExtra("password");

        userInfo.putString("host", host);
        userInfo.putString("email", email);
        userInfo.putString("password", password);

        mTitleView = (EditText) findViewById(com.coagmento.mobile.R.id.project_title_form);

        mDescriptionView = (EditText) findViewById(com.coagmento.mobile.R.id.project_description_form);

        mPrivateView = (Switch) findViewById(com.coagmento.mobile.R.id.project_privacy_switch);

        mCreateProjectButton = (Button) findViewById(com.coagmento.mobile.R.id.create_project_button);
        mCreateProjectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptCreateProject();
            }
        });
    }

    protected void attemptCreateProject() {

        // Reset Errors
        mTitleView.setError(null);
        mDescriptionView.setError(null);

        String title = mTitleView.getText().toString();
        String description = mDescriptionView.getText().toString();
        boolean mPrivateProject = mPrivateView.isChecked();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid title
        if(TextUtils.isEmpty(title)) {
            mTitleView.setError(getString(com.coagmento.mobile.R.string.error_field_required));
            focusView = mTitleView;
            cancel = true;
        }

        // Check for a description
        boolean noDescription = TextUtils.isEmpty(description);

        // Set up retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(host)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        EndpointsInterface apiService = retrofit.create(EndpointsInterface.class);

        int projectPrivacy = (mPrivateProject) ? 1 : 0;

        if(cancel) {
            focusView.requestFocus();
        } else if(noDescription) {

            Call<ProjectResponse> call = apiService.createProject(title, projectPrivacy, email, password);

            call.enqueue(new Callback<ProjectResponse>() {

                @Override
                public void onResponse(Response<ProjectResponse> response, Retrofit retrofit) {
                    if (response.code() == 200) returnToActivity();
                    else {
                        alertDialog.setTitle("Error");
                        alertDialog.setMessage("Unable to create project. Please try again.");
                        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                    }

                }

                @Override
                public void onFailure(Throwable t) {
                    alertDialog.setTitle("Network Failure");
                    alertDialog.setMessage("Unable to connect to host server. Please check your connection and try again.");
                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    returnToActivity();
                                }
                            });
                    alertDialog.show();
                }
            });
        } else {

            Call<ProjectResponse> call = apiService.createProject(title, description, projectPrivacy, email, password);

            call.enqueue(new Callback<ProjectResponse>() {

                @Override
                public void onResponse(Response<ProjectResponse> response, Retrofit retrofit) {
                    if (response.code() == 200) returnToActivity();
                    else {
                        alertDialog.setTitle("Error");
                        alertDialog.setMessage("Unable to create project. Please try again.");
                        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                    }
                }

                @Override
                public void onFailure(Throwable t) {
                    alertDialog.setTitle("Network Failure");
                    alertDialog.setMessage("Unable to connect to host server. Please check your connection and try again.");
                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    returnToActivity();
                                }
                            });
                    alertDialog.show();
                }
            });

        }


    }

    protected void returnToActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("userInfo", userInfo);
        startActivity(intent);
        finish();
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }

    @Override
    public boolean onSupportNavigateUp(){
        boolean x = super.onNavigateUp();
        returnToActivity();
        return x;
    }

    @Override
    public void onBackPressed() {
        super.onNavigateUp();
        returnToActivity();
    }
}
