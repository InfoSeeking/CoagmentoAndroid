package org.coagmento.android.adapter;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.squareup.picasso.Picasso;

import org.coagmento.android.R;
import org.coagmento.android.data.EndpointsInterface;
import org.coagmento.android.fragment.BookmarksFragment;
import org.coagmento.android.models.DeleteBookmarkResponse;
import org.coagmento.android.models.Result;
import org.coagmento.android.models.User;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import retrofit.Call;
import retrofit.Callback;
import retrofit.GsonConverterFactory;
import retrofit.Response;
import retrofit.Retrofit;
import retrofit.http.Url;

/**
 * Created by Yash Shah on 2/13/15.
 */
public class BookmarksRecyclerViewAdapter extends RecyclerView.Adapter<BookmarksRecyclerViewAdapter.ViewHolder> {

    List<Result> bookmarks;
    Bundle userInfo;
    String host, email, password;

    public BookmarksRecyclerViewAdapter(List<Result> bookmarks, Bundle userInfo) {
        this.bookmarks = bookmarks;
        this.userInfo = userInfo;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_card_big, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
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

        holder.popup = new PopupMenu(holder.overflowMenu.getContext(), holder.overflowMenu);
        final MenuInflater inflater = holder.popup.getMenuInflater();
        inflater.inflate(R.menu.bookmarks_menu, holder.popup.getMenu());

        holder.overflowMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.popup.show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return bookmarks.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final ImageView thumbnail, overflowMenu;
        public final TextView title;
        public final TextView url;
        public PopupMenu popup;
        public final TextView notes;

        public Result bookmarkItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            thumbnail = (ImageView) view.findViewById(R.id.big_thumbnail);
            title = (TextView) view.findViewById(R.id.big_title);
            url = (TextView) view.findViewById(R.id.big_url);
            notes = (TextView) view.findViewById(R.id.big_notes);
            overflowMenu = (ImageView) view.findViewById(R.id.album_overflow);
        }
    }
}