package com.cnpc.study;

public class Main {

    public static void main(String[] args) {

        Component a1 = new ConcreateComponent();

        Decorator d = new DecoratorA(new DecoratorB(new DecoratorC(a1)));
        d.operation();

    }
}
