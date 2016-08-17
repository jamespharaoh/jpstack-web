package wbs.framework.object;

import static wbs.framework.utils.etc.Misc.classForNameRequired;
import static wbs.framework.utils.etc.Misc.ifElse;
import static wbs.framework.utils.etc.Misc.isNotNull;
import static wbs.framework.utils.etc.StringUtils.camelToUnderscore;
import static wbs.framework.utils.etc.StringUtils.capitalise;
import static wbs.framework.utils.etc.StringUtils.stringFormat;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Provider;

import com.google.common.collect.ImmutableList;

import lombok.Cleanup;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j;
import wbs.framework.activitymanager.ActiveTask;
import wbs.framework.activitymanager.ActivityManager;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.application.context.ApplicationContext;
import wbs.framework.application.context.NoSuchComponentException;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.entity.helper.EntityHelper;
import wbs.framework.entity.model.Model;
import wbs.framework.entity.model.ModelMethods;
import wbs.framework.record.Record;

@Accessors (fluent = true)
@SingletonComponent ("objectHelperBuilder")
@Log4j
public
class ObjectHelperBuilder {

	// dependencies

	@Inject
	ActivityManager activityManager;

	@Inject
	ApplicationContext applicationContext;

	@Inject
	Database database;

	@Inject
	EntityHelper entityHelper;

	@Inject
	ObjectTypeRegistry objectTypeRegistry;

	// prototype dependencies

	@Inject
	Provider<ObjectDatabaseHelper<?>> objectDatabaseHelperProvider;

	// component prototype dependencies

	@Inject
	Provider<ObjectHelperChildrenImplementation<?>>
	objectHelperChildrenImplementationProvider;

	@Inject
	Provider<ObjectHelperCodeImplementation<?>>
	objectHelperCodeImplementationProvider;

	@Inject
	Provider<ObjectHelperFindImplementation<?>>
	objectHelperFindImplementationProvider;

	@Inject
	Provider<ObjectHelperIdImplementation<?>>
	objectHelperIdImplementationProvider;

	@Inject
	Provider<ObjectHelperIndexImplementation<?>>
	objectHelperIndexImplementationProvider;

	@Inject
	Provider<ObjectHelperModelImplementation<?>>
	objectHelperModelImplementationProvider;

	@Inject
	Provider<ObjectHelperPropertyImplementation<?>>
	objectHelperPropertyImplementationProvider;

	@Inject
	Provider<ObjectHelperUpdateImplementation<?>>
	objectHelperUpdateImplementationProvider;

	// state

	List<ObjectHelper<?>> list =
		new ArrayList<ObjectHelper<?>> ();

	ObjectHelper<?> rootObjectHelper;

	Map<String,ObjectHelper<?>> byObjectName =
		new HashMap<String,ObjectHelper<?>> ();

	Map<Class<?>,ObjectHelper<?>> byObjectClass =
		new HashMap<Class<?>,ObjectHelper<?>> ();

	Map<Long,ObjectHelper<?>> byObjectTypeId =
		new HashMap<> ();

	Map<String,ObjectHelper<?>> byObjectTypeCode =
		new HashMap<String,ObjectHelper<?>> ();

	@Getter
	boolean ready = false;

	boolean dynamic;

	@PostConstruct
	@SuppressWarnings ({ "rawtypes", "unchecked" })
	public
	void init () {

		log.info (
			stringFormat (
				"Initialising"));

		@Cleanup
		Transaction transaction =
			database.beginReadOnly (
				"ObjectHelperBuilder.init ()",
				this);

		Map<String,ObjectTypeEntry> objectTypesByCode =
			objectTypeRegistry.findAll ().stream ()

			.collect (
				Collectors.toMap (
					objectType -> objectType.getCode (),
					objectType -> objectType));

		for (
			Model model
				: entityHelper.models ()
		) {

			ObjectTypeEntry objectType =
				objectTypesByCode.get (
					camelToUnderscore (
						model.objectName ()));

			// parent type

			Model parentModel =
				ifElse (
					model.isRooted (),

					() -> entityHelper.modelsByClass ().get (
						objectTypeRegistry.rootRecordClass ()),

					() -> ifElse (
						model.parentTypeIsFixed ()
						&& ! model.isRoot (),

						() -> entityHelper.modelsByClass ().get (
							model.parentClass ()),

						() -> null

					)
				);

			ObjectTypeEntry parentType =
				ifElse (
					model.parentTypeIsFixed ()
					&& ! model.isRoot (),

				() -> objectTypesByCode.get (
					camelToUnderscore (
						parentModel.objectName ())),

				() -> null


			);

			// dao implementation

			String daoImplementationBeanName =
				stringFormat (
					"%sDao",
					model.objectName ());

			Object daoImplementation =
				applicationContext.getComponentOrElse (
					daoImplementationBeanName,
					Object.class,
					() -> null);

			// dao interface

			Class<?> daoInterface =
				ifElse (
					isNotNull (
						daoImplementation),

				() -> classForNameRequired (
					stringFormat (
						"%s.%sDaoMethods",
						model.objectClass ().getPackage ().getName (),
						capitalise (
							model.objectName ()))),

				() -> null

			);

			// hooks implementation

			String hooksImplementationBeanName =
				stringFormat (
					"%sHooks",
					model.objectName ());

			ObjectHooks hooksImplementation =
				applicationContext.getComponentOrElse (
					hooksImplementationBeanName,
					ObjectHooks.class,
					() ->
						new ObjectHooks.DefaultImplementation ());

			// object model

			ObjectModel objectModel =
				new ObjectModelImplementation ()

				.model (
					model)

				.objectTypeId (
					objectType.getId ())

				.objectTypeCode (
					objectType.getCode ())

				.parentTypeId (
					ifElse (
						isNotNull (
							parentType),
						() -> parentType.getId (),
						() -> null))

				.parentClass (
					ifElse (
						isNotNull (
							parentType),
						() -> parentModel.objectClass (),
						() -> null))

				.daoImplementation (
					daoImplementation)

				.daoInterface (
					daoInterface)

				.hooks (
					hooksImplementation);

			ObjectDatabaseHelper<?> objectDatabaseHelper =
				objectDatabaseHelperProvider.get ()

				.model (
					objectModel);

			ObjectHelper<?> objectHelper =
				new Builder ()

				.model (
					objectModel)

				.objectTypesByCode (
					objectTypesByCode)

				.objectDatabaseHelper (
					objectDatabaseHelper)

				.build ();

			log.debug (
				stringFormat (
					"Build object helper for %s (%s, %s)",
					objectHelper.objectName (),
					objectHelper.objectClass ().getSimpleName (),
					objectHelper.objectTypeId ()));

			addToLists (
				objectHelper);

		}

		if (rootObjectHelper == null) {
			throw new RuntimeException ();
		}

		log.info (
			stringFormat (
				"Done"));

		ready = true;

	}

