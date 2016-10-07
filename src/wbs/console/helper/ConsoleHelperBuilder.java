package wbs.console.helper;

import static wbs.utils.etc.LogicUtils.ifThenElse;
import static wbs.utils.etc.Misc.isNotNull;
import static wbs.utils.etc.OptionalUtils.optionalOrNull;
import static wbs.utils.etc.TypeUtils.classForName;
import static wbs.utils.etc.TypeUtils.classInSafe;
import static wbs.utils.string.StringUtils.capitalise;
import static wbs.utils.string.StringUtils.stringFormat;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;

import wbs.console.context.ConsoleContextStuff;
import wbs.console.forms.EntityFinder;
import wbs.console.lookup.ObjectLookup;
import wbs.console.module.ConsoleMetaManager;
import wbs.console.request.ConsoleRequestContext;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.manager.ComponentManager;
import wbs.framework.component.tools.NoSuchComponentException;
import wbs.framework.entity.model.ModelMethods;
import wbs.framework.entity.record.Record;
import wbs.framework.object.ObjectHelper;
import wbs.framework.object.ObjectHelperMethods;
import wbs.framework.object.ObjectManager;
import wbs.utils.string.FormatWriter;

@Accessors (fluent = true)
@PrototypeComponent ("consoleHelperBuilder")
@SuppressWarnings ({ "rawtypes", "unchecked" })
public
class ConsoleHelperBuilder {

	// singleton dependencies

	@SingletonDependency
	ComponentManager componentManager;

	@SingletonDependency
	ConsoleHelperProviderManager consoleHelperPoviderManager;

	@SingletonDependency
	ConsoleHelperRegistry consoleHelperRegistry;

	@SingletonDependency
	ConsoleMetaManager consoleMetaManager;

	@SingletonDependency
	ObjectManager objectManager;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	// required parameters

	@Getter @Setter
	ObjectHelper<?> objectHelper;

	@Getter @Setter
	Class<? extends ConsoleHelper<?>> consoleHelperClass;

	@Getter @Setter
	ConsoleHelperProvider<?> consoleHelperProvider;

	// state

	ConsoleHelperImplementation consoleHelperImplementation;
	EntityFinderImplementation entityFinderImplementation;
	ObjectLookupImplementation objectLookupImplementation;
	ConsoleHooks consoleHooks;

	Object extraImplementation;
	Class<?> extraInterface;

	Class<?> daoMethodsInterface;
	Object daoImplementation;

	@SneakyThrows ({
		IllegalAccessException.class,
		InstantiationException.class,
		InvocationTargetException.class,
		NoSuchMethodException.class
	})
	public
	ConsoleHelper<?> build () {

		Class<?> objectClass =
			consoleHelperProvider.objectClass ();

		String modelPackageName =
			objectClass.getPackage ().getName ();

		Class<?> proxyClass =
			Proxy.getProxyClass (
				consoleHelperClass ().getClassLoader (),
				consoleHelperClass);

		Constructor<?> constructor =
			proxyClass.getConstructor (
				InvocationHandler.class);

		consoleHelperImplementation =
			new ConsoleHelperImplementation ();

		entityFinderImplementation =
			new EntityFinderImplementation ();

		objectLookupImplementation =
			new ObjectLookupImplementation ();

		// extra methods

		String extraInterfaceName =
			stringFormat (
				"%s.%sObjectHelperMethods",
				modelPackageName,
				capitalise (
					consoleHelperProvider.objectName ()));

		extraInterface =
			optionalOrNull (
				classForName (
					extraInterfaceName));

		String extraImplementationBeanName =
			stringFormat (
				"%sObjectHelperMethodsImplementation",
				consoleHelperProvider.objectName ());

		extraImplementation =
			ifThenElse (
				isNotNull (
					extraInterface),
				() -> componentManager.getComponentRequired (
					extraImplementationBeanName,
					Object.class),
				() -> null);

		// dao methods

		String daoMethodsInterfaceName =
			stringFormat (
				"%s.%sDaoMethods",
				modelPackageName,
				capitalise (
					consoleHelperProvider.objectName ()));

		daoMethodsInterface =
			optionalOrNull (
				classForName (
					daoMethodsInterfaceName));

		String daoImplementationBeanName =
			stringFormat (
				"%sDao",
				consoleHelperProvider.objectName ());

		daoImplementation =
			ifThenElse (
				isNotNull (
					daoMethodsInterface),
				() -> componentManager.getComponentRequired (
					daoImplementationBeanName,
					Object.class),
				() -> null);

		// console hooks

		String consoleHooksBeanName =
			stringFormat (
				"%sConsoleHooks",
				consoleHelperProvider.objectName ());

		try {

			consoleHooks =
				componentManager.getComponentRequired (
					consoleHooksBeanName,
					ConsoleHooks.class);

		} catch (NoSuchComponentException exception) {

			consoleHooks =
				new ConsoleHooks.DefaultImplementation ();

		}

		// instance

		MyInvocationHandler invocationHandler =
			new MyInvocationHandler ();

		ConsoleHelper<?> consoleHelper =
			consoleHelperClass ().cast (
				constructor.newInstance (
					invocationHandler));

		return consoleHelper;

	}

