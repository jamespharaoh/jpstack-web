package wbs.framework.object;

import static wbs.utils.etc.TypeUtils.dynamicCast;
import static wbs.utils.etc.TypeUtils.isInstanceOf;
import static wbs.utils.string.StringUtils.stringFormat;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.entity.record.EphemeralRecord;
import wbs.framework.entity.record.Record;
import wbs.framework.entity.record.UnsavedRecordDetector;

@Accessors (fluent = true)
@PrototypeComponent ("objectHelperUpdateImplementation")
public
class ObjectHelperUpdateImplementation<RecordType extends Record<RecordType>>
	implements
		ObjectHelperComponent<RecordType>,
		ObjectHelperUpdateMethods<RecordType> {

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
	public <RecordTypeAgain extends Record<?>>
	RecordTypeAgain insert (
			@NonNull RecordTypeAgain objectUncast) {

		if (
			! model.objectClass ().isInstance (
				objectUncast)
		) {

			throw new ClassCastException (
				stringFormat (
					"Can't insert %s as %s",
					objectUncast.getClass ().getSimpleName (),
					model.objectClass ().getSimpleName ()));

		}

		@SuppressWarnings ("unchecked")
		RecordType object =
			(RecordType)
			objectUncast;

		objectDatabaseHelper.insert (
			object);

		UnsavedRecordDetector.instance.removeRecord (
			object);

		for (
			ObjectHelper<?> childObjectHelper
				: objectManager.objectHelpers ()
		) {

			childObjectHelper.createSingletons (
				objectHelper,
				object);

		}

		return objectUncast;

	}

	@Override
	public <RecordTypeAgain extends Record<?>>
	RecordTypeAgain insertSpecial (
			@NonNull RecordTypeAgain objectUncast) {

		if (
			! model.objectClass ().isInstance (
				objectUncast)
		) {

			throw new ClassCastException (
				stringFormat (
					"Can't insert %s as %s",
					objectUncast.getClass ().getSimpleName (),
					model.objectClass ().getSimpleName ()));

		}

		@SuppressWarnings ("unchecked")
		RecordType object =
			(RecordType)
			objectUncast;

		objectDatabaseHelper.insertSpecial (
			object);

		UnsavedRecordDetector.instance.removeRecord (
			object);

		for (
			ObjectHelper<?> childObjectHelper
				: objectManager.objectHelpers ()
		) {

			childObjectHelper.createSingletons (
				objectHelper,
				object);

		}

		return objectUncast;

	}

	@Override
	public <RecordTypeAgain extends Record<?>>
	RecordTypeAgain update (
			@NonNull RecordTypeAgain objectUncast) {

		if (
			! model.objectClass ().isInstance (
				objectUncast)
		) {

			throw new ClassCastException (
				stringFormat (
					"Can't update %s as %s",
					objectUncast.getClass ().getSimpleName (),
					model.objectClass ().getSimpleName ()));

		}

		@SuppressWarnings ("unchecked")
		RecordType object =
			(RecordType)
			objectUncast;

		objectDatabaseHelper.update (
			object);

		for (
			ObjectHelper<?> childObjectHelper
				: objectManager.objectHelpers ()
		) {

			childObjectHelper.createSingletons (
				objectHelper,
				object);

		}

		return objectUncast;

	}

	@Override
	public
	void createSingletons (
			@NonNull ObjectHelper<?> parentHelper,
			@NonNull Record<?> parentObject) {

		model.hooks ().createSingletons (
			objectHelper,
			parentHelper,
			parentObject);

	}

	@Override
	public <RecordTypeAgain extends Record<?>>
	RecordTypeAgain remove (
			@NonNull RecordTypeAgain objectUncast) {

		@SuppressWarnings ("unchecked")
		EphemeralRecord <RecordType> object =
			(EphemeralRecord <RecordType>)
			dynamicCast (
				model.objectClass (),
				objectUncast);

		objectDatabaseHelper.remove (
			object);

		return objectUncast;

	}

	@Override
	public
	RecordType createInstance () {

		try {

			@SuppressWarnings ("unchecked")
			Constructor<RecordType> constructor =
				(Constructor<RecordType>)
				model.objectClass ().getDeclaredConstructor ();

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
			@NonNull RecordTypeAgain objectUncast) {

		@SuppressWarnings ("unchecked")
		RecordType object =
			(RecordType)
			dynamicCast (
				model.objectClass (),
				objectUncast);

		objectDatabaseHelper.lock (
			object);

		return objectUncast;

	}

}
