package com.example.smarthomeapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smarthomeapp.R;
import com.example.smarthomeapp.models.Device;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder> {

    private List<Device> deviceList;
    private OnDeviceDeleteListener deleteListener;
    private OnDeviceSwitchListener switchListener;

    // Интерфейс для обработки удаления устройства
    public interface OnDeviceDeleteListener {
        void onDeviceDelete(int position);
    }

    // Интерфейс для обработки переключения устройства
    public interface OnDeviceSwitchListener {
        void onDeviceSwitch(int position, boolean isOn);
    }

    // Конструктор с передачей слушателей
    public DeviceAdapter(List<Device> deviceList, OnDeviceDeleteListener deleteListener,
                         OnDeviceSwitchListener switchListener) {
        this.deviceList = deviceList;
        this.deleteListener = deleteListener;
        this.switchListener = switchListener;
    }

    public static class DeviceViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;
        Switch powerSwitch;
        ImageView iconImageView;
        TextView statusTextView;
        ImageButton deleteButton;

        public DeviceViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.deviceName);
            powerSwitch = itemView.findViewById(R.id.deviceSwitch);
            iconImageView = itemView.findViewById(R.id.deviceIcon);
            statusTextView = itemView.findViewById(R.id.statusTextView);
            deleteButton = itemView.findViewById(R.id.deleteDeviceButton);
        }
    }

    @NonNull
    @Override
    public DeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_device_list, parent, false);
        return new DeviceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceViewHolder holder, int position) {
        Device device = deviceList.get(position);
        holder.nameTextView.setText(device.getName());

        // Отключаем слушатель перед изменением состояния, чтобы избежать рекурсии
        holder.powerSwitch.setOnCheckedChangeListener(null);
        holder.powerSwitch.setChecked(device.isOn());

        // Устанавливаем слушатель, который сообщает наружу о смене состояния
        holder.powerSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (switchListener != null) {
                switchListener.onDeviceSwitch(position, isChecked);
            }
        });

        // Иконка
        int iconResId = getIconForDeviceName(device.getName());
        holder.iconImageView.setImageResource(iconResId);

        // Таймер и статус
        if (device.isOn() && device.getRemainingTimeSeconds() > 0) {
            String timeLeft = formatTime(device.getRemainingTimeSeconds());
            holder.statusTextView.setText("Осталось: " + timeLeft);
            holder.statusTextView.setVisibility(View.VISIBLE);
        } else {
            holder.statusTextView.setVisibility(View.GONE);
        }

        // Кнопка удаления
        holder.deleteButton.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDeviceDelete(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return deviceList.size();
    }

    private int getIconForDeviceName(String name) {
        switch (name.toLowerCase()) {
            case "чайник":
                return R.drawable.ic_kettle;
            case "стиральная машина":
                return R.drawable.ic_washer;
            case "розетка":
                return R.drawable.ic_socket;
            case "динамик":
                return R.drawable.ic_speaker;
            case "робот-пылесос":
                return R.drawable.ic_robot_vacuum;
            default:
                return R.drawable.ic_kettle;
        }
    }

    private String formatTime(long totalSeconds) {
        long minutes = TimeUnit.SECONDS.toMinutes(totalSeconds);
        long seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
}
