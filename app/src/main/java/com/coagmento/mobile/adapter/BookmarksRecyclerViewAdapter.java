package com.coagmento.mobile.adapter;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import com.coagmento.mobile.R;
import com.coagmento.mobile.models.Result;

import java.util.List;

/**
 * Created by Yash Shah on 2/13/15.
 */
public class BookmarksRecyclerViewAdapter extends RecyclerView.Adapter<BookmarksRecyclerViewAdapter.ViewHolder> {

    public interface OnItemClickListener {
        public void onItemClicked(int position);
    }

    public interface OnItemLongClickListener {
        public boolean onItemLongClicked(int position);
    }

    List<Result> bookmarks;
    Bundle userInfo;
    String host, email, password;
    OnItemClickListener onItemClickListener;
    OnItemLongClickListener onItemLongClickListener;

    public BookmarksRecyclerViewAdapter(List<Result> bookmarks, Bundle userInfo, OnItemClickListener onItemClickListener, OnItemLongClickListener onItemLongClickListener) {
        this.bookmarks = bookmarks;
        this.userInfo = userInfo;
        this.onItemClickListener = onItemClickListener;
        this.onItemLongClickListener = onItemLongClickListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_card_big, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.bookmarkItem = bookmarks.get(position);

        // Build Image URL
        host = userInfo.getString("host");
        email = userInfo.getString("email");
        password = userInfo.getString("password");
        String file_name = holder.bookmarkItem.getThumbnail().getImageLarge();
        Uri imageUri = Uri.parse(host).buildUpon()
                .appendPath("images")
                .appendPath("thumbnails")
                .appendPath("large")
                .appendPath(file_name)
                .build();
        String imageURL = imageUri.toString();

        // Use Picasso to retrieve image
        Picasso.with(holder.thumbnail.getContext())
                .load(imageURL)
                .placeholder(R.drawable.unavailable)
                .error(R.drawable.unavailable)
                .into(holder.thumbnail);

        holder.title.setText(holder.bookmarkItem.getTitle());

        holder.url.setText(holder.bookmarkItem.getUrl());
        holder.notes.setText(holder.bookmarkItem.getNotes());

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClickListener.onItemClicked(position);
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
        return bookmarks.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final ImageView thumbnail;
        public final TextView title;
        public final TextView url;
        public final TextView notes;

        public Result bookmarkItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            thumbnail = (ImageView) view.findViewById(R.id.big_thumbnail);
            title = (TextView) view.findViewById(R.id.big_title);
            url = (TextView) view.findViewById(R.id.big_url);
            notes = (TextView) view.findViewById(R.id.big_notes);
        }
    }

}