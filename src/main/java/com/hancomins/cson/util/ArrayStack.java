package com.hancomins.cson.util;

import java.util.*;

public class ArrayStack<T>  {

    private static final int DEFAULT_CAPACITY = 16;

    private Object[] stack;

    private int top = -1;

    public ArrayStack() {
        this(DEFAULT_CAPACITY);
    }

    public ArrayStack(int capacity) {
        stack = new Object[capacity];
    }

    public ArrayStack<T> push(T value) {
        ++top;
        ensureCapacity(top);
        stack[top] = value;
        return this;
    }


    public T pop() {
        T value = poll();
        if(value == null) {
            throw new EmptyStackException();
        }
        return value;
    }

    @SuppressWarnings("unchecked")
    public T poll() {
        if(top < 0) {
            return null;
        }
        T value = (T)stack[top];
        stack[top] = null;
        --top;
        return value;
    }





    // This method is not used in the project

    public boolean isEmpty() {
        return top < 0;
    }

    @SuppressWarnings("unchecked")
    public T peek() {
        if(top < 0) {
            throw new EmptyStackException();
        }
        return (T)stack[top];
    }

    @SuppressWarnings("unchecked")
    public T top() {
        if(top < 0) {
            return null;
        }
        return (T)stack[top];
    }




    private void ensureCapacity(int capacity) {
        if(stack.length <= capacity) {
            int newCapacity = stack.length * 2;
            if(newCapacity < capacity) {
                newCapacity = capacity;
            }
            stack = Arrays.copyOf(stack, newCapacity);
        }
    }




    public void clear() {
        for(int i = 0; i <= top; ++i) {
            stack[i] = null;
        }
        top = -1;
    }





}
