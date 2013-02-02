/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.deephacks.westty;

import java.lang.reflect.InvocationTargetException;

public class Westty {
	private static final String WESTTY_CORE = "org.deephacks.westty.internal.core.WesttyCore";
	private static Object WESTTY;

	public static void main(String[] args) throws Throwable {
		Westty westty = new Westty();
		westty.startup();
	}

	public Westty() {
		if (WESTTY == null) {
			ClassLoader cl = Thread.currentThread().getContextClassLoader();
			try {
				WESTTY = cl.loadClass(WESTTY_CORE).newInstance();
			} catch (Exception e) {
				throw new RuntimeException();
			}
		}
	}

	public synchronized void startup() throws Throwable {
		call("startup");
	}

	public synchronized <V> V getInstance(Class<V> cls) throws Throwable {
		return (V) call("getInstance", cls);
	}

	public synchronized void stop() throws Throwable {
		call("shutdown");
	}

	private Object call(String method, Object... args) throws Throwable {
		try {
			if (args == null || args.length == 0) {
				return WESTTY.getClass().getMethod(method).invoke(WESTTY);
			} else {
				Class<?>[] classes = new Class<?>[args.length];
				for (int i = 0; i < args.length; i++) {
					classes[i] = args[i].getClass();
				}
				return WESTTY.getClass().getMethod(method, classes)
						.invoke(WESTTY, args);
			}
		} catch (InvocationTargetException e) {
			Throwable t = e.getTargetException();
			if (t instanceof ExceptionInInitializerError) {
				ExceptionInInitializerError err = (ExceptionInInitializerError) t;
				throw err.getCause();
			}
			throw t;

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
