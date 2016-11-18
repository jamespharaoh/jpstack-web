package wbs.framework.object;

import static wbs.utils.etc.LogicUtils.allOf;
import static wbs.utils.etc.Misc.isNotNull;
import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.utils.etc.OptionalUtils.optionalOrNull;
import static wbs.utils.string.StringUtils.joinWithFullStop;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.Arrays;
import java.util.function.Supplier;

import javax.inject.Provider;

import com.google.common.base.Optional;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.tuple.Pair;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.WeakSingletonDependency;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.entity.record.Record;

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

	@WeakSingletonDependency
	ObjectManager objectManager;

	// prototype dependencies

	@PrototypeDependency
	Provider <IdCacheBuilder <Pair <Long, String>, Long, RecordType>>
	idCacheBuilderProvider;

	// properties

	@Getter @Setter
	ObjectModel <RecordType> objectModel;

	@Getter @Setter
	ObjectHelper <RecordType> objectHelper;

	@Getter @Setter
	ObjectDatabaseHelper <RecordType> objectDatabaseHelper;

	// state

	AdvancedCache <Pair <Long, String>, RecordType> parentIdAndCodeCache;

	// life cycle

	@Override
	public
	ObjectHelperCodeImplementation <RecordType> setup () {

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
					objectId ->
						Optional.fromNullable (
							objectDatabaseHelper.find (
								objectId)))

				.lookupByKeyFunction (
					key ->
						Optional.fromNullable (
							objectDatabaseHelper.findByParentAndCode (
								new GlobalId (
									objectModel.parentTypeId (),
									key.getLeft ()),
								key.getRight ())))

				.getIdFunction (
					record ->
						record.getId ())

				.build ();

		}

		// return

		return this;

	}

	// public implementation

	@Override
	public
	Optional <RecordType> findByCode (
			@NonNull GlobalId ancestorGlobalId,
			@NonNull String ... codes) {

		if (codes.length == 1) {

			return Optional.fromNullable (
				objectDatabaseHelper.findByParentAndCode (
					ancestorGlobalId,
					codes [0]));

		}

		if (codes.length > 1) {

			ObjectHelper <?> parentHelper =
				objectManager.objectHelperForClassRequired (
					objectModel.parentClass ());

			Record <?> parent =
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
				objectDatabaseHelper.findByParentAndCode (
					parentGlobalId,
					codes [1]));

		}

		throw new IllegalArgumentException (
			"codes");

	}

	@Override
	public
	RecordType findByCodeRequired (
			@NonNull GlobalId ancestorGlobalId,
			@NonNull String ... codes) {

		Optional <RecordType> recordOptional =
			findByCode (
				ancestorGlobalId,
				codes);

		if (
			optionalIsNotPresent (
				recordOptional)
		) {

			throw new RuntimeException (
				stringFormat (
					"No such %s with parent %s and code %s",
					objectModel.objectName (),
					objectManager.objectPath (
						objectManager.findObject (
							ancestorGlobalId)),
					joinWithFullStop (
						codes)));

		}

		return optionalGetRequired (
			recordOptional);

	}

	@Override
	public
	RecordType findByCodeOrNull (
			@NonNull GlobalId ancestorGlobalId,
			@NonNull String ... codes) {

		return optionalOrNull (
			findByCode (
				ancestorGlobalId,
				codes));

	}

	@Override
	public
	RecordType findByCodeOrThrow (
			@NonNull GlobalId ancestorGlobalId,
			@NonNull String code,
			@NonNull Supplier <? extends RuntimeException> orThrow) {

		Optional <RecordType> recordOptional =
			findByCode (
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
			@NonNull Record <?> ancestor,
			@NonNull String code,
			@NonNull Supplier <? extends RuntimeException> orThrow) {

		Optional<RecordType> recordOptional =
			findByCode (
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
			@NonNull GlobalId ancestorGlobalId,
			@NonNull String code0,
			@NonNull String code1,
			@NonNull Supplier <? extends RuntimeException> orThrow) {

		Optional <RecordType> recordOptional =
			findByCode (
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
			@NonNull Record <?> ancestor,
			@NonNull String code0,
			@NonNull String code1,
			@NonNull Supplier <? extends RuntimeException> orThrow) {

		Optional <RecordType> recordOptional =
			findByCode (
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
			@NonNull Record <?> parent,
			@NonNull String ... codes) {

		ObjectHelper <?> parentHelper =
			objectManager.objectHelperForClassRequired (
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
	RecordType findByCodeRequired (
			@NonNull Record <?> parent,
			@NonNull String ... codes) {

		ObjectHelper <?> parentHelper =
			objectManager.objectHelperForClassRequired (
				parent.getClass ());

		GlobalId parentGlobalId =
			new GlobalId (
				parentHelper.objectTypeId (),
				parent.getId ());

		return findByCodeRequired (
			parentGlobalId,
			codes);

	}

	@Override
	public
	RecordType findByCodeOrNull (
			@NonNull Record<?> parent,
			@NonNull String... codes) {

		ObjectHelper<?> parentHelper =
			objectManager.objectHelperForObjectRequired (
				parent);

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
	RecordType findByTypeAndCode (
			@NonNull Record<?> parent,
			@NonNull String typeCode,
			@NonNull String... codes) {

		ObjectHelper<?> parentHelper =
			objectManager.objectHelperForObjectRequired (
				parent);

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
	RecordType findByTypeAndCode (
			@NonNull GlobalId parentGlobalId,
			@NonNull String typeCode,
			@NonNull String... codes) {

		if (codes.length != 1)
			throw new IllegalArgumentException (
				"codes");

		return objectDatabaseHelper.findByParentAndTypeAndCode (
			parentGlobalId,
			typeCode,
			codes [0]);

	}

}
