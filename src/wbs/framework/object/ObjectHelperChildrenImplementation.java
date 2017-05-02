package wbs.framework.object;

import static wbs.utils.etc.TypeUtils.classNotEqual;
import static wbs.utils.etc.TypeUtils.genericCastUnchecked;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.WeakSingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.entity.record.Record;
import wbs.framework.logging.LogContext;

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

	@ClassSingletonDependency
	LogContext logContext;

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
			@NonNull Transaction parentTransaction,
			@NonNull RecordType object,
			@NonNull Class <ChildType> childClass) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"getChildren");

		) {

			ObjectHelper <?> childHelper =
				objectManager.objectHelperForClassRequired (
					childClass);

			return genericCastUnchecked (
				childHelper.findByParent (
					transaction,
					objectHelper.getGlobalId (
						genericCastUnchecked (
							object))));

		}

	}

	@Override
	public
	List <Record <?>> getMinorChildren (
			@NonNull Transaction parentTransaction,
			@NonNull RecordType object) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"getMinorChildren");

		) {

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
						transaction,
						globalId));

			}

			return children;

		}

	}

	@Override
	public
	List <Record <?>> getChildren (
			@NonNull Transaction parentTransaction,
			@NonNull RecordType object) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"getChildren");

		) {

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
						transaction,
						globalId));

			}

			return children;

		}

	}

}
