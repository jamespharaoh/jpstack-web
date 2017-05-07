package wbs.framework.object;

import static wbs.utils.collection.CollectionUtils.collectionHasOneElement;
import static wbs.utils.collection.CollectionUtils.collectionIsEmpty;
import static wbs.utils.collection.CollectionUtils.collectionSize;
import static wbs.utils.collection.IterableUtils.iterableMapToList;
import static wbs.utils.etc.Misc.isNotNull;
import static wbs.utils.etc.Misc.isNull;
import static wbs.utils.etc.Misc.lessThan;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.OptionalUtils.optionalFromNullable;
import static wbs.utils.string.StringUtils.camelToSpaces;
import static wbs.utils.string.StringUtils.capitalise;
import static wbs.utils.string.StringUtils.joinWithCommaAndSpace;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.google.common.base.Optional;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.WeakSingletonDependency;
import wbs.framework.database.Transaction;
import wbs.framework.entity.record.Record;

import wbs.utils.etc.OptionalUtils;

@Accessors (fluent = true)
@PrototypeComponent ("objectHelperIdImplementation")
public
class ObjectHelperIdImplementation <
	RecordType extends Record <RecordType>
>
	implements
		ObjectHelperComponent <RecordType>,
		ObjectHelperIdMethods <RecordType> {

	// singleton dependencies

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
	Optional <RecordType> find (
			@NonNull Transaction parentTransaction,
			@NonNull Long id) {

		return optionalFromNullable (
			objectDatabaseHelper.find (
				parentTransaction,
				id));

	}

	@Override
	public
	RecordType findRequired (
			@NonNull Transaction parentTransaction,
			@NonNull Long id) {

		RecordType record =
			objectDatabaseHelper.find (
				parentTransaction,
				id);

		if (
			isNull (
				record)
		) {

			throw new RuntimeException (
				stringFormat (
					"%s with id %s not found",
					capitalise (
						camelToSpaces (
							objectModel.objectName ())),
					integerToDecimalString (
						id)));

		}

		return record;

	}

	@Override
	public
	RecordType findOrNull (
			@NonNull Transaction parentTransaction,
			@NonNull Long id) {

		return objectDatabaseHelper.find (
			parentTransaction,
			id);

	}

	@Override
	public
	RecordType findOrThrow (
			@NonNull Transaction parentTransaction,
			@NonNull Long id,
			@NonNull Supplier <? extends RuntimeException> orThrow) {

		RecordType object =
			objectDatabaseHelper.find (
				parentTransaction,
				id);

		if (
			isNotNull (
				object)
		) {

			return object;

		} else {

			throw (RuntimeException)
				orThrow.get ();

		}

	}

	@Override
	public
	List <Optional <RecordType>> findMany (
			@NonNull Transaction parentTransaction,
			@NonNull List <Long> ids) {

		return iterableMapToList (
			objectDatabaseHelper.findMany (
				parentTransaction,
				ids),
			OptionalUtils::optionalFromNullable);

	}

	@Override
	public
	List <RecordType> findManyRequired (
			@NonNull Transaction parentTransaction,
			@NonNull List <Long> ids) {

		List <RecordType> objects =
			objectDatabaseHelper.findMany (
				parentTransaction,
				ids);

		List <Long> missingIds =
			new ArrayList<> ();

		for (
			int index = 0;
			index < ids.size ();
			index ++
		) {

			if (
				isNotNull (
					objects.get (
						index))
			) {
				continue;
			}

			missingIds.add (
				ids.get (
					index));

		}

		if (
			collectionIsEmpty (
				missingIds)
		) {

			return objects;

		} else if (
			collectionHasOneElement (
				missingIds)
		) {

			throw new RuntimeException (
				stringFormat (
					"No such %s with id %s",
					camelToSpaces (
						objectModel.objectName ()),
					integerToDecimalString (
						missingIds.get (0))));

		} else if (
			lessThan (
				collectionSize (
					missingIds),
				6)
		) {

			throw new RuntimeException (
				stringFormat (
					"No such %s with ids %s",
					camelToSpaces (
						objectModel.objectName ()),
					joinWithCommaAndSpace (
						missingIds.stream ()
							.map (longValue -> longValue.toString ())
							.collect (Collectors.toList ()))));

		} else {

			throw new RuntimeException (
				stringFormat (
					"No such %s with ids %s (and %s others)",
					camelToSpaces (
						objectModel.objectName ()),
					joinWithCommaAndSpace (
						missingIds.subList (0, 5).stream ()
							.map (longValue -> longValue.toString ())
							.collect (Collectors.toList ())),
					integerToDecimalString (
						missingIds.size () - 5)));

		}

	}

}
