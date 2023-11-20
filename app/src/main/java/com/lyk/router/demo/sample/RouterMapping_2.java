package com.lyk.router.demo.sample;

import java.util.HashMap;
import java.util.Map;

public class RouterMapping_2 {

    public static Map<String, String> get() {

        Map<String, String> mapping = new HashMap<>();

        mapping.put("router://page-kotlin", "com.lyk.router.demo.KtMainActivity");
        mapping.put("router://page-home", "com.lyk.router.demo.MainActivity");
        return mapping;
    }
}
