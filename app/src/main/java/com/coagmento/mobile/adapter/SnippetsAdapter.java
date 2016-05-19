package com.coagmento.mobile.adapter;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.coagmento.mobile.EditSnippet;
import com.coagmento.mobile.R;
import com.coagmento.mobile.models.Result;
import com.coagmento.mobile.models.User;

import java.util.ArrayList;
import java.util.List;


public class SnippetsAdapter extends RecyclerView.Adapter<SnippetsAdapter.ViewHolder> {

    private List<Result> snippets = new ArrayList<Result>();
    private Result currentProject;
    private Bundle currentUserInfo;
    private OnItemLongClickListener onItemLongClickListener;
    private FragmentActivity fragmentActivity;

    private int EDIT_SNIPPET_CODE = 1;

    public interface OnItemLongClickListener {
        public boolean onItemLongClicked(int position);
    }

    public SnippetsAdapter(Bundle userInfo, List<Result> snippets, OnItemLongClickListener onItemLongClickListener, FragmentActivity fragmentActivity) {

        currentUserInfo = userInfo;
        this.snippets = snippets;
        this.onItemLongClickListener = onItemLongClickListener;
        this.fragmentActivity = fragmentActivity;

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.snippets_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.snippet = snippets.get(position);
        holder.userItem = holder.snippet.getUser();

        holder.titleView.setText(holder.snippet.getTitle());
        holder.textView.setText(holder.snippet.getText());

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(fragmentActivity, EditSnippet.class);
                intent.putExtra("title", holder.snippet.getTitle());
                intent.putExtra("text", holder.snippet.getText());
                intent.putExtra("url", holder.snippet.getUrl());
                intent.putExtra("project_id", currentUserInfo.getInt("project_id"));
                intent.putExtra("snippet_id", holder.snippet.getId());
                intent.putExtra("INTENT_ACTION", "EDIT");
                fragmentActivity.startActivityForResult(intent, EDIT_SNIPPET_CODE);
            }
        });

        holder.mView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                onItemLongClickListener.onItemLongClicked(position);
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return snippets.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView titleView;
        public final TextView textView;
        public User userItem;
        public Result snippet;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            titleView = (TextView) view.findViewById(R.id.title);
            textView = (TextView) view.findViewById(R.id.text);
        }

    }
}
