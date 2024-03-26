package com.example.demo.entity;

import java.time.LocalDate;

public class IrrigationInformation {
    String time;
    double amount;
    double duration;

    public IrrigationInformation() {
    }

    public double getDuration() {
        return duration;
    }

    public void setDuration(double duration) {
        this.duration = duration;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public IrrigationInformation(String time, double amount, double duration) {
        this.time = time;
        this.amount = amount;
        this.duration = duration;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    @Override
    public String toString() {
        return "IrrigationInformation{" +
                "time='" + time + '\'' +
                ", amount=" + amount +
                '}';
    }
}
