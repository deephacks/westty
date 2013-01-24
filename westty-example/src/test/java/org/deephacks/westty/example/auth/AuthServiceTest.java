package org.deephacks.westty.example.auth;


public class AuthServiceTest {
    //    public static final String prop = "conf/jpa.properties";
    //    private static final JaxrsClient client = new JaxrsClient("localhost", 8080, "/jaxrs");
    //    @Test
    //    public void test() throws Exception {
    //        Westty westty = new Westty();
    //        try {
    //            DdlExec.executeResource("META-INF/uninstall_derby.ddl", prop, true);
    //            DdlExec.executeResource("META-INF/install_derby.ddl", prop, true);
    //            DdlExec.executeResource("META-INF/uninstall.ddl", prop, true);
    //            DdlExec.executeResource("META-INF/install.ddl", prop, true);
    //
    //            File file = new File("./src/main/resources");
    //            westty.setRootDir(file);
    //            westty.startup();
    //            String user = "kriss";
    //            String pw = "pw";
    //            createUser(user, pw);
    //            cookieLogin(user, pw);
    //            
    //            Thread.sleep(1000000);
    //        } catch (Throwable e) {
    //            e.printStackTrace();
    //        }
    //    }
    //    
    //    private static String sessionLogin(String username, String password){
    //        FormParam u = new FormParam("username", username);
    //        FormParam p = new FormParam("password", password);
    //        return client.postHttpForm("/auth-service/sessionLogin", u, p);
    //    }
    //
    //    private static String cookieLogin(String username, String password){
    //        FormParam u = new FormParam("username", username);
    //        FormParam p = new FormParam("password", password);
    //        return client.postHttpForm("/auth-service/cookieLogin", u, p);
    //    }
    //    
    //    private static String createUser(String username, String password){
    //        FormParam u = new FormParam("username", username);
    //        FormParam p = new FormParam("password", password);
    //        return client.postHttpForm("/auth-service/createUser", u, p);
    //    }
}
