package com.sancai.oa.quartz.util;

import java.util.*;

public class TaskMessage {
    private static Map<String, List<String>> messages = new HashMap<>();
    private static List<String> exception = new ArrayList<>();
    private static Set<String> finished = new HashSet<>();

    public static void addMessage(String taskInstanceId,String message){
        List<String> msgList = messages.get(taskInstanceId);
        if(msgList == null){
            msgList = new ArrayList<>();
        }
        msgList.add(message);
        messages.put(taskInstanceId,msgList);
    }

    public static void finishMessage(String taskInstanceId){
        System.out.println("定时任务完成");
        finished.add(taskInstanceId);
    }

    public static boolean isFinishMessage(String taskInstanceId){
        return finished.contains(taskInstanceId);
    }

    public static List<String> getMessage(String taskInstanceId){
        return messages.get(taskInstanceId);
    }

    public static void clean(String taskInstanceId){
        messages.remove(taskInstanceId);
        exception = new ArrayList<>();
    }

    public static void addException(String message){
        exception.add(message);
    }
    public static List<String> getExceptions(){
        return exception;
    }

}
