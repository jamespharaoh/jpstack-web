package wbs.framework.object;

import static wbs.framework.utils.etc.Misc.getMethodRequired;
import static wbs.framework.utils.etc.OptionalUtils.optionalOrNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;

import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.entity.record.Record;

@Accessors (fluent = true)
@PrototypeComponent ("objectHelperFindImplementation")
public
class ObjectHelperFindImplementation <RecordType extends Record <RecordType>>
	implements
		ObjectHelperComponent <RecordType>,
		ObjectHelperFindMethods <RecordType> {

	// properties

	@Setter
	ObjectModel <RecordType> model;

	@Setter
	ObjectHelper <RecordType> objectHelper;

	@Setter
	ObjectManager objectManager;

	@Setter
	ObjectDatabaseHelper <RecordType> objectDatabaseHelper;

	// public implementation

	@Override
	public
	List <RecordType> findAll () {

		return objectDatabaseHelper.findAll ();

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
			@NonNull Object search) {

		List <Long> objectIds =
			searchIds (
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

	@Override
	public
	List<Long> searchIds (
			@NonNull Object search) {

		Class <?> searchClass;

		if (search instanceof Map) {

			searchClass =
				Map.class;

		} else {

			searchClass =
				search.getClass ();

		}

		Method searchIdsMethod =
			getMethodRequired (
				model.daoImplementation ().getClass (),
				"searchIds",
				ImmutableList.of (
					searchClass));

		try {

			@SuppressWarnings ("unchecked")
			List <Long> objectIds =
				(List <Long>)
				searchIdsMethod.invoke (
					model.daoImplementation (),
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
	List <RecordType> findByParent (
			@NonNull Record<?> parent) {

		ObjectHelper <?> parentHelper =
			objectManager.objectHelperForClassRequired (
				parent.getClass ());

		List <?> objectsUncast =
			parentHelper.getChildren (
				parent,
				model.objectClass ());

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
