package com.github.aop;


public class Computer {

    public <E> boolean isEmptyArray(E... array) {
        return null == array
                || array.length == 0;
    }

    public int sum(int... numberArray) {
        if (isEmptyArray(numberArray)) {
            return 0;
        }
        int sum = 0;
        for (int n : numberArray) {
            sum += n;
        }
        return sum;
    }

}
