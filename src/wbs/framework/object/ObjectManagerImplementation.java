package wbs.framework.object;

import static wbs.utils.collection.MapUtils.mapItemForKey;
import static wbs.utils.collection.MapUtils.mapItemForKeyOrThrow;
import static wbs.utils.collection.MapUtils.mapWithDerivedKey;
import static wbs.utils.etc.Misc.doNothing;
import static wbs.utils.etc.Misc.isNull;
import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.utils.etc.OptionalUtils.optionalOrNull;
import static wbs.utils.etc.TypeUtils.isSubclassOf;
import static wbs.utils.string.StringUtils.stringEqualSafe;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.string.StringUtils.stringSplitFullStop;
import static wbs.utils.string.StringUtils.stringStartsWithSimple;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.inject.Named;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import lombok.NonNull;
import lombok.experimental.Accessors;

import wbs.framework.component.annotations.NormalLifecycleSetup;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.entity.record.EphemeralRecord;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.entity.record.Record;
import wbs.utils.etc.PropertyUtils;

@Accessors (fluent = true)
@SingletonComponent ("objectManager")
public
class ObjectManagerImplementation
	implements ObjectManager {

	// singleton dependencies

	@SingletonDependency
	Database database;

	@SingletonDependency
	ObjectTypeRegistry objectTypeRegistry;

	@SingletonDependency
	@Named
	ObjectHelper <?> rootObjectHelper;

	// collection dependencies

	@SingletonDependency
	Map <String, ObjectHelper <?>> objectHelpersByComponentName;

	// state

	List <ObjectHelper <?>> objectHelpers;
	Map <String, ObjectHelper <?>> objectHelpersByName;
	Map <Long, ObjectHelper <?>> objectHelpersByTypeId;
	Map <String, ObjectHelper <?>> objectHelpersByTypeCode;
	Map <Class <?>, ObjectHelper <?>> objectHelpersByClass;

	// init

	@NormalLifecycleSetup
	public
	void init () {

		// index object helpers

		objectHelpers =
			ImmutableList.copyOf (
				objectHelpersByComponentName.values ());

		objectHelpersByName =
			mapWithDerivedKey (
				objectHelpers,
				ObjectHelper::objectName);

		objectHelpersByTypeId =
			mapWithDerivedKey (
				objectHelpers,
				ObjectHelper::objectTypeId);

		objectHelpersByTypeCode =
			mapWithDerivedKey (
				objectHelpers,
				ObjectHelper::objectTypeCode);

		objectHelpersByClass =
			mapWithDerivedKey (
				objectHelpers,
				ObjectHelper::objectClass);

		// inject us back into the object helpers

		objectHelpers.forEach (
			objectHelper -> {

			ObjectHelperImplementation objectHelperImplementation =
				(ObjectHelperImplementation)
				objectHelper;

			objectHelperImplementation.objectManager (
				this);

		});

	}

	// implementation

	@Override
	public
	ObjectHelper<?> objectHelperForTypeCodeRequired (
			@NonNull String typeCode) {

		return mapItemForKeyOrThrow (
			objectHelpersByTypeCode,
			typeCode,
			() -> new NoSuchElementException (
				stringFormat (
					"No object helper for type code '%s'",
					typeCode)));

	}

	@Override
	public
	ObjectHelper<?> objectHelperForObjectNameRequired (
			@NonNull String objectName) {

		return mapItemForKeyOrThrow (
			objectHelpersByName,
			objectName,
			() -> new NoSuchElementException (
				stringFormat (
					"No object helper for object name '%s'",
					objectName)));

	}

	@Override
	public <ChildType extends Record<ChildType>>
	List<ChildType> getChildren (
			@NonNull Record<?> object,
			@NonNull Class<ChildType> childClass) {

		ObjectHelper<?> objectHelper =
			objectHelperForObjectRequired (object);

		return objectHelper.getChildren (
			object,
			childClass);

	}

	@Override
	public
	Record<?> getParent (
			@NonNull Record<?> object) {

		ObjectHelper<?> objectHelper =
			objectHelperForClassRequired (
				object.getClass ());

		return objectHelper.getParent (
			object);

	}

	@Override
	public
	Class <?> objectClassForTypeCodeRequired (
			@NonNull String typeCode) {

		ObjectHelper <?> objectHelper =
			objectHelperForTypeCodeRequired (
				typeCode);

		return objectHelper.objectClass ();

	}

	@Override
	public
	ObjectHelper <?> objectHelperForObjectRequired (
			@NonNull Record <?> object) {

		return objectHelperForClassRequired (
			object.getClass ());

	}

	@Override
	public
	ObjectHelper <?> objectHelperForClassRequired (
			@NonNull Class <?> objectClass) {

		Class <?> currentObjectClass =
			objectClass;

		while (
			isSubclassOf (
				Record.class,
				currentObjectClass)
		) {

			Optional <ObjectHelper <?>> objectHelperOptional =
				mapItemForKey (
					objectHelpersByClass,
					currentObjectClass);

			if (
				optionalIsPresent (
					objectHelperOptional)
			) {

				return optionalGetRequired (
					objectHelperOptional);

			}

			currentObjectClass =
				currentObjectClass.getSuperclass ();

		}

		throw new NoSuchElementException (
			stringFormat (
				"No object helper for object class %s",
				objectClass.getSimpleName ()));

	}

	@Override
	public
	String objectPath (
			Record<?> object,
			@NonNull Optional<Record<?>> assumedRoot,
			boolean mini,
			boolean preload) {

		if (object == null)
			return "-";

		ObjectHelper<?> objectHelper =
			objectHelperForObjectRequired (object);

		if (objectHelper.isRoot ())
			return "root";

		List<String> partsToReturn =
			new ArrayList<String>();

		ObjectHelper<?> specificObjectHelper = null;

		do {

			// get some stuff

			Record<?> parent =
				objectHelper.getParent (object);

			ObjectHelper<?> parentHelper =
				objectHelperForObjectRequired (parent);

			// work out this part

			if (objectHelper.parentTypeIsFixed ()) {

				if (specificObjectHelper == null) {

					specificObjectHelper =
						objectHelper;

					if (partsToReturn.size () > 0)
						partsToReturn.add ("/");

				} else {

					partsToReturn.add (".");

				}

				partsToReturn.add (
					getCode (object));

			} else {

				if (specificObjectHelper != null) {

					if (mini) {

						mini = false;
					} else {

						partsToReturn.add (
							":");

						partsToReturn.add (
							specificObjectHelper.objectTypeCode ());

					}

					specificObjectHelper = null;

				}

				if (partsToReturn.size () > 0)
					partsToReturn.add ("/");

				partsToReturn.add (
					getCode (object));

				if (mini) {

					mini = false;

				} else {

					partsToReturn.add (
						":");

					partsToReturn.add (
						objectHelper.objectTypeCode ());

				}

			}

			// then move onto the parent

			object =
				parent;

			objectHelper =
				parentHelper;

		} while (
			! objectHelper.isRoot ()
			&& object != assumedRoot.orNull ()
		);

		if (
			specificObjectHelper != null
			&& ! mini
		) {

			partsToReturn.add (
				":");

			partsToReturn.add (
				specificObjectHelper.objectTypeCode ());

		}

		// reverse and assemble the result

		StringBuilder stringBuilder =
			new StringBuilder ();

		Collections.reverse (
			partsToReturn);

		for (
			String string
				: partsToReturn
		) {

			stringBuilder.append (
				string);

		}

		return stringBuilder.toString ();

	}

	@Override
	public
	String objectIdString (
			Record<?> object) {

		ObjectHelper<?> objectHelper =
			objectHelperForObjectRequired (object);

		return stringFormat (
			"%s#%s",
			objectHelper.objectTypeCode (),
			object.getId ());

	}

	@Override
	public
	GlobalId getGlobalId (
			@NonNull Record<?> object) {

		ObjectHelper<?> objectHelper =
			objectHelperForObjectRequired (object);

		return objectHelper.getGlobalId (
			object);

	}

	@Override
	public
	GlobalId getParentGlobalId (
			@NonNull Record<?> object) {

		ObjectHelper<?> objectHelper =
			objectHelperForObjectRequired (object);

		return objectHelper
			.getParentGlobalId (object);

	}

	@Override
	public <RecordType extends Record <?>>
	SortedMap <String, RecordType> pathMap (
			@NonNull Collection <RecordType> objects,
			Record <?> root,
			boolean mini) {

		SortedMap<String,RecordType> ret =
			new TreeMap<String,RecordType> ();

		for (
			RecordType object
				: objects
		) {

			ret.put (
				objectPath (
					object,
					Optional.<Record<?>>fromNullable (
						root),
					mini,
					false),
				object);

		}

		return ret;

	}

	@Override
	public <RecordType extends Record<?>>
	RecordType update (
			@NonNull RecordType object) {

		ObjectHelper<?> objectHelper =
			objectHelperForClassRequired (object.getClass ());

		return objectHelper.update (
			object);

	}

	@Override
	public
	String getCode (
			@NonNull Record<?> object) {

		ObjectHelper <?> objectHelper =
			objectHelperForClassRequired (
				object.getClass ());

		return objectHelper.getCode (
			object);

	}

	@Override
	public
	Record<?> findObject (
			@NonNull GlobalId objectGlobalId) {

		ObjectHelper <?> objectHelper =
			objectHelpersByTypeId.get (
				objectGlobalId.typeId ());

		return optionalOrNull (
			objectHelper.find (
				objectGlobalId.objectId ()));

	}

	@Override
	public
	List<Record<?>> getMinorChildren (
			@NonNull Record<?> parent) {

		ObjectHelper<?> objectHelper =
			objectHelperForObjectRequired (parent);

		return objectHelper.getMinorChildren (
			parent);

	}

	@Override
	public <ItemType extends EphemeralRecord<?>>
	ItemType remove (
			@NonNull ItemType object) {

		ObjectHelper<?> objectHelper =
			objectHelperForObjectRequired (object);

		return objectHelper.remove (
			object);

	}

	@Override
	public
	String getObjectTypeCode (
			@NonNull Record<?> object) {

		ObjectHelper<?> objectHelper =
			objectHelperForObjectRequired (object);

		return objectHelper.objectTypeCode ();

	}

	@Override
	public
	Long getObjectTypeId (
			@NonNull Record<?> object) {

		ObjectHelper<?> objectHelper =
			objectHelperForObjectRequired (object);

		return objectHelper.objectTypeId ();

	}

	@Override
	public
	Long objectClassToTypeId (
			@NonNull Class<?> objectClass) {

		ObjectHelper<?> objectHelper =
			objectHelperForClassRequired (
				objectClass);

		return objectHelper.objectTypeId ();

	}

	@Override
	public
	ObjectHelper <?> objectHelperForTypeId (
			@NonNull Long typeId) {

		return objectHelpersByTypeId.get (
			typeId);

	}

	@Override
	public
	List <ObjectHelper <?>> objectHelpers () {

		return objectHelpers;

	}

	@Override
	public
	boolean isParent (
			Record<?> object,
			Record<?> parent) {

		Record<?> foundParent =
			firstParent (
				object,
				Collections.singleton (parent));

		return foundParent != null;

	}

	@Override
	public <ParentType extends Record<?>>
	ParentType firstParent (
			Record<?> object,
			Set<ParentType> parents) {

		Record<?> current =
			object;

		ObjectHelper<?> currentHelper =
			objectHelperForObjectRequired (current);

		for (;;) {

			if (parents.contains (current)) {

				@SuppressWarnings ("unchecked")
				ParentType temp =
					(ParentType) current;

				return temp;

			}

			if (currentHelper.isRoot ())
				return null;

			current =
				currentHelper.getParent (current);

			currentHelper =
				objectHelperForObjectRequired (current);

		}

	}

	@Override
	public
	Object dereference (
			@NonNull Object object,
			@NonNull String path,
			@NonNull Map <String, Object> hints) {

		// check hints

		for (
			Map.Entry <String, Object> hintEntry
				: hints.entrySet ()
		) {

			if (
				stringEqualSafe (
					hintEntry.getKey (),
					path)
			) {

				return hintEntry.getValue ();

			} else if (
				stringStartsWithSimple (
					hintEntry.getKey () + ".",
					path)
			) {

				path =
					path.substring (
						hintEntry.getKey ().length () + 1);

				object =
					hintEntry.getValue ();

				break;

			}

		}

		// iterate through path

		List <String> pathParts =
			stringSplitFullStop (
				path);

		for (
			String pathPart
				: pathParts
		) {

			if (
				stringEqualSafe (
					pathPart,
					"root")
			) {

				object =
					rootObjectHelper.findRequired (
						0l);

			} else if (
				stringEqualSafe (
					pathPart,
					"this")
			) {

				// same object

			} else if (
				isNull (
					object)
			) {

				doNothing ();

			} else if (
				stringEqualSafe (
					pathPart,
					"parent")
			) {

				object =
					getParent (
						(Record<?>) object);

			} else if (
				stringEqualSafe (
					pathPart,
					"grandparent")
			) {

				object =
					getParent (
					getParent (
						(Record<?>)
						object));

			} else if (
				stringEqualSafe (
					pathPart,
					"greatgrandparent")
			) {

				object =
					getParent (
					getParent (
					getParent (
						(Record<?>)
						object)));

			} else {

				object =
					PropertyUtils.getProperty (
						object,
						pathPart);

			}

		}

		return object;

	}

	Optional<Class<?>> parentType (
			@NonNull Optional<Class<?>> objectClass) {

		if (! objectClass.isPresent ())
			return Optional.absent ();

		ObjectHelper<?> objectHelper =
			objectHelperForClassRequired (
				objectClass.get ());

		if (! objectHelper.parentTypeIsFixed ())
			return Optional.absent ();

		return Optional.<Class<?>>of (
			objectHelper.parentClass ());

	}

	@Override
	public
	Optional<Class<?>> dereferenceType (
			@NonNull Optional<Class<?>> objectClass,
			@NonNull Optional<String> path) {

		if (! path.isPresent ())
			return objectClass;

		List <String> pathParts =
			stringSplitFullStop (
				path.get ());

		for (
			String pathPart
				: pathParts
		) {

			if (! objectClass.isPresent ())
				return Optional.absent ();

			if (
				stringEqualSafe (
					pathPart,
					"this")
			) {

				doNothing ();

			} else if (
				stringEqualSafe (
					pathPart,
					"parent")
			) {

				objectClass =
					parentType (
						objectClass);

			} else if (
				stringEqualSafe (
					pathPart,
					"grandparent")
			) {

				objectClass =
					parentType (
					parentType (
						objectClass));

			} else if (
				stringEqualSafe (
					pathPart,
					"greatgrandparent")
			) {

				objectClass =
					parentType (
					parentType (
					parentType (
						objectClass)));

			} else if (
				stringEqualSafe (
					pathPart,
					"root")
			) {

				objectClass =
					Optional.of (
						rootObjectHelper.objectClass ());

			} else {

				objectClass =
					Optional.of (
						PropertyUtils.propertyClassForClass (
							objectClass.get (),
							pathPart));

			}

		}

		return objectClass;

	}

	@Override
	public
	<ObjectType extends Record <ObjectType>>
	Optional <ObjectType> getAncestor (
			Class <ObjectType> ancestorClass,
			Record <?> object) {

		for (;;) {

			// return if we found it

			if (
				ancestorClass.isInstance (
					object)
			) {

				return Optional.<ObjectType>of (
					ancestorClass.cast (
						object));

			}

			// stop at root

			if (
				rootObjectHelper.objectClass ().isInstance (
					object)
			) {

				return Optional.absent ();

			}

			// iterate via parent

			object =
				getParent (
					object);

		}

	}

}
