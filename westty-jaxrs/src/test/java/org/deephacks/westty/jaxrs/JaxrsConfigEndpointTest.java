package org.deephacks.westty.jaxrs;

import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import com.google.common.io.CharStreams;
import org.deephacks.confit.ConfigContext;
import org.deephacks.confit.admin.AdminContext;
import org.deephacks.confit.internal.jpa.Jpa20BeanManager;
import org.deephacks.confit.jaxrs.AdminContextJaxrsProxy;
import org.deephacks.confit.model.Schema;
import org.deephacks.westty.config.DataSourceConfig;
import org.deephacks.westty.config.ServerConfig;
import org.deephacks.westty.internal.jaxrs.ResteasyHttpHandler;
import org.deephacks.westty.test.SQLExec;
import org.deephacks.westty.test.TestBootstrap;
import org.deephacks.westty.test.WesttyJUnit4Runner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

@RunWith(WesttyJUnit4Runner.class)
public class JaxrsConfigEndpointTest {
    private static final String INSTALL_DDL = "install_config_derby.ddl";
    private static final String UNINSTALL_DDL = "uninstall_config_derby.ddl";
    private AdminContext admin = AdminContextJaxrsProxy.get("localhost", ServerConfig.DEFAULT_HTTP_PORT, ResteasyHttpHandler.JAXRS_CONTEXT_URI);
    private Parent p = new Parent();
    private Child c1 = new Child("c1", "v1");
    private Child c2 = new Child("c2", "v2");
    private Child c3 = new Child("c3", "v3");
    private static ConfigContext config = ConfigContext.get();
    static {
        config.register(Child.class, Parent.class);
    }
    @TestBootstrap
    public static void bootstrap() throws IOException, SQLException {
        new JaxrsConfigEndpointTest().before();

    }

    @Before
    public void before(){
        try {
            DataSourceConfig p = new DataSourceConfig();
            SQLExec sql = new SQLExec(p.getUser(), p.getPassword(), p.getUrl());
            List<String> install = readMetaInfResource(Jpa20BeanManager.class, INSTALL_DDL);
            sql.execute(install, false);
        } catch (Exception e) {
            // ignore
        }
    }

    @After
    public void after() throws IOException, SQLException {
        try {
            DataSourceConfig p = new DataSourceConfig();
            SQLExec sql = new SQLExec(p.getUser(), p.getPassword(), p.getUrl());
            List<String> uninstall = readMetaInfResource(Jpa20BeanManager.class, UNINSTALL_DDL);
            sql.execute(uninstall, false);
        } catch (Exception e) {
            // ignore
        }
    }

    @Test
    public void test_get_singleton() {
        Optional<Parent> optional  = admin .get(Parent.class);
        assertTrue(optional.isPresent());
        p = optional.get();
        assertNull(p.getValue());
        assertThat(p.getChildren().size(), is(0));

        admin.createObject(new Parent("newvalue"));
        p = admin .get(Parent.class).get();
        assertThat(p.getValue(), is("newvalue"));
        assertThat(p.getChildren().size(), is(0));
    }

    @Test
    public void test_get_regular() {
        admin.createObject(c1);
        Child c = admin.get(Child.class, c1.getId()).get();
        assertThat(c.getId(), is(c1.getId()));
        assertThat(c.getValue(), is(c1.getValue()));

    }

    @Test
    public void test_create_parent_child_relationship() {
        p.put(c1, c2, c3);
        for (Child c : p.getChildren().values()){
            admin.createObject(c);
        }
        admin.createObject(p);
        p = admin .get(Parent.class).get();

        assertThat(p.getChildren().size(), is(3));
        assertThat(p.get(c1.getId()).getValue(), is(c1.getValue()));
        assertThat(p.get(c2.getId()).getValue(), is(c2.getValue()));
        assertThat(p.get(c3.getId()).getValue(), is(c3.getValue()));
    }

    @Test
    public void test_list() {
        admin.createObject(c1);
        admin.createObject(c2);
        admin.createObject(c3);

        Collection<Child> children = admin.list(Child.class);
        Map<String, Child> map = new HashMap<>();

        for (Child child : children) {
            map.put(child.getId(), child);
        }

        assertThat(children.size(), is(3));
        assertThat(map.get(c1.getId()).getValue(), is(c1.getValue()));
        assertThat(map.get(c2.getId()).getValue(), is(c2.getValue()));
        assertThat(map.get(c3.getId()).getValue(), is(c3.getValue()));

    }

    @Test
    public void test_set_parent_child_relationship() {
        admin.createObject(p);
        p = admin.get(Parent.class).get();

        assertThat(p.getChildren().size(), is(0));

        admin.createObject(c1);
        admin.createObject(c2);
        admin.createObject(c3);

        p.put(c1, c2, c3);
        admin.setObject(p);

        p = admin.get(Parent.class).get();
        assertNull(p.getValue());
        assertThat(p.getChildren().size(), is(3));
        assertThat(p.get(c1.getId()).getValue(), is(c1.getValue()));
        assertThat(p.get(c2.getId()).getValue(), is(c2.getValue()));
        assertThat(p.get(c3.getId()).getValue(), is(c3.getValue()));
    }

    @Test
    public void test_merge_parent_child_relationship() {
        Parent p = new Parent("value");
        admin.createObject(p);
        p = admin.get(Parent.class).get();

        assertThat(p.getChildren().size(), is(0));

        admin.createObject(c1);
        admin.createObject(c2);
        admin.createObject(c3);

        p.put(c1, c2, c3);
        admin.mergeObject(p);

        p = admin.get(Parent.class).get();
        assertThat(p.getValue(), is("value"));
        assertThat(p.getChildren().size(), is(3));
        assertThat(p.get(c1.getId()).getValue(), is(c1.getValue()));
        assertThat(p.get(c2.getId()).getValue(), is(c2.getValue()));
        assertThat(p.get(c3.getId()).getValue(), is(c3.getValue()));
    }


    @Test
    public void test_getschema() {
        Map<String, Schema> schemas = admin.getSchemas();
        Schema schema = schemas.get(Child.class.getName());
        assertNotNull(schema);
        assertThat(schema.getClassType().getName(), is(Child.class.getName()));

        schema = schemas.get(Parent.class.getName());
        assertNotNull(schema);
        assertThat(schema.getClassType().getName(), is(Parent.class.getName()));

    }

    public static List<String> readMetaInfResource(Class<?> context, String filepath) {
        InputStream in = context.getResourceAsStream("/META-INF/" + filepath);
        ArrayList<String> list = new ArrayList<>();
        try {
            String content = CharStreams.toString(new InputStreamReader(in, Charsets.UTF_8));
            list.addAll(Arrays.asList(content.split("\n")));
            return list;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
