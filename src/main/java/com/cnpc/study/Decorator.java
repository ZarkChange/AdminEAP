package com.cnpc.study;

public abstract class Decorator implements Component {

    protected Component component;

    public Decorator(Component component) {

        this.component = component;
    }

    public void operation() {

        System.out.println("I am Decorator");
    };
}
