package wbs.framework.object;

import static wbs.utils.etc.LogicUtils.allOf;
import static wbs.utils.etc.Misc.isNotNull;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.OptionalUtils.optionalOrNull;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.List;

import javax.inject.Provider;

import com.google.common.base.Optional;

import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.tuple.Pair;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.entity.record.Record;
import wbs.utils.cache.AdvancedCache;
import wbs.utils.cache.IdCacheBuilder;

@Accessors (fluent = true)
@PrototypeComponent ("objectHelperIndexImplementation")
public
class ObjectHelperIndexImplementation <RecordType extends Record <RecordType>>
	implements
		ObjectHelperComponent <RecordType>,
		ObjectHelperIndexMethods <RecordType> {

	// prototype dependencies

	@PrototypeDependency
	Provider <IdCacheBuilder <Pair <Long, Long>, Long, RecordType>>
	parentIdAndIndexCacheBuilderProvider;

	@PrototypeDependency
	Provider <IdCacheBuilder <Pair <GlobalId, Long>, Long, RecordType>>
	parentGlobalIdAndIndexCacheBuilderProvider;

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

	AdvancedCache <Pair <GlobalId, Long>, RecordType>
	parentGlobalIdAndIndexCache;

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
				parentIdAndIndexCacheBuilderProvider.get ()

				.dummy (! allOf (
					() -> model.parentField ().cacheable (),
					() -> model.indexField ().cacheable ()
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
							objectDatabaseHelper.findByParentAndIndex (
								new GlobalId (
									model.parentTypeId (),
									key.getLeft ()),
								key.getRight ())))

				.getIdFunction (
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
				parentGlobalIdAndIndexCacheBuilderProvider.get ()

				.dummy (! allOf (
					() -> model.parentTypeField ().cacheable (),
					() -> model.parentIdField ().cacheable (),
					() -> model.indexField ().cacheable ()
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
							objectDatabaseHelper.findByParentAndIndex (
								key.getLeft (),
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
	Optional <RecordType> findByIndex (
			@NonNull Record <?> parent,
			@NonNull Long index) {

		if (model.canGetParent ()) {

			return parentIdAndIndexCache.find (
				Pair.of (
					parent.getId (),
					index));

		} else {

			ObjectHelper <?> parentHelper =
				objectManager.objectHelperForClassRequired (
					parent.getClass ());

			return parentGlobalIdAndIndexCache.find (
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
