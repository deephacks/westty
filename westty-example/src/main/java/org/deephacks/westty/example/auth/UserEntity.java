package org.deephacks.westty.example.auth;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

@Entity
@Table(name = "EXAMPLE_USERS")
public class UserEntity {
    @Id
    @Column(name = "USERNAME")
    private String username;

    @Column(name = "PASSWORD")
    private String password;
    public UserEntity(){
    	
    }
    public UserEntity(String username, String password) {
        this.username = Preconditions.checkNotNull(username);
        this.password = Preconditions.checkNotNull(password);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(UserEntity.class).add("username", username)
                .add("password", password).toString();
    }

}
