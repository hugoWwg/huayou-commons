package com.huayou.commons.util;

import com.google.common.collect.Maps;

import java.util.Map;
import java.util.Random;

/**
 * 根据概率随机选择一个元素
 *
 * @author wwg
 */
public class RandomSelector<T> {

    private Map<T, Integer> items = Maps.newHashMap();
    private Random rand = new Random();
    private int totalSum = 0;

    private RandomSelector(Map<T, Integer> items) {
        this.items = items;
        for (Integer prob : items.values()) {
            totalSum = totalSum + prob;
        }
    }

    public static <T> RandomSelector<T> build(Map<T, Integer> items) {
        return new RandomSelector<T>(items);
    }

    public T getRandom() {
        if (null == items || items.isEmpty()) {
            return null;
        }
        int index = rand.nextInt(totalSum);
        int sum = -1;
        while (true) {
            for (Map.Entry<T, Integer> entry : items.entrySet()) {
                sum = sum + entry.getValue();
                if (sum >= index) {
                    return entry.getKey();
                }
            }
        }
    }

    //
    public static void main(String[] args) {
        Map<String, Integer> map = Maps.newHashMap();
        map.put("a", 1);
        map.put("b", 7);
        map.put("c", 1);
        RandomSelector<String> randomSelector = RandomSelector.build(map);
        for (int i = 0; i < 100; i++) {
            System.out.println(randomSelector.getRandom());
        }

    }
}
