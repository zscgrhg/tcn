package com.example.make;

import com.example.exec.Handler;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by root on 17-4-30.
 */
public class LscpuHandler implements Handler<Map<String,String>> {

    private final Map<String,String> map=new HashMap<>();
    @Override
    public void onComplete(int processExitValue) {

    }

    @Override
    public void onStderrEnd() {

    }

    @Override
    public void onStdoutEnd() {

    }

    @Override
    public void receiveError(String line) {

    }

    @Override
    public void receive(String line) {
        int i = line.indexOf(":");
        if(i >0&&i<line.length()-1){
            String key=line.substring(0, i);
            String value=line.substring(i+1);
            map.put(key.trim(),value.trim());
        }

    }

    @Override
    public Map<String, String> get() {
        return map;
    }
}
