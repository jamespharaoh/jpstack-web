package wbs.console.helper;

import static wbs.framework.utils.etc.Misc.capitalise;
import static wbs.framework.utils.etc.Misc.doNothing;
import static wbs.framework.utils.etc.Misc.isNotNull;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;

import javax.inject.Inject;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;

import com.google.common.base.Optional;

import wbs.console.context.ConsoleContextStuff;
import wbs.console.forms.EntityFinder;
import wbs.console.lookup.ObjectLookup;
import wbs.console.module.ConsoleMetaManager;
import wbs.console.request.ConsoleRequestContext;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.application.context.ApplicationContext;
import wbs.framework.application.context.NoSuchBeanException;
import wbs.framework.entity.model.ModelMethods;
import wbs.framework.object.ObjectHelper;
import wbs.framework.object.ObjectHelperMethods;
import wbs.framework.object.ObjectManager;
import wbs.framework.record.Record;
import wbs.framework.utils.etc.Html;

@Accessors (fluent = true)
@PrototypeComponent ("consoleHelperBuilder")
@SuppressWarnings ({ "rawtypes", "unchecked" })
public
class ConsoleHelperBuilder {

	// dependencies

	@Inject
	ApplicationContext applicationContext;

	@Inject
	ConsoleHelperProviderManager consoleHelperPoviderManager;

	@Inject
	ConsoleHelperRegistry consoleHelperRegistry;

	@Inject
	ConsoleMetaManager consoleMetaManager;

	@Inject
	ObjectManager objectManager;

	@Inject
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

		String extraImplementationBeanName =
			stringFormat (
				"%sObjectHelperImplementation",
				consoleHelperProvider.objectName ());

		try {

			extraImplementation =
				applicationContext.getBean (
					extraImplementationBeanName,
					Object.class);

		} catch (NoSuchBeanException exception) {
		}

		try {

			String extraInterfaceName =
				stringFormat (
					"%s.%sObjectHelperMethods",
					modelPackageName,
					capitalise (
						consoleHelperProvider.objectName ()));

			extraInterface =
				Class.forName (
					extraInterfaceName);

		} catch (ClassNotFoundException exception) {
		}

		// dao methods

		String daoImplementationBeanName =
			stringFormat (
				"%sDao",
				consoleHelperProvider.objectName ());

		try {

			daoImplementation =
				applicationContext.getBean (
					daoImplementationBeanName,
					Object.class);

		} catch (NoSuchBeanException exception) {
		}

		String daoMethodsInterfaceName =
			stringFormat (
				"%s.%sDaoMethods",
				modelPackageName,
				capitalise (
					consoleHelperProvider.objectName ()));

		try {

			daoMethodsInterface =
				Class.forName (
					daoMethodsInterfaceName);

		} catch (ClassNotFoundException exception) {
		}

		if (daoMethodsInterface != null
				&& daoImplementation == null) {

			throw new RuntimeException (
				stringFormat (
					"Found dao methods interface %s but no implementation bean %s",
					daoMethodsInterfaceName,
					daoImplementationBeanName));

		}

		// console hooks

		String consoleHooksBeanName =
			stringFormat (
				"%sConsoleHooks",
				consoleHelperProvider.objectName ());

		try {

			consoleHooks =
				applicationContext.getBean (
					consoleHooksBeanName,
					ConsoleHooks.class);

		} catch (NoSuchBeanException exception) {
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

			if (declaringClass == ObjectHelperMethods.class
					|| declaringClass == ModelMethods.class) {

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

			Integer id =
				(Integer)
				contextStuff.get (
					consoleHelperProvider.idKey ());

			if (id == null) {

				throw new RuntimeException (
					stringFormat (
						"Id key %s not present in context stuff",
						consoleHelperProvider.idKey ()));

			}

			Record<?> object =
				objectHelper.find (id);

			return object;

		}

	}

	private
	class EntityFinderImplementation
		implements EntityFinder {

		@Override
		public
		Record<?> findEntity (
				int id) {

			return objectHelper.find (id);

		}

		@Override
		public
		List<Record<?>> findEntities () {

			return
				(List<Record<?>>)
				(Object)
				objectHelper.findAll ();

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
				Record object) {

			return consoleHelperProvider.getPathId (
				object.getId ());

		}

		@Override
		public
		String getPathId (
				Integer objectId) {

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
		String getHtml (
				@NonNull Record object,
				@NonNull Optional assumedRoot,
				@NonNull Boolean mini) {

			if (consoleHooks != null) {

				Optional<String> optionalHtml =
					consoleHooks.getHtml (
						object);

				if (optionalHtml.isPresent ()) {
					return optionalHtml.get ();
				}

			}

			String path =
				objectManager.objectPath (
					object,
					assumedRoot,
					mini,
					false);

			StringBuilder stringBuilder =
				new StringBuilder ();

			if (objectHelper != null) {

				stringBuilder.append (
					stringFormat (
						"<a href=\"%h\">%h</a>",
						requestContext.resolveLocalUrl (
							getDefaultLocalPath (
								object)),
						path));

			} else {

				stringBuilder.append (
					Html.encode (path));

			}

			return stringBuilder.toString ();

		}

		@Override
		public
		Optional getListClass (
				Record object) {

			if (
				isNotNull (
					consoleHooks)
			) {

				return consoleHooks.getListClass (
					object);

			} else {

				return Optional.<String>absent ();

			}

		}

		@Override
		public
		void applySearchFilter (
				Object searchObject) {

			if (
				isNotNull (
					consoleHooks)
			) {

				consoleHooks.applySearchFilter (
					searchObject);

			} else {

				doNothing ();

			}

		}

		/*
		@Override
		public
		String contextNameForChild (
				Record object) {

			return consoleHelperProvider
				.contextNameForChild (object);

		}

		@Override
		public
		boolean hasListContext () {
			return consoleHelperProvider.hasListContext ();
		}

		@Override
		public
		boolean hasObjectContext () {
			return consoleHelperProvider.hasObjectContext ();
		}

		@Override
		public
		boolean hasBothContexts () {
			return consoleHelperProvider.hasBothContexts ();
		}

		@Override
		public
		String[] objectContextTypes () {
			return consoleHelperProvider.objectContextTypes ();
		}

		@Override
		public
		String[] listContextTypes () {
			return consoleHelperProvider.listContextTypes ();
		}

		@Override
		public
		List<Pair<String,String>> contextLinkSpecs () {
			return consoleHelperProvider.contextLinkSpecs ();
		}
		*/

	}

}
