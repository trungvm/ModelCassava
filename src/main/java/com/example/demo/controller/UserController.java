package com.example.demo.controller;

import com.example.demo.data.ResponseDTO;
import com.example.demo.entity.User;
import com.example.demo.service.UserService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api")
public class UserController {
    @Autowired
    private UserService userService;
    @PostMapping("/creatAcount")
    public String  creatAcount(@RequestBody User user) throws ExecutionException, InterruptedException {
        return userService.creatAcount(user);
    }

    @PostMapping("/login")
    public ResponseDTO login(@RequestBody String input) throws ExecutionException, InterruptedException {
        try {
            JSONObject jsonData = new JSONObject(input);
            String email = jsonData.optString("email");
            String password = jsonData.optString("password");
            User user =  userService.getAccountByName(email);
            if ( user != null && user.getPassword().equals(password)) {
                return new ResponseDTO(true, "Đăng nhập thành công", user);
            } else
                return new ResponseDTO(false, "Đăng nhập không thành công", null);
        } catch (Exception e) {
            return new ResponseDTO(false, e.getMessage(), null);
        }
    }

}