	@Accessors (fluent = true)
	public
	class MyInvocationHandler
		implements InvocationHandler {

		@Override
		public
		Object invoke (
				Object target,
				Method method,
				Object[] arguments)
				throws Throwable {

			Class<?> declaringClass =
				method.getDeclaringClass ();

			if (
				classInSafe (
					declaringClass,
					objectHelperInterfaces)
			) {

				return method.invoke (
					objectHelper,
					arguments);

			} else if (declaringClass == ConsoleHelperMethods.class
					|| declaringClass == Object.class) {

				return method.invoke (
					consoleHelperImplementation,
					arguments);

			} else if (declaringClass == extraInterface) {

				return method.invoke (
					extraImplementation,
					arguments);

			} else if (declaringClass == daoMethodsInterface) {

				return method.invoke (
					daoImplementation,
					arguments);

			} else if (declaringClass == ObjectLookup.class) {

				return method.invoke (
					objectLookupImplementation,
					arguments);

			} else if (declaringClass == EntityFinder.class) {

				return method.invoke (
					entityFinderImplementation,
					arguments);

			} else {

				throw new RuntimeException (
					stringFormat (
						"Don't know how to handle %s.%s",
						declaringClass.getName (),
						method.getName ()));

			}

		}

	}

	private
	class ObjectLookupImplementation
		implements ObjectLookup<Record<?>> {

		@Override
		public
		Record<?> lookupObject (
				ConsoleContextStuff contextStuff) {

			Long id =
				(Long)
				contextStuff.get (
					consoleHelperProvider.idKey ());

			if (id == null) {

				throw new RuntimeException (
					stringFormat (
						"Id key %s not present in context stuff",
						consoleHelperProvider.idKey ()));

			}

			Record<?> object =
				objectHelper.findRequired (
					id);

			return object;

		}

	}

	private
	class EntityFinderImplementation
		implements EntityFinder {

		@Override
		public
		Class entityClass () {

			return objectHelper.objectClass ();

		}

		@Override
		public
		Record <?> findEntity (
				@NonNull Long id) {

			return optionalOrNull (
				objectHelper.find (
					id));

		}

		@Override
		public
		List <Record <?>> findAllEntities () {

			return (List <Record <?>>)
				objectHelper.findAll ();

		}

		@Override
		public
		Boolean isDeleted (
				Record entity) {

			return objectHelper.getDeleted (
				entity,
				true);

		}

	}

	private
	class ConsoleHelperImplementation
		implements ConsoleHelperMethods {

		@Override
		public
		String idKey () {

			return consoleHelperProvider
				.idKey ();

		}

		@Override
		public
		String getDefaultContextPath (
				Record object) {

			return consoleHelperProvider
				.getDefaultContextPath (object);

		}

		@Override
		public
		String getPathId (
				@NonNull Record object) {

			return consoleHelperProvider.getPathId (
				object.getId ());

		}

		@Override
		public
		String getPathId (
				@NonNull Long objectId) {

			return consoleHelperProvider.getPathId (
				objectId);

		}

		@Override
		public
		String getDefaultLocalPath (
				Record object) {

			return consoleHelperProvider.localPath (
				object);

		}

		@Override
		public
		boolean canView (
				Record object) {

			return consoleHelperProvider.canView (
				object);

		}

		@Override
		public
		ConsoleHooks consoleHooks () {

			return consoleHooks;

		}

		@Override
		public
		void writeHtml (
				@NonNull FormatWriter formatWriter,
				Record object,
				Optional assumedRoot,
				Boolean mini) {

			Optional <String> optionalHtml =
				consoleHooks.getHtml (
					object,
					mini);

			if (optionalHtml.isPresent ()) {

				formatWriter.writeLineFormat (
					"%s",
					optionalHtml.get ());

			} else {

				String path =
					objectManager.objectPath (
						object,
						assumedRoot,
						false,
						mini);

				formatWriter.writeLineFormat (
					"<a href=\"%h\">%h</a>",
					requestContext.resolveLocalUrl (
						getDefaultLocalPath (
							object)),
					path);

			}

		}

	}

	public final static
	Set <Class <?>> objectHelperInterfaces =
		ImmutableSet.<Class <?>> builder ()

		.add (
			ModelMethods.class)

		.addAll (
			Arrays.asList (
				ObjectHelperMethods.class.getInterfaces ()))

		.build ();

}
