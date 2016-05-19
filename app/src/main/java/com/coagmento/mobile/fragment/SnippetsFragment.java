package com.coagmento.mobile.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.customtabs.CustomTabsIntent;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.coagmento.mobile.R;
import com.coagmento.mobile.adapter.SnippetsAdapter;
import com.coagmento.mobile.data.EndpointsInterface;
import com.coagmento.mobile.models.NullResponse;
import com.coagmento.mobile.models.Result;
import com.coagmento.mobile.models.SnippetListResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit.Call;
import retrofit.Callback;
import retrofit.GsonConverterFactory;
import retrofit.Response;
import retrofit.Retrofit;

public class SnippetsFragment extends Fragment implements SnippetsAdapter.OnItemLongClickListener {

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private SwipeRefreshLayout refreshLayout;
    private TextView noDataMessage;

    private String host, email, password;
    private int project_id;
    private Bundle userInfo;
    private View rootView;

    private int ITEM_COUNT = 0;
    private List<Result> snippets = new ArrayList<>();

    public static SnippetsFragment newInstance(Bundle userInfo) {
        SnippetsFragment fragment = new SnippetsFragment();
        fragment.setArguments(userInfo);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_swipe_refresh, container, false);
        userInfo = getArguments();

        host = userInfo.getString("host");
        email = userInfo.getString("email");
        password = userInfo.getString("password");
        project_id = userInfo.getInt("project_id");

        loadList(project_id);

        return rootView;
    }

    @SuppressLint("ValidFragment")
    public SnippetsFragment() {
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.bookmarks_recyclerView);
        RecyclerView.LayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        mAdapter = new SnippetsAdapter(userInfo, snippets, this, getActivity());
        mRecyclerView.setAdapter(mAdapter);

        noDataMessage = (TextView) view.findViewById(R.id.noDataFound);
        noDataMessage.setText(getString(R.string.prompt_no_snippets_found));

        refreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.bookmarks_swiperefresh);
        refreshLayout.setColorSchemeColors(Color.RED, Color.BLUE, Color.YELLOW);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadList(project_id);
            }
        });
    }

    public void loadList(final int projectId) {

        project_id = projectId;

        // Fetch Project Titles, Id's and Ownership
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(host)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        EndpointsInterface apiService = retrofit.create(EndpointsInterface.class);

        Call<SnippetListResponse> call = apiService.getMultipleSnippets(email, password);

        call.enqueue(new Callback<SnippetListResponse>() {
            @Override
            public void onResponse(Response<SnippetListResponse> response, Retrofit retrofit) {
                if (response.code() == 200) {
                    snippets.clear();
                    for (Result snippet : response.body().getResult()) {
                        if (snippet.getProjectId() == projectId) {
                            snippets.add(snippet);
                            ITEM_COUNT++;
                        }
                    }
                    mAdapter.notifyDataSetChanged();
                    refreshLayout.setRefreshing(false);

                    if (snippets.size() > 0) {
                        noDataMessage.setVisibility(View.GONE);
                        mRecyclerView.setVisibility(View.VISIBLE);
                    } else {
                        noDataMessage.setVisibility(View.VISIBLE);
                        mRecyclerView.setVisibility(View.GONE);
                    }
                } else {
                    Log.e("Response Code: ", String.valueOf(response.code()));
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Log.e("Retrofit Failure: ", t.toString());
            }
        });
    }

    @Override
    public boolean onItemLongClicked(final int position) {

        final Context context = getContext();

        AlertDialog.Builder builderSingle = new AlertDialog.Builder(context);
        builderSingle.setTitle(snippets.get(position).getTitle());

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                getContext(),
                R.layout.dialog_selectable_list);
        arrayAdapter.add("Visit Link");
        arrayAdapter.add("Delete Snippet");

        builderSingle.setAdapter(
                arrayAdapter,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String strName = arrayAdapter.getItem(which);

                        switch (strName) {
                            case "Visit Link":
                                dialog.dismiss();

                                String url = snippets.get(position).getUrl();
                                Uri uri = Uri.parse(url);

                                CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                                builder.setToolbarColor(getResources().getColor(R.color.colorPrimaryDark));
                                CustomTabsIntent customTabsIntent = builder.build();
                                customTabsIntent.launchUrl(getActivity(), uri);
                                break;
                            case "Delete Snippet":
                                dialog.dismiss();
                                deletePage(snippets.get(position));
                                break;
                            default:
                                Snackbar snackbar = Snackbar
                                        .make(rootView, strName, Snackbar.LENGTH_LONG);
                                snackbar.show();
                                break;
                        }


                    }
                });
        builderSingle.show();

        return false;
    }

    public void deletePage(Result snippet) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(host)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        EndpointsInterface apiService = retrofit.create(EndpointsInterface.class);

        Call<NullResponse> call = apiService.deleteSnippet(snippet.getId(), email, password);

        call.enqueue(new Callback<NullResponse>() {
            @Override
            public void onResponse(Response<NullResponse> response, Retrofit retrofit) {
                if(response.code() == 200) {
                    Snackbar snackbar = Snackbar
                            .make(rootView, "Snippet Deleted.", Snackbar.LENGTH_SHORT);
                    snackbar.show();
                    loadList(project_id);
                } else {
                    Snackbar snackbar = Snackbar
                            .make(rootView, "HTTP Error Code: " + response.code(), Snackbar.LENGTH_SHORT);
                    snackbar.show();
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
