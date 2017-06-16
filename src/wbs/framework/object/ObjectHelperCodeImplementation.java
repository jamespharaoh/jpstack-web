package wbs.framework.object;

import static wbs.utils.collection.ArrayUtils.arrayHasOneItem;
import static wbs.utils.collection.ArrayUtils.arrayIsEmpty;
import static wbs.utils.collection.IterableUtils.iterableMapToList;
import static wbs.utils.collection.MapUtils.mapItemForKey;
import static wbs.utils.collection.MapUtils.mapWithDerivedKey;
import static wbs.utils.etc.LogicUtils.allOf;
import static wbs.utils.etc.LogicUtils.referenceEqualWithClass;
import static wbs.utils.etc.NullUtils.isNotNull;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.OptionalUtils.optionalFromNullable;
import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.utils.etc.OptionalUtils.optionalOrNull;
import static wbs.utils.string.StringUtils.joinWithFullStop;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

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
@PrototypeComponent ("objectHelperCodeImplementation")
public
class ObjectHelperCodeImplementation <RecordType extends Record <RecordType>>
	implements
		ObjectHelperCodeMethods <RecordType>,
		ObjectHelperComponent <RecordType> {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@WeakSingletonDependency
	ObjectManager objectManager;

	// prototype dependencies

	@PrototypeDependency
	Provider <IdCacheBuilder <
		CloseableTransaction,
		Pair <Long, String>,
		Long,
		RecordType
	>> idCacheBuilderProvider;

	// properties

	@Getter @Setter
	ObjectModel <RecordType> objectModel;

	@Getter @Setter
	ObjectHelper <RecordType> objectHelper;

	@Getter @Setter
	ObjectDatabaseHelper <RecordType> objectDatabaseHelper;

	// state

	AdvancedCache <CloseableTransaction, Pair <Long, String>, RecordType>
		parentIdAndCodeCache;

	// life cycle

	@Override
	public
	ObjectHelperCodeImplementation <RecordType> setup (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"setup");

		) {

			// parent id and code

			if (allOf (
				() -> isNotNull (objectModel.parentField ()),
				() -> isNotNull (objectModel.codeField ())
			)) {

				parentIdAndCodeCache =
					idCacheBuilderProvider.get ()

					.dummy (! allOf (
						() -> objectModel.parentField ().cacheable (),
						() -> objectModel.codeField ().cacheable ()
					))

					.cacheNegatives (
						false)

					.lookupByIdFunction (
						(innerTransaction, objectId) ->
							optionalFromNullable (
								objectDatabaseHelper.find (
									innerTransaction,
									objectId)))

					.lookupByKeyFunction (
						(innerTransaction, key) ->
							optionalFromNullable (
								objectDatabaseHelper.findByParentAndCode (
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

			// return

			return this;

		}

	}

	// public implementation

	@Override
	public
	Optional <RecordType> findByCode (
			@NonNull Transaction parentTransaction,
			@NonNull GlobalId ancestorGlobalId,
			@NonNull String ... codes) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"findByCode");

		) {

			if (
				arrayIsEmpty (
					codes)
			) {

				throw new IllegalArgumentException (
					"Must supply one or more code");

			} else if (
				arrayHasOneItem (
					codes)
			) {

				return Optional.fromNullable (
					objectDatabaseHelper.findByParentAndCode (
						transaction,
						ancestorGlobalId,
						codes [0]));

			} else {

				ObjectHelper <?> parentHelper =
					objectManager.objectHelperForClassRequired (
						objectModel.parentClassRequired ());

				Record <?> parent =
					parentHelper.findByCodeRequired (
						transaction,
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
					objectDatabaseHelper.findByParentAndCode (
						transaction,
						parentGlobalId,
						codes [1]));

			}

		}

	}

	@Override
	public
	List <Optional <RecordType>> findManyByCode (
			@NonNull Transaction parentTransaction,
			@NonNull GlobalId parentGlobalId,
			@NonNull List <String> codes) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"findManyByCode");

		) {

			Map <String, RecordType> recordsByCode =
				mapWithDerivedKey (
					objectDatabaseHelper.findManyByParentAndCode (
						transaction,
						parentGlobalId,
						codes),
					objectModel::getCode);

			return iterableMapToList (
				codes,
				code -> mapItemForKey (
					recordsByCode,
					code));

		}

	}

	@Override
	public
	RecordType findByCodeRequired (
			@NonNull Transaction parentTransaction,
			@NonNull GlobalId ancestorGlobalId,
			@NonNull String ... codes) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"findByCodeRequired");

		) {

			Optional <RecordType> recordOptional =
				findByCode (
					transaction,
					ancestorGlobalId,
					codes);

			if (
				optionalIsNotPresent (
					recordOptional)
			) {

				if (
					referenceEqualWithClass (
						GlobalId.class,
						GlobalId.root,
						ancestorGlobalId)
				) {

					throw new RuntimeException (
						stringFormat (
							"No such %s with parent root and code %s",
							objectModel.objectName (),
							joinWithFullStop (
								codes)));

				}

				Optional <Record <?>> ancestorOptional =
					objectManager.findObject (
						transaction,
						ancestorGlobalId);

				if (
					optionalIsNotPresent (
						ancestorOptional)
				) {

					throw new RuntimeException (
						stringFormat (
							"No such object with type %s and id %s",
							integerToDecimalString (
								ancestorGlobalId.typeId ()),
							integerToDecimalString (
								ancestorGlobalId.objectId ())));
				}

				Record <?> ancestor =
					optionalGetRequired (
						ancestorOptional);

				throw new RuntimeException (
					stringFormat (
						"No such %s with parent %s and code %s",
						objectModel.objectName (),
						objectManager.objectPath (
							transaction,
							ancestor),
						joinWithFullStop (
							codes)));

			}

			return optionalGetRequired (
				recordOptional);

		}

	}

	@Override
	public
	RecordType findByCodeOrNull (
			@NonNull Transaction parentTransaction,
			@NonNull GlobalId ancestorGlobalId,
			@NonNull String ... codes) {

		return optionalOrNull (
			findByCode (
				parentTransaction,
				ancestorGlobalId,
				codes));

	}

	@Override
	public
	RecordType findByCodeOrThrow (
			@NonNull Transaction parentTransaction,
			@NonNull GlobalId ancestorGlobalId,
			@NonNull String code,
			@NonNull Supplier <? extends RuntimeException> orThrow) {

		Optional <RecordType> recordOptional =
			findByCode (
				parentTransaction,
				ancestorGlobalId,
				code);

		if (
			optionalIsPresent (
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
	RecordType findByCodeOrThrow (
			@NonNull Transaction parentTransaction,
			@NonNull Record <?> ancestor,
			@NonNull String code,
			@NonNull Supplier <? extends RuntimeException> orThrow) {

		Optional <RecordType> recordOptional =
			findByCode (
				parentTransaction,
				ancestor,
				code);

		if (
			optionalIsPresent (
				recordOptional)
		) {

			return recordOptional.get ();

		} else {

			throw orThrow.get ();

		}

	}

	@Override
	public
	RecordType findByCodeOrThrow (
			@NonNull Transaction parentTransaction,
			@NonNull GlobalId ancestorGlobalId,
			@NonNull String code0,
			@NonNull String code1,
			@NonNull Supplier <? extends RuntimeException> orThrow) {

		Optional <RecordType> recordOptional =
			findByCode (
				parentTransaction,
				ancestorGlobalId,
				code0,
				code1);

		if (
			optionalIsPresent (
				recordOptional)
		) {

			return recordOptional.get ();

		} else {

			throw orThrow.get ();

		}

	}

	@Override
	public
	RecordType findByCodeOrThrow (
			@NonNull Transaction parentTransaction,
			@NonNull Record <?> ancestor,
			@NonNull String code0,
			@NonNull String code1,
			@NonNull Supplier <? extends RuntimeException> orThrow) {

		Optional <RecordType> recordOptional =
			findByCode (
				parentTransaction,
				ancestor,
				code0,
				code1);

		if (
			optionalIsPresent (
				recordOptional)
		) {

			return recordOptional.get ();

		} else {

			throw orThrow.get ();

		}

	}

	@Override
	public
	Optional <RecordType> findByCode (
			@NonNull Transaction parentTransaction,
			@NonNull Record <?> parent,
			@NonNull String ... codes) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"findByCode");

		) {

			ObjectHelper <?> parentHelper =
				objectManager.objectHelperForClassRequired (
					parent.getClass ());

			GlobalId parentGlobalId =
				new GlobalId (
					parentHelper.objectTypeId (),
					parent.getId ());

			return findByCode (
				transaction,
				parentGlobalId,
				codes);

		}

	}

	@Override
	public
	RecordType findByCodeRequired (
			@NonNull Transaction parentTransaction,
			@NonNull Record <?> parent,
			@NonNull String ... codes) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"findByCodeRequired");

		) {

			ObjectHelper <?> parentHelper =
				objectManager.objectHelperForClassRequired (
					parent.getClass ());

			GlobalId parentGlobalId =
				new GlobalId (
					parentHelper.objectTypeId (),
					parent.getId ());

			return findByCodeRequired (
				transaction,
				parentGlobalId,
				codes);

		}

	}

	@Override
	public
	RecordType findByCodeOrNull (
			@NonNull Transaction parentTransaction,
			@NonNull Record <?> parent,
			@NonNull String... codes) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"findByCodeOrNull");

		) {

			ObjectHelper <?> parentHelper =
				objectManager.objectHelperForObjectRequired (
					parent);

			GlobalId parentGlobalId =
				new GlobalId (
					parentHelper.objectTypeId (),
					parent.getId ());

			return findByCodeOrNull (
				transaction,
				parentGlobalId,
				codes);

		}

	}

	@Override
	public
	RecordType findByTypeAndCode (
			@NonNull Transaction parentTransaction,
			@NonNull Record <?> parent,
			@NonNull String typeCode,
			@NonNull String ... codes) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"findByTypeAndCode");

		) {

			ObjectHelper <?> parentHelper =
				objectManager.objectHelperForObjectRequired (
					parent);

			GlobalId parentGlobalId =
				new GlobalId (
					parentHelper.objectTypeId (),
					parent.getId ());

			return findByTypeAndCode (
				transaction,
				parentGlobalId,
				typeCode,
				codes);

		}

	}

	@Override
	public
	RecordType findByTypeAndCode (
			@NonNull Transaction parentTransaction,
			@NonNull GlobalId parentGlobalId,
			@NonNull String typeCode,
			@NonNull String ... codes) {

		if (codes.length != 1) {

			throw new IllegalArgumentException (
				"codes");

		}

		return objectDatabaseHelper.findByParentAndTypeAndCode (
			parentTransaction,
			parentGlobalId,
			typeCode,
			codes [0]);

	}

}