	void addToLists (
			ObjectHelper<?> objectHelper) {

		list.add (
			objectHelper);

		byObjectClass.put (
			objectHelper.objectClass (),
			objectHelper);

		byObjectName.put (
			objectHelper.objectName (),
			objectHelper);

		byObjectTypeId.put (
			objectHelper.objectTypeId (),
			objectHelper);

		byObjectTypeCode.put (
			objectHelper.objectTypeCode (),
			objectHelper);

		if (objectHelper.isRoot ()) {

			if (rootObjectHelper != null) {
				throw new RuntimeException ();
			}

			rootObjectHelper = objectHelper;

		}

	}

	public
	List<ObjectHelper<?>> asList () {

		return list;

	}

	public
	ObjectHelper<?> forObjectClassRequired (
			@NonNull Class<?> objectClassParam) {

		Class<?> objectClass =
			objectClassParam;

		while (
			Record.class.isAssignableFrom (
				objectClass)
		) {

			ObjectHelper<?> objectHelper =
				byObjectClass.get (objectClass);

			if (objectHelper != null)
				return objectHelper;

			objectClass =
				objectClass.getSuperclass ();

		}

		throw new RuntimeException (
			stringFormat (
				"Can't find ObjectHelper for %s",
				objectClassParam.getName ()));

	}

	public
	ObjectHelper<?> forObjectName (
			@NonNull String objectName) {

		ObjectHelper<?> objectHelper =
			byObjectName.get (
				objectName);

		if (objectHelper == null) {

			throw new IllegalArgumentException (
				stringFormat (
					"No object helper for name '%s'",
					objectName));

		}

		return objectHelper;

	}

	public
	ObjectHelper<?> forObjectTypeCode (
			@NonNull String objectTypeCode) {

		ObjectHelper<?> objectHelper =
			byObjectTypeCode.get (
				objectTypeCode);

		if (objectHelper == null) {

			throw new IllegalArgumentException (
				stringFormat (
					"No object helper for type code '%s'",
					objectTypeCode));

		}

		return objectHelper;

	}

	public
	ObjectHelper<?> forObjectTypeId (
			Long objectTypeId) {

		ObjectHelper<?> objectHelper =
			byObjectTypeId.get (
				objectTypeId);

		if (objectHelper == null) {

			throw new IllegalArgumentException (
				stringFormat (
					"No object helper for type id '%s'",
					objectTypeId));

		}

		return objectHelper;

	}

