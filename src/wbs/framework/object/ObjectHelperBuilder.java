package wbs.framework.object;

import static wbs.framework.utils.etc.Misc.camelToSpaces;
import static wbs.framework.utils.etc.Misc.capitalise;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import lombok.Cleanup;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.application.context.ApplicationContext;
import wbs.framework.application.context.NoSuchBeanException;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.entity.model.ModelMethods;
import wbs.framework.record.CommonRecord;
import wbs.framework.record.EphemeralRecord;
import wbs.framework.record.GlobalId;
import wbs.framework.record.MajorRecord;
import wbs.framework.record.MinorRecord;
import wbs.framework.record.Record;

import com.google.common.collect.ImmutableList;

@Accessors (fluent = true)
@SingletonComponent ("objectHelperBuilder")
@Log4j
public
class ObjectHelperBuilder {

	@Inject
	ApplicationContext applicationContext;

	@Inject
	Database database;

	@Inject
	ObjectTypeRegistry objectTypeRegistry;

	@Inject
	ObjectHelperProviderManager objectHelperProviderManager;

	List<ObjectHelper<?>> list =
		new ArrayList<ObjectHelper<?>> ();

	ObjectHelper<?> rootObjectHelper;

	Map<String,ObjectHelper<?>> byObjectName =
		new HashMap<String,ObjectHelper<?>> ();

	Map<Class<?>,ObjectHelper<?>> byObjectClass =
		new HashMap<Class<?>,ObjectHelper<?>> ();

	Map<Integer,ObjectHelper<?>> byObjectTypeId =
		new HashMap<Integer,ObjectHelper<?>> ();

	Map<String,ObjectHelper<?>> byObjectTypeCode =
		new HashMap<String,ObjectHelper<?>> ();

	@Getter
	boolean ready = false;
	
	boolean dynamic;

