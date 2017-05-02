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
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.entity.record.Record;
import wbs.framework.logging.LogContext;

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
	List <RecordType> findAll (
			@NonNull Transaction parentTransaction) {

		return objectDatabaseHelper.findAll (
			parentTransaction);

	}

	@Override
	public
	List <RecordType> findNotDeleted (
			@NonNull Transaction parentTransaction) {

		return objectDatabaseHelper.findNotDeleted (
			parentTransaction);

	}

	@Override
	public
	List <RecordType> findByParent (
			@NonNull Transaction parentTransaction,
			@NonNull GlobalId parentGlobalId) {

		return objectDatabaseHelper.findAllByParent (
			parentTransaction,
			parentGlobalId);

	}

	@Override
	public
	List <RecordType> findByParentAndType (
			@NonNull Transaction parentTransaction,
			@NonNull Record <?> parent,
			@NonNull String typeCode) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"findByParentAndType");

		) {

			ObjectHelper <?> parentHelper =
				objectManager.objectHelperForClassRequired (
					parent.getClass ());

			GlobalId parentGlobalId =
				new GlobalId (
					parentHelper.objectTypeId (),
					parent.getId ());

			return objectDatabaseHelper.findAllByParentAndType (
				transaction,
				parentGlobalId,
				typeCode);

		}

	}

	@Override
	public
	List <RecordType> search (
			@NonNull Transaction parentTransaction,
			@NonNull Object search) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"search");

		) {

			List <Long> objectIds =
				searchIds (
					transaction,
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
							transaction,
							objectId)));

			}

			return objectsBuilder.build ();

		}

	}

	@Override
	public
	List <Long> searchIds (
			@NonNull Transaction parentTransaction,
			@NonNull Object search) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
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
						Transaction.class,
						searchClass));

			try {

				List <Long> objectIds =
					genericCastUnchecked (
						searchIdsMethod.invoke (
							objectModel.daoImplementation (),
							transaction,
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
			@NonNull Transaction parentTransaction,
			@NonNull Record <?> parent) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"findByParent");

		) {

			ObjectHelper <?> parentHelper =
				objectManager.objectHelperForClassRequired (
					parent.getClass ());

			return genericCastUnchecked (
				parentHelper.getChildrenGeneric (
					transaction,
					parent,
					objectModel.objectClass ()));

		}

	}

	@Override
	public
	List <RecordType> findByParentAndType (
			@NonNull Transaction parentTransaction,
			@NonNull GlobalId parentGlobalId,
			@NonNull String typeCode) {

		return objectDatabaseHelper.findAllByParentAndType (
			parentTransaction,
			parentGlobalId,
			typeCode);

	}

}
