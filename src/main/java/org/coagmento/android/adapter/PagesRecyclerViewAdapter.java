package org.coagmento.android.adapter;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.coagmento.android.R;
import org.coagmento.android.models.Result;

import java.util.List;

/**
 * Created by Yash Shah on 2/13/15.
 */
public class PagesRecyclerViewAdapter extends RecyclerView.Adapter<PagesRecyclerViewAdapter.ViewHolder> {

    public interface OnItemClickListener {
        public void onItemClicked(int position);
    }

    public interface OnItemLongClickListener {
        public boolean onItemLongClicked(int position);
    }

    List<Result> pages;
    Bundle userInfo;
    String host, email, password;
    OnItemClickListener onItemClickListener;
    OnItemLongClickListener onItemLongClickListener;

    public PagesRecyclerViewAdapter(List<Result> pages, Bundle userInfo, OnItemClickListener onItemClickListener, OnItemLongClickListener onItemLongClickListener) {
        this.pages = pages;
        this.userInfo = userInfo;
        this.onItemClickListener = onItemClickListener;
        this.onItemLongClickListener = onItemLongClickListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_1, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.bookmarkItem = pages.get(position);

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
        return pages.size();
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