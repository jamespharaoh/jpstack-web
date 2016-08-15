package wbs.framework.object;

import static wbs.framework.utils.etc.Misc.doNothing;
import static wbs.framework.utils.etc.Misc.equal;
import static wbs.framework.utils.etc.Misc.isNull;
import static wbs.framework.utils.etc.OptionalUtils.optionalOrNull;
import static wbs.framework.utils.etc.StringUtils.startsWith;
import static wbs.framework.utils.etc.StringUtils.stringFormat;
import static wbs.framework.utils.etc.StringUtils.stringSplitRegexp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import com.google.common.base.Optional;

import lombok.NonNull;
import lombok.experimental.Accessors;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.database.Database;
import wbs.framework.object.ObjectHelperBuilder.ObjectHelperBuilderMethods;
import wbs.framework.record.EphemeralRecord;
import wbs.framework.record.GlobalId;
import wbs.framework.record.Record;
import wbs.framework.utils.etc.BeanLogic;

@Accessors (fluent = true)
@SingletonComponent ("objectManager")
public
class ObjectManagerImplementation
	implements ObjectManager {

	// dependencies

	@Inject
	Database database;

	@Inject
	ObjectHelperBuilder objectHelperBuilder;

	@Inject
	ObjectTypeRegistry objectTypeRegistry;

	// state

	ObjectHelper<?> rootHelper;

	// init

	@PostConstruct
	public
	void init () {

		// lookup root object helper, which we use a lot

		rootHelper =
			objectHelperBuilder.forObjectName (
				"root");

		// inject us back into the object helpers

		objectHelperBuilder.asList ().forEach (
			objectHelper -> {

			ObjectHelperBuilderMethods objectHelperBuilder =
				(ObjectHelperBuilderMethods)
				objectHelper;

			objectHelperBuilder.objectManager (
				this);

		});

	}

	// implementation

	@Override
	public
	ObjectHelper<?> objectHelperForTypeCode (
			@NonNull String typeCode) {

		return objectHelperBuilder.forObjectTypeCode (
			typeCode);

	}

	@Override
	public
	ObjectHelper<?> objectHelperForObjectName (
			@NonNull String objectName) {

		return objectHelperBuilder
			.forObjectName (objectName);

	}

	@Override
	public <ChildType extends Record<ChildType>>
	List<ChildType> getChildren (
			@NonNull Record<?> object,
			@NonNull Class<ChildType> childClass) {

		ObjectHelper<?> objectHelper =
			objectHelperForObject (object);

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
	Class<?> objectTypeCodeToClass (
			@NonNull String typeCode) {

		ObjectHelper<?> objectHelper =
			objectHelperForTypeCode (typeCode);

		return objectHelper.objectClass ();

	}

	@Override
	public
	ObjectHelper<?> objectHelperForObject (
			@NonNull Record<?> object) {

		return objectHelperForClassRequired (
			object.getClass ());

	}

	@Override
	public
	ObjectHelper<?> objectHelperForClassRequired (
			@NonNull Class<?> objectClass) {

		return objectHelperBuilder.forObjectClassRequired (
			objectClass);

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
			objectHelperForObject (object);

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
				objectHelperForObject (parent);

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
			objectHelperForObject (object);

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
			objectHelperForObject (object);

		return objectHelper.getGlobalId (
			object);

	}

	@Override
	public
	GlobalId getParentGlobalId (
			@NonNull Record<?> object) {

		ObjectHelper<?> objectHelper =
			objectHelperForObject (object);

		return objectHelper
			.getParentGlobalId (object);

	}

	@Override
	public <RecordType extends Record<?>>
	SortedMap<String,RecordType> pathMap (
			@NonNull Collection<RecordType> objects,
			Record<?> root,
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

		ObjectHelper<?> objectHelper =
			objectHelperForClassRequired (object.getClass ());

		return objectHelper.getCode (object);

	}

	@Override
	public
	Record<?> findObject (
			@NonNull GlobalId objectGlobalId) {

		ObjectHelper<?> objectHelper =
			objectHelperBuilder.forObjectTypeId (
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
			objectHelperForObject (parent);

		return objectHelper.getMinorChildren (
			parent);

	}

	@Override
	public <ItemType extends EphemeralRecord<?>>
	ItemType remove (
			@NonNull ItemType object) {

		ObjectHelper<?> objectHelper =
			objectHelperForObject (object);

		return objectHelper.remove (
			object);

	}

	@Override
	public
	String getObjectTypeCode (
			@NonNull Record<?> object) {

		ObjectHelper<?> objectHelper =
			objectHelperForObject (object);

		return objectHelper.objectTypeCode ();

	}

	@Override
	public
	Long getObjectTypeId (
			@NonNull Record<?> object) {

		ObjectHelper<?> objectHelper =
			objectHelperForObject (object);

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
	ObjectHelper<?> objectHelperForTypeId (
			@NonNull Long typeId) {

		return objectHelperBuilder.forObjectTypeId (
			typeId);

	}

	@Override
	public
	List<ObjectHelper<?>> objectHelpers () {

		return objectHelperBuilder
			.asList ();

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
			objectHelperForObject (current);

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
				objectHelperForObject (current);

		}

	}

	@Override
	public
	Object dereference (
			@NonNull Object object,
			@NonNull String path,
			@NonNull Map<String,Object> hints) {

		// check hints

		for (
			Map.Entry<String,Object> hintEntry
				: hints.entrySet ()
		) {

			if (
				equal (
					hintEntry.getKey (),
					path)
			) {

				return hintEntry.getValue ();

			} else if (
				startsWith (
					path,
					hintEntry.getKey () + ".")
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

		List<String> pathParts =
			stringSplitRegexp (
				path,
				"\\.");

		for (
			String pathPart
				: pathParts
		) {

			if (
				equal (
					pathPart,
					"root")
			) {

				object =
					rootHelper.findRequired (
						0);

			} else if (
				equal (
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
				equal (
					pathPart,
					"parent")
			) {

				object =
					getParent (
						(Record<?>) object);

			} else if (
				equal (
					pathPart,
					"grandparent")
			) {

				object =
					getParent (
					getParent (
						(Record<?>)
						object));

			} else if (
				equal (
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
					BeanLogic.getProperty (
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

		List<String> pathParts =
			stringSplitRegexp (
				path.get (),
				"\\.");

		for (
			String pathPart
				: pathParts
		) {

			if (! objectClass.isPresent ())
				return Optional.absent ();

			if (
				equal (
					pathPart,
					"this")
			) {

				doNothing ();

			} else if (
				equal (
					pathPart,
					"parent")
			) {

				objectClass =
					parentType (
						objectClass);

			} else if (
				equal (
					pathPart,
					"grandparent")
			) {

				objectClass =
					parentType (
					parentType (
						objectClass));

			} else if (
				equal (
					pathPart,
					"greatgrandparent")
			) {

				objectClass =
					parentType (
					parentType (
					parentType (
						objectClass)));

			} else if (
				equal (
					pathPart,
					"root")
			) {

				objectClass =
					Optional.<Class<?>>of (
						rootHelper.objectClass ());

			} else {

				objectClass =
					Optional.<Class<?>>of (
						BeanLogic.propertyClassForClass (
							objectClass.get (),
							pathPart));

			}

		}

		return objectClass;

	}

	@Override
	public
	<ObjectType extends Record<ObjectType>>
	Optional<ObjectType> getAncestor (
			Class<ObjectType> ancestorClass,
			Record<?> object) {

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
				rootHelper.objectClass ().isInstance (
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
