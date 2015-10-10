package wbs.framework.application.tools;

import static wbs.framework.utils.etc.Misc.equal;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.framework.application.context.BeanFactory;

@Accessors (fluent = true)
public
class ThreadLocalProxyBeanFactory
	implements
		BeanFactory,
		InvocationHandler {

	@Getter @Setter
	String beanName;

	@Getter @Setter
	Class<?> beanClass;

	ThreadLocal<Object> targets =
		new ThreadLocal<Object> ();

	@Override
	public
	Object instantiate () {

		return Proxy.newProxyInstance (
			beanClass.getClassLoader (),
			new Class<?> [] { beanClass, Control.class },
			this);

	}

	@Override
	public
	Object invoke (
			Object proxy,
			Method method,
			Object[] args)
		throws Throwable {

		if (method.getDeclaringClass ().isAssignableFrom (beanClass)) {

			Object target =
				targets.get ();

			return method.invoke (
				target,
				args);

		}

		if (method.getDeclaringClass () == Control.class) {

			if (
				equal (
					method.getName (),
					"threadLocalProxySet")
			) {

				if (targets.get () != null) {

					throw new RuntimeException (
						stringFormat (
							"Tried to set target for %s twice without reset",
							beanName));

				}

				targets.set (args [0]);

				return null;

			}

			if (
				equal (
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
