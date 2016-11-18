package wbs.framework.object;

import static wbs.utils.etc.LogicUtils.allOf;
import static wbs.utils.etc.Misc.isNotNull;
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

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.WeakSingletonDependency;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.entity.record.Record;

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

	@WeakSingletonDependency
	ObjectManager objectManager;

	// prototype dependencies

	@PrototypeDependency
	Provider <IdCacheBuilder <Pair <Long, Long>, Long, RecordType>>
	parentIdAndIndexCacheBuilderProvider;

	@PrototypeDependency
	Provider <IdCacheBuilder <Pair <GlobalId, Long>, Long, RecordType>>
	parentGlobalIdAndIndexCacheBuilderProvider;

	// properties

	@Getter @Setter
	ObjectModel <RecordType> objectModel;

	@Getter @Setter
	ObjectHelper <RecordType> objectHelper;

	@Getter @Setter
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
			() -> isNotNull (objectModel.parentField ()),
			() -> isNotNull (objectModel.indexField ())
		)) {

			parentIdAndIndexCache =
				parentIdAndIndexCacheBuilderProvider.get ()

				.dummy (! allOf (
					() -> objectModel.parentField ().cacheable (),
					() -> objectModel.indexField ().cacheable ()
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
									objectModel.parentTypeId (),
									key.getLeft ()),
								key.getRight ())))

				.getIdFunction (
					record ->
						record.getId ())

				.build ();

		}

		// parent global id and index

		if (allOf (
			() -> isNotNull (objectModel.parentTypeField ()),
			() -> isNotNull (objectModel.parentIdField ()),
			() -> isNotNull (objectModel.indexField ())
		)) {

			parentGlobalIdAndIndexCache =
				parentGlobalIdAndIndexCacheBuilderProvider.get ()

				.dummy (! allOf (
					() -> objectModel.parentTypeField ().cacheable (),
					() -> objectModel.parentIdField ().cacheable (),
					() -> objectModel.indexField ().cacheable ()
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

		if (objectModel.canGetParent ()) {

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
					objectManager.objectPath (
						parent),
					"and index %s",
					integerToDecimalString (
						index)));

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
