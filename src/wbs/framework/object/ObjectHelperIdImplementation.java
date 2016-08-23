package wbs.framework.object;

import static wbs.framework.utils.etc.CollectionUtils.collectionHasOneElement;
import static wbs.framework.utils.etc.CollectionUtils.collectionSize;
import static wbs.framework.utils.etc.Misc.isEmpty;
import static wbs.framework.utils.etc.Misc.isNotNull;
import static wbs.framework.utils.etc.Misc.isNull;
import static wbs.framework.utils.etc.Misc.lessThan;
import static wbs.framework.utils.etc.StringUtils.camelToSpaces;
import static wbs.framework.utils.etc.StringUtils.capitalise;
import static wbs.framework.utils.etc.StringUtils.joinWithCommaAndSpace;
import static wbs.framework.utils.etc.StringUtils.stringFormat;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.google.common.base.Optional;

import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.record.Record;

@Accessors (fluent = true)
@PrototypeComponent ("objectHelperIdImplementation")
public 
class ObjectHelperIdImplementation<RecordType extends Record<RecordType>>
	implements
		ObjectHelperComponent<RecordType>,
		ObjectHelperIdMethods<RecordType> {

	// properties

	@Setter
	ObjectModel<RecordType> model;

	@Setter
	ObjectHelper<RecordType> objectHelper;

	@Setter
	ObjectManager objectManager;

	@Setter
	ObjectDatabaseHelper<RecordType> objectDatabaseHelper;

	// public implementation

	@Override
	public
	Optional<RecordType> find (
			@NonNull Long id) {

		return Optional.fromNullable (
			objectDatabaseHelper.find (
				id));

	}

	@Override
	public
	RecordType findRequired (
			@NonNull Long id) {

		RecordType record =
			objectDatabaseHelper.find (
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
							model.objectName ())),
					id));

		}

		return record;

	}

	@Override
	public
	RecordType findOrNull (
			@NonNull Long id) {

		return objectDatabaseHelper.find (
			id);

	}

	@Override
	public
	RecordType findOrThrow (
			@NonNull Long id,
			@NonNull Supplier<? extends RuntimeException> orThrow) {

		RecordType object =
			objectDatabaseHelper.find (
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
	List<RecordType> findManyRequired (
			@NonNull List<Long> ids) {

		List<RecordType> objects =
			objectDatabaseHelper.findMany (
				ids);

		List<Long> missingIds =
			new ArrayList<Long> ();

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
			isEmpty (
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
						model.objectName ()),
					missingIds.get (0)));

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
						model.objectName ()),
					joinWithCommaAndSpace (
						missingIds.stream ()
							.map (longValue -> longValue.toString ())
							.collect (Collectors.toList ()))));

		} else {

			throw new RuntimeException (
				stringFormat (
					"No such %s with ids %s (and %s others)",
					camelToSpaces (
						model.objectName ()),
					joinWithCommaAndSpace (
						missingIds.subList (0, 5).stream ()
							.map (longValue -> longValue.toString ())
							.collect (Collectors.toList ())),
					missingIds.size () - 5));

		}

	}

}
