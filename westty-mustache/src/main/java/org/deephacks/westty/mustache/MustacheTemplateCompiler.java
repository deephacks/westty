package org.deephacks.westty.mustache;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;

import javax.enterprise.context.spi.Context;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.deephacks.westty.spi.TemplateContext;
import org.deephacks.westty.spi.WesttyTemplateCompiler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.mustachejava.Code;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.DefaultMustacheVisitor;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheVisitor;
import com.github.mustachejava.codes.DefaultCode;

@Singleton
public class MustacheTemplateCompiler implements WesttyTemplateCompiler {
	private static final Logger log = LoggerFactory
			.getLogger(MustacheTemplateCompiler.class);
	private static final WesttyMustacheFactory mf = new WesttyMustacheFactory();

	@Inject
	private BeanManager bm;

	@Inject
	private ThreadPoolExecutor executor;

	@Override
	public boolean process(TemplateContext ctx) {
		ctx.getWriter();
		mf.setExecutorService(executor);
		WesttyMustache mustache = mf.compile(ctx);
		HashMap<String, Object> scopes = new HashMap<String, Object>();
		for (String var : mustache.getVariables()) {
			int dot = var.indexOf('.');
			if (dot != -1) {
				var = var.substring(0, dot);
			}
			final Context context = bm.getContext(Singleton.class);
			final Set<Bean<?>> beans = bm.getBeans(var);
			if (beans != null && !beans.isEmpty()) {
				@SuppressWarnings("unchecked")
				final Bean<Object> bean = (Bean<Object>) bm.resolve(beans);
				final CreationalContext<Object> creationalContext = bm
						.createCreationalContext(bean);
				Object instance = context.get(bean, creationalContext);
				scopes.put(var, instance);
			}

		}

		mustache.execute(ctx.getWriter(), scopes);

		try {
			ctx.getWriter().flush();
		} catch (IOException e) {
			log.warn("Unexpected exception when flushing writer.", e);
			return false;
		}
		return true;
	}

	private static class WesttyMustacheFactory extends DefaultMustacheFactory {
		private List<String> variables = new ArrayList<String>();
		private WesttyMustacheVisitor first;

		public WesttyMustacheFactory() {
			super();
		}

		@Override
		public MustacheVisitor createMustacheVisitor() {
			if (first == null) {
				return new WesttyMustacheVisitor(this, variables);
			} else {
				return new DefaultMustacheVisitor(this);
			}
		}

		public WesttyMustache compile(TemplateContext ctx) {
			return new WesttyMustache(super.compile(ctx.getReader(),
					ctx.getUri()), variables);
		}
	}

	private static class WesttyMustacheVisitor extends DefaultMustacheVisitor {
		private List<String> variables;

		public WesttyMustacheVisitor(DefaultMustacheFactory factory,
				List<String> variables) {
			super(factory);
			this.variables = variables;
		}

		@Override
		public void name(
				com.github.mustachejava.TemplateContext templateContext,
				String variable, Mustache mustache) {
			super.name(templateContext, variable, mustache);
		}

		@Override
		public void extend(
				com.github.mustachejava.TemplateContext templateContext,
				String variable, Mustache mustache) {
			super.extend(templateContext, variable, mustache);
		}

		@Override
		public void pragma(
				com.github.mustachejava.TemplateContext templateContext,
				String pragma, String args) {
			System.out.println(pragma);
			super.pragma(templateContext, pragma, args);
		}

		@Override
		public void iterable(
				com.github.mustachejava.TemplateContext templateContext,
				String variable, Mustache mustache) {
			variables.add(variable);
			super.iterable(templateContext, variable, mustache);
		}

		@Override
		public void partial(
				com.github.mustachejava.TemplateContext templateContext,
				String variable) {
			super.partial(templateContext, variable);
		};

		@Override
		public void value(
				com.github.mustachejava.TemplateContext templateContext,
				String variable, boolean encoded) {
			variables.add(variable);
			super.value(templateContext, variable, encoded);
		};

	}

	private static class WesttyMustache extends DefaultCode implements Mustache {
		private final Mustache mustache;
		private final List<String> variables;

		public WesttyMustache(Mustache mustache, List<String> variables) {
			this.mustache = mustache;
			this.variables = variables;
		}

		public void append(String text) {
			mustache.append(text);
		}

		public Object clone() {
			return mustache.clone();
		}

		public Writer execute(Writer writer, Object scope) {
			return mustache.execute(writer, scope);
		}

		public Writer execute(Writer writer, Object[] scopes) {
			return mustache.execute(writer, scopes);
		}

		public Code[] getCodes() {
			return mustache.getCodes();
		}

		public void identity(Writer writer) {
			mustache.identity(writer);
		}

		public void init() {
			mustache.init();
		}

		public void setCodes(Code[] codes) {
			mustache.setCodes(codes);
		}

		public Writer run(Writer writer, Object[] scopes) {
			return mustache.run(writer, scopes);
		}

		public List<String> getVariables() {
			return variables;
		}
	}

}
