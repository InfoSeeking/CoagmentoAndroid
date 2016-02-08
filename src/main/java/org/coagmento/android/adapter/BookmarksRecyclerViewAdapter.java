package org.coagmento.android.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;

import org.coagmento.android.R;
import org.coagmento.android.fragment.BookmarksFragment;
import org.coagmento.android.models.Result;
import org.coagmento.android.models.User;

import java.util.List;

/**
 * Created by florentchampigny on 24/04/15.
 */
public class BookmarksRecyclerViewAdapter extends RecyclerView.Adapter<BookmarksRecyclerViewAdapter.ViewHolder> {

    List<Result> bookmarks;

    public BookmarksRecyclerViewAdapter(List<Result> bookmarks) {
        this.bookmarks = bookmarks;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_card_small, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.bookmarkItem = bookmarks.get(position);

        holder.title.setText(holder.bookmarkItem.getTitle());
        holder.url.setText(holder.bookmarkItem.getUrl());

    }

    @Override
    public int getItemCount() {
        return bookmarks.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView title;
        public final TextView url;
        public Result bookmarkItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            title = (TextView) view.findViewById(R.id.small_title);
            url = (TextView) view.findViewById(R.id.small_url);

        }

    }
}