	@PostConstruct
	public
	void init () {

		log.info (
			stringFormat (
				"Initialising"));

		@Cleanup
		Transaction transaction =
			database.beginReadOnly ();

		for (ObjectHelperProvider objectHelperProvider
				: objectHelperProviderManager.list ()) {

			ObjectHelper<?> objectHelper =
				new Builder ()
					.objectHelperProvider (objectHelperProvider)
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

		if (objectHelper.root ())
			rootObjectHelper = objectHelper;

	}

	public
	List<ObjectHelper<?>> asList () {

		return list;

	}

	public
	ObjectHelper<?> forObjectClass (
			Class<?> objectClassParam) {

		Class<?> objectClass =
			objectClassParam;

		while (Record.class.isAssignableFrom (objectClass)) {

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
			Integer objectTypeId) {

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
	@Data
	private
	class Builder {

		@Getter @Setter
		ObjectHelperProvider objectHelperProvider;

		String objectTypeCode;
		Integer objectTypeId;

		String parentTypeCode;
		Integer parentTypeId;

		ObjectHelperMethodsImplementation coreImplementation;

		ObjectHelper<?> objectHelper;

		Class<?> extraInterface;
		Object extraImplementation;

		Class<?> daoMethodsInterface;
		Object daoImplementation;

		@SneakyThrows ({
			IllegalAccessException.class,
			InstantiationException.class,
			InvocationTargetException.class,
			NoSuchMethodException.class
		})
		public
		ObjectHelper<?> build () {

			ObjectTypeEntry objectType =
				objectTypeRegistry.findByCode (
					objectHelperProvider.objectTypeCode ());

			objectTypeCode =
				objectType.getCode ();

			objectTypeId =
				objectType.getId ();

			if (
				objectHelperProvider.parentTypeIsFixed ()
				&& ! objectHelperProvider.root ()
			) {

				ObjectHelperProvider parentHelperProvider =
					objectHelperProviderManager.forObjectClass (
						objectHelperProvider.parentClass ());

				if (parentHelperProvider == null) {

					throw new RuntimeException (
						stringFormat (
							"No helper provider for parent class %s of %s",
							objectHelperProvider.parentClass ().getSimpleName (),
							objectHelperProvider.objectName ()));

				}

				ObjectTypeEntry parentType =
					objectTypeRegistry.findByCode (
						parentHelperProvider.objectTypeCode ());

				parentTypeCode =
					parentType.getCode ();

				parentTypeId =
					parentType.getId ();

			}

			// extra methods

			String extraImplementationBeanName =
				stringFormat (
					"%sObjectHelperImplementation",
					objectHelperProvider.objectName ());

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
						"%s$%sObjectHelperMethods",
						objectHelperProvider.objectClass ().getName (),
						capitalise (objectHelperProvider.objectName ()));

				extraInterface =
					Class.forName (
						extraInterfaceName);

			} catch (ClassNotFoundException exception) {
			}

			// dao methods

			String daoImplementationBeanName =
				stringFormat (
					"%sDao",
					objectHelperProvider.objectName ());

			try {

				daoImplementation =
					applicationContext.getBean (
						daoImplementationBeanName,
						Object.class);

			} catch (NoSuchBeanException exception) {
			}

			String daoMethodsInterfaceName =
				stringFormat (
					"%s$%sDaoMethods",
					objectHelperProvider.objectClass ().getName (),
					capitalise (objectHelperProvider.objectName ()));

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

			// proxy class

			Class<?> proxyClass =
				Proxy.getProxyClass (
					objectHelperProvider.helperClass ().getClassLoader (),
					objectHelperProvider.helperClass ());

			Constructor<?> constructor =
				proxyClass.getConstructor (
					InvocationHandler.class);

			coreImplementation =
				new ObjectHelperMethodsImplementation ();

			InvocationHandler invocationHandler =
				new InvocationHandlerImplementation ();

			objectHelper =
				objectHelperProvider.helperClass ().cast (
					constructor.newInstance (
						invocationHandler));

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

				if (declaringClass == ObjectHelperMethods.class) {

					return method.invoke (
						coreImplementation,
						arguments);

				} else if (declaringClass == ModelMethods.class) {

					return method.invoke (
						objectHelperProvider.model (),
						arguments);

				} else if (declaringClass == extraInterface) {

					return method.invoke (
						extraImplementation,
						arguments);

				} else if (declaringClass == daoMethodsInterface) {

					return method.invoke (
						daoImplementation,
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

		@Accessors (fluent = true)
		@Data
		@SuppressWarnings ("rawtypes")
		private
		class ObjectHelperMethodsImplementation
			implements ObjectHelperMethods {

			@Override
			public
			Class<?> objectClass () {
				return objectHelperProvider.objectClass ();

			}

			@Override
			public
			String objectTypeCode () {
				return objectHelperProvider.objectTypeCode ();
			}

			@Override
			public
			Integer objectTypeId () {
				return objectTypeId;
			}

			@Override
			public
			Class<?> parentClass () {
				return objectHelperProvider.parentClass ();
			}

			@Override
			public
			String parentFieldName () {
				return objectHelperProvider.parentFieldName ();
			}

			@Override
			public
			String parentLabel () {
				return objectHelperProvider.parentLabel ();
			}

			@Override
			public
			Boolean parentExists () {
				return objectHelperProvider.parentExists ();
			}

			@Override
			public
			String typeCodeFieldName () {
				return objectHelperProvider.typeCodeFieldName ();
			}

			@Override
			public
			String typeCodeLabel () {
				return objectHelperProvider.typeCodeLabel ();
			}

			@Override
			public
			Boolean typeCodeExists () {
				return objectHelperProvider.typeCodeExists ();
			}

			@Override
			public
			String codeFieldName () {
				return objectHelperProvider.codeFieldName ();
			}

			@Override
			public
			String codeLabel () {
				return objectHelperProvider.codeLabel ();
			}

			@Override
			public
			Boolean codeExists () {
				return objectHelperProvider.codeExists ();
			}

			@Override
			public
			String indexFieldName () {
				return objectHelperProvider.indexFieldName ();
			}

			@Override
			public
			String indexLabel () {
				return objectHelperProvider.indexLabel ();
			}

			@Override
			public
			Boolean indexExists () {
				return objectHelperProvider.indexExists ();
			}

			@Override
			public
			String indexCounterFieldName () {
				return objectHelperProvider.indexCounterFieldName ();
			}

			@Override
			public
			String deletedFieldName () {
				return objectHelperProvider.deletedFieldName ();
			}

			@Override
			public
			String deletedLabel () {
				return objectHelperProvider.deletedLabel ();
			}

			@Override
			public
			Boolean deletedExists () {
				return objectHelperProvider.deletedExists ();
			}

			@Override
			public
			String descriptionFieldName () {
				return objectHelperProvider.descriptionFieldName ();
			}

			@Override
			public
			String descriptionLabel () {
				return objectHelperProvider.descriptionLabel ();
			}

			@Override
			public
			Boolean descriptionExists () {
				return objectHelperProvider.descriptionExists ();
			}

			@Override
			public
			String nameFieldName () {
				return objectHelperProvider.nameFieldName ();
			}

			@Override
			public
			String nameLabel () {
				return objectHelperProvider.nameLabel ();
			}

			@Override
			public
			Boolean nameExists () {
				return objectHelperProvider.nameExists ();
			}

			@Override
			public
			Boolean nameIsCode () {
				return objectHelperProvider.nameIsCode ();
			}

			@Override
			public
			boolean major () {
				return MajorRecord.class.isAssignableFrom (
					objectClass ());
			}

			@Override
			public
			boolean minor () {
				return MinorRecord.class.isAssignableFrom (
					objectClass ());
			}

			@Override
			public
			boolean ephemeral () {
				return EphemeralRecord.class.isAssignableFrom (
					objectClass ());
			}

			@Override
			public
			boolean common () {
				return CommonRecord.class.isAssignableFrom (
					objectClass ());
			}

			@Override
			public
			Record<?> find (
					long id) {

				return objectHelperProvider
					.find (id);

			}

			@Override
			public
			Record<?> findByCode (
					@NonNull GlobalId ancestorGlobalId,
					@NonNull String... codes) {

				if (codes.length == 1) {

					return objectHelperProvider.findByParentAndCode (
						ancestorGlobalId,
						codes [0]);

				}

				if (codes.length > 1) {

					ObjectHelper<?> parentHelper =
						forObjectClass (
							parentClass ());

					Record<?> parent =
						parentHelper.findByCode (
							ancestorGlobalId,
							Arrays.copyOfRange (
								codes,
								0,
								codes.length - 1));

					GlobalId parentGlobalId =
						new GlobalId (
							parentHelper.objectTypeId (),
							parent.getId ());

					return objectHelperProvider.findByParentAndCode (
						parentGlobalId,
						codes [1]);

				}

				throw new IllegalArgumentException (
					"codes");

			}

			@Override
			public
			Record findByIndex (
					@NonNull Record parent,
					@NonNull Integer index) {

				ObjectHelper<?> parentHelper =
					forObjectClass (
						parent.getClass ());

				GlobalId parentGlobalId =
					new GlobalId (
						parentHelper.objectTypeId (),
						parent.getId ());

				return objectHelperProvider.findByParentAndIndex (
					parentGlobalId,
					index);

			}

			@Override
			public
			List<Record<?>> findAll () {

				return objectHelperProvider
					.findAll ();

			}

			@Override
			public
			List<Record<?>> findByParent (
					GlobalId parentGlobalId) {

				return objectHelperProvider
					.findAllByParent (parentGlobalId);

			}

			@Override
			public
			List findByParentAndType (
					@NonNull Record parent,
					@NonNull String typeCode) {

				ObjectHelper<?> parentHelper =
					forObjectClass (parent.getClass ());

				GlobalId parentGlobalId =
					new GlobalId (
						parentHelper.objectTypeId (),
						parent.getId ());

				return objectHelperProvider.findAllByParentAndType (
					parentGlobalId,
					typeCode);

			}

			@Override
			public
			Record insert (
					@NonNull Record object) {

				if (! objectClass ().isInstance (
						object)) {

					throw new ClassCastException (
						stringFormat (
							"Can't insert %s as %s",
							object.getClass ().getSimpleName (),
							objectClass ().getSimpleName ()));

				}

				objectHelperProvider.insert (
					object);

				for (ObjectHelper childObjectHelper
						: list) {

					ObjectHelperProvider childObjectHelperProvider =
						childObjectHelper.objectHelperProvider ();

					childObjectHelperProvider.createSingletons (
						childObjectHelper,
						objectHelper,
						object);

				}

				return object;

			}

			@Override
			public
			void setParent (
					@NonNull Record object,
					@NonNull Record parent) {

				objectHelperProvider.setParent (
					object,
					parent);

			}

			@Override
			public
			String getName (
					@NonNull Record object) {

				return objectHelperProvider.getName (
					object);

			}

			@Override
			public
			String getTypeCode (
					@NonNull Record object) {

				return objectHelperProvider
					.getTypeCode (object);

			}

			@Override
			public
			String getCode (
					@NonNull Record object) {

				return objectHelperProvider
					.getCode (object);

			}

			@Override
			public
			String getDescription (
					@NonNull Record object) {

				return objectHelperProvider
					.getDescription (object);

			}

			@Override
			public
			Record getParentObjectType (
					@NonNull Record object) {

				return objectHelperProvider
					.getParentType (object);

			}

			@Override
			public
			Integer getParentTypeId (
					@NonNull Record object) {

				if (! objectClass ().isInstance (object))
					throw new IllegalArgumentException ();

				if (root ())
					throw new UnsupportedOperationException ();

				if (parentTypeId != null)
					return parentTypeId;

				return objectHelperProvider
					.getParentType (object)
					.getId ();

			}

			@Override
			public
			Integer getParentId (
					@NonNull Record object) {

				if (! objectClass ().isInstance (object))
					throw new IllegalArgumentException ();

				if (objectHelperProvider.root ())
					throw new UnsupportedOperationException ();

				if (objectHelperProvider.rooted ())
					return 0;

				if (objectHelperProvider.canGetParent ()) {

					Record<?> parent =
						objectHelperProvider.getParent (
							object);

					return parent.getId ();

				} else {

					return objectHelperProvider.getParentId (
						object);

				}

			}

			@Override
			public
			boolean canGetParent () {

				return objectHelperProvider
					.canGetParent ();

			}

			@Override
			public
			boolean parentTypeIsFixed () {

				return objectHelperProvider
					.parentTypeIsFixed ();

			}

			@Override
			public
			boolean root () {

				return objectHelperProvider
					.root ();

			}

			@Override
			public
			boolean rooted () {

				return objectHelperProvider.rooted ();

			}

			@Override
			public
			List<Record<?>> search (
					@NonNull Object search) {

				List<Integer> objectIds =
					objectHelperProvider.searchIds (
						search);

				ImmutableList.Builder<Record<?>> objectsBuilder =
					ImmutableList.builder ();

				for (Integer objectId
						: objectIds) {

					objectsBuilder.add (
						find (
							objectId));

				}

				return objectsBuilder.build ();

			}

			@Override
			public
			List<Integer> searchIds (
					@NonNull Object search) {

				return objectHelperProvider.searchIds (
					search);

			}

			@Override
			public
			EphemeralRecord remove (
					@NonNull EphemeralRecord object) {

				return objectHelperProvider.remove (
					object);

			}

			@Override
			public
			String objectName () {

				return objectHelperProvider.objectName ();

			}

			@Override
			public
			ObjectHelperProvider objectHelperProvider () {

				return objectHelperProvider;

			}

			@Override
			public
			GlobalId getParentGlobalId (
					@NonNull Record object) {

				if (root ())
					return null;

				return new GlobalId (
					getParentTypeId (object),
					getParentId (object));

			}

			@Override
			public
			Record getParent (
					@NonNull Record object) {

				if (objectHelperProvider.root ())
					return null;

				if (objectHelperProvider.rooted ())
					return rootObjectHelper.find (0);

				if (objectHelperProvider.canGetParent ()) {

					Record parent =
						objectHelperProvider.getParent (
							object);

					if (parent == null) {

						throw new RuntimeException (
							stringFormat (
								"Failed to get parent of %s with id %s",
								objectHelperProvider.objectName (),
								object.getId ()));

					}

					return parent;

				} else {

					Record<?> parentObjectType =
						objectHelperProvider.getParentType (
							object);

					Integer parentObjectId =
						objectHelperProvider.getParentId (
							object);

					if (parentObjectId == null) {

						throw new RuntimeException (
							stringFormat (
								"Failed to get parent id of %s with id %s",
								objectHelperProvider.objectName (),
								object.getId ()));

					}

					ObjectHelperProvider parentHelperProvider =
						objectHelperProviderManager.forObjectTypeId (
							parentObjectType.getId ());

					if (parentHelperProvider == null) {

						throw new RuntimeException (
							stringFormat (
								"No object helper provider for %s, ",
								parentObjectType.getId (),
								"parent of %s (%s)",
								objectHelperProvider.objectName (),
								object.getId ()));

					}

					Record parent =
						parentHelperProvider.find (
							parentObjectId);

					if (parent == null) {

						throw new RuntimeException (
							stringFormat (
								"Can't find %s with id %s",
								parentHelperProvider.objectName (),
								parentObjectId));

					}

					return parent;

				}

			}

			@Override
			public
			List findByParent (
					@NonNull Record parent) {

				ObjectHelper<?> parentHelper =
					forObjectClass (
						parent.getClass ());

				return parentHelper.getChildren (
					parent,
					objectHelperProvider.objectClass ());

			}

			@Override
			public
			GlobalId getGlobalId (
					@NonNull Record object) {

				return new GlobalId (
					objectTypeId,
					object.getId ());

			}

			@Override
			public
			List getChildren (
					@NonNull Record object,
					@NonNull Class childClass) {

				ObjectHelperProvider childHelperProvider =
					objectHelperProviderManager.forObjectClass (
						childClass);

				List<Record<?>> objects =
					childHelperProvider.findAllByParent (
						getGlobalId (object));

				return objects;


			}

			@Override
			public
			List getMinorChildren (
					@NonNull Record object) {

				List<Record<?>> children =
					new ArrayList<Record<?>> ();

				GlobalId globalId =
					getGlobalId (object);

				for (ObjectHelper<?> childHelper
						: list) {

					if (! childHelper.minor ())
						continue;

					if (childHelper.parentTypeIsFixed ()
							&& childHelper.parentClass ()
								!= objectHelperProvider.objectClass ())
						continue;

					children.addAll (
						childHelper.findByParent (
							globalId));

				}

				return children;

			}

			@Override
			public
			List getChildren (
					@NonNull Record object) {

				List<Record<?>> children =
					new ArrayList<Record<?>> ();

				GlobalId globalId =
					getGlobalId (object);

				for (ObjectHelperProvider childHelperProvider
						: objectHelperProviderManager.list ()) {

					if (childHelperProvider.root ())
						continue;

					if (childHelperProvider.parentTypeIsFixed ()
							&& childHelperProvider.parentClass ()
								!= objectHelperProvider.objectClass ())
						continue;

					children.addAll (
						childHelperProvider.findAllByParent (
							globalId));

				}

				return children;

			}

			@Override
			public
			boolean getDeleted (
					@NonNull Record object,
					boolean checkParents) {

				Record currentObject =
					object;

				ObjectHelperProvider currentHelperProvider =
					objectHelperProvider;

				for (;;) {

					// root is never deleted

					if (currentHelperProvider.root ())
						return false;

					// check our deleted flag

					if (currentHelperProvider.getDeleted (
							currentObject))
						return true;

					if (! checkParents)
						return false;

					// try parent

					if (currentHelperProvider.rooted ())
						return false;

					if (currentHelperProvider.canGetParent ()) {

						currentObject =
							currentHelperProvider.getParent (
								currentObject);

						currentHelperProvider =
							objectHelperProviderManager.forObjectClassSearch (
								currentObject.getClass ());

					} else {

						Record<?> parentObjectType =
							objectHelperProvider.getParentType (
								currentObject);

						Integer parentObjectId =
							objectHelperProvider.getParentId (
								currentObject);

						currentHelperProvider =
							objectHelperProviderManager.forObjectTypeId (
								parentObjectType.getId ());

						currentObject =
							currentHelperProvider.find (
								parentObjectId);

					}

				}

			}

			@Override
			public
			Record findByCode (
					@NonNull Record parent,
					@NonNull String... codes) {

				ObjectHelper<?> parentHelper =
					forObjectClass (
						parent.getClass ());

				GlobalId parentGlobalId =
					new GlobalId (
						parentHelper.objectTypeId (),
						parent.getId ());

				return findByCode (
					parentGlobalId,
					codes);

			}

			@Override
			public
			Record findByTypeAndCode (
					@NonNull Record parent,
					@NonNull String typeCode,
					@NonNull String... codes) {

				ObjectHelper<?> parentHelper =
					forObjectClass (parent.getClass ());

				GlobalId parentGlobalId =
					new GlobalId (
						parentHelper.objectTypeId (),
						parent.getId ());

				return findByTypeAndCode (
					parentGlobalId,
					typeCode,
					codes);

			}

			@Override
			public
			String friendlyName () {

				return camelToSpaces (
					objectName ());

			}

			@Override
			public
			String friendlyNamePlural () {

				return friendlyName () + "s";

			}

			@Override
			public
			String shortName () {

				return friendlyName ();

			}

			@Override
			public
			String shortNamePlural () {

				return friendlyNamePlural ();

			}

			@Override
			public
			Record findByTypeAndCode (
					@NonNull GlobalId parentGlobalId,
					@NonNull String typeCode,
					@NonNull String... codes) {

				if (codes.length != 1)
					throw new IllegalArgumentException (
						"codes");

				return objectHelperProvider.findByParentAndTypeAndCode (
					parentGlobalId,
					typeCode,
					codes [0]);

			}

			@Override
			public
			List findByParentAndType (
					@NonNull GlobalId parentGlobalId,
					@NonNull String typeCode) {

				return objectHelperProvider.findAllByParentAndType (
					parentGlobalId,
					typeCode);

			}

			@Override
			@SneakyThrows (Exception.class)
			public
			Record createInstance () {

				return (Record)
					objectClass ().newInstance ();

			}

			@Override
			public
			Record lock (
					@NonNull Record object) {

				return objectHelperProvider.lock (
					object);

			}
			
			@Override
			public 
			boolean getDynamic (
				Record object, 
				String name) {
				
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public 
			void setDynamic (
				Record object, 
				String name, 
				Object value) {

				
			}

		}

	}

}
