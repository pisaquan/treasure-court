package com.sancai.oa.dingding.clockin;

import javafx.util.Pair;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author chenm
 * @create 2019/8/3 17:09
 */
public class Test {

    public static void main(String[] args) {

        List<Pair> list = getIntervalTimeList(1563760215000L,1563846615000L,7);
        for (Pair s : list) {
            //System.out.println(s.getKey()+"-"+s.getValue());
        }
    }


    public static List<Pair> getIntervalTimeList(long start, long end, int day) {

        Instant startTimeInstant =  Instant.ofEpochMilli(start);
        Instant endTimeInstant = Instant.ofEpochMilli(end);

        List<Pair> list = new ArrayList<>();

        while (startTimeInstant.isBefore(endTimeInstant)) {

            LocalDateTime startTime =LocalDateTime.ofInstant(startTimeInstant, ZoneId.systemDefault());
            LocalDateTime endTime = startTime.plusDays(day);
            Instant endInstant = endTime.toInstant(ZoneOffset.ofHours(8));
            startTimeInstant = endInstant;

            if (endInstant.isAfter(endTimeInstant)) {
                Pair p = new Pair(startTime.toInstant(ZoneOffset.ofHours(8)).toEpochMilli(),endTimeInstant.toEpochMilli());
                list.add(p);
            } else {
                Pair p = new Pair(startTime.toInstant(ZoneOffset.ofHours(8)).toEpochMilli(),endInstant.toEpochMilli()-1);
                list.add(p);
            }
        }
        return list;
    }

}
