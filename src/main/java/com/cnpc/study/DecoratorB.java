package com.cnpc.study;

public class DecoratorB extends Decorator {

    public DecoratorB(Component component) {

        super(component);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void operation() {

        // TODO Auto-generated method stub
        super.operation();
        System.out.println("i am Decorator B");
    }

}
