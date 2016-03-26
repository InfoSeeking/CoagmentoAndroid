package org.coagmento.android;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.widget.AppCompatButton;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.okhttp.OkHttpClient;

import org.coagmento.android.data.EndpointsInterface;
import org.coagmento.android.models.Input;
import org.coagmento.android.models.UserResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import retrofit.Call;
import retrofit.Callback;
import retrofit.GsonConverterFactory;
import retrofit.Response;
import retrofit.Retrofit;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A register screen that offers registration of user account
 */
public class RegisterActivity extends AppCompatActivity {


    // UI references.
    private EditText mUserNameView;
    private EditText mEmailView;
    private EditText mPasswordView;
    private AlertDialog alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Set up the login form.
        mUserNameView = (EditText) findViewById(R.id.name_register_form);

        mEmailView = (EditText) findViewById(R.id.email);

        //Set up Alert Dialog
        alertDialog = new AlertDialog.Builder(RegisterActivity.this).create();

        mPasswordView = (EditText) findViewById(R.id.password);

        AppCompatButton mRegisterButton = (AppCompatButton) findViewById(R.id.register_form_button);
        mRegisterButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptRegister();
            }
        });

        TextView returnToLogin = (TextView) findViewById(R.id.link_login);
        returnToLogin.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptRegister() {
        // Reset errors.
        mUserNameView.setError(null);
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String host = getString(R.string.server_base_url);
        String name = mUserNameView.getText().toString();
        final String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        } else if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        // Check for a valid name
        if (TextUtils.isEmpty(name)) {
            mUserNameView.setError(getString(R.string.error_field_required));
            focusView = mUserNameView;
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

            final String BASE_URL = host;
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            EndpointsInterface apiService = retrofit.create(EndpointsInterface.class);

            Call<UserResponse> call = apiService.createUser(email,password,name);

            call.enqueue(new Callback<UserResponse>() {
                 @Override
                 public void onResponse(Response<UserResponse> response, Retrofit retrofit) {
                     if (response.code() == 200) {
                         UserResponse userResponse = response.body();
                         if (userResponse.getStatus().equals("ok")) {
                             checkCredentials(true);
                         } else {
                             checkCredentials(false);
                         }
                     }  else if (response.code() == 400) {
                         checkCredentials("This email is already in use. Try logging in or register with a different email.");
                     }
                     showProgress(false);
                 }

                 @Override
                 public void onFailure(Throwable t) {
                     showProgress(false);
                     alertDialog.setTitle("Network Failure.");
                     alertDialog.setMessage("Connection to host failed. Please check your network connection and try logging in again.");
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
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 6;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {

    }

    private void checkCredentials(boolean responseStatus) {


        if(responseStatus) {
            Toast.makeText(this, "Registration Successful.", Toast.LENGTH_SHORT);
            finish();

        } else {
            alertDialog.setTitle("Registration Failed.");
            alertDialog.setMessage("Unspecified error. Please try again.");
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            alertDialog.show();
        }

    }

    private void checkCredentials(String error) {

        alertDialog.setTitle("Registration Failed.");
        alertDialog.setMessage(error);
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();

    }
}

