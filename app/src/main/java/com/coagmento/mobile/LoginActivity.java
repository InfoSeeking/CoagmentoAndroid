package com.coagmento.mobile;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.coagmento.mobile.data.EndpointsInterface;
import com.coagmento.mobile.models.UserResponse;

import retrofit.Call;
import retrofit.Callback;
import retrofit.GsonConverterFactory;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity  {

    // UI references.
    private EditText mEmailView;
    private EditText mPasswordView;
    private AlertDialog alertDialog;
    private AppCompatButton demoLoginButton;

    private Bundle userInfo = new Bundle();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.coagmento.mobile.R.layout.activity_login);


        //Set up Alert Dialog
        alertDialog = new AlertDialog.Builder(LoginActivity.this).create();

        // Set up the login form.
        mEmailView = (EditText) findViewById(com.coagmento.mobile.R.id.email);

        mPasswordView = (EditText) findViewById(com.coagmento.mobile.R.id.password);

        AppCompatButton mEmailSignInButton = (AppCompatButton) findViewById(com.coagmento.mobile.R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        TextView mRegisterButton = (TextView) findViewById(com.coagmento.mobile.R.id.register_button);
        mRegisterButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        demoLoginButton = (AppCompatButton) findViewById(R.id.demo_sign_in_button);
        demoLoginButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mEmailView.setText("coagmento_demo@demo.demo");
                mPasswordView.setText("demo");
                attemptLogin();
            }
        });

    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        final String email = mEmailView.getText().toString();
        final String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(com.coagmento.mobile.R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        } else if(TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(com.coagmento.mobile.R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(com.coagmento.mobile.R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            alertDialog.setTitle("Invalid Email.");
            alertDialog.setMessage("Support for a Coagmento username is being dropped in favor of emails. If you are an existing user with only a username, please press 'CONTINUE' to log in with your temporary credentials. It is recommended that you login to the web interface and add an email to your account as soon as possible.");
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "CONTINUE",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            String filteredEmail = email.replace(" ", "");
                            mEmailView.setText(filteredEmail + "@coagmento.org");
                            attemptLogin();
                            dialog.dismiss();
                            return;
                        }
                    });
            alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "CANCEL", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mEmailView.setError(getString(com.coagmento.mobile.R.string.error_invalid_email));
                    dialog.dismiss();
                }
            });
            alertDialog.show();

            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            final String BASE_URL = getString(com.coagmento.mobile.R.string.server_base_url);
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            EndpointsInterface apiService = retrofit.create(EndpointsInterface.class);

            Call<UserResponse> call = apiService.authenticateUser(email,password);

            call.enqueue(new Callback<UserResponse>() {
                     @Override
                     public void onResponse(Response<UserResponse> response, Retrofit retrofit) {
                         boolean responseStatus = false;
                         if (response.isSuccess()) {
                             UserResponse userResponse = response.body();
                             if (userResponse.getStatus().equals("ok")) {
                                 responseStatus = true;
                                 int response_id = userResponse.getResult().getUser().getId();
                                 String response_name = userResponse.getResult().getUser().getName();
                                 String response_email = userResponse.getResult().getUser().getEmail();
                                 saveCredentials(response_id, BASE_URL, response_name, response_email, password);
                             }
                         }
                         showProgress(false);
                         checkCredentials(responseStatus);
                     }

                     @Override
                     public void onFailure(Throwable t) {
                         showProgress(false);
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
                 }
            );
        }
    }

    private boolean isEmailValid(String email) {
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 3;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {

        final ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this,
                com.coagmento.mobile.R.style.FancyLogin_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Authenticating...");
        if (show) progressDialog.show();
        else progressDialog.dismiss();

    }

    private void checkCredentials(boolean responseStatus) {


        if(responseStatus) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();

        } else {
            alertDialog.setTitle("Login Failed.");
            alertDialog.setMessage("The email or password you entered was incorrect. Please try again.");
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            alertDialog.show();
        }

    }

    private void saveCredentials(int user_id, String host, String name, String email, String password) {
        SharedPreferences prefs = this.getSharedPreferences("Login", 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("user_id", user_id);
        editor.putString("host", host);
        editor.putString("name", name);
        editor.putString("email", email);
        editor.putString("password", password);
        editor.commit();
    }
}

