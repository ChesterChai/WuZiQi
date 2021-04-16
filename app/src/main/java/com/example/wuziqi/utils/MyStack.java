package com.example.wuziqi.utils;

import java.util.Stack;

//pop和peek不抛异常，而是直接返回null
public class MyStack<T> extends Stack<T> {

    public synchronized T safePop() {
        try{
            return super.pop();
        }catch (Exception e){
            return null;
        }
    }

    public synchronized T safePeek() {
        try{
            return super.peek();
        }catch (Exception e){
            return null;
        }
    }
}
