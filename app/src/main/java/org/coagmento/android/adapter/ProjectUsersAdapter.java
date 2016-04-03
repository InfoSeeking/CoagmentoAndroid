package org.coagmento.android.adapter;

import android.os.Bundle;
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
import org.coagmento.android.models.Result;
import org.coagmento.android.models.User;

import java.util.ArrayList;
import java.util.List;


public class ProjectUsersAdapter extends RecyclerView.Adapter<ProjectUsersAdapter.ViewHolder> {

    private List<Result> mUsers = new ArrayList<Result>();
    private Result currentProject;
    private Bundle currentUserInfo;


    public ProjectUsersAdapter(Bundle userInfo, Result project, List<Result> userList) {

        currentProject = project;
        currentUserInfo = userInfo;
        mUsers = userList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.project_users_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.userItem = mUsers.get(position).getUser();
        holder.resultItem = mUsers.get(position);

        if(currentProject.getLevel().equals("o")) {
            holder.userItemListView.setClickable(true);
        } else {
            holder.userItemListView.setClickable(false);
        }

        if (holder.userItem.getEmail().equals(currentUserInfo.getString("email"))) {
            holder.mUserNameView.setText("You");
            holder.mUserEmailView.setText(holder.userItem.getEmail());
        } else {
            holder.mUserNameView.setText(holder.userItem.getName());
            holder.mUserEmailView.setText(holder.userItem.getEmail());
        }

        ColorGenerator generator = ColorGenerator.MATERIAL;
        TextDrawable drawable = TextDrawable.builder()
                .beginConfig()
                    .width(150)
                    .height(150)
                .endConfig()
                .buildRound(String.valueOf(holder.userItem.getName().charAt(0)), generator.getRandomColor());
        holder.mUserImageView.setImageDrawable(drawable);
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mUserNameView;
        public final TextView mUserEmailView;
        public final ImageView mUserImageView;
        public final LinearLayout userItemListView;
        public User userItem;
        public Result resultItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mUserEmailView = (TextView) view.findViewById(R.id.user_email_list_item);
            mUserNameView = (TextView) view.findViewById(R.id.user_name_list_item);
            mUserImageView = (ImageView) view.findViewById(R.id.user_image_list_item);
            userItemListView = (LinearLayout) view.findViewById(R.id.user_list_item);
        }

    }
}
