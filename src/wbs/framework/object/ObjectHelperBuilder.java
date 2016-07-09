package wbs.framework.object;

import static wbs.framework.utils.etc.Misc.camelToSpaces;
import static wbs.framework.utils.etc.Misc.capitalise;
import static wbs.framework.utils.etc.Misc.equal;
import static wbs.framework.utils.etc.Misc.getMethodRequired;
import static wbs.framework.utils.etc.Misc.isEmpty;
import static wbs.framework.utils.etc.Misc.isNotNull;
import static wbs.framework.utils.etc.Misc.isNotPresent;
import static wbs.framework.utils.etc.Misc.isNull;
import static wbs.framework.utils.etc.Misc.isPresent;
import static wbs.framework.utils.etc.Misc.joinWithFullStop;
import static wbs.framework.utils.etc.Misc.joinWithSeparator;
import static wbs.framework.utils.etc.Misc.lessThan;
import static wbs.framework.utils.etc.Misc.naivePluralise;
import static wbs.framework.utils.etc.Misc.optionalOrNull;
import static wbs.framework.utils.etc.Misc.optionalRequired;
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
import java.util.function.Supplier;
import java.util.stream.Collectors;

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

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.application.context.ApplicationContext;
import wbs.framework.application.context.NoSuchBeanException;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.entity.model.Model;
import wbs.framework.entity.model.ModelMethods;
import wbs.framework.record.CommonRecord;
import wbs.framework.record.EphemeralRecord;
import wbs.framework.record.EventRecord;
import wbs.framework.record.GlobalId;
import wbs.framework.record.MajorRecord;
import wbs.framework.record.MinorRecord;
import wbs.framework.record.Record;
import wbs.framework.record.TypeRecord;
import wbs.framework.record.UnsavedRecordDetector;

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
			database.beginReadOnly (
				this);

		for (
			ObjectHelperProvider objectHelperProvider
				: objectHelperProviderManager.list ()
		) {

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

		Model model;

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

			model =
				objectHelperProvider.model ();

			ObjectTypeEntry objectType =
				objectTypeRegistry.findByCode (
					objectHelperProvider.objectTypeCode ());

			objectTypeCode =
				objectType.getCode ();

			objectTypeId =
				objectType.getId ();

			if (
				model.parentTypeIsFixed ()
				&& ! model.isRoot ()
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
						"%s.%sObjectHelperMethods",
						objectHelperProvider.objectClass ().getPackage ().getName (),
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
					"%s.%sDaoMethods",
					objectHelperProvider.objectClass ().getPackage ().getName (),
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

				try {

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

				} catch (InvocationTargetException exception) {

					throw exception.getTargetException ();

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
			boolean event () {
				return EventRecord.class.isAssignableFrom (
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
			boolean type () {
				return TypeRecord.class.isAssignableFrom (
					objectClass ());
			}

			@Override
			public
			Optional<Record<?>> find (
					long id) {

				return Optional.fromNullable (
					objectHelperProvider.find (
						id));

			}

			@Override
			public
			Record<?> findRequired (
					long id) {

				Record<?> record =
					objectHelperProvider.find (
						id);

				if (
					isNull (
						record)
				) {

					throw new RuntimeException (
						stringFormat (
							"%s with id %s not found",
							capitalise (
								camelToSpaces (
									model ().objectName ())),
							id));

				}

				return record;

			}

			@Override
			public
			Record<?> findOrNull (
					long id) {

				return objectHelperProvider
					.find (id);

			}

			@Override
			public
			Record findOrThrow (
					long id,
					@NonNull Supplier orThrow) {

				Record<?> object =
					objectHelperProvider.find (
						id);

				if (
					isNotNull (
						object)
				) {

					return object;

				} else {

					throw (RuntimeException)
						orThrow.get ();

				}

			}

			@Override
			public
			List findManyRequired (
					@NonNull List ids) {

				@SuppressWarnings ("unchecked")
				List<Long> longIds =
					(List<Long>)
					ids;

				List<Record<?>> objects =
					objectHelperProvider.findMany (
						longIds);

				List<Long> missingIds =
					new ArrayList<Long> ();

				for (
					int index = 0;
					index < ids.size ();
					index ++
				) {

					if (
						isNotNull (
							objects.get (
								index))
					) {
						continue;
					}

					missingIds.add (
						longIds.get (
							index));

				}

				if (
					isEmpty (
						missingIds)
				) {

					return objects;

				} else if (
					equal (
						missingIds.size (),
						1)
				) {

					throw new RuntimeException (
						stringFormat (
							"No such %s with id %s",
							camelToSpaces (
								model.objectName ()),
							missingIds.get (0)));

				} else if (
					lessThan (
						missingIds.size (),
						6)
				) {

					throw new RuntimeException (
						stringFormat (
							"No such %s with ids %s",
							camelToSpaces (
								model.objectName ()),
							joinWithSeparator (
								", ",
								missingIds.stream ()
									.map (longValue -> longValue.toString ())
									.collect (Collectors.toList ()))));

				} else {

					throw new RuntimeException (
						stringFormat (
							"No such %s with ids %s (and %s others)",
							camelToSpaces (
								model.objectName ()),
							joinWithSeparator (
								", ",
								missingIds.subList (0, 5).stream ()
									.map (longValue -> longValue.toString ())
									.collect (Collectors.toList ())),
							missingIds.size () - 5));

				}

			}

			@Override
			public
			Optional<Record<?>> findByCode (
					@NonNull GlobalId ancestorGlobalId,
					@NonNull String... codes) {

				if (codes.length == 1) {

					return Optional.fromNullable (
						objectHelperProvider.findByParentAndCode (
							ancestorGlobalId,
							codes [0]));

				}

				if (codes.length > 1) {

					ObjectHelper<?> parentHelper =
						forObjectClassRequired (
							parentClass ());

					Record<?> parent =
						parentHelper.findByCodeRequired (
							ancestorGlobalId,
							Arrays.copyOfRange (
								codes,
								0,
								codes.length - 1));

					GlobalId parentGlobalId =
						new GlobalId (
							parentHelper.objectTypeId (),
							parent.getId ());

					return Optional.fromNullable (
						objectHelperProvider.findByParentAndCode (
							parentGlobalId,
							codes [1]));

				}

				throw new IllegalArgumentException (
					"codes");

			}

			@Override
			public
			Record<?> findByCodeRequired (
					@NonNull GlobalId ancestorGlobalId,
					@NonNull String... codes) {

				Optional<Record<?>> recordOptional =
					findByCode (
						ancestorGlobalId,
						codes);

				if (
					isNotPresent (
						recordOptional)
				) {

					throw new RuntimeException (
						stringFormat (
							"No such object %s ",
							joinWithFullStop (
								codes),
							"with parent %s",
							ancestorGlobalId));

				}

				return optionalRequired (
					recordOptional);

			}

			@Override
			public
			Record<?> findByCodeOrNull (
					@NonNull GlobalId ancestorGlobalId,
					@NonNull String... codes) {

				return optionalOrNull (
					findByCode (
						ancestorGlobalId,
						codes));

			}

			@Override
			public
			Record<?> findByCodeOrThrow (
					@NonNull GlobalId ancestorGlobalId,
					@NonNull String code,
					@NonNull Supplier orThrow) {

				Optional<Record<?>> recordOptional =
					findByCode (
						ancestorGlobalId,
						code);

				if (
					isPresent (
						recordOptional)
				) {

					return recordOptional.get ();

				} else {

					throw (RuntimeException)
						orThrow.get ();

				}

			}

			@Override
			public
			Record<?> findByCodeOrThrow (
					@NonNull Record ancestor,
					@NonNull String code,
					@NonNull Supplier orThrow) {

				Optional<Record<?>> recordOptional =
					findByCode (
						ancestor,
						code);

				if (
					isPresent (
						recordOptional)
				) {

					return recordOptional.get ();

				} else {

					throw (RuntimeException)
						orThrow.get ();

				}

			}

			@Override
			public
			Record<?> findByCodeOrThrow (
					@NonNull GlobalId ancestorGlobalId,
					@NonNull String code0,
					@NonNull String code1,
					@NonNull Supplier orThrow) {

				Optional<Record<?>> recordOptional =
					findByCode (
						ancestorGlobalId,
						code0,
						code1);

				if (
					isPresent (
						recordOptional)
				) {

					return recordOptional.get ();

				} else {

					throw (RuntimeException)
						orThrow.get ();

				}

			}

			@Override
			public
			Record<?> findByCodeOrThrow (
					@NonNull Record ancestor,
					@NonNull String code0,
					@NonNull String code1,
					@NonNull Supplier orThrow) {

				Optional<Record<?>> recordOptional =
					findByCode (
						ancestor,
						code0,
						code1);

				if (
					isPresent (
						recordOptional)
				) {

					return recordOptional.get ();

				} else {

					throw (RuntimeException)
						orThrow.get ();

				}

			}

			@Override
			public
			Record findByIndex (
					@NonNull Record parent,
					@NonNull Long index) {

				ObjectHelper<?> parentHelper =
					forObjectClassRequired (
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
					forObjectClassRequired (parent.getClass ());

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

				if (
					! objectClass ().isInstance (
						object)
				) {

					throw new ClassCastException (
						stringFormat (
							"Can't insert %s as %s",
							object.getClass ().getSimpleName (),
							objectClass ().getSimpleName ()));

				}

				objectHelperProvider.insert (
					object);

				UnsavedRecordDetector.instance.removeRecord (
					object);

				for (
					ObjectHelper childObjectHelper
						: list
				) {

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
			Record insertSpecial (
					@NonNull Record object) {

				if (
					! objectClass ().isInstance (
						object)
				) {

					throw new ClassCastException (
						stringFormat (
							"Can't insert %s as %s",
							object.getClass ().getSimpleName (),
							objectClass ().getSimpleName ()));

				}

				objectHelperProvider.insertSpecial (
					object);

				UnsavedRecordDetector.instance.removeRecord (
					object);

				for (
					ObjectHelper childObjectHelper
						: list
				) {

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
			Record update (
					@NonNull Record object) {

				if (
					! objectClass ().isInstance (
						object)
				) {

					throw new ClassCastException (
						stringFormat (
							"Can't update %s as %s",
							object.getClass ().getSimpleName (),
							objectClass ().getSimpleName ()));

				}

				objectHelperProvider.update (
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

				if (
					! objectClass ().isInstance (
						object)
				) {

					throw new IllegalArgumentException ();

				} else if (model.isRoot ()) {

					throw new UnsupportedOperationException ();

				} else if (parentTypeId != null) {

					return parentTypeId;

				} else {

					return objectHelperProvider
						.getParentType (object)
						.getId ();

				}

			}

			@Override
			public
			Integer getParentId (
					@NonNull Record object) {

				if (! objectClass ().isInstance (object)) {

					throw new IllegalArgumentException ();

				} else if (model.isRoot ()) {

					throw new UnsupportedOperationException ();

				} else if (model.isRooted ()) {

					return 0;

				} else if (model.canGetParent ()) {

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
			List<Record<?>> search (
					@NonNull Object search) {

				List<Integer> objectIds =
					searchIds (
						search);

				ImmutableList.Builder<Record<?>> objectsBuilder =
					ImmutableList.builder ();

				for (
					Integer objectId
						: objectIds
				) {

					objectsBuilder.add (
						findOrNull (
							objectId));

				}

				return objectsBuilder.build ();

			}

			@Override
			public
			List<Integer> searchIds (
					@NonNull Object search) {

				Class<?> searchClass;

				if (search instanceof Map) {

					searchClass =
						Map.class;

				} else {

					searchClass =
						search.getClass ();

				}

				Method searchIdsMethod =
					getMethodRequired (
						daoImplementation.getClass (),
						"searchIds",
						ImmutableList.<Class<?>>of (
							searchClass));

				try {

					@SuppressWarnings ("unchecked")
					List<Integer> objectIds =
						(List<Integer>)
						searchIdsMethod.invoke (
							daoImplementation,
							search);

					return objectIds;

				} catch (InvocationTargetException exception) {

					Throwable targetException =
						exception.getTargetException ();

					if (targetException instanceof RuntimeException) {

						throw
							(RuntimeException)
							targetException;

					} else {

						throw new RuntimeException (
							exception);

					}

				} catch (IllegalAccessException exception) {

					throw new RuntimeException (
						exception);

				}

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

				if (model.isRoot ()) {

					return null;

				} else {

					return new GlobalId (
						getParentTypeId (object),
						getParentId (object));

				}

			}

			@Override
			public
			Record getParent (
					@NonNull Record object) {

				if (model.isRoot ()) {

					return null;

				} else if (model.isRooted ()) {

					return rootObjectHelper.findRequired (
						0);

				} else if (model.canGetParent ()) {

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
					forObjectClassRequired (
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

				for (
					ObjectHelperProvider childHelperProvider
						: objectHelperProviderManager.list ()
				) {

					Model childModel =
						childHelperProvider.model ();

					if (childModel.isRoot ()) {
						continue;
					}

					if (
						childModel.parentTypeIsFixed ()
						&& childHelperProvider.parentClass ()
							!= objectHelperProvider.objectClass ()
					) {
						continue;
					}

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

				Model currentModel =
					currentHelperProvider.model ();

				for (;;) {

					// root is never deleted

					if (currentModel.isRoot ()) {
						return false;
					}

					// check our deleted flag

					if (
						currentHelperProvider.getDeleted (
							currentObject)
					) {
						return true;
					}

					// try parent

					if (! checkParents) {
						return false;
					}

					if (currentModel.isRooted ()) {
						return false;
					}

					if (currentModel.canGetParent ()) {

						currentObject =
							currentHelperProvider.getParent (
								currentObject);

						currentHelperProvider =
							objectHelperProviderManager.forObjectClassSearch (
								currentObject.getClass ());

						currentModel =
							currentHelperProvider.model ();

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

						currentModel =
							currentHelperProvider.model ();

						currentObject =
							currentHelperProvider.find (
								parentObjectId);

					}

				}

			}

			@Override
			public
			Optional<Record<?>> findByCode (
					@NonNull Record parent,
					@NonNull String... codes) {

				ObjectHelper<?> parentHelper =
					forObjectClassRequired (
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
			Record<?> findByCodeRequired (
					@NonNull Record parent,
					@NonNull String... codes) {

				ObjectHelper<?> parentHelper =
					forObjectClassRequired (
						parent.getClass ());

				GlobalId parentGlobalId =
					new GlobalId (
						parentHelper.objectTypeId (),
						parent.getId ());

				return optionalRequired (
					findByCode (
						parentGlobalId,
						codes));

			}

			@Override
			public
			Record findByCodeOrNull (
					@NonNull Record parent,
					@NonNull String... codes) {

				ObjectHelper<?> parentHelper =
					forObjectClassRequired (
						parent.getClass ());

				GlobalId parentGlobalId =
					new GlobalId (
						parentHelper.objectTypeId (),
						parent.getId ());

				return findByCodeOrNull (
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
					forObjectClassRequired (parent.getClass ());

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

				return naivePluralise (
					friendlyName ());

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
			List findByIndexRange (
					@NonNull Record parent,
					@NonNull Long indexStart,
					@NonNull Long indexEnd) {

				ObjectHelper<?> parentHelper =
					forObjectClassRequired (
						parent.getClass ());

				GlobalId parentGlobalId =
					new GlobalId (
						parentHelper.objectTypeId (),
						parent.getId ());

				return objectHelperProvider.findByParentAndIndexRange (
					parentGlobalId,
					indexStart,
					indexEnd);

			}

			@Override
			public
			List findByIndexRange (
					@NonNull GlobalId parentGlobalId,
					@NonNull Long indexStart,
					@NonNull Long indexEnd) {

				return objectHelperProvider.findByParentAndIndexRange (
					parentGlobalId,
					indexStart,
					indexEnd);

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

				Constructor constructor =
					objectClass ().getDeclaredConstructor ();

				constructor.setAccessible (
					true);

				Record object =
					(Record)
					constructor.newInstance ();

				UnsavedRecordDetector.instance.addRecord (
					object);

				return object;

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
			Object getDynamic (
					@NonNull Record object,
					@NonNull String name) {

				return objectHelperProvider.getDynamic (
					object,
					name);

			}

			@Override
			public
			void setDynamic (
					@NonNull Record object,
					@NonNull String name,
					@NonNull Object value) {

				objectHelperProvider.setDynamic (
					object,
					name,
					value);

			}

		}

	}

}
