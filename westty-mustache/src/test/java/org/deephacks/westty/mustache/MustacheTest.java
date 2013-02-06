package org.deephacks.westty.mustache;

import java.io.File;
import java.io.FileReader;
import java.io.StringReader;
import java.io.StringWriter;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.deephacks.westty.properties.WesttyProperties;
import org.deephacks.westty.properties.WesttyPropertyBuilder;
import org.deephacks.westty.spi.TemplateContext;
import org.deephacks.westty.spi.WesttyTemplateCompiler;
import org.deephacks.westty.test.WesttyJUnit4Runner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(WesttyJUnit4Runner.class)
public class MustacheTest {
	static final StringBuilder SB = new StringBuilder();

	static {
		SB.append("{{name}}, {{feature.desc.test}}!");
		SB.append("{{#in_ca}} Well, ${{taxed_value}}, after taxes. {{/in_ca}}");
		SB.append("{{#nest1}} {{#nest2}} im nested {{/nest2}} {{/nest1}}");
		SB.append("From bean: {{test.name}}");
	}
	static final StringReader strReader = new StringReader(SB.toString());

	@WesttyPropertyBuilder(priority = 0)
	public static void prop(WesttyProperties prop) {
		prop.setHtmlDir(new File("./src/main/resources"));
	}

	@Inject
	private Instance<WesttyTemplateCompiler> compilers;

	@Test
	public void test() {
		try {
			Thread.sleep(10000000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		for (WesttyTemplateCompiler c : compilers) {
			try {
				File f = new File("./src/main/resources/index.html");
				if (!f.exists()) {
					System.out.println("sdkjsd!!!");
				}
				FileReader reader = new FileReader(f);
				StringWriter writer = new StringWriter();
				TemplateContext ctx = new TemplateContext(writer, reader,
						"example");
				if (c.process(ctx)) {
					System.out.println(ctx.getWriter().toString());
					break;
				}
				System.out.println("sdjkhsd");
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}
}
