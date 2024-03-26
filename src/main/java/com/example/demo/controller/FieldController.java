package com.example.demo.controller;

import com.example.demo.entity.*;
import com.example.demo.service.FieldService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api")
public class FieldController {
    @Autowired
    private FieldService fieldService;

    @PostMapping("/insertField")
    public String insertField(@RequestBody String field1) {
        return fieldService.insertMyField(field1);
    }

    @PostMapping("/getListField")
    public String getListField() {
//         CompletableFuture<String> future = fieldService.getListFieldNew();
//         return future.join();
        return fieldService.getFirebaseData();
    }

    @PostMapping("/getUpdateListField")
    public String getUpdateListField() {
        fieldService.updateFirebaseData();
//         CompletableFuture<String> future = fieldService.getListField();
        return fieldService.getFirebaseData();
        // return fieldService.getFieldsFromCache();
    }

    @PostMapping("/updateWeatherData")
    public void updateWeatherData() {
        try {
            fieldService.updateWeatherData("field2");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/updateHumidity")
    public void updateHumidity() {
        try {
            fieldService.updateHumidity("Field1");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/getModelField")
    public String getModelField(@RequestBody String fieldName) throws IOException {
        return fieldService.getModelField();
    }

    @PostMapping("/calculateModel")
    public String caculateModel(@RequestBody String fieldName) throws IOException {

        //return fieldService.calculateModel(fieldName);
        return fieldService.getModelField();
    }

    @PostMapping("/getWeatherData")
    public CompletableFuture<List<MeasuredData>> getWeatherData(@RequestBody String fieldName) {
        return fieldService.getWeatherData(fieldName);
    }

    @PostMapping("/deleteField")
    public void deleteField(@RequestBody String fieldName) {
        fieldService.deleteField(fieldName);
    }

    @PostMapping("/updateCustomizedParameters")
    public String updateCustomizedParameters(@RequestBody FieldDTO input) {
        return fieldService.updateCustomizedParameters(input);
    }

    @PostMapping("/setIrrigation")
    public String setIrrigation(@RequestBody String input) {
        return fieldService.setIrrigation(input);
    }

    @PostMapping("/getHistoryIrrigation")
    public CompletableFuture<List<HistoryIrrigation>> getHistoryIrrigation(@RequestBody String input) {
        return fieldService.getHistoryIrrigation(input);
    }

    @PostMapping("/getHumidity")
    public CompletableFuture<List<Humidity>> getHumidity(@RequestBody String input) {
        return fieldService.getHumidity(input);
    }

    @PostMapping("/getHumidityRecentTime")
    public CompletableFuture<Humidity> getHumidityRecentTime(@RequestBody String input) {
        return fieldService.getHumidityRecentTime(input);
    }

    @PostMapping("/getDisease")
    public CompletableFuture<List<Disease>> getDisease(@RequestBody String fieldName) {
        return fieldService.getDisease(fieldName);
    }

    @PostMapping("/getField")
    public CompletableFuture<FieldDTO> getField(@RequestBody String fieldName) {
        return fieldService.getField(fieldName);
    }
    @PostMapping("/calculateCSV")
    public String calculateCSV(@RequestBody List<WeatherRequest> weather) throws IOException {
        // Sử dụng ObjectMapper để chuyển đổi chuỗi JSON thành danh sách 2 chiều
        return fieldService.calculateCSV(weather);
    }
    public static List<List<Object>> convertJsonToList(String jsonString) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            // Sử dụng TypeReference để chuyển đổi JSON thành List<List<Object>>
            return objectMapper.readValue(jsonString, new TypeReference<List<List<Object>>>() {});
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    @PostMapping("/calculateExcel")
    public String calculateExcel() throws IOException {
        // Sử dụng ObjectMapper để chuyển đổi chuỗi JSON thành danh sách 2 chiều
        return fieldService.calculateExcel();
    }

}
