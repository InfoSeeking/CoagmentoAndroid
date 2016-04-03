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


public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    private List<Result> messages = new ArrayList<Result>();
    private Result currentProject;
    private Bundle currentUserInfo;


    public ChatAdapter(Bundle userInfo, Result project, List<Result> messages) {

        currentProject = project;
        currentUserInfo = userInfo;
        this.messages = messages;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.project_users_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.message = messages.get(position);
        holder.userItem = holder.message.getUser();

        if(currentProject.getLevel().equals("o") || currentProject.getLevel().equals("w")) {
        } else {
        }

        ColorGenerator generator = ColorGenerator.MATERIAL;
        TextDrawable drawable = TextDrawable.builder()
                .beginConfig()
                    .width(150)
                    .height(150)
                .endConfig()
                .buildRound(String.valueOf(holder.userItem.getName().charAt(0)), generator.getRandomColor());
        holder.userImageView.setImageDrawable(drawable);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView userNameView;
        public final TextView messageView;
        public final ImageView userImageView;
        public User userItem;
        public Result message;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            messageView = (TextView) view.findViewById(R.id.user_email_list_item);
            userNameView = (TextView) view.findViewById(R.id.user_name_list_item);
            userImageView = (ImageView) view.findViewById(R.id.user_image_list_item);
        }

    }
}
