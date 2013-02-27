package org.deephacks.westty.test;

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;

public class WeldJUnit4Runner extends BlockJUnit4ClassRunner {

    private Class<?> cls;
    private Weld weld;
    private WeldContainer container;

    public WeldJUnit4Runner(final Class<?> klass) throws InitializationError {
        super(klass);
        this.cls = klass;
    }

    @Override
    protected Object createTest() throws Exception {
        try {
            this.weld = new Weld();
            try {
                this.container = weld.initialize();
            } catch (Exception e) {
                e.printStackTrace();
                throw new InitializationError(e);
            }
            return container.instance().select(cls).get();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
