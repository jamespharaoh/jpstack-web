package wbs.framework.object;

import static wbs.framework.utils.etc.Misc.equal;
import static wbs.framework.utils.etc.Misc.split;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import lombok.NonNull;
import lombok.experimental.Accessors;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.database.Database;
import wbs.framework.record.EphemeralRecord;
import wbs.framework.record.GlobalId;
import wbs.framework.record.Record;
import wbs.framework.utils.etc.BeanLogic;

import com.google.common.base.Optional;

@Accessors (fluent = true)
@SingletonComponent ("objectManager")
public
class ObjectManagerImpl
	implements ObjectManager {

	// dependencies

	@Inject
	Database database;

	@Inject
	ObjectHelperBuilder objectHelperManager;

	@Inject
	ObjectTypeRegistry objectTypeRegistry;

	// state

	ObjectHelper<?> rootHelper;

	// init

	@PostConstruct
	public
	void init () {

		rootHelper =
			objectHelperManager.forObjectName (
				"root");

	}

	// implementation

	@Override
	public
	ObjectHelper<?> objectHelperForTypeCode (
			@NonNull String typeCode) {

		return objectHelperManager
			.forObjectTypeCode (typeCode);

	}

	@Override
	public
	ObjectHelper<?> objectHelperForObjectName (
			@NonNull String objectName) {

		return objectHelperManager
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
			objectHelperForClass (object.getClass ());

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

		return objectHelperForClass (
			object.getClass ());

	}

	@Override
	public
	ObjectHelper<?> objectHelperForClass (
			@NonNull Class<?> objectClass) {

		return objectHelperManager
			.forObjectClass (objectClass);

	}

	@Override
	public
	String objectPath (
			Record<?> dataObject) {

		return objectPath (
			dataObject,
			null,
			false,
			false);

	}

	@Override
	public
	String objectPathMini (
			Record<?> dataObject) {

		return objectPath (
			dataObject,
			null,
			true,
			false);

	}

	@Override
	public
	String objectPath (
			Record<?> dataObject,
			Record<?> root) {

		return objectPath (
			dataObject,
			root,
			false,
			false);

	}

	@Override
	public
	String objectPath (
			Record<?> dataObject,
			Record<?> root,
			boolean mini) {

		return objectPath (
			dataObject,
			root,
			mini,
			false);

	}

	@Override
	public
	String objectPath (
			Record<?> object,
			Record<?> assumedRoot,
			boolean mini,
			boolean preload) {

		if (object == null)
			return "-";

		ObjectHelper<?> objectHelper =
			objectHelperForObject (object);

		if (objectHelper.root ())
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
			! objectHelper.root ()
			&& object != assumedRoot
		);

		if (specificObjectHelper != null
				&& ! mini) {

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

		for (String string
				: partsToReturn) {

			stringBuilder.append (string);

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

		for (RecordType object
				: objects) {

			ret.put (
				objectPath (
					object,
					root,
					mini,
					false),
				object);

		}

		return ret;

	}

	@Override
	public <RecordType extends Record<?>>
	RecordType findChildByCode (
			@NonNull Class<RecordType> objectClass,
			@NonNull GlobalId parentGlobalId,
			@NonNull String code) {

		ObjectHelper<?> objectHelper =
			objectHelperForClass (objectClass);

		return objectClass.cast (
			objectHelper.findByCode (
				parentGlobalId,
				code));

	}

	@Override
	public <RecordType extends Record<?>>
	RecordType findChildByCode (
			@NonNull Class<RecordType> objectClass,
			@NonNull Record<?> parent,
			@NonNull String code) {

		return findChildByCode (
			objectClass,
			getGlobalId (parent),
			code);

	}

	@Override
	public <RecordType extends Record<?>>
	RecordType insert (
			@NonNull RecordType object) {

		ObjectHelper<?> objectHelper =
			objectHelperForClass (object.getClass ());

		return objectHelper.insert (
			object);

	}

	@Override
	public
	String getCode (
			@NonNull Record<?> object) {

		ObjectHelper<?> objectHelper =
			objectHelperForClass (object.getClass ());

		return objectHelper.getCode (object);

	}

	@Override
	public
	Record<?> findObject (
			@NonNull GlobalId objectGlobalId) {

		ObjectHelper<?> objectHelper =
			objectHelperManager.forObjectTypeId (
				objectGlobalId.typeId ());

		return objectHelper.find (
			objectGlobalId.objectId ());

	}

	@Override
	public
	List<Record<?>> getMinorChildren (
			@NonNull Record<?> parent) {

		ObjectHelper<?> objectHelper =
			objectHelperForObject (parent);

		return objectHelper
			.getMinorChildren (parent);

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
	int getObjectTypeId (
			@NonNull Record<?> object) {

		ObjectHelper<?> objectHelper =
			objectHelperForObject (object);

		return objectHelper.objectTypeId ();

	}

	@Override
	public
	int objectClassToTypeId (
			@NonNull Class<?> objectClass) {

		ObjectHelper<?> objectHelper =
			objectHelperForClass (objectClass);

		return objectHelper.objectTypeId ();

	}

	@Override
	public
	ObjectHelper<?> objectHelperForTypeId (
			Integer typeId) {

		return objectHelperManager
			.forObjectTypeId (typeId);

	}

	@Override
	public
	List<ObjectHelper<?>> objectHelpers () {

		return objectHelperManager
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
				Collections.singleton (object));

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

			if (currentHelper.root ())
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
			Object object,
			String path) {

		List<String> pathParts =
			split (
				path,
				"\\.");

		for (
			String pathPart
				: pathParts
		) {

			if (
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

			} else if (
				equal (
					pathPart,
					"root")
			) {

				object =
					rootHelper.find (0);

			} else {

				object =
					(Record<?>)
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
			objectHelperForClass (
				objectClass.get ());

		if (! objectHelper.parentTypeIsFixed ())
			return Optional.absent ();

		return Optional.<Class<?>>of (
			objectHelper.parentClass ());

	}

	public
	Optional<Class<?>> dereferenceType (
			@NonNull Optional<Class<?>> objectClass,
			@NonNull Optional<String> path) {

		if (! path.isPresent ())
			return objectClass;

		List<String> pathParts =
			split (
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
						BeanLogic.propertyClass (
							objectClass,
							pathPart));

			}

		}

		return objectClass;

	}

}
