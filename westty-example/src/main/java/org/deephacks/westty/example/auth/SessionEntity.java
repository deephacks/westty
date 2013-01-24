package org.deephacks.westty.example.auth;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.google.common.base.Objects;

@Entity
@Table(name = "EXAMPLE_SESSIONS")
public class SessionEntity {
    @Id
    @Column(name = "ID")
    private String id;

    public SessionEntity() {
        this.id = UUID.randomUUID().toString();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(SessionEntity.class).add("id", id).toString();
    }

}
