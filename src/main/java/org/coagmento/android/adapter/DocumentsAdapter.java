package org.coagmento.android.adapter;

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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Yash Shah on 2/13/15.
 */
public class DocumentsAdapter extends RecyclerView.Adapter<DocumentsAdapter.ViewHolder> {

    public interface OnItemClickListener {
        public void onItemClicked(int position);
    }

    public interface OnItemLongClickListener {
        public boolean onItemLongClicked(int position);
    }

    private HashMap<Integer, String> userHashMap = new HashMap();
    List<Result> pages;
    List<Result> users;
    Bundle userInfo;
    String host, email, password;
    OnItemClickListener onItemClickListener;
    OnItemLongClickListener onItemLongClickListener;

    public DocumentsAdapter(List<Result> pages, List<Result> users, Bundle userInfo, OnItemClickListener onItemClickListener, OnItemLongClickListener onItemLongClickListener) {
        this.pages = pages;
        this.userInfo = userInfo;
        this.onItemClickListener = onItemClickListener;
        this.onItemLongClickListener = onItemLongClickListener;
        this.users = users;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.pages_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.page = pages.get(position);

        // Build Server Details
        host = userInfo.getString("host");
        email = userInfo.getString("email");
        password = userInfo.getString("password");

        for (Result user : users) {
            userHashMap.put(user.getId(), user.getName());
        }

        String user_name = userHashMap.get(holder.page.getuser_id());

        String file_name = holder.page.getThumbnail().getImageLarge();
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

        String formattedDate = "";
        SimpleDateFormat dateFormat2 = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
        try {
            Date date2 = dateFormat2.parse(holder.page.getcreated_at());
            formattedDate = new SimpleDateFormat("EEEE, MMMM d").format(date2);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        String description = "Saved " + formattedDate + " by " + user_name;

        holder.title.setText(holder.page.getTitle());

        holder.description.setText(description);

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
        public ImageView thumbnail;
        public final TextView title;
        public final TextView description;

        public Result page;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            thumbnail = (ImageView) view.findViewById(R.id.page_image_list_item);
            title = (TextView) view.findViewById(R.id.page_title_list_item);
            description = (TextView) view.findViewById(R.id.page_desc_list_item);
        }
    }

}