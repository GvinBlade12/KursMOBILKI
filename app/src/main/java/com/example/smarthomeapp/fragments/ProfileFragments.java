package com.example.smarthomeapp.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.*;
import android.widget.*;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smarthomeapp.R;
import com.example.smarthomeapp.adapters.UserAdapter;
import com.example.smarthomeapp.models.User;
import com.example.smarthomeapp.utils.UserManager;

import java.util.List;

public class ProfileFragments extends Fragment {

    private TextView currentUserTextView;
    private Button addUserButton;
    private RecyclerView userListView;  // changed from ListView to RecyclerView

    private UserManager userManager;
    private UserAdapter adapter;
    private List<User> userList;

    public ProfileFragments() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        currentUserTextView = view.findViewById(R.id.currentUserTextView);
        addUserButton = view.findViewById(R.id.addUserButton);
        userListView = view.findViewById(R.id.userListView);

        userManager = new UserManager(getContext());

        // Устанавливаем LayoutManager для RecyclerView
        userListView.setLayoutManager(new LinearLayoutManager(getContext()));

        loadUsers();

        addUserButton.setOnClickListener(v -> showAddUserDialog());

        return view;
    }

    private void loadUsers() {
        userList = userManager.getUsers();

        if (adapter == null) {
            adapter = new UserAdapter(getContext(), userList);
            userListView.setAdapter(adapter);

            adapter.setOnUserDeletedListener(() -> {
                userList = userManager.getUsers();
                adapter.notifyDataSetChanged();
                User current = userManager.getCurrentUser();
                if (current == null || !userList.contains(current)) {
                    if (!userList.isEmpty()) {
                        userManager.setCurrentUser(userList.get(0));
                    } else {
                        userManager.setCurrentUser(null);
                    }
                }
                updateCurrentUserText();
            });

            adapter.setOnUserSelectedListener(user -> {
                userManager.setCurrentUser(user);
                updateCurrentUserText();
                Toast.makeText(getContext(), "Выбран аккаунт: " + user.getEmail(), Toast.LENGTH_SHORT).show();
            });
        } else {
            // Обновляем данные, если адаптер уже есть
            adapter.setUserList(userList);
            adapter.notifyDataSetChanged();
        }

        updateCurrentUserText();
    }

    private void updateCurrentUserText() {
        User current = userManager.getCurrentUser();
        if (current != null) {
            currentUserTextView.setText("Текущий аккаунт: " + current.getEmail());
        } else {
            currentUserTextView.setText("Аккаунт не выбран");
        }
    }

    private void showAddUserDialog() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_user, null);
        EditText emailInput = dialogView.findViewById(R.id.emailInput);
        EditText passwordInput = dialogView.findViewById(R.id.passwordInput);

        new AlertDialog.Builder(getContext())
                .setTitle("Новый аккаунт")
                .setView(dialogView)
                .setPositiveButton("Добавить", (dialog, which) -> {
                    String email = emailInput.getText().toString().trim();
                    String password = passwordInput.getText().toString().trim();

                    if (!email.isEmpty() && !password.isEmpty()) {
                        boolean exists = false;
                        for (User user : userList) {
                            if (user.getEmail().equalsIgnoreCase(email)) {
                                exists = true;
                                break;
                            }
                        }
                        if (!exists) {
                            userList.add(new User(email, password));
                            userManager.saveUsers(userList);
                            adapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(getContext(), "Пользователь с таким email уже существует", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Отмена", null)
                .show();
    }
    @Override
    public void onResume() {
        super.onResume();
        loadUsers(); // Загружаем список пользователей заново
    }

}
