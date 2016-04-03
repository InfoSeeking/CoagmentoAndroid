package org.coagmento.android;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatSpinner;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Toast;

import com.squareup.okhttp.ResponseBody;

import org.coagmento.android.data.EndpointsInterface;
import org.coagmento.android.fragment.SnippetsFragment;
import org.coagmento.android.models.Errors;
import org.coagmento.android.models.ProjectListResponse;
import org.coagmento.android.models.Result;
import org.coagmento.android.models.SnippetResponse;

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

public class EditSnippet extends AppCompatActivity implements AdapterView.OnItemClickListener {

    // UI References
    private EditText titleView, textView, urlView;
    private AppCompatButton addSnippet;
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
    private int project_id, snippet_id;
    private String url, title, text;
    private String action;
    private boolean textCheck = false, urlCheck = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_snippet);

        b = getIntent().getExtras();

        titleView = (EditText) findViewById(R.id.snippet_title_form);
        urlView = (EditText) findViewById(R.id.snippet_url_form);
        textView = (EditText) findViewById(R.id.snippet_description_form);
        addSnippet = (AppCompatButton) findViewById(R.id.create_snippet_button);
        projectsSpinner = (AppCompatSpinner) findViewById(R.id.snippets_project_snipper);

        //Set up Dialogs
        alertDialog = new AlertDialog.Builder(EditSnippet.this).create();
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
                                     projectSpinnerAdapter = new ArrayAdapter<String>(EditSnippet.this,
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
                getSupportActionBar().setTitle("Add Snippet");

                project_id = b.getInt("project_id");
                url = b.getString("data");

                projectsSpinner.setAdapter(projectSpinnerAdapter);
                projectsSpinner.setSelection(projectPositionById.get(project_id));

                urlView.setText(url);
                urlView.setInputType(InputType.TYPE_NULL);

            } else if(action.equals("EDIT")) {
                getSupportActionBar().setTitle("Edit Snippet");

                project_id = b.getInt("project_id");
                url = b.getString("url");
                text = b.getString("text");
                title = b.getString("title");
                snippet_id = b.getInt("snippet_id");

                projectsSpinner.setEnabled(false);
                projectsSpinner.setClickable(false);
                projectsSpinner.setAdapter(projectSpinnerAdapter);
                projectsSpinner.setSelection(projectPositionById.get(project_id));

                titleView.setText(title);
                titleView.setInputType(InputType.TYPE_NULL);

                urlView.setText(url);
                textView.setText(text);

            }
        }  else {

            getSupportActionBar().setTitle("Add Snippet");

            action = "";

            projectsSpinner.setEnabled(true);
            projectsSpinner.setClickable(true);
            projectsSpinner.setAdapter(projectSpinnerAdapter);

        }

        addSnippet.setOnClickListener(new View.OnClickListener() {
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
        textView.setError(null);

        int projectInputId = filteredProjectList.get(projectsSpinner.getSelectedItemPosition()).getId();
        String titleInput = titleView.getText().toString();
        String urlInput = urlView.getText().toString();
        String textInput = textView.getText().toString();

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
        }  else if(!(URLUtil.isValidUrl(urlInput))) {
            urlView.setError("URL Invalid. Check input");
            focusView = urlView;
            cancel = true;
        }

        if(TextUtils.isEmpty(textInput)) {
            textView.setError("Field cannot be empty");
            focusView = textView;
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
                Call<SnippetResponse> call = apiService.createSnippet(projectInputId, urlInput, textInput, titleInput, email, password);
                call.enqueue(new Callback<SnippetResponse>() {
                    @Override
                    public void onResponse(Response<SnippetResponse> response, Retrofit retrofit) {
                        if(response.code() == 200) {
                            Toast.makeText(EditSnippet.this, "Snippet created successful", Toast.LENGTH_SHORT).show();
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

                if(!textInput.equals(text)) {
                    Call<SnippetResponse> updateTextCall = apiService.updateSnippetTEXT(snippet_id, textInput, email, password);
                    updateTextCall.enqueue(new Callback<SnippetResponse>() {
                        @Override
                        public void onResponse(Response<SnippetResponse> response, Retrofit retrofit) {
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
                    Call<SnippetResponse> updateURLCall = apiService.updateSnippetURL(snippet_id, urlInput, email, password);
                    updateURLCall.enqueue(new Callback<SnippetResponse>() {
                        @Override
                        public void onResponse(Response<SnippetResponse> response, Retrofit retrofit) {
                            if (response.code() == 200) {
                                Toast.makeText(EditSnippet.this, "Snippet Updated", Toast.LENGTH_SHORT).show();
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
                Call<SnippetResponse> call = apiService.createSnippet(projectInputId, urlInput, textInput, titleInput, email, password);
                final int tmpId = projectInputId;
                call.enqueue(new Callback<SnippetResponse>() {
                    @Override
                    public void onResponse(Response<SnippetResponse> response, Retrofit retrofit) {
                        if(response.code() == 200) {
                            Toast.makeText(EditSnippet.this, "Snippet created successful", Toast.LENGTH_SHORT).show();
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
