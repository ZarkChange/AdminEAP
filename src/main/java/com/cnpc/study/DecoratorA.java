package com.cnpc.study;

public class DecoratorA extends Decorator {

    public DecoratorA(Component component) {

        super(component);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void operation() {

        // TODO Auto-generated method stub
        this.operation();
        System.out.println("i am Decorator A");
    }

}
