package org.coagmento.android;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ProgressBar;

import org.coagmento.android.data.EndpointsInterface;
import org.coagmento.android.models.User;
import org.coagmento.android.models.UserResponse;

import retrofit.Call;
import retrofit.Callback;
import retrofit.GsonConverterFactory;
import retrofit.Response;
import retrofit.Retrofit;

public class SplashScreen extends AppCompatActivity {

    private ProgressBar mProgress;
    private AlertDialog alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        //Hide the Action Bar for this activity
        getSupportActionBar().hide();

        // Start the Progress Bar Animation
        mProgress = (ProgressBar) findViewById(R.id.splash_progressBar);
        mProgress.setProgress(10);

        //Check for previous login before showing ui
        SharedPreferences prefs = this.getSharedPreferences("Login", 0);
        String host = prefs.getString("host", getString(R.string.server_base_url));
        String email = prefs.getString("email", null);
        String password = prefs.getString("password", null);

        alertDialog = new AlertDialog.Builder(SplashScreen.this).create();

        // Check for Network Connection
        ConnectivityManager cm =
                (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        if(isConnected) {

            if (email != null && password != null) {

                final String BASE_URL = host;
                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl(BASE_URL)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();

                EndpointsInterface apiService = retrofit.create(EndpointsInterface.class);

                Call<UserResponse> call = apiService.authenticateUser(email, password);

                call.enqueue(new Callback<UserResponse>() {
                    @Override
                    public void onResponse(Response<UserResponse> response, Retrofit retrofit) {
                        UserResponse userResponse = response.body();
                        boolean responseStatus = false;
                        if (userResponse.getStatus().equals("ok")) responseStatus = true;
                        checkCredentials(responseStatus);
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        alertDialog.setTitle("Network Failure.");
                        alertDialog.setMessage("Coagmento was unable to connect to the server. Please check if server is running and try again. If this issue persists, try resetting your credentials and logging in again.");
                        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });
                        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "RESET CREDENTIALS", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                SharedPreferences prefs = getSharedPreferences("Login", 0);
                                SharedPreferences.Editor editor = prefs.edit();
                                editor.clear();
                                editor.apply();

                                Intent intent = new Intent(SplashScreen.this, LoginActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        });
                        alertDialog.show();
                    }
                });

            } else {
                // Head to Login Form
                Intent intent = new Intent(SplashScreen.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        } else {
            alertDialog.setTitle("Internet Connection Required");
            alertDialog.setMessage("Coagmento currently requires active internet access. Please try again after connecting to a network.");
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

    private void checkCredentials(boolean responseStatus) {


        if(responseStatus) {
            Intent intent = new Intent(SplashScreen.this, MainActivity.class);
            startActivity(intent);
            finish();

        } else {
            alertDialog.setTitle("Login Failed.");
            alertDialog.setMessage("Authentication with saved credentials failed. Please sign-in again.");
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(SplashScreen.this, LoginActivity.class);
                            dialog.dismiss();
                            startActivity(intent);
                            finish();
                        }
                    });
            alertDialog.show();
        }

    }

}
