package com.example.demo.entity;

import javax.annotation.Nullable;

public class CustomizedParameters {
    @Nullable
    String fieldName;
    public double fieldCapacity;
    public boolean autoIrrigation;
    public double irrigationDuration;
    public double dripRate;
    public double distanceBetweenHole;
    public double distanceBetweenRow;
    public double scaleRain;
    public double fertilizationLevel;
    public double acreage;
    public int numberOfHoles;

    @Nullable
    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(@Nullable String fieldName) {
        this.fieldName = fieldName;
    }

    public CustomizedParameters() {
    }
    public CustomizedParameters(double acreage, double fieldCapacity,
                                boolean autoIrrigation, double irrigationDuration,
                                double dripRate, double distanceBetweenHole,
                                double distanceBetweenRow, double scaleRain,
                                double fertilizationLevel, int numberOfHoles) {
        this.acreage = acreage;
        this.fieldCapacity = fieldCapacity;
        this.autoIrrigation = autoIrrigation;
        this.irrigationDuration = irrigationDuration;
        this.dripRate = dripRate;
        this.distanceBetweenHole = distanceBetweenHole;
        this.distanceBetweenRow = distanceBetweenRow;
        this.scaleRain = scaleRain;
        this.fertilizationLevel = fertilizationLevel;
        this.numberOfHoles = numberOfHoles;
    }


    public double getFieldCapacity() {
        return fieldCapacity;
    }

    public void setFieldCapacity(double fieldCapacity) {
        this.fieldCapacity = fieldCapacity;
    }

    public boolean isAutoIrrigation() {
        return autoIrrigation;
    }

    public void setAutoIrrigation(boolean autoIrrigation) {
        this.autoIrrigation = autoIrrigation;
    }

    public double getIrrigationDuration() {
        return irrigationDuration;
    }

    public void setIrrigationDuration(double irrigationDuration) {
        this.irrigationDuration = irrigationDuration;
    }

    public double getDripRate() {
        return dripRate;
    }

    public void setDripRate(double dripRate) {
        this.dripRate = dripRate;
    }

    public double getDistanceBetweenHole() {
        return distanceBetweenHole;
    }

    public void setDistanceBetweenHole(double distanceBetweenHole) {
        this.distanceBetweenHole = distanceBetweenHole;
    }

    public double getDistanceBetweenRow() {
        return distanceBetweenRow;
    }

    public void setDistanceBetweenRow(double distanceBetweenRow) {
        this.distanceBetweenRow = distanceBetweenRow;
    }

    public double getScaleRain() {
        return scaleRain;
    }

    public void setScaleRain(double scaleRain) {
        this.scaleRain = scaleRain;
    }

    public double getFertilizationLevel() {
        return fertilizationLevel;
    }

    public void setFertilizationLevel(double fertilizationLevel) {
        this.fertilizationLevel = fertilizationLevel;
    }

    public double getAcreage() {
        return acreage;
    }

    public void setAcreage(double acreage) {
        this.acreage = acreage;
    }

    public int getNumberOfHoles() {
        return numberOfHoles;
    }

    public void setNumberOfHoles(int numberOfHoles) {
        this.numberOfHoles = numberOfHoles;
    }

    public CustomizedParameters(String name) {
        this.acreage = 50;
        this.fieldCapacity = 70;
        this.distanceBetweenHole = 30;
        this.irrigationDuration = 7;
        this.distanceBetweenRow = 100;
        this.dripRate = 1.6;
        this.fertilizationLevel = 100;
        this.scaleRain = 100;
        this.numberOfHoles = 8;
        this.autoIrrigation = true;
    }

    public CustomizedParameters(CustomizedParameters customizedParameters) {
        this.acreage = customizedParameters.acreage;
        this.fieldCapacity = customizedParameters.fieldCapacity;
        this.distanceBetweenHole = customizedParameters.distanceBetweenHole;
        this.irrigationDuration = customizedParameters.irrigationDuration;
        this.distanceBetweenRow = customizedParameters.distanceBetweenRow;
        this.dripRate = customizedParameters.dripRate;
        this.fertilizationLevel = customizedParameters.fertilizationLevel;
        this.scaleRain = customizedParameters.scaleRain;
        this.numberOfHoles = customizedParameters.numberOfHoles;
        this.autoIrrigation = customizedParameters.autoIrrigation;
    }

}
