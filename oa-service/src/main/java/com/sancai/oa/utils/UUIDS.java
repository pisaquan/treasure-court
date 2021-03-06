package com.sancai.oa.utils;

import java.util.UUID;

/**
 * UUIDS
 * @author  FANS
 **/

public class UUIDS {
    /*获得大写的UUID*/
    public static String genReqIDtoUppCase() {
        return UUID.randomUUID().toString().toUpperCase();
    }

    /*获得小写的UUID*/
    public static String getID() {
        return UUID.randomUUID().toString().replace("-", "").toLowerCase();
    }
    public static String genReqIDtoLowCase() {
        return UUID.randomUUID().toString().replace("-", "").toUpperCase();
    }

    public static void main(String[] args) {
        System.out.println(getID());
    }
}
