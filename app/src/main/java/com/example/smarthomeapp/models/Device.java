package com.example.smarthomeapp.models;

public class Device {

    private String name;
    private boolean isOn;
    private String type;
    private String status = "";

    private long timerStartTime = 0;         // время включения устройства (в миллисекундах)
    private long timerDurationSeconds = 0;   // длительность таймера в секундах

    public Device(String name, boolean isOn) {
        this.name = name;
        this.isOn = isOn;
        this.type = inferTypeFromName(name);
        this.timerDurationSeconds = getDefaultDurationForType(this.type);

        if (isOn) {
            this.timerStartTime = System.currentTimeMillis();
        }
    }

    public String getName() {
        return name;
    }

    public boolean isOn() {
        return isOn;
    }

    public void setOn(boolean on) {
        this.isOn = on;
        if (on) {
            timerStartTime = System.currentTimeMillis();
        } else {
            resetTimer();
        }
    }

    public String getType() {
        return type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getTimerDuration() {
        return timerDurationSeconds;
    }

    public long getRemainingTimeSeconds() {
        if (!isOn || timerStartTime == 0) return 0;
        long elapsed = (System.currentTimeMillis() - timerStartTime) / 1000;
        return Math.max(0, timerDurationSeconds - elapsed);
    }

    public boolean isTimerExpired() {
        if (!isOn || timerStartTime == 0 || timerDurationSeconds == 0) return false;
        long elapsed = (System.currentTimeMillis() - timerStartTime) / 1000;
        return elapsed >= timerDurationSeconds;
    }

    public void resetTimer() {
        timerStartTime = 0;
    }

    private String inferTypeFromName(String name) {
        switch (name.toLowerCase()) {
            case "чайник": return "kettle";
            case "стиральная машина": return "washer";
            case "розетка": return "socket";
            case "динамик": return "speaker";
            case "робот-пылесос": return "robot_vacuum";
            default: return "unknown";
        }
    }

    private long getDefaultDurationForType(String type) {
        switch (type) {
            case "kettle": return 5 * 60;          // 5 мин
            case "washer": return 90 * 60;         // 1.5 ч
            case "socket": return 30 * 60;         // 30 мин
            case "speaker": return 10 * 60;        // 10 мин
            case "robot_vacuum": return 45 * 60;   // 45 мин
            default: return 10 * 60;                // по умолчанию 10 мин
        }
    }
}
