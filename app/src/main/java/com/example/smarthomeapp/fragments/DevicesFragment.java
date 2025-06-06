package com.example.smarthomeapp.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smarthomeapp.R;
import com.example.smarthomeapp.adapters.DeviceAdapter;
import com.example.smarthomeapp.models.Device;
import com.example.smarthomeapp.utils.UserManager;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DevicesFragment extends Fragment {

    private SpeechRecognizer speechRecognizer;
    private Intent speechRecognizerIntent;
    private boolean isListening = false;


    private RecyclerView devicesRecyclerView;
    private Button addDeviceButton;
    private Button voiceCommandButton;

    private List<Device> deviceList;
    private DeviceAdapter adapter;

    private SharedPreferences prefs;
    private UserManager userManager;

    private Handler handler = new Handler();

    private final Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            adapter.notifyDataSetChanged();

            for (Device device : deviceList) {
                if (device.isOn() && device.isTimerExpired()) {
                    device.setOn(false);
                    device.resetTimer();
                    sendNotification(device.getName() + " был автоматически отключен по таймеру.");
                    saveDevices();
                }
            }

            if (!devicesRecyclerView.isComputingLayout()) {
                adapter.notifyDataSetChanged();
            } else {
                devicesRecyclerView.post(() -> adapter.notifyDataSetChanged());
            }

            handler.postDelayed(this, 1000);
        }
    };

    public static final String[] AVAILABLE_DEVICE_NAMES = {
            "Чайник", "Стиральная машина", "Розетка", "Динамик", "Робот-пылесос"
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        initSpeechRecognition();

        View view = inflater.inflate(R.layout.fragment_devices, container, false);

        devicesRecyclerView = view.findViewById(R.id.devicesRecyclerView);
        addDeviceButton = view.findViewById(R.id.addDeviceButton);
        voiceCommandButton = view.findViewById(R.id.voiceCommandButton);

        prefs = requireContext().getSharedPreferences("devices_pref", Context.MODE_PRIVATE);
        userManager = new UserManager(requireContext());

        deviceList = new ArrayList<>();
        adapter = new DeviceAdapter(deviceList);
        devicesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        devicesRecyclerView.setAdapter(adapter);

        loadDevices();
        handler.post(timerRunnable);

        addDeviceButton.setOnClickListener(v -> showDevicePickerDialog());

        voiceCommandButton.setOnClickListener(v -> startVoiceRecognition());

        return view;

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacks(timerRunnable);
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
            speechRecognizer = null;
        }

    }

    private void showDevicePickerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Выберите устройство");

        builder.setItems(AVAILABLE_DEVICE_NAMES, (dialog, which) -> {
            String selectedDevice = AVAILABLE_DEVICE_NAMES[which];

            for (Device d : deviceList) {
                if (d.getName().equals(selectedDevice)) {
                    return;
                }
            }

            deviceList.add(new Device(selectedDevice, false));
            adapter.notifyItemInserted(deviceList.size() - 1);
            saveDevices();
        });

        builder.setNegativeButton("Отмена", null);
        builder.create().show();
    }

    private void saveDevices() {
        String key = getDeviceStorageKey();
        String json = new Gson().toJson(deviceList);
        prefs.edit().putString(key, json).apply();
    }

    private void loadDevices() {
        String key = getDeviceStorageKey();
        String json = prefs.getString(key, "");
        if (!json.isEmpty()) {
            Type type = new TypeToken<List<Device>>() {}.getType();
            deviceList.clear();
            deviceList.addAll(new Gson().fromJson(json, type));
            adapter.notifyDataSetChanged();
        }
    }

    private String getDeviceStorageKey() {
        String email = userManager.getCurrentUser().getEmail();
        return "devices_" + email.replaceAll("[^a-zA-Z0-9]", "_");
    }

    private void sendNotification(String message) {
        Context context = requireContext();
        String channelId = "device_timer_channel";

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Уведомления устройств",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_device_notification)
                .setContentTitle("Устройство отключено")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
    }

    private void startVoiceRecognition() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ru-RU");
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Скажите команду...");

        try {
            startActivityForResult(intent, 1001);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getContext(), "Голосовое управление недоступно", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1001 && resultCode == Activity.RESULT_OK && data != null) {
            ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (results != null && !results.isEmpty()) {
                handleVoiceCommand(results.get(0).toLowerCase(Locale.ROOT));
            }
        }
    }

    private void handleVoiceCommand(String command) {
        for (Device device : deviceList) {
            String name = device.getName().toLowerCase();

            if (command.contains("включи") && command.contains(name)) {
                device.setOn(true);
                Toast.makeText(getContext(), "Включено: " + device.getName(), Toast.LENGTH_SHORT).show();
            } else if (command.contains("выключи") && command.contains(name)) {
                device.setOn(false);
                Toast.makeText(getContext(), "Выключено: " + device.getName(), Toast.LENGTH_SHORT).show();
            }
        }

        adapter.notifyDataSetChanged();
        saveDevices();
    }
    private void initSpeechRecognition() {
        if (SpeechRecognizer.isRecognitionAvailable(requireContext())) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(requireContext());
            speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ru-RU");

            speechRecognizer.setRecognitionListener(new RecognitionListener() {
                @Override public void onReadyForSpeech(Bundle params) {}
                @Override public void onBeginningOfSpeech() {}
                @Override public void onRmsChanged(float rmsdB) {}
                @Override public void onBufferReceived(byte[] buffer) {}
                @Override public void onEndOfSpeech() {}
                @Override public void onError(int error) { restartListening(); }

                @Override
                public void onResults(Bundle results) {
                    ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    if (matches != null && !matches.isEmpty()) {
                        handleVoiceCommand(matches.get(0).toLowerCase(Locale.ROOT));
                    }
                    restartListening();
                }

                @Override public void onPartialResults(Bundle partialResults) {}
                @Override public void onEvent(int eventType, Bundle params) {}
            });

            startListening();
        }
    }
    private void startListening() {
        if (!isListening && speechRecognizer != null) {
            isListening = true;
            speechRecognizer.startListening(speechRecognizerIntent);
        }
    }

    private void restartListening() {
        isListening = false;
        handler.postDelayed(this::startListening, 1000);
    }


}
