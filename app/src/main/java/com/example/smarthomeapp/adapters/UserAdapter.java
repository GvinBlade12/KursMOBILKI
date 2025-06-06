package com.example.smarthomeapp.adapters;

import android.content.Context;
import android.view.*;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.smarthomeapp.utils.UserManager;


import com.example.smarthomeapp.R;
import com.example.smarthomeapp.models.User;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private Context context;
    private List<User> userList;
    private OnUserSelectedListener userSelectedListener;
    private OnUserDeletedListener userDeletedListener;

    public UserAdapter(Context context, List<User> userList) {
        this.context = context;
        this.userList = userList;
    }

    public interface OnUserSelectedListener {
        void onUserSelected(User user);
    }

    public interface OnUserDeletedListener {
        void onUserDeleted();
    }

    public void setOnUserSelectedListener(OnUserSelectedListener listener) {
        this.userSelectedListener = listener;
    }

    public void setOnUserDeletedListener(OnUserDeletedListener listener) {
        this.userDeletedListener = listener;
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView emailTextView;
        Button selectButton;
        Button deleteButton;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            emailTextView = itemView.findViewById(R.id.emailTextView);
            selectButton = itemView.findViewById(R.id.selectButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_device, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);
        holder.emailTextView.setText(user.getEmail());

        holder.selectButton.setOnClickListener(v -> {
            if (userSelectedListener != null) {
                userSelectedListener.onUserSelected(user);
            }
        });

        holder.deleteButton.setOnClickListener(v -> {
            userList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, userList.size());

            UserManager userManager = new UserManager(context);
            userManager.saveUsers(userList);

            if (userDeletedListener != null) {
                userDeletedListener.onUserDeleted();
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public void setUserList(List<User> newList) {
        this.userList = newList;
    }

}
