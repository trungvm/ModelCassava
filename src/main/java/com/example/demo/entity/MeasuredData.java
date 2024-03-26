package com.example.demo.entity;

import javax.persistence.Entity;

@Entity
public class MeasuredData {
    double rainFall;
    double relativeHumidity;
    double temperature;
    double windSpeed;
    float radiation;
    String time;


    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public MeasuredData(double rainFall, double relativeHumidity,
                        double temperature, double windSpeed, float radiation,
                        double soil30Humidity, double soil60Humidity, String time) {
        this.rainFall = rainFall;
        this.relativeHumidity = relativeHumidity;
        this.temperature = temperature;
        this.windSpeed = windSpeed;
        this.radiation = radiation;
        this.time = time;
    }

    public double getRainFall() {
        return rainFall;
    }

    public void setRainFall(double rainFall) {
        this.rainFall = rainFall;
    }

    public double getRelativeHumidity() {
        return relativeHumidity;
    }

    public void setRelativeHumidity(double relativeHumidity) {
        this.relativeHumidity = relativeHumidity;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public double getWindSpeed() {
        return windSpeed;
    }

    public void setWindSpeed(double windSpeed) {
        this.windSpeed = windSpeed;
    }

    public float getRadiation() {
        return radiation;
    }

    public void setRadiation(float radiation) {
        this.radiation = radiation;
    }
    public MeasuredData(){
    }


    public MeasuredData(String name) {
        this.rainFall = 0;
        this.relativeHumidity = 0;
        this.temperature = 0;
        this.windSpeed = 0;
        this.radiation = 0;
        this.time = "";
    }

    public MeasuredData(MeasuredData measuredData) {
        this.rainFall = measuredData.rainFall;
        this.relativeHumidity = measuredData.relativeHumidity;
        this.temperature = measuredData.temperature;
        this.windSpeed = measuredData.windSpeed;
        this.radiation = measuredData.radiation;
        this.time = measuredData.time;
    }
}
