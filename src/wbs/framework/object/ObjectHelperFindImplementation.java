package wbs.framework.object;

import static wbs.utils.etc.OptionalUtils.optionalOrNull;
import static wbs.utils.etc.ReflectionUtils.methodGetRequired;
import static wbs.utils.etc.TypeUtils.genericCastUnchecked;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.WeakSingletonDependency;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.entity.record.Record;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

@Accessors (fluent = true)
@PrototypeComponent ("objectHelperFindImplementation")
public
class ObjectHelperFindImplementation <RecordType extends Record <RecordType>>
	implements
		ObjectHelperComponent <RecordType>,
		ObjectHelperFindMethods <RecordType> {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@WeakSingletonDependency
	ObjectManager objectManager;

	// properties

	@Getter @Setter
	ObjectModel <RecordType> objectModel;

	@Getter @Setter
	ObjectHelper <RecordType> objectHelper;

	@Getter @Setter
	ObjectDatabaseHelper <RecordType> objectDatabaseHelper;

	// public implementation

	@Override
	public
	List <RecordType> findAll () {

		return objectDatabaseHelper.findAll ();

	}

	@Override
	public
	List <RecordType> findNotDeleted () {

		return objectDatabaseHelper.findNotDeleted ();

	}

	@Override
	public
	List <RecordType> findByParent (
			@NonNull GlobalId parentGlobalId) {

		return objectDatabaseHelper.findAllByParent (
			parentGlobalId);

	}

	@Override
	public
	List<RecordType> findByParentAndType (
			@NonNull Record <?> parent,
			@NonNull String typeCode) {

		ObjectHelper <?> parentHelper =
			objectManager.objectHelperForClassRequired (
				parent.getClass ());

		GlobalId parentGlobalId =
			new GlobalId (
				parentHelper.objectTypeId (),
				parent.getId ());

		return objectDatabaseHelper.findAllByParentAndType (
			parentGlobalId,
			typeCode);

	}

	@Override
	public
	List <RecordType> search (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Object search) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"search");

		) {

			List <Long> objectIds =
				searchIds (
					taskLogger,
					search);

			ImmutableList.Builder <RecordType> objectsBuilder =
				ImmutableList.builder ();

			for (
				Long objectId
					: objectIds
			) {

				objectsBuilder.add (
					optionalOrNull (
						objectHelper.find (
							objectId)));

			}

			return objectsBuilder.build ();

		}

	}

	@Override
	public
	List <Long> searchIds (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Object search) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"searchIds");

		) {

			Class <?> searchClass;

			if (search instanceof Map) {

				searchClass =
					Map.class;

			} else {

				searchClass =
					search.getClass ();

			}

			Method searchIdsMethod =
				methodGetRequired (
					objectModel.daoImplementation ().getClass (),
					"searchIds",
					ImmutableList.of (
						TaskLogger.class,
						searchClass));

			try {

				List <Long> objectIds =
					genericCastUnchecked (
						searchIdsMethod.invoke (
							objectModel.daoImplementation (),
							taskLogger,
							search));

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

	}

	@Override
	public
	List <RecordType> findByParent (
			@NonNull Record <?> parent) {

		ObjectHelper <?> parentHelper =
			objectManager.objectHelperForClassRequired (
				parent.getClass ());

		List <?> objectsUncast =
			parentHelper.getChildrenGeneric (
				parent,
				objectModel.objectClass ());

		@SuppressWarnings ("unchecked")
		List <RecordType> objects =
			(List <RecordType>)
			objectsUncast;

		return objects;

	}

	@Override
	public
	List <RecordType> findByParentAndType (
			@NonNull GlobalId parentGlobalId,
			@NonNull String typeCode) {

		return objectDatabaseHelper.findAllByParentAndType (
			parentGlobalId,
			typeCode);

	}

}