	@Accessors (fluent = true)
	@SuppressWarnings ("rawtypes")
	private
	class Builder
		implements ObjectHelperBuilderMethods {

		@Getter @Setter
		ObjectModel model;

		@Getter @Setter
		Map<String,ObjectTypeEntry> objectTypesByCode;

		@Getter @Setter
		ObjectDatabaseHelper<?> objectDatabaseHelper;

		ObjectHelperChildrenImplementation<?> childrenImplementation;
		ObjectHelperCodeImplementation<?> codeImplementation;
		ObjectHelperFindImplementation<?> findImplementation;
		ObjectHelperIdImplementation<?> idImplementation;
		ObjectHelperIndexImplementation<?> indexImplementation;
		ObjectHelperModelImplementation<?> modelImplementation;
		ObjectHelperPropertyImplementation<?> propertyImplementation;
		ObjectHelperUpdateImplementation<?> updateImplementation;

		List<ObjectHelperComponent> components;

		ObjectHelper<?> objectHelper;

		Class<?> extraInterface;
		Object extraImplementation;

		@SuppressWarnings ("unchecked")
		@SneakyThrows ({
			IllegalAccessException.class,
			InstantiationException.class,
			InvocationTargetException.class,
			NoSuchMethodException.class
		})
		public
		ObjectHelper<?> build () {

			@Cleanup
			ActiveTask activeTask =
				activityManager.start (
					"function",
					stringFormat (
						"%sObjectHelperBuilder.build ()",
						objectDatabaseHelper.model ().objectName ()),
					this);

			// extra methods

			String extraImplementationBeanName =
				stringFormat (
					"%sObjectHelperImplementation",
					model.objectName ());

			try {

				extraImplementation =
					applicationContext.getComponentRequired (
						extraImplementationBeanName,
						Object.class);

			} catch (NoSuchComponentException exception) {
			}

			try {

				String extraInterfaceName =
					stringFormat (
						"%s.%sObjectHelperMethods",
						model.objectClass ().getPackage ().getName (),
						capitalise (
							model.objectName ()));

				extraInterface =
					Class.forName (
						extraInterfaceName);

			} catch (ClassNotFoundException exception) {
			}

			// proxy class

			Class<?> proxyClass =
				Proxy.getProxyClass (
					model.helperClass ().getClassLoader (),
					model.helperClass (),
					ObjectHelperBuilderMethods.class);

			Constructor<?> constructor =
				proxyClass.getConstructor (
					InvocationHandler.class);

			components =
				ImmutableList.<ObjectHelperComponent>builder ()

				.add (
					childrenImplementation =
						objectHelperChildrenImplementationProvider.get ())

				.add (
					codeImplementation =
						objectHelperCodeImplementationProvider.get ())

				.add (
					findImplementation =
						objectHelperFindImplementationProvider.get ())

				.add (
					idImplementation =
						objectHelperIdImplementationProvider.get ())

				.add (
					indexImplementation =
						objectHelperIndexImplementationProvider.get ())

				.add (
					modelImplementation =
						objectHelperModelImplementationProvider.get ())

				.add (
					propertyImplementation =
						objectHelperPropertyImplementationProvider.get ())

				.add (
					updateImplementation =
						objectHelperUpdateImplementationProvider.get ())

				.build ();

			InvocationHandler invocationHandler =
				new InvocationHandlerImplementation ();

			objectHelper =
				model.helperClass ().cast (
					constructor.newInstance (
						invocationHandler));

			components.forEach (
				component ->
					component

				.objectHelper (
					objectHelper)

				.objectDatabaseHelper (
					objectDatabaseHelper)

				.model (
					model)

				.setup ()

			);

			// return

			return objectHelper;

		}

		private
		class InvocationHandlerImplementation
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

				try {

					if (
						declaringClass == ObjectHelperBuilderMethods.class
						|| declaringClass == Object.class
					) {

						return method.invoke (
							Builder.this,
							arguments);

					} else if (declaringClass == ObjectHelperChildrenMethods.class) {

						return method.invoke (
							childrenImplementation,
							arguments);

					} else if (declaringClass == ObjectHelperCodeMethods.class) {

						return method.invoke (
							codeImplementation,
							arguments);

					} else if (declaringClass == ObjectHelperFindMethods.class) {

						return method.invoke (
							findImplementation,
							arguments);

					} else if (declaringClass == ObjectHelperIdMethods.class) {

						return method.invoke (
							idImplementation,
							arguments);

					} else if (declaringClass == ObjectHelperIndexMethods.class) {

						return method.invoke (
							indexImplementation,
							arguments);

					} else if (declaringClass == ObjectHelperModelMethods.class) {

						return method.invoke (
							modelImplementation,
							arguments);

					} else if (declaringClass == ObjectHelperPropertyMethods.class) {

						return method.invoke (
							propertyImplementation,
							arguments);

					} else if (declaringClass == ObjectHelperUpdateMethods.class) {

						return method.invoke (
							updateImplementation,
							arguments);

					} else if (declaringClass == ModelMethods.class) {

						return method.invoke (
							model,
							arguments);

					} else if (declaringClass == extraInterface) {

						return method.invoke (
							extraImplementation,
							arguments);

					} else if (declaringClass == model.daoInterface ()) {

						return method.invoke (
							model.daoImplementation (),
							arguments);

					} else {

						throw new RuntimeException (
							stringFormat (
								"Don't know how to handle %s.%s",
								declaringClass.getName (),
								method.getName ()));

					}

				} catch (InvocationTargetException exception) {

					throw exception.getTargetException ();

				}

			}

		}

		@Override
		public
		ObjectHelperBuilderMethods objectManager (
				@NonNull ObjectManager objectManager) {

			components.forEach (
				component ->
					component.objectManager (
						objectManager));

			return this;

		}

	}

	public static
	interface ObjectHelperBuilderMethods {

		ObjectHelperBuilderMethods objectManager (
				ObjectManager objectManager);

	}

}
