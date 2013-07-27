package org.deephacks.westty.tests;

import org.deephacks.confit.Config;

import java.util.HashMap;
import java.util.Map;

@Config(desc="test")
public class Parent {

    @Config(desc="children")
    private Map<String, Child> children = new HashMap<>();

    @Config(desc="value")
    private String value;

    public Parent(){

    }

    public Parent(String value){
        this.value = value;
    }

    public void put(Child... child){
        for (int i = 0; i < child.length; i++) {
            children.put(child[i].getId(), child[i]);
        }
    }

    public Child get(String id){
        return children.get(id);
    }

    public void setChildren(Map<String, Child> children) {
        this.children = children;
    }

    public Map<String, Child> getChildren() {
        return children;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
