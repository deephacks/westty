package org.deephacks.westty.test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.deephacks.westty.Westty;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;

public class WesttyJUnit4Runner extends BlockJUnit4ClassRunner {

    private final Class<?> cls;
    private Westty westty;

    public WesttyJUnit4Runner(final Class<?> cls) throws InitializationError {
        super(cls);
        this.cls = cls;
    }

    @Override
    protected Object createTest() throws Exception {
        try {
            for (Method m : cls.getDeclaredMethods()) {
                WesttyTestBootstrap bootstrap = m.getAnnotation(WesttyTestBootstrap.class);
                if (bootstrap == null) {
                    continue;
                }
                if (!Modifier.isStatic(m.getModifiers())) {
                    throw new IllegalStateException("WesttyTestBootstrap methods must be static.");
                }
                if (m.getParameterTypes().length > 0) {
                    throw new IllegalStateException(
                            "WesttyTestBootstrap methods must not have parameters.");
                }
                m.setAccessible(true);
                m.invoke(null, null);
            }
            if (westty == null) {
                this.westty = new Westty();
                try {
                    westty.startup();
                } catch (Throwable e) {
                    e.printStackTrace();
                    throw new InitializationError(e);
                }
            }

            return westty.getInstance(cls);
        } catch (Throwable e) {
            e.printStackTrace();
            throw new Exception(e);
        }
    }
}
