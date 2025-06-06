package com.example.smarthomeapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.smarthomeapp.R;
import com.example.smarthomeapp.models.User;
import com.example.smarthomeapp.utils.UserManager;

import java.util.List;

public class RegisterActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private Button registerButton;
    private UserManager userManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        userManager = new UserManager(this);

        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        registerButton = findViewById(R.id.registerButton);

        registerButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Введите Email и пароль", Toast.LENGTH_SHORT).show();
                return;
            }

            // Проверим, что пользователь с таким email еще не существует
            List<User> users = userManager.getUsers();
            for (User user : users) {
                if (user.getEmail().equalsIgnoreCase(email)) {
                    Toast.makeText(this, "Пользователь с таким email уже существует", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            // Создаем нового пользователя и сохраняем
            User newUser = new User(email, password);
            users.add(newUser);
            userManager.saveUsers(users);

            // Устанавливаем текущего пользователя
            userManager.setCurrentUser(newUser);

            // Переходим на основной экран
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
