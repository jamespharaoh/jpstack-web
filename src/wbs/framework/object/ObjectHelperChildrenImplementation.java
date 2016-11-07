package wbs.framework.object;

import static wbs.utils.etc.TypeUtils.classNotEqual;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.WeakSingletonDependency;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.entity.record.Record;

@Accessors (fluent = true)
@PrototypeComponent ("objectHelperChildrenImplementation")
public
class ObjectHelperChildrenImplementation <
	RecordType extends Record <RecordType>
>
	implements
		ObjectHelperChildrenMethods <RecordType>,
		ObjectHelperComponent <RecordType> {

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

	// implementation

	@Override
	public <ChildType extends Record <?>>
	List <ChildType> getChildren (
			@NonNull RecordType object,
			@NonNull Class <ChildType> childClass) {

		ObjectHelper <?> childHelper =
			objectManager.objectHelperForClassRequired (
				childClass);

		List <?> childrenTemp =
			childHelper.findByParent (
				objectHelper.getGlobalIdGeneric (
					object));

		@SuppressWarnings ("unchecked")
		List <ChildType> children =
			(List <ChildType>)
			childrenTemp;

		return children;


	}

	@Override
	public
	List <Record <?>> getMinorChildren (
			@NonNull RecordType object) {

		List <Record <?>> children =
			new ArrayList <Record <?>> ();

		GlobalId globalId =
			objectHelper.getGlobalId (
				object);

		for (
			ObjectHelper <?> childHelper
				: objectManager.objectHelpers ()
		) {

			if (! childHelper.minor ())
				continue;

			if (

				childHelper.parentTypeIsFixed ()

				&& classNotEqual (
					childHelper.parentClass (),
					objectHelper ().objectClass ())

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
	List <Record <?>> getChildren (
			@NonNull RecordType object) {

		List <Record <?>> children =
			new ArrayList <Record <?>> ();

		GlobalId globalId =
			objectHelper.getGlobalId (
				object);

		for (
			ObjectHelper <?> childHelper
				: objectManager.objectHelpers ()
		) {

			if (childHelper.isRoot ()) {
				continue;
			}

			if (

				childHelper.parentTypeIsFixed ()

				&& classNotEqual (
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
