package com.atguigu.gulimall.search.thread;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @ClassName ListTest
 * @Description: TODO
 * @Author fengjc
 * @Date 2021/1/28
 * @Version V1.0
 **/
public class ListTest {
    public static void main(String[] args) {
        List<String> strings =new ArrayList<>();
        int count =10000000;


//        for (int i = 0; i < count; i++) {
//            Map<String,String> map =new HashMap<>();
//            int a = (int) (Math.random()*100);
//            map.put("test"+a,"app"+a);
//            maps.add(map);
//        }
        List<Map<String,String>> maps =testMap();
        long startime = System.currentTimeMillis();
        System.out.println("插入成功");
        List<Map<String,String>> newList =distinct(maps);
        System.out.println(count + "数据量，耗时(毫秒):" + (System.currentTimeMillis() - startime));
        System.out.println(newList.size());
    }
    public static List<Map<String,String>> testMap(){
        List<Map<String,String>> maps =new ArrayList<>();
        Map<String,String> map1 =new HashMap<>();
        map1.put("test","test");
        map1.put("test2","test2");
        maps.add(map1);
        Map<String,String> map2 =new HashMap<>();
        map2.put("testzzz","testzzz");
        map2.put("test2","test2");
        maps.add(map2);
        Map<String,String> map3 =new HashMap<>();
        map3.put("test","test");
        map3.put("test2","test2");
        maps.add(map3);

        return maps;
    }

    public static <T> List<T> distinct(List<T> list) {
        if (list == null) {
            return null;
        } else {
//          List<T> result =new ArrayList<>(new HashSet<>(list));
            List<T> result =   list.stream().distinct().collect(Collectors.toList());
//            List<T> result = new ArrayList<>();
//            for (T item : list) {
//                if (!result.contains(item)) {
//                    result.add(item);
//                }
//            }
            return result;
        }
    }
}
