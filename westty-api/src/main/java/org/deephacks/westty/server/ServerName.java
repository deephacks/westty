package org.deephacks.westty.server;

import javax.enterprise.inject.Alternative;

@Alternative
public class ServerName {
    private String name;

    public ServerName(String name){
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
