package com.example.demo.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.NoArgsConstructor;
public class WeatherRequest{
    @JsonProperty("Doy")
    public String doy;

    @JsonProperty("Rain")
    public String rain;

    @JsonProperty("dt")
    public String dt;

    @JsonProperty("Temp")
    public String temp;

    @JsonProperty("Radiation")
    public String radiation;

    @JsonProperty("Relative Humidity")
    public String relativeHumidity;

    @JsonProperty("Wind")
    public String wind;

    @JsonProperty("lat")
    public String lat;

    @JsonProperty("long")
    public String lon;

    @JsonProperty("elev")
    public String elev;

    @JsonProperty("height")
    public String height;

    @JsonProperty("irr\r")
    public String irr;
}
