package com.coagmento.mobile.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.customtabs.CustomTabsIntent;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.coagmento.mobile.EditBookmark;
import com.coagmento.mobile.R;
import com.coagmento.mobile.adapter.BookmarksRecyclerViewAdapter;
import com.coagmento.mobile.data.EndpointsInterface;
import com.coagmento.mobile.models.BookmarksListResponse;
import com.coagmento.mobile.models.DeleteBookmarkResponse;
import com.coagmento.mobile.models.NullResponse;
import com.coagmento.mobile.models.Result;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import retrofit.Call;
import retrofit.Callback;
import retrofit.GsonConverterFactory;
import retrofit.Response;
import retrofit.Retrofit;

public class BookmarksFragment extends Fragment implements BookmarksRecyclerViewAdapter.OnItemClickListener, BookmarksRecyclerViewAdapter.OnItemLongClickListener {

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private SwipeRefreshLayout refreshLayout;
    private TextView noDataMessage;

    private String host, email, password;
    private int project_id;
    private Bundle userInfo;
    private View rootView;

    private int ITEM_COUNT = 0;
    private List<Result> bookmarks = new ArrayList<>();
    private BookmarksFragmentInteraction bookmarksFragmentInteraction;

    public static BookmarksFragment newInstance(Bundle userInfo, BookmarksFragmentInteraction bookmarksFragmentInteraction) {
        BookmarksFragment fragment = new BookmarksFragment(bookmarksFragmentInteraction);
        fragment.setArguments(userInfo);
        return fragment;
    }

    public BookmarksFragment() {

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
    public BookmarksFragment(BookmarksFragmentInteraction bookmarksFragmentInteraction) {
        this.bookmarksFragmentInteraction = bookmarksFragmentInteraction;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.bookmarks_recyclerView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);
        mAdapter = new BookmarksRecyclerViewAdapter(bookmarks, userInfo, this, this);
        mRecyclerView.setAdapter(mAdapter);

        noDataMessage = (TextView) view.findViewById(R.id.noDataFound);
        noDataMessage.setText(getString(R.string.prompt_no_bookmarks_found));

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

        Call<BookmarksListResponse> call = apiService.getBookmarks(project_id, email, password);

        call.enqueue(new Callback<BookmarksListResponse>() {
            @Override
            public void onResponse(Response<BookmarksListResponse> response, Retrofit retrofit) {
                if (response.code() == 200) {
                    bookmarks.clear();
                    bookmarks.addAll(response.body().getResult());
                    ITEM_COUNT = bookmarks.size();
                    mAdapter.notifyDataSetChanged();
                    refreshLayout.setRefreshing(false);

                    if (bookmarks.size() > 0) {
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
    public void onItemClicked(int position) {
        String url = bookmarks.get(position).getUrl();
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
        builderSingle.setTitle(bookmarks.get(position).getTitle());

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                getContext(),
                R.layout.dialog_selectable_list);
        arrayAdapter.add("Edit Bookmark");
        arrayAdapter.add("Move to another project");
        arrayAdapter.add("Delete Bookmark");

        builderSingle.setAdapter(
                arrayAdapter,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String strName = arrayAdapter.getItem(which);

                        switch (strName) {
                            case "Move to another project":
                                dialog.dismiss();
                                List<Result> projects = bookmarksFragmentInteraction.getProjectList();
                                moveBookmarkToProject(context, projects, position);
                                break;
                            case "Delete Bookmark":
                                dialog.dismiss();
                                deleteBookmark(bookmarks.get(position));
                                break;
                            case "Edit Bookmark":
                                Intent intent = new Intent(getActivity(), EditBookmark.class);
                                intent.putExtra("title", bookmarks.get(position).getTitle());
                                intent.putExtra("notes", bookmarks.get(position).getNotes());
                                intent.putExtra("url", bookmarks.get(position).getUrl());
                                intent.putExtra("project_id", project_id);
                                intent.putExtra("bookmark_id", bookmarks.get(position).getId());
                                intent.putExtra("INTENT_ACTION", "EDIT");
                                getActivity().startActivityForResult(intent, 2);
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

    public void moveBookmarkToProject(Context context, final List<Result> projectList, final int bookmark_position) {
        AlertDialog.Builder projectsBuilder = new AlertDialog.Builder(context);
        projectsBuilder.setTitle("Select a project");
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                context,
                R.layout.dialog_selectable_list);
        final ArrayList<Integer> project_ids = new ArrayList<>();
        int count = 0;
        for(int i=0; i<projectList.size();i++) {
            if(projectList.get(i).getProjectId() != project_id) {
                arrayAdapter.add(projectList.get(i).getTitle());
                project_ids.add(count, projectList.get(i).getProjectId());
                count++;
            }
        }
        projectsBuilder.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl(host)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();

                EndpointsInterface apiService = retrofit.create(EndpointsInterface.class);

                Call<NullResponse> call = apiService.moveProject(bookmarks.get(bookmark_position).getId(),
                                                                    project_ids.get(which),
                                                                        email, password);

                call.enqueue(new Callback<NullResponse>() {
                    @Override
                    public void onResponse(Response<NullResponse> response, Retrofit retrofit) {
                        if(response.code() == 200) {
                            Snackbar snackbar = Snackbar
                                    .make(rootView, "Bookmark Moved.", Snackbar.LENGTH_SHORT);
                            snackbar.show();
                            loadList(project_id);
                        } else {
                            Snackbar snackbar = Snackbar
                                    .make(rootView, "Error Code: " + response.code(), Snackbar.LENGTH_SHORT);
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
        });
        projectsBuilder.show();
    }

    public void deleteBookmark(Result bookmark) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(host)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        EndpointsInterface apiService = retrofit.create(EndpointsInterface.class);

        Call<DeleteBookmarkResponse> call = apiService.deleteBookmark(bookmark.getId(), email, password);

        call.enqueue(new Callback<DeleteBookmarkResponse>() {
            @Override
            public void onResponse(Response<DeleteBookmarkResponse> response, Retrofit retrofit) {
                if(response.code() == 200) {
                    Snackbar snackbar = Snackbar
                            .make(rootView, "Bookmark Deleted.", Snackbar.LENGTH_SHORT);
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

    public interface BookmarksFragmentInteraction extends Serializable {
        public List<Result> getProjectList();
    }
}
