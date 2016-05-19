package com.coagmento.mobile;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatSpinner;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Toast;

import com.squareup.okhttp.ResponseBody;

import com.coagmento.mobile.data.EndpointsInterface;
import com.coagmento.mobile.models.Errors;
import com.coagmento.mobile.models.NullResponse;
import com.coagmento.mobile.models.ProjectListResponse;
import com.coagmento.mobile.models.Result;

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

public class EditBookmark extends AppCompatActivity implements AdapterView.OnItemClickListener {

    // UI References
    private EditText titleView, notesView, urlView;
    private AppCompatButton addBookmark;
    private AppCompatSpinner projectsSpinner;
    private AlertDialog alertDialog;
    private ProgressDialog progressDialog;

    // User Data References
    String host, email, password;
    List<Result> projectList, filteredProjectList = new ArrayList<Result>();
    List<String> filteredProjectTitleList = new ArrayList<>();
    HashMap<Integer, Integer> projectPositionById = new HashMap<>();
    private Bundle b = null;
    private ArrayAdapter<String> projectSpinnerAdapter;

    // Snippet Data
    private int project_id, bookmark_id;
    private String url, title, notes;
    private String action;
    private boolean textCheck = false, urlCheck = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_bookmark);

        b = getIntent().getExtras();

        titleView = (EditText) findViewById(R.id.bookmarks_title_form);
        urlView = (EditText) findViewById(R.id.bookmarks_url_form);
        notesView = (EditText) findViewById(R.id.bookmark_description_form);
        addBookmark = (AppCompatButton) findViewById(R.id.create_bookmark_button );
        projectsSpinner = (AppCompatSpinner) findViewById(R.id.bookmarks_project_spinner);

        //Set up Dialogs
        alertDialog = new AlertDialog.Builder(EditBookmark.this).create();
        progressDialog = ProgressDialog.show(this, "", "Loading...", true);

        // Fetch host, email, password, and names
        SharedPreferences prefs = getSharedPreferences("Login", 0);
        host = getString(R.string.server_base_url);
        email = prefs.getString("email", null);
        password = prefs.getString("password", null);

        // Fetch Project Titles, Id's and Ownership
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(getString(R.string.server_base_url))
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
                                     int count = 0;
                                     for(Result project : projectList) {
                                         if (project.getLevel().equals("w") || project.getLevel().equals("o")) {
                                             filteredProjectList.add(project);
                                             filteredProjectTitleList.add(project.getTitle());
                                             projectPositionById.put(project.getId(), count);
                                             count++;
                                         }
                                     }
                                     projectSpinnerAdapter = new ArrayAdapter<String>(EditBookmark.this,
                                             android.R.layout.simple_spinner_dropdown_item, filteredProjectTitleList);
                                     setupSnippetForm();
                                     progressDialog.dismiss();
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

        urlView.setInputType(InputType.TYPE_TEXT_VARIATION_URI);

    }

    void setupSnippetForm() {

        if(b != null) {
            action = b.getString("INTENT_ACTION");

            if(action.equals("SHARE")) {
                getSupportActionBar().setTitle("Add Bookmark");

                project_id = b.getInt("project_id");
                url = b.getString("data");

                projectsSpinner.setAdapter(projectSpinnerAdapter);
                projectsSpinner.setSelection(projectPositionById.get(project_id));

                urlView.setText(url);
                urlView.setInputType(InputType.TYPE_NULL);

            } else if(action.equals("EDIT")) {
                getSupportActionBar().setTitle("Edit Bookmark");

                project_id = b.getInt("project_id");
                url = b.getString("url");
                notes = b.getString("notes");
                title = b.getString("title");
                bookmark_id = b.getInt("bookmark_id");

                projectsSpinner.setEnabled(false);
                projectsSpinner.setClickable(false);
                projectsSpinner.setAdapter(projectSpinnerAdapter);
                projectsSpinner.setSelection(projectPositionById.get(project_id));

                titleView.setText(title);
                urlView.setText(url);

                notesView.setText(notes);
                if(notes.length() == 0) notesView.setText("Editing bookmark notes is not yet supported.");
                notesView.setInputType(InputType.TYPE_NULL);

            }
        } else {
            getSupportActionBar().setTitle("Add Bookmark");

            action = "";

            projectsSpinner.setEnabled(true);
            projectsSpinner.setClickable(true);
            projectsSpinner.setAdapter(projectSpinnerAdapter);

        }

        addBookmark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveSnippet(action);
            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        project_id = filteredProjectList.get(position).getId();
    }

    void saveSnippet(String action) {
        // Reset Errors
        titleView.setError(null);
        urlView.setError(null);
        notesView.setError(null);

        int projectInputId = filteredProjectList.get(projectsSpinner.getSelectedItemPosition()).getId();
        String titleInput = titleView.getText().toString();
        String urlInput = urlView.getText().toString();
        String textInput = notesView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if(TextUtils.isEmpty(titleInput)) {
            titleView.setError("Field cannot be empty");
            focusView = titleView;
            cancel = true;
        }

        if(TextUtils.isEmpty(urlInput)) {
            urlView.setError("Field cannot be empty");
            focusView = urlView;
            cancel = true;
        } else if(!(Patterns.WEB_URL.matcher(urlInput).matches())) {
            urlView.setError("URL Invalid. Check input");
            focusView = urlView;
            cancel = true;
        }

        if(TextUtils.isEmpty(textInput)) {
            notesView.setError(textInput);
            focusView = notesView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            final String BASE_URL = getString(R.string.server_base_url);
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            EndpointsInterface apiService = retrofit.create(EndpointsInterface.class);

            if(action.equals("SHARE")) {
                Call<NullResponse> call = apiService.createBookmark(projectInputId, urlInput, textInput, titleInput, email, password);
                call.enqueue(new Callback<NullResponse>() {
                    @Override
                    public void onResponse(Response<NullResponse> response, Retrofit retrofit) {
                        if(response.code() == 200) {
                            Toast.makeText(EditBookmark.this, "Bookmark created successful", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Log.e("HTTP Error", String.valueOf(response.code()));
                        }
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        Log.e("Error", t.getMessage());
                    }
                });
            } else if (action.equals("EDIT")) {

                if(!titleInput.equals(title)) {
                    Call<NullResponse> updateTextCall = apiService.updateBookmarksTITLE(bookmark_id, titleInput, email, password);
                    updateTextCall.enqueue(new Callback<NullResponse>() {
                        @Override
                        public void onResponse(Response<NullResponse> response, Retrofit retrofit) {
                            if(response.code() == 200) {
                                textCheck = true;
                            } else {
                                Log.e("HTTP Error", String.valueOf(response.code()));
                            }
                        }

                        @Override
                        public void onFailure(Throwable t) {
                            Log.e("Error", t.getMessage());
                        }
                    });
                } else textCheck = true;

                if(!urlInput.equals(url)) {
                    Call<NullResponse> updateURLCall = apiService.updateBookmarksURL(bookmark_id, urlInput, email, password);
                    updateURLCall.enqueue(new Callback<NullResponse>() {
                        @Override
                        public void onResponse(Response<NullResponse> response, Retrofit retrofit) {
                            if (response.code() == 200) {
                                Toast.makeText(EditBookmark.this, "Bookmark Updated", Toast.LENGTH_SHORT).show();
                                urlCheck = true;
                            } else {
                                Log.e("HTTP Error", String.valueOf(response.code()));
                            }
                        }

                        @Override
                        public void onFailure(Throwable t) {
                            Log.e("Error", t.getMessage());
                        }
                    });
                } else urlCheck = true;

                if(urlCheck && textCheck) {
                    finish();
                }
            } else {
                Call<NullResponse> call = apiService.createBookmark(projectInputId, urlInput, textInput, titleInput, email, password);
                call.enqueue(new Callback<NullResponse>() {
                    @Override
                    public void onResponse(Response<NullResponse> response, Retrofit retrofit) {
                        if(response.code() == 200) {
                            Toast.makeText(EditBookmark.this, "Bookmark created successful", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Log.e("HTTP Error", String.valueOf(response.code()));
                        }
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        Log.e("Error", t.getMessage());
                    }
                });
            }
        }
    }
}
