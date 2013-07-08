package org.deephacks.westty.jaxrs;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import org.deephacks.tools4j.config.internal.core.jpa.Jpa20BeanManager;
import org.deephacks.westty.datasource.DataSourceProperties;
import org.deephacks.westty.jaxrs.JaxrsConfigClient.HttpException;
import org.deephacks.westty.config.ServerConfig;
import org.deephacks.westty.test.SQLExec;
import org.deephacks.westty.test.WesttyJUnit4Runner;
import org.deephacks.westty.test.TestBootstrap;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ws.rs.core.Response.Status;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

@RunWith(WesttyJUnit4Runner.class)
public class JaxrsConfigEndpointTest {
    private static final String INSTALL_DDL = "install_config_derby.ddl";
    private static final String UNINSTALL_DDL = "uninstall_config_derby.ddl";
    private JaxrsConfigClient client = new JaxrsConfigClient("localhost", ServerConfig.DEFAULT_HTTP_PORT);
    private Parent p = new Parent();
    private Child c1 = new Child("c1", "v1");
    private Child c2 = new Child("c2", "v2");
    private Child c3 = new Child("c3", "v3");

    @TestBootstrap
    public static void bootstrap() throws IOException, SQLException {
        new JaxrsConfigEndpointTest().before();
    }

    @Before
    public void before(){
        try {
            DataSourceProperties p = new DataSourceProperties();
            SQLExec sql = new SQLExec(p.getUsername(), p.getPassword(), p.getUrl());
            List<String> install = readMetaInfResource(Jpa20BeanManager.class, INSTALL_DDL);
            sql.execute(install, false);
        } catch (Exception e) {
            // ignore
        }
    }

    @After
    public void after() throws IOException, SQLException {
        try {
            DataSourceProperties p = new DataSourceProperties();
            SQLExec sql = new SQLExec(p.getUsername(), p.getPassword(), p.getUrl());
            List<String> uninstall = readMetaInfResource(Jpa20BeanManager.class, UNINSTALL_DDL);
            sql.execute(uninstall, false);
        } catch (Exception e) {
            // ignore
        }
    }

    @Test
    public void test_get_singleton() {
        Parent p = client.getSingleton(Parent.class);
        assertNull(p.getValue());
        assertThat(p.getChildren().size(), is(0));

        client.create(new Parent("newvalue"));
        p = client.getSingleton(Parent.class);
        assertThat(p.getValue(), is("newvalue"));
        assertThat(p.getChildren().size(), is(0));
    }

    @Test
    public void test_get_regular() {
        client.create(c1);
        Child c = client.get(Child.class, c1.getId());
        assertThat(c.getId(), is(c1.getId()));
        assertThat(c.getValue(), is(c1.getValue()));

    }

    @Test
    public void test_create_parent_child_relationship() {
        p.put(c1, c2, c3);
        for (Child c : p.getChildren().values()){
            client.create(c);
        }
        client.create(p);
        p = client.getSingleton(Parent.class);

        assertThat(p.getChildren().size(), is(3));
        assertThat(p.get(c1.getId()).getValue(), is(c1.getValue()));
        assertThat(p.get(c2.getId()).getValue(), is(c2.getValue()));
        assertThat(p.get(c3.getId()).getValue(), is(c3.getValue()));
    }

    @Test
    public void test_list() {
        client.create(c1);
        client.create(c2);
        client.create(c3);

        List<Child> children = client.list(Child.class);
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
        client.create(p);
        p = client.getSingleton(Parent.class);

        assertThat(p.getChildren().size(), is(0));

        client.create(c1);
        client.create(c2);
        client.create(c3);

        p.put(c1, c2, c3);
        client.set(p);

        p = client.getSingleton(Parent.class);
        assertNull(p.getValue());
        assertThat(p.getChildren().size(), is(3));
        assertThat(p.get(c1.getId()).getValue(), is(c1.getValue()));
        assertThat(p.get(c2.getId()).getValue(), is(c2.getValue()));
        assertThat(p.get(c3.getId()).getValue(), is(c3.getValue()));
    }

    @Test
    public void test_merge_parent_child_relationship() {
        Parent p = new Parent("value");
        client.create(p);
        p = client.getSingleton(Parent.class);

        assertThat(p.getChildren().size(), is(0));

        client.create(c1);
        client.create(c2);
        client.create(c3);

        p.put(c1, c2, c3);
        client.merge(p);

        p = client.getSingleton(Parent.class);
        assertThat(p.getValue(), is("value"));
        assertThat(p.getChildren().size(), is(3));
        assertThat(p.get(c1.getId()).getValue(), is(c1.getValue()));
        assertThat(p.get(c2.getId()).getValue(), is(c2.getValue()));
        assertThat(p.get(c3.getId()).getValue(), is(c3.getValue()));
    }


    @Test
    public void test_exception_message() {
        try {
            client.get(Child.class, c1.getId());
        } catch (HttpException e) {
            assertThat(e.getCode(), is(Status.NOT_FOUND.getStatusCode()));
        }
    }

    @Test
    public void test_getschema() {
        Map<String, JaxrsSchema> schemas = client.getSchemas();
        JaxrsSchema schema = schemas.get(Child.class.getName());
        assertNotNull(schema);
        assertThat(schema.getClassName(), is(Child.class.getName()));

        schema = schemas.get(Parent.class.getName());
        assertNotNull(schema);
        assertThat(schema.getClassName(), is(Parent.class.getName()));

    }

    public static List<String> readMetaInfResource(Class<?> context, String filepath) {
        InputStream in = context.getResourceAsStream("/META-INF/" + filepath);
        ArrayList<String> list = new ArrayList<String>();
        try {
            String content = CharStreams.toString(new InputStreamReader(in, Charsets.UTF_8));
            list.addAll(Arrays.asList(content.split("\n")));
            return list;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
