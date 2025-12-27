package com.meng.tools.normal;

public class Pair<T, U> {
    private T e1;

    public Pair(T e1, U e2) {
        this.e1 = e1;
        this.e2 = e2;
    }

    private U e2;

    public T getKey() {
        return e1;
    }

    public U getValue() {
        return e2;
    }

    @Override
    public String toString() {
        return "Pair{" + "e1=" + e1 + ", e2=" + e2 + '}';
    }
}
