package wbs.framework.object;

import static wbs.utils.etc.NullUtils.isNotNull;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.OptionalUtils.optionalOrNull;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.List;

import javax.inject.Provider;

import com.google.common.base.Optional;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.tuple.Pair;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.WeakSingletonDependency;
import wbs.framework.database.CloseableTransaction;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.entity.record.Record;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

import wbs.utils.cache.AdvancedCache;
import wbs.utils.cache.IdCacheBuilder;

@Accessors (fluent = true)
@PrototypeComponent ("objectHelperIndexImplementation")
public
class ObjectHelperIndexImplementation <
	RecordType extends Record <RecordType>
>
	implements
		ObjectHelperComponent <RecordType>,
		ObjectHelperIndexMethods <RecordType> {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@WeakSingletonDependency
	ObjectManager objectManager;

	// prototype dependencies

	@PrototypeDependency
	Provider <IdCacheBuilder <
		CloseableTransaction,
		Pair <Long, Long>,
		Long,
		RecordType
	>> parentIdAndIndexCacheBuilderProvider;

	@PrototypeDependency
	Provider <IdCacheBuilder <
		CloseableTransaction,
		Pair <GlobalId, Long>,
		Long,
		RecordType
	>> parentGlobalIdAndIndexCacheBuilderProvider;

	// properties

	@Getter @Setter
	ObjectModel <RecordType> objectModel;

	@Getter @Setter
	ObjectHelper <RecordType> objectHelper;

	@Getter @Setter
	ObjectDatabaseHelper <RecordType> objectDatabaseHelper;

	// state

	AdvancedCache <CloseableTransaction, Pair <Long ,Long>, RecordType>
		parentIdAndIndexCache;

	AdvancedCache <CloseableTransaction, Pair <GlobalId, Long>, RecordType>
		parentGlobalIdAndIndexCache;

	// life cycle

	@Override
	public
	ObjectHelperIndexImplementation <RecordType> setup (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"setup");

		) {

			// parent id and index

			if (

				isNotNull (
					objectModel.parentField ())

				&& isNotNull (
					objectModel.indexField ())

			) {

				parentIdAndIndexCache =
					parentIdAndIndexCacheBuilderProvider.get ()

					.dummy (
						! objectModel.parentField ().cacheable ()
						|| ! objectModel.indexField ().cacheable ())

					.cacheNegatives (
						false)

					.lookupByIdFunction (
						(innerTransaction, objectId) ->
							Optional.fromNullable (
								objectDatabaseHelper.find (
									innerTransaction,
									objectId)))

					.lookupByKeyFunction (
						(innerTransaction, key) ->
							Optional.fromNullable (
								objectDatabaseHelper.findByParentAndIndex (
									innerTransaction,
									new GlobalId (
										objectModel.parentTypeId (),
										key.getLeft ()),
									key.getRight ())))

					.getIdFunction (
						record ->
							record.getId ())

					.wrapperFunction (
						CloseableTransaction::genericWrapper)

					.build (
						taskLogger);

			}

			// parent global id and index

			if (

				isNotNull (
					objectModel.parentTypeField ())

				&& isNotNull (
					objectModel.parentIdField ())

				&& isNotNull (
					objectModel.indexField ())

			) {

				parentGlobalIdAndIndexCache =
					parentGlobalIdAndIndexCacheBuilderProvider.get ()

					.dummy (
						! objectModel.parentTypeField ().cacheable ()
						|| ! objectModel.parentIdField ().cacheable ()
						|| ! objectModel.indexField ().cacheable ()
					)

					.cacheNegatives (
						false)

					.lookupByIdFunction (
						(innerTransaction, objectId) ->
							Optional.fromNullable (
								objectDatabaseHelper.find (
									innerTransaction,
									objectId)))

					.lookupByKeyFunction (
						(innerTransaction, key) ->
							Optional.fromNullable (
								objectDatabaseHelper.findByParentAndIndex (
									innerTransaction,
									key.getLeft (),
									key.getRight ())))

					.getIdFunction (
						record ->
							record.getId ())

					.build (
						taskLogger);

			}

			// return

			return this;

		}

	}

	// public implementation

	@Override
	public
	Optional <RecordType> findByIndex (
			@NonNull Transaction parentTransaction,
			@NonNull Record <?> parent,
			@NonNull Long index) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"findByIndex");

		) {

			if (objectModel.canGetParent ()) {

				return parentIdAndIndexCache.find (
					transaction,
					Pair.of (
						parent.getId (),
						index));

			} else {

				ObjectHelper <?> parentHelper =
					objectManager.objectHelperForClassRequired (
						parent.getClass ());

				return parentGlobalIdAndIndexCache.find (
					transaction,
					Pair.of (
						GlobalId.of (
							parentHelper.objectTypeId (),
							parent.getId ()),
						index));

			}

		}

	}

	@Override
	public
	RecordType findByIndexRequired (
			@NonNull Transaction parentTransaction,
			@NonNull Record <?> parent,
			@NonNull Long index) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"findByIndexRequired");

		) {

			Optional <RecordType> recordOptional =
				findByIndex (
					transaction,
					parent,
					index);

			if (
				optionalIsNotPresent (
					recordOptional)
			) {

				throw new RuntimeException (
					stringFormat (
						"Object not found with parent %s ",
						objectManager.objectPath (
							transaction,
							parent),
						"and index %s",
						integerToDecimalString (
							index)));

			}

			return recordOptional.get ();

		}

	}

	@Override
	public
	RecordType findByIndexOrNull (
			@NonNull Transaction parentTransaction,
			@NonNull Record <?> parent,
			@NonNull Long index) {

		return optionalOrNull (
			findByIndex (
				parentTransaction,
				parent,
				index));

	}

	@Override
	public
	List <RecordType> findByIndexRange (
			@NonNull Transaction parentTransaction,
			@NonNull GlobalId parentGlobalId,
			@NonNull Long indexStart,
			@NonNull Long indexEnd) {

		return objectDatabaseHelper.findByParentAndIndexRange (
			parentTransaction,
			parentGlobalId,
			indexStart,
			indexEnd);

	}

	@Override
	public
	List <RecordType> findByIndexRange (
			@NonNull Transaction parentTransaction,
			@NonNull Record <?> parent,
			@NonNull Long indexStart,
			@NonNull Long indexEnd) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"findByIndexRange");

		) {

			ObjectHelper <?> parentHelper =
				objectManager.objectHelperForObjectRequired (
					parent);

			GlobalId parentGlobalId =
				new GlobalId (
					parentHelper.objectTypeId (),
					parent.getId ());

			return objectDatabaseHelper.findByParentAndIndexRange (
				transaction,
				parentGlobalId,
				indexStart,
				indexEnd);

		}

	}

	@Override
	public
	RecordType findByIndex (
			@NonNull Transaction parentTransaction,
			@NonNull GlobalId parentGlobalId,
			@NonNull Long index) {

		return objectDatabaseHelper.findByParentAndIndex (
			parentTransaction,
			parentGlobalId,
			index);

	}

}
