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
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.coagmento.mobile.DividerItemDecoration;
import com.coagmento.mobile.R;
import com.coagmento.mobile.adapter.PagesRecyclerViewAdapter;
import com.coagmento.mobile.data.EndpointsInterface;
import com.coagmento.mobile.models.NullResponse;
import com.coagmento.mobile.models.PagesListResponse;
import com.coagmento.mobile.models.Result;
import com.coagmento.mobile.models.UserListResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit.Call;
import retrofit.Callback;
import retrofit.GsonConverterFactory;
import retrofit.Response;
import retrofit.Retrofit;

public class ActivityFragment extends Fragment implements PagesRecyclerViewAdapter.OnItemClickListener, PagesRecyclerViewAdapter.OnItemLongClickListener {

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private SwipeRefreshLayout refreshLayout;
    private TextView noDataMessage;

    private String host, email, password;
    private int project_id;
    private Bundle userInfo;
    private View rootView;

    private int ITEM_COUNT = 0;
    private List<Result> pages = new ArrayList<>();
    private List<Result> users = new ArrayList<>();

    public static ActivityFragment newInstance(Bundle userInfo) {
        ActivityFragment fragment = new ActivityFragment();
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
    public ActivityFragment() {
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.bookmarks_recyclerView);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mAdapter = new PagesRecyclerViewAdapter(pages, users, userInfo, this, this);
        mRecyclerView.setAdapter(mAdapter);

        noDataMessage = (TextView) view.findViewById(R.id.noDataFound);
        noDataMessage.setText(getString(R.string.prompt_no_pages_found));

        refreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.bookmarks_swiperefresh);
        refreshLayout.setColorSchemeColors(Color.RED, Color.BLUE, Color.YELLOW);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadList(project_id);
            }
        });
    }

    public void loadList(int projectId) {

        project_id = projectId;

        // Fetch Project Titles, Id's and Ownership
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(host)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        EndpointsInterface apiService = retrofit.create(EndpointsInterface.class);

        Call<PagesListResponse> call = apiService.getMultiplePages(project_id, email, password);

        call.enqueue(new Callback<PagesListResponse>() {
            @Override
            public void onResponse(Response<PagesListResponse> response, Retrofit retrofit) {
                if (response.code() == 200) {
                    pages.clear();
                    pages.addAll(response.body().getResult());
                    ITEM_COUNT = pages.size();
                    mAdapter.notifyDataSetChanged();
                    refreshLayout.setRefreshing(false);

                    if(pages.size() > 0) {
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

        Call<UserListResponse> call1 = apiService.getUserList();

        call1.enqueue(new Callback<UserListResponse>() {
            @Override
            public void onResponse(Response<UserListResponse> response, Retrofit retrofit) {
                if(response.code() == 200) {
                    users.clear();
                    users.addAll(response.body().getResult());
                }
            }

            @Override
            public void onFailure(Throwable t) {

            }
        });
    }

    @Override
    public void onItemClicked(int position) {
        String url = pages.get(position).getUrl();
        Uri uri = Uri.parse(url);
        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
        builder.setToolbarColor(getResources().getColor(R.color.colorPrimaryDark));
        CustomTabsIntent customTabsIntent = builder.build();
        customTabsIntent.launchUrl(getActivity(), uri);
    }

    @Override
    public boolean onItemLongClicked(final int position) {

        final Context context = getContext();

        AlertDialog.Builder builderSingle = new AlertDialog.Builder(context);
        builderSingle.setTitle(pages.get(position).getTitle());

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                getContext(),
                R.layout.dialog_selectable_list);
        arrayAdapter.add("Delete Page");

        builderSingle.setAdapter(
                arrayAdapter,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String strName = arrayAdapter.getItem(which);

                        switch (strName) {
                            case "Delete Page":
                                dialog.dismiss();
                                deletePage(pages.get(position));
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

    public void deletePage(Result page) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(host)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        EndpointsInterface apiService = retrofit.create(EndpointsInterface.class);

        Call<NullResponse> call = apiService.deletePage(page.getId(), email, password);

        call.enqueue(new Callback<NullResponse>() {
            @Override
            public void onResponse(Response<NullResponse> response, Retrofit retrofit) {
                if(response.code() == 200) {
                    Snackbar snackbar = Snackbar
                            .make(rootView, "Page Deleted.", Snackbar.LENGTH_SHORT);
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
