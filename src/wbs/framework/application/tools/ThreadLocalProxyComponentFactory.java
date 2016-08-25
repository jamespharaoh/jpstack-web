package wbs.framework.application.tools;

import static wbs.framework.utils.etc.StringUtils.stringEqualSafe;
import static wbs.framework.utils.etc.StringUtils.stringFormat;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.framework.application.context.ComponentFactory;

@Accessors (fluent = true)
public
class ThreadLocalProxyComponentFactory
	implements
		ComponentFactory,
		InvocationHandler {

	@Getter @Setter
	String componentName;

	@Getter @Setter
	Class <?> componentClass;

	ThreadLocal <Object> targets =
		new ThreadLocal<> ();

	@Override
	public
	Object makeComponent () {

		return Proxy.newProxyInstance (
			componentClass.getClassLoader (),
			new Class <?> [] {
				componentClass,
				Control.class,
			},
			this);

	}

	@Override
	public
	Object invoke (
			Object proxy,
			Method method,
			Object[] args)
		throws Throwable {

		if (method.getDeclaringClass ().isAssignableFrom (componentClass)) {

			Object target =
				targets.get ();

			return method.invoke (
				target,
				args);

		}

		if (method.getDeclaringClass () == Control.class) {

			if (
				stringEqualSafe (
					method.getName (),
					"threadLocalProxySet")
			) {

				if (targets.get () != null) {

					throw new RuntimeException (
						stringFormat (
							"Tried to set target for %s twice without reset",
							componentName));

				}

				targets.set (args [0]);

				return null;

			}

			if (
				stringEqualSafe (
					method.getName (),
					"threadLocalProxyReset")
			) {

				if (targets.get () == null)
					throw new RuntimeException ();

				targets.remove ();

				return null;

			}

		}

		throw new RuntimeException (
			stringFormat (
				"Don't know how to handle %s.%s",
				method.getDeclaringClass ().getName (),
				method.getName ()));

	}

	public static
	interface Control {

		void threadLocalProxySet (
				Object target);

		void threadLocalProxyReset ();

	}

}
