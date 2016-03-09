package org.coagmento.android.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.customtabs.CustomTabsIntent;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.PopupMenu;

import org.coagmento.android.R;
import org.coagmento.android.RecyclerItemClickListener;
import org.coagmento.android.adapter.BookmarksRecyclerViewAdapter;
import org.coagmento.android.data.EndpointsInterface;
import org.coagmento.android.models.BookmarksListResponse;
import org.coagmento.android.models.DeleteBookmarkResponse;
import org.coagmento.android.models.Result;

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

    private String host, email, password;
    private int project_id;
    private Bundle userInfo;
    private View rootView;

    private int ITEM_COUNT = 0;
    private List<Result> bookmarks = new ArrayList<>();

    public static BookmarksFragment newInstance(Bundle userInfo) {
        BookmarksFragment fragment = new BookmarksFragment();
        fragment.setArguments(userInfo);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_bookmarks, container, false);
        userInfo = getArguments();


        host = userInfo.getString("host");
        email = userInfo.getString("email");
        password = userInfo.getString("password");
        project_id = userInfo.getInt("project_id");

        loadList(project_id);

        return rootView;
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
    public boolean onItemLongClicked(int position) {

        AlertDialog.Builder builderSingle = new AlertDialog.Builder(getContext());
        builderSingle.setTitle("Options:");

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                getContext(),
                android.R.layout.simple_selectable_list_item);
        arrayAdapter.add("Edit Bookmark");
        arrayAdapter.add("Move to another project");
        arrayAdapter.add("Delete Bookmark");

        builderSingle.setNegativeButton(
                "cancel",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        builderSingle.setAdapter(
                arrayAdapter,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String strName = arrayAdapter.getItem(which);
                        Snackbar snackbar = Snackbar
                                .make(rootView, strName, Snackbar.LENGTH_LONG);
                        snackbar.show();
                    }
                });
        builderSingle.show();

        return false;
    }
}
