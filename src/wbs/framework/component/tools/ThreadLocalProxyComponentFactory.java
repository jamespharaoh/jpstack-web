package wbs.framework.component.tools;

import static wbs.utils.etc.TypeUtils.genericCastUnchecked;
import static wbs.utils.string.StringUtils.stringEqualSafe;
import static wbs.utils.string.StringUtils.stringFormat;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

@PrototypeComponent ("threadLoaclProxyComponentFactory")
@Accessors (fluent = true)
public
class ThreadLocalProxyComponentFactory <ComponentType>
	implements
		ComponentFactory <ComponentType>,
		InvocationHandler {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// properties

	@Getter @Setter
	String componentName;

	@Getter @Setter
	Class <ComponentType> componentClass;

	// state

	ThreadLocal <Object> targets =
		new ThreadLocal<> ();

	// public implementation

	@Override
	public
	ComponentType makeComponent (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"makeComponent");

		) {

			return genericCastUnchecked (
				Proxy.newProxyInstance (
					componentClass.getClassLoader (),
					new Class <?> [] {
						componentClass,
						Control.class,
					},
					this));

		}

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
