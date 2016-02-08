package org.coagmento.android.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.florent37.materialviewpager.MaterialViewPagerHelper;
import com.github.florent37.materialviewpager.adapter.RecyclerViewMaterialAdapter;

import org.coagmento.android.R;
import org.coagmento.android.adapter.BookmarksRecyclerViewAdapter;
import org.coagmento.android.adapter.TestRecyclerViewAdapter;
import org.coagmento.android.data.EndpointsInterface;
import org.coagmento.android.models.BookmarksListResponse;
import org.coagmento.android.models.Result;

import java.util.ArrayList;
import java.util.List;

import retrofit.Call;
import retrofit.Callback;
import retrofit.GsonConverterFactory;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * Created by florentchampigny on 24/04/15.
 */
public class BookmarksFragment extends Fragment {

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;

    private String host, email, password;
    private int project_id;

    private int ITEM_COUNT = 0;
    private List<Result> bookmarks = new ArrayList<>();

    public static BookmarksFragment newInstance(Bundle userInfo) {
        BookmarksFragment fragment = new BookmarksFragment();
        fragment.setArguments(userInfo);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Bundle userInfo = getArguments();

        host = userInfo.getString("host");
        email = userInfo.getString("email");
        password = userInfo.getString("password");
        project_id = userInfo.getInt("project_id");

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
                if(response.code() == 200) {
                    bookmarks = response.body().getResult();
                    ITEM_COUNT = bookmarks.size();
                    loadList();
                } else {
                    Log.e("Response Code: ", String.valueOf(response.code()));
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Log.e("Retrofit Failure: ", t.toString());
            }
        });

        return inflater.inflate(R.layout.fragment_recyclerview, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);
    }

    protected void loadList() {
        mAdapter = new RecyclerViewMaterialAdapter(new BookmarksRecyclerViewAdapter(bookmarks));
        mRecyclerView.setAdapter(mAdapter);
//
//        {
//            for (int i = 0; i < ITEM_COUNT; ++i)
//                bookmarks.add(new Result());
//            mAdapter.notifyDataSetChanged();
//        }

        MaterialViewPagerHelper.registerRecyclerView(getActivity(), mRecyclerView, null);
    }
}
