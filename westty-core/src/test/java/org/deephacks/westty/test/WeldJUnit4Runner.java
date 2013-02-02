package org.deephacks.westty.test;

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;

public class WeldJUnit4Runner extends BlockJUnit4ClassRunner {

    private final Class<?> klass;
    private final Weld weld;
    private final WeldContainer container;

    public WeldJUnit4Runner(final Class<?> klass) throws InitializationError {
        super(klass);

        this.klass = klass;
        this.weld = new Weld();
        try {
            this.container = weld.initialize();
        } catch (Exception e) {
            e.printStackTrace();
            throw new InitializationError(e);
        }
    }

    @Override
    protected Object createTest() throws Exception {
        try {
            return container.instance().select(klass).get();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
