package wbs.framework.object;

import static wbs.utils.etc.TypeUtils.dynamicCast;
import static wbs.utils.etc.TypeUtils.genericCastUnchecked;
import static wbs.utils.etc.TypeUtils.isInstanceOf;
import static wbs.utils.string.StringUtils.stringFormat;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.WeakSingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.entity.record.EphemeralRecord;
import wbs.framework.entity.record.Record;
import wbs.framework.entity.record.UnsavedRecordDetector;
import wbs.framework.logging.LogContext;

@Accessors (fluent = true)
@PrototypeComponent ("objectHelperUpdateImplementation")
public
class ObjectHelperUpdateImplementation <
	RecordType extends Record <RecordType>
>
	implements
		ObjectHelperComponent <RecordType>,
		ObjectHelperUpdateMethods <RecordType> {

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

	// public implementation

	@Override
	public <RecordTypeAgain extends Record <?>>
	RecordTypeAgain insert (
			@NonNull Transaction parentTransaction,
			@NonNull RecordTypeAgain objectUncast) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"insert");

		) {

			if (
				! objectModel.objectClass ().isInstance (
					objectUncast)
			) {

				throw new ClassCastException (
					stringFormat (
						"Can't insert %s as %s",
						objectUncast.getClass ().getSimpleName (),
						objectModel.objectClass ().getSimpleName ()));

			}

			RecordType object =
				genericCastUnchecked (
					objectUncast);

			objectDatabaseHelper.insert (
				transaction,
				object);

			UnsavedRecordDetector.instance.removeRecord (
				object);

			for (
				ObjectHelper<?> childObjectHelper
					: objectManager.objectHelpers ()
			) {

				childObjectHelper.createSingletons (
					transaction,
					objectHelper,
					object);

			}

			return objectUncast;

		}

	}

	@Override
	public <RecordTypeAgain extends Record<?>>
	RecordTypeAgain insertSpecial (
			@NonNull Transaction parentTransaction,
			@NonNull RecordTypeAgain objectUncast) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"insertSpecial");

		) {

			if (
				! objectModel.objectClass ().isInstance (
					objectUncast)
			) {

				throw new ClassCastException (
					stringFormat (
						"Can't insert %s as %s",
						objectUncast.getClass ().getSimpleName (),
						objectModel.objectClass ().getSimpleName ()));

			}

			RecordType object =
				genericCastUnchecked (
					objectUncast);

			objectDatabaseHelper.insertSpecial (
				transaction,
				object);

			UnsavedRecordDetector.instance.removeRecord (
				object);

			for (
				ObjectHelper<?> childObjectHelper
					: objectManager.objectHelpers ()
			) {

				childObjectHelper.createSingletons (
					transaction,
					objectHelper,
					object);

			}

			return objectUncast;

		}

	}

	@Override
	public <RecordTypeAgain extends Record<?>>
	RecordTypeAgain update (
			@NonNull Transaction parentTransaction,
			@NonNull RecordTypeAgain objectUncast) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"update");

		) {

			if (
				! objectModel.objectClass ().isInstance (
					objectUncast)
			) {

				throw new ClassCastException (
					stringFormat (
						"Can't update %s as %s",
						objectUncast.getClass ().getSimpleName (),
						objectModel.objectClass ().getSimpleName ()));

			}

			RecordType object =
				genericCastUnchecked (
					objectUncast);

			objectDatabaseHelper.update (
				transaction,
				object);

			for (
				ObjectHelper<?> childObjectHelper
					: objectManager.objectHelpers ()
			) {

				childObjectHelper.createSingletons (
					transaction,
					objectHelper,
					object);

			}

			return objectUncast;

		}

	}

	@Override
	public
	void createSingletons (
			@NonNull Transaction parentTransaction,
			@NonNull ObjectHelper <?> parentHelper,
			@NonNull Record <?> parentObject) {

		objectModel.hooks ().createSingletons (
			parentTransaction,
			objectHelper,
			parentHelper,
			parentObject);

	}

	@Override
	public <RecordTypeAgain extends Record<?>>
	RecordTypeAgain remove (
			@NonNull Transaction parentTransaction,
			@NonNull RecordTypeAgain objectUncast) {

		EphemeralRecord <RecordType> object =
			(EphemeralRecord <RecordType>)
			dynamicCast (
				objectModel.objectClass (),
				objectUncast);

		objectDatabaseHelper.remove (
			parentTransaction,
			object);

		return objectUncast;

	}

	@Override
	public
	RecordType createInstance () {

		try {

			Constructor <RecordType> constructor =
				objectModel.objectClass ().getDeclaredConstructor ();

			constructor.setAccessible (
				true);

			RecordType object =
				(RecordType)
				constructor.newInstance ();

			UnsavedRecordDetector.instance.addRecord (
				object);

			return object;

		} catch (SecurityException securityException) {

			throw new RuntimeException (
				securityException);

		} catch (NoSuchMethodException noSuchMethodException) {

			throw new RuntimeException (
				noSuchMethodException);

		} catch (InvocationTargetException invocationTargetException) {

			if (
				isInstanceOf (
					RuntimeException.class,
					invocationTargetException)
			) {

				throw (RuntimeException)
					invocationTargetException.getTargetException ();

			} else {

				throw new RuntimeException (
					invocationTargetException.getTargetException ());

			}

		} catch (IllegalAccessException illegalAccessException) {

			throw new RuntimeException (
				illegalAccessException);

		} catch (InstantiationException instantiationException) {

			throw new RuntimeException (
				instantiationException);

		}

	}

	@Override
	public <RecordTypeAgain extends Record<?>>
	RecordTypeAgain lock (
			@NonNull Transaction parentTransaction,
			@NonNull RecordTypeAgain objectUncast) {

		RecordType object =
			dynamicCast (
				objectModel.objectClass (),
				objectUncast);

		objectDatabaseHelper.lock (
			parentTransaction,
			object);

		return objectUncast;

	}

}
