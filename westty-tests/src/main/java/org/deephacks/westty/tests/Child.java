package org.deephacks.westty.tests;

import org.deephacks.confit.Config;
import org.deephacks.confit.Id;

@Config(desc="child")
public class Child {

    @Id(desc="id")
    private String id;

    @Config(desc="value")
    private String value;


    public Child() {

    }

    public Child(String id, String value){
        this.id = id;
        this.value = value;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}
