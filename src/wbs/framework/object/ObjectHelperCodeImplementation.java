package wbs.framework.object;

import static wbs.framework.utils.etc.LogicUtils.allOf;
import static wbs.framework.utils.etc.Misc.isNotNull;
import static wbs.framework.utils.etc.OptionalUtils.isNotPresent;
import static wbs.framework.utils.etc.OptionalUtils.isPresent;
import static wbs.framework.utils.etc.OptionalUtils.optionalOrNull;
import static wbs.framework.utils.etc.OptionalUtils.optionalRequired;
import static wbs.framework.utils.etc.StringUtils.joinWithFullStop;
import static wbs.framework.utils.etc.StringUtils.stringFormat;

import java.util.Arrays;
import java.util.function.Supplier;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.base.Optional;

import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.record.GlobalId;
import wbs.framework.record.Record;
import wbs.framework.utils.cache.AdvancedCache;

@Accessors (fluent = true)
@PrototypeComponent ("objectHelperCodeImplementation")
public 
class ObjectHelperCodeImplementation<RecordType extends Record<RecordType>>
	implements
		ObjectHelperCodeMethods<RecordType>,
		ObjectHelperComponent<RecordType> {

	// properties

	@Setter
	ObjectModel<RecordType> model;

	@Setter
	ObjectHelper<RecordType> objectHelper;

	@Setter
	ObjectDatabaseHelper<RecordType> objectDatabaseHelper;

	@Setter
	ObjectManager objectManager;

	// state

	AdvancedCache<Pair<Long,String>,RecordType> parentIdAndCodeCache;

	// life cycle

	@Override
	public
	ObjectHelperCodeImplementation<RecordType> setup () {

		// parent id and code

		if (allOf (
			() -> isNotNull (model.parentField ()),
			() -> isNotNull (model.codeField ())
		)) { 

			parentIdAndCodeCache =
				new AdvancedCache.IdBuilder<Pair<Long,String>,Long,RecordType> ()

				.dummy (! allOf (				
					() -> model.parentField ().cacheable (),
					() -> model.codeField ().cacheable ()
				))

				.cacheNegatives (
					false)

				.lookupById (
					objectId ->
						Optional.fromNullable (
							objectDatabaseHelper.find (
								objectId)))

				.lookupByKey (
					key ->
						Optional.fromNullable (
							objectDatabaseHelper.findByParentAndCode (
								new GlobalId (
									model.parentTypeId (),
									key.getLeft ()),
								key.getRight ())))

				.getId (
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
	Optional<RecordType> findByCode (
			@NonNull GlobalId ancestorGlobalId,
			@NonNull String... codes) {

		if (codes.length == 1) {

			return Optional.fromNullable (
				objectDatabaseHelper.findByParentAndCode (
					ancestorGlobalId,
					codes [0]));

		}

		if (codes.length > 1) {

			ObjectHelper<?> parentHelper =
				objectManager.objectHelperForClassRequired (
					model.parentClass ());

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
			@NonNull String... codes) {

		Optional<RecordType> recordOptional =
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
	RecordType findByCodeOrNull (
			@NonNull GlobalId ancestorGlobalId,
			@NonNull String... codes) {

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
			@NonNull Supplier<? extends RuntimeException> orThrow) {

		Optional<RecordType> recordOptional =
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
	RecordType findByCodeOrThrow (
			@NonNull Record<?> ancestor,
			@NonNull String code,
			@NonNull Supplier<? extends RuntimeException> orThrow) {

		Optional<RecordType> recordOptional =
			findByCode (
				ancestor,
				code);

		if (
			isPresent (
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
			@NonNull Supplier<? extends RuntimeException> orThrow) {

		Optional<RecordType> recordOptional =
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

			throw orThrow.get ();

		}

	}

	@Override
	public
	RecordType findByCodeOrThrow (
			@NonNull Record<?> ancestor,
			@NonNull String code0,
			@NonNull String code1,
			@NonNull Supplier<? extends RuntimeException> orThrow) {

		Optional<RecordType> recordOptional =
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

			throw orThrow.get ();

		}

	}

	@Override
	public
	Optional<RecordType> findByCode (
			@NonNull Record<?> parent,
			@NonNull String... codes) {

		ObjectHelper<?> parentHelper =
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
			@NonNull Record<?> parent,
			@NonNull String... codes) {

		ObjectHelper<?> parentHelper =
			objectManager.objectHelperForClassRequired (
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
	RecordType findByCodeOrNull (
			@NonNull Record<?> parent,
			@NonNull String... codes) {

		ObjectHelper<?> parentHelper =
			objectManager.objectHelperForObject (
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
			objectManager.objectHelperForObject (
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
