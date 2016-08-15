package wbs.framework.object;

import static wbs.framework.utils.etc.Misc.notEqual;

import java.util.ArrayList;
import java.util.List;

import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.record.GlobalId;
import wbs.framework.record.Record;

@Accessors (fluent = true)
@PrototypeComponent ("objectHelperChildrenImplementation")
public
class ObjectHelperChildrenImplementation<RecordType extends Record<RecordType>>
	implements
		ObjectHelperChildrenMethods<RecordType>,
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

	// implementation

	@Override
	public <ChildType extends Record<?>>
	List<ChildType> getChildren (
			@NonNull Record<?> object,
			@NonNull Class<ChildType> childClass) {

		ObjectHelper<?> childHelper =
			objectManager.objectHelperForClassRequired (
				childClass);

		List<?> childrenTemp =
			childHelper.findByParent (
				objectHelper.getGlobalId (
					object));

		@SuppressWarnings ("unchecked")
		List<ChildType> children =
			(List<ChildType>)
			childrenTemp;

		return children;


	}

	@Override
	public
	List<Record<?>> getMinorChildren (
			@NonNull Record<?> object) {

		List<Record<?>> children =
			new ArrayList<Record<?>> ();

		GlobalId globalId =
			objectHelper.getGlobalId (
				object);

		for (
			ObjectHelper<?> childHelper
				: objectManager.objectHelpers ()
		) {

			if (! childHelper.minor ())
				continue;

			if (

				childHelper.parentTypeIsFixed ()

				&& notEqual (
					childHelper.parentClass (),
					model.objectClass ())

			) {
				continue;
			}

			children.addAll (
				childHelper.findByParent (
					globalId));

		}

		return children;

	}

	@Override
	public
	List<Record<?>> getChildren (
			@NonNull Record<?> object) {

		List<Record<?>> children =
			new ArrayList<Record<?>> ();

		GlobalId globalId =
			objectHelper.getGlobalId (
				object);

		for (
			ObjectHelper<?> childHelper
				: objectManager.objectHelpers ()
		) {

			if (childHelper.isRoot ()) {
				continue;
			}

			if (

				childHelper.parentTypeIsFixed ()

				&& notEqual (
					childHelper.parentClass (),
					objectHelper.objectClass ())

			) {
				continue;
			}

			children.addAll (
				childHelper.findByParent (
					globalId));

		}

		return children;

	}

}
