package wbs.framework.object;

import static wbs.framework.utils.etc.LogicUtils.allOf;
import static wbs.framework.utils.etc.Misc.isNotNull;
import static wbs.framework.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.framework.utils.etc.OptionalUtils.optionalOrNull;
import static wbs.framework.utils.etc.StringUtils.stringFormat;

import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.base.Optional;

import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.entity.record.Record;
import wbs.framework.utils.cache.AdvancedCache;

@Accessors (fluent = true)
@PrototypeComponent ("objectHelperIndexImplementation")
public
class ObjectHelperIndexImplementation <RecordType extends Record <RecordType>>
	implements
		ObjectHelperComponent <RecordType>,
		ObjectHelperIndexMethods <RecordType> {

	// properties

	@Setter
	ObjectModel <RecordType> model;

	@Setter
	ObjectManager objectManager;

	@Setter
	ObjectHelper <RecordType> objectHelper;

	@Setter
	ObjectDatabaseHelper <RecordType> objectDatabaseHelper;

	// state

	AdvancedCache <Pair <Long ,Long>, RecordType> parentIdAndIndexCache;
	AdvancedCache <Pair <GlobalId, Long>, RecordType> parentGlobalIdAndIndexCache;

	// life cycle

	@Override
	public
	ObjectHelperIndexImplementation <RecordType> setup () {

		// parent id and index

		if (allOf (
			() -> isNotNull (model.parentField ()),
			() -> isNotNull (model.indexField ())
		)) {

			parentIdAndIndexCache =
				new AdvancedCache.IdBuilder<Pair<Long,Long>,Long,RecordType> ()

				.dummy (! allOf (
					() -> model.parentField ().cacheable (),
					() -> model.indexField ().cacheable ()
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
							objectDatabaseHelper.findByParentAndIndex (
								new GlobalId (
									model.parentTypeId (),
									key.getLeft ()),
								key.getRight ())))

				.getId (
					record ->
						record.getId ())

				.build ();

		}

		// parent global id and index

		if (allOf (
			() -> isNotNull (model.parentTypeField ()),
			() -> isNotNull (model.parentIdField ()),
			() -> isNotNull (model.indexField ())
		)) {

			parentGlobalIdAndIndexCache =
				new AdvancedCache.IdBuilder<Pair<GlobalId,Long>,Long,RecordType> ()

				.dummy (! allOf (
					() -> model.parentTypeField ().cacheable (),
					() -> model.parentIdField ().cacheable (),
					() -> model.indexField ().cacheable ()
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
							objectDatabaseHelper.findByParentAndIndex (
								key.getLeft (),
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
	Optional <RecordType> findByIndex (
			@NonNull Record <?> parent,
			@NonNull Long index) {

		if (model.canGetParent ()) {

			return parentIdAndIndexCache.get (
				Pair.of (
					parent.getId (),
					index));

		} else {

			ObjectHelper <?> parentHelper =
				objectManager.objectHelperForClassRequired (
					parent.getClass ());

			return parentGlobalIdAndIndexCache.get (
				Pair.of (
					GlobalId.of (
						parentHelper.objectTypeId (),
						parent.getId ()),
					index));

		}

	}

	@Override
	public
	RecordType findByIndexRequired (
			@NonNull Record <?> parent,
			@NonNull Long index) {

		Optional <RecordType> recordOptional =
			findByIndex (
				parent,
				index);

		if (
			optionalIsNotPresent (
				recordOptional)
		) {

			throw new RuntimeException (
				stringFormat (
					"Object not found with parent %s ",
					parent,
					"and index %s",
					index));

		}

		return recordOptional.get ();

	}

	@Override
	public
	RecordType findByIndexOrNull (
			@NonNull Record <?> parent,
			@NonNull Long index) {

		return optionalOrNull (
			findByIndex (
				parent,
				index));

	}

	@Override
	public
	List <RecordType> findByIndexRange (
			@NonNull GlobalId parentGlobalId,
			@NonNull Long indexStart,
			@NonNull Long indexEnd) {

		return objectDatabaseHelper.findByParentAndIndexRange (
			parentGlobalId,
			indexStart,
			indexEnd);

	}

	@Override
	public
	List <RecordType> findByIndexRange (
			@NonNull Record <?> parent,
			@NonNull Long indexStart,
			@NonNull Long indexEnd) {

		ObjectHelper <?> parentHelper =
			objectManager.objectHelperForObjectRequired (
				parent);

		GlobalId parentGlobalId =
			new GlobalId (
				parentHelper.objectTypeId (),
				parent.getId ());

		return objectDatabaseHelper.findByParentAndIndexRange (
			parentGlobalId,
			indexStart,
			indexEnd);

	}

	@Override
	public
	RecordType findByIndex (
			@NonNull GlobalId parentGlobalId,
			@NonNull Long index) {

		return objectDatabaseHelper.findByParentAndIndex (
			parentGlobalId,
			index);

	}

}
