package org.deephacks.westty.mustache;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
@Named("users")
public class Test {
	public List<User> getUsers() {
		List<User> users = new ArrayList<User>();
		users.add(new User("stoffe", "stoffe@gmail.com"));
		users.add(new User("johnny", "johnny@gmail.com"));
		return users;
	}

	public static final class User {
		private String user;
		private String email;

		public User(String user, String email) {
			this.user = user;
			this.email = email;

		}

		public String getName() {
			return user;
		}

		public String getEmail() {
			return email;
		}
	}
}
