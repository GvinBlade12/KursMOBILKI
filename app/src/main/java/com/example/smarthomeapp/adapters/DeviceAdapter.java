package com.example.smarthomeapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
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

    public DeviceAdapter(List<Device> deviceList) {
        this.deviceList = deviceList;
    }

    public static class DeviceViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;
        Switch powerSwitch;
        ImageView iconImageView;
        TextView statusTextView;

        public DeviceViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.deviceName);
            powerSwitch = itemView.findViewById(R.id.deviceSwitch);
            iconImageView = itemView.findViewById(R.id.deviceIcon);
            statusTextView = itemView.findViewById(R.id.statusTextView);
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

        // Сначала сбрасываем слушатель, чтобы не вызвать бесконечный цикл при setChecked
        holder.powerSwitch.setOnCheckedChangeListener(null);
        holder.powerSwitch.setChecked(device.isOn());

        holder.powerSwitch.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> {
            device.setOn(isChecked);

            // Если включаем, можно задать таймер (например, 5 минут)
            // Здесь логика таймера может быть внешней или внутри устройства — добавь если нужно

            notifyItemChanged(position);
        });

        // Подставляем иконку по названию устройства
        int iconResId = getIconForDeviceName(device.getName());
        holder.iconImageView.setImageResource(iconResId);

        // Отображаем таймер, если устройство включено и осталось время
        if (device.isOn() && device.getRemainingTimeSeconds() > 0) {
            String timeLeft = formatTime(device.getRemainingTimeSeconds());
            holder.statusTextView.setText("Осталось: " + timeLeft);
            holder.statusTextView.setVisibility(View.VISIBLE);
        } else {
            holder.statusTextView.setVisibility(View.GONE);
        }
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
