package wbs.framework.object;

import static wbs.utils.collection.CollectionUtils.collectionIsNotEmpty;
import static wbs.utils.collection.CollectionUtils.collectionSize;
import static wbs.utils.collection.CollectionUtils.listFirstElementRequired;
import static wbs.utils.collection.CollectionUtils.listSlice;
import static wbs.utils.collection.CollectionUtils.listSliceAllButFirstItemRequired;
import static wbs.utils.collection.MapUtils.mapContainsKey;
import static wbs.utils.collection.MapUtils.mapItemForKey;
import static wbs.utils.collection.MapUtils.mapItemForKeyOrThrow;
import static wbs.utils.collection.MapUtils.mapItemForKeyRequired;
import static wbs.utils.collection.MapUtils.mapWithDerivedKey;
import static wbs.utils.etc.Misc.doNothing;
import static wbs.utils.etc.Misc.isNull;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.NumberUtils.notMoreThan;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalFromNullable;
import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.utils.etc.OptionalUtils.optionalMapRequired;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.etc.OptionalUtils.optionalOrNull;
import static wbs.utils.etc.OptionalUtils.optionalOrThrow;
import static wbs.utils.etc.PropertyUtils.propertyGetAuto;
import static wbs.utils.etc.ResultUtils.errorResultFormat;
import static wbs.utils.etc.ResultUtils.getError;
import static wbs.utils.etc.ResultUtils.isError;
import static wbs.utils.etc.ResultUtils.resultValueRequired;
import static wbs.utils.etc.ResultUtils.successResult;
import static wbs.utils.etc.TypeUtils.genericCastUnchecked;
import static wbs.utils.etc.TypeUtils.isSubclassOf;
import static wbs.utils.string.StringUtils.joinWithFullStop;
import static wbs.utils.string.StringUtils.stringEqualSafe;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.string.StringUtils.stringInSafe;
import static wbs.utils.string.StringUtils.stringSplitFullStop;

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

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.NormalLifecycleSetup;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.entity.record.EphemeralRecord;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.entity.record.Record;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

import wbs.utils.etc.PropertyUtils;

import fj.data.Either;

@Accessors (fluent = true)
@SingletonComponent ("objectManager")
public
class ObjectManagerImplementation
	implements ObjectManager {

	// singleton dependencies

	@SingletonDependency
	Database database;

	@ClassSingletonDependency
	LogContext logContext;

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
	void setup (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"setup");

		) {

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

				ObjectHelperImplementation <?> objectHelperImplementation =
					(ObjectHelperImplementation <?>)
					objectHelper;

				objectHelperImplementation.objectManager (
					taskLogger,
					this);

			});

		}

	}

	// implementation

	Optional <ObjectHelper <?>> objectHelperForTypeCode (
			@NonNull String typeCode) {

		return mapItemForKey (
			objectHelpersByTypeCode,
			typeCode);

	}

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
	public <ChildType extends Record <ChildType>>
	List <ChildType> getChildren (
			@NonNull Transaction parentTransaction,
			@NonNull Record <?> object,
			@NonNull Class <ChildType> childClass) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"getChildren");

		) {

			ObjectHelper <?> objectHelper =
				objectHelperForObjectRequired (
					object);

			return objectHelper.getChildrenGeneric (
				transaction,
				object,
				childClass);

		}

	}

	@Override
	public
	Either <Optional <Record <?>>, String> getParentOrError (
			@NonNull Transaction parentTransaction,
			@NonNull Record <?> object) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"getParentOrError");

		) {

			ObjectHelper <?> objectHelper =
				objectHelperForClassRequired (
					object.getClass ());

			return objectHelper.getParentOrError (
				transaction,
				genericCastUnchecked (
					object));

		}

	}

	@Override
	public
	Optional <Class <?>> objectClassForTypeCode (
			@NonNull String typeCode) {

		Optional <ObjectHelper <?>> objectHelperOptional =
				objectHelperForTypeCode (
					typeCode);

		return optionalMapRequired (
			objectHelperOptional,
			ObjectHelper::objectClass);

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
			@NonNull Transaction parentTransaction,
			Record <?> startingObject,
			@NonNull Optional <Record <?>> assumedRoot,
			boolean startingMini,
			boolean preload) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"objectPath");

		) {

			if (startingObject == null)
				return "-";

			Record <?> currentObject =
				startingObject;

			ObjectHelper <?> objectHelper =
				objectHelperForObjectRequired (
					currentObject);

			if (objectHelper.isRoot ())
				return "root";

			List <String> partsToReturn =
				new ArrayList<> ();

			ObjectHelper <?> specificObjectHelper = null;

			Boolean currentMini =
				startingMini;

			do {

				// get some stuff

				Record <?> parent =
					objectHelper.getParentRequired (
						transaction,
						genericCastUnchecked (
							currentObject));

				ObjectHelper <?> parentHelper =
					objectHelperForObjectRequired (
						parent);

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
						getCode (
							transaction,
							currentObject));

				} else {

					if (specificObjectHelper != null) {

						if (currentMini) {

							currentMini = false;

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
						getCode (
							transaction,
							currentObject));

					if (currentMini) {

						currentMini = false;

					} else {

						partsToReturn.add (
							":");

						partsToReturn.add (
							objectHelper.objectTypeCode ());

					}

				}

				// then move onto the parent

				currentObject =
					parent;

				objectHelper =
					parentHelper;

			} while (
				! objectHelper.isRoot ()
				&& currentObject != assumedRoot.orNull ()
			);

			if (
				specificObjectHelper != null
				&& ! currentMini
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
			integerToDecimalString (
				object.getId ()));

	}

	@Override
	public
	GlobalId getGlobalId (
			@NonNull Transaction parentTransaction,
			@NonNull Record <?> object) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"getGlobalId");

		) {

			ObjectHelper <?> objectHelper =
				objectHelperForObjectRequired (
					object);

			return objectHelper.getGlobalId (
				genericCastUnchecked (
					object));

		}

	}

	@Override
	public
	GlobalId getParentGlobalId (
			@NonNull Transaction parentTransaction,
			@NonNull Record <?> object) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"getParentGlobalId");

		) {

			ObjectHelper <?> objectHelper =
				objectHelperForObjectRequired (
					object);

			return objectHelper.getParentGlobalId (
				transaction,
				genericCastUnchecked (
					object));

		}

	}

	@Override
	public <RecordType extends Record <?>>
	SortedMap <String, RecordType> pathMap (
			@NonNull Transaction parentTransaction,
			@NonNull Collection <RecordType> objects,
			Record <?> root,
			boolean mini) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"pathMap");

		) {

			SortedMap <String, RecordType> ret =
				new TreeMap<> ();

			for (
				RecordType object
					: objects
			) {

				ret.put (
					objectPath (
						transaction,
						object,
						optionalFromNullable (
							root),
						mini,
						false),
					object);

			}

			return ret;

		}

	}

	@Override
	public <RecordType extends Record <?>>
	RecordType update (
			@NonNull Transaction parentTransaction,
			@NonNull RecordType object) {

		ObjectHelper <?> objectHelper =
			objectHelperForClassRequired (
				object.getClass ());

		return objectHelper.update (
			parentTransaction,
			object);

	}

	@Override
	public
	String getCode (
			@NonNull Transaction parentTransaction,
			@NonNull Record <?> object) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"getCode");

		) {

			ObjectHelper <?> objectHelper =
				objectHelperForClassRequired (
					object.getClass ());

			return objectHelper.getCode (
				genericCastUnchecked (
					object));

		}

	}

	@Override
	public
	Record <?> findObject (
			@NonNull Transaction parentTransaction,
			@NonNull GlobalId objectGlobalId) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"findObject");

		) {

			ObjectHelper <?> objectHelper =
				objectHelpersByTypeId.get (
					objectGlobalId.typeId ());

			return optionalOrNull (
				objectHelper.find (
					transaction,
					objectGlobalId.objectId ()));

		}

	}

	@Override
	public
	List <Record <?>> getMinorChildren (
			@NonNull Transaction parentTransaction,
			@NonNull Record <?> parent) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"getMinorChildren");

		) {

			ObjectHelper <?> objectHelper =
				objectHelperForObjectRequired (
					parent);

			return objectHelper.getMinorChildrenGeneric (
				transaction,
				parent);

		}

	}

	@Override
	public <ItemType extends EphemeralRecord<?>>
	ItemType remove (
			@NonNull Transaction parentTransaction,
			@NonNull ItemType object) {

		ObjectHelper <?> objectHelper =
			objectHelperForObjectRequired (
				object);

		return objectHelper.remove (
			parentTransaction,
			object);

	}

	@Override
	public
	String getObjectTypeCode (
			@NonNull Transaction parentTransaction,
			@NonNull Record <?> object) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"getObjectTypeCode");

		) {

			ObjectHelper <?> objectHelper =
				objectHelperForObjectRequired (
					object);

			return objectHelper.objectTypeCode ();

		}

	}

	@Override
	public
	Long getObjectTypeId (
			@NonNull Transaction parentTransaction,
			@NonNull Record<?> object) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"getObjectTypeId");

		) {

			ObjectHelper <?> objectHelper =
				objectHelperForObjectRequired (
					object);

			return objectHelper.objectTypeId ();

		}

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
	Optional <ObjectHelper <?>> objectHelperForTypeId (
			@NonNull Long typeId) {

		return optionalFromNullable  (
			objectHelpersByTypeId.get (
				typeId));

	}

	@Override
	public
	ObjectHelper <?> objectHelperForTypeIdRequired (
			@NonNull Long typeId) {

		return optionalOrThrow (
			optionalFromNullable (
				objectHelpersByTypeId.get (
					typeId)),
			() -> new NoSuchElementException (
				stringFormat (
					"No object helper with type id %s",
					integerToDecimalString (
						typeId))));

	}

	@Override
	@Deprecated
	public
	ObjectHelper <?> objectHelperForTypeIdOrNull (
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
			@NonNull Transaction parentTransaction,
			@NonNull Record <?> object,
			@NonNull Record <?> parent) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"isParent");

		) {

			Record <?> foundParent =
				firstParent (
					transaction,
					object,
					Collections.singleton (
						parent));

			return foundParent != null;

		}

	}

	@Override
	public <ParentType extends Record <?>>
	ParentType firstParent (
			@NonNull Transaction parentTransaction,
			@NonNull Record <?> object,
			@NonNull Set <ParentType> parents) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"firstParent");

		) {

			Record <?> current =
				object;

			ObjectHelper <?> currentHelper =
				objectHelperForObjectRequired (
					current);

			for (;;) {

				if (parents.contains (current)) {

					ParentType temp =
						genericCastUnchecked (
							current);

					return temp;

				}

				if (currentHelper.isRoot ())
					return null;

				current =
					currentHelper.getParentRequired (
						transaction,
						genericCastUnchecked (
							current));

				currentHelper =
					objectHelperForObjectRequired (
						current);

			}

		}

	}

	@Override
	public
	Either <Optional <Object>, String> dereferenceOrError (
			@NonNull Transaction parentTransaction,
			@NonNull Object startingObject,
			@NonNull String path,
			@NonNull Map <String, Object> hints) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"dereferenceOrError");

		) {

			Object currentObject =
				startingObject;

			List <String> pathParts =
				stringSplitFullStop (
					path);

			// check hints

			for (
				long numParts = 1l;
				notMoreThan (
					numParts,
					collectionSize (
						pathParts));
				numParts ++
			) {

				String pathPrefix =
					joinWithFullStop (
						listSlice (
							pathParts,
							0,
							numParts));

				if (
					mapContainsKey (
						hints,
						pathPrefix)
				) {

					// found in hints

					currentObject =
						mapItemForKeyRequired (
							hints,
							pathPrefix);

					pathParts =
						listSlice (
							pathParts,
							numParts,
							collectionSize (
								pathParts));

				}

			}

			// check global values

			if (
				collectionIsNotEmpty (
					pathParts)
			) {

				String firstPathPart =
					listFirstElementRequired (
						pathParts);

				if (
					stringEqualSafe (
						firstPathPart,
						"root")
				) {

					// root

					currentObject =
						rootObjectHelper.findRequired (
							transaction,
							0l);

					pathParts =
						listSliceAllButFirstItemRequired (
							pathParts);

				} else if (
					stringEqualSafe (
						firstPathPart,
						"this")
				) {

					// same object

					pathParts =
						listSliceAllButFirstItemRequired (
							pathParts);

				}

			}

			// iterate through path

			for (
				String pathPart
					: pathParts
			) {

				if (
					stringInSafe (
						pathPart,
						"parent",
						"grandparent",
						"greatgrandparent")
				) {

					// parent

					Either <Record <?>, String> parentOrError =
						getParentRequiredOrError (
							transaction,
							genericCastUnchecked (
								currentObject));

					if (
						isError (
							parentOrError)
					) {

						return errorResultFormat (
							"Error getting parent of %s: %s",
							currentObject.toString (),
							getError (
								parentOrError));

					}

					currentObject =
						resultValueRequired (
							parentOrError);

					// grandparent

					if (
						stringInSafe (
							pathPart,
							"grandparent",
							"greatgrandparent")
					) {

						Either <Record <?>, String> grandparentOrError =
							getParentRequiredOrError (
								transaction,
								genericCastUnchecked (
									currentObject));

						if (
							isError (
								grandparentOrError)
						) {

							return errorResultFormat (
								"Error getting parent of %s: %s",
								currentObject.toString (),
								getError (
									grandparentOrError));

						}

						currentObject =
							resultValueRequired (
								grandparentOrError);

					}

					// great-grandparent

					if (
						stringInSafe (
							pathPart,
							"greatgrandparent")
					) {

						Either <Record <?>, String> greatGrandparentOrError =
							getParentRequiredOrError (
								transaction,
								genericCastUnchecked (
									currentObject));

						if (
							isError (
								greatGrandparentOrError)
						) {

							return errorResultFormat (
								"Error getting parent of %s: %s",
								currentObject.toString (),
								getError (
									greatGrandparentOrError));

						}

						currentObject =
							resultValueRequired (
								greatGrandparentOrError);

					}

				} else {

					try {

						currentObject =
							propertyGetAuto (
								currentObject,
								pathPart);

					} catch (RuntimeException runtimeException) {

						return errorResultFormat (
							"Error getting field '%s' of %s",
							pathPart,
							currentObject.toString ());

					}

					if (
						isNull (
							currentObject)
					) {
						return successResult (
							optionalAbsent ());
					}

				}

			}

			return successResult (
				optionalOf (
					currentObject));

		}

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
	Optional <Class <?>> dereferenceType (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Optional <Class <?>> startingObjectClass,
			@NonNull Optional <String> path) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"dereferenceType");

		) {

			if (! path.isPresent ())
				return startingObjectClass;

			Optional <Class <?>> currentObjectClass =
				startingObjectClass;

			List <String> pathParts =
				stringSplitFullStop (
					path.get ());

			for (
				String pathPart
					: pathParts
			) {

				if (! currentObjectClass.isPresent ())
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

					currentObjectClass =
						parentType (
							currentObjectClass);

				} else if (
					stringEqualSafe (
						pathPart,
						"grandparent")
				) {

					currentObjectClass =
						parentType (
						parentType (
							currentObjectClass));

				} else if (
					stringEqualSafe (
						pathPart,
						"greatgrandparent")
				) {

					currentObjectClass =
						parentType (
						parentType (
						parentType (
							currentObjectClass)));

				} else if (
					stringEqualSafe (
						pathPart,
						"root")
				) {

					currentObjectClass =
						Optional.of (
							rootObjectHelper.objectClass ());

				} else {

					currentObjectClass =
						Optional.of (
							PropertyUtils.propertyClassForClass (
								currentObjectClass.get (),
								pathPart));

				}

			}

			return currentObjectClass;

		}

	}

	@Override
	public
	<ObjectType extends Record <ObjectType>>
	Optional <ObjectType> getAncestor (
			@NonNull Transaction parentTransaction,
			@NonNull Class <ObjectType> ancestorClass,
			@NonNull Record <?> startingObject) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"getAncestor");

		) {

			Record <?> currentObject =
				startingObject;

			for (;;) {

				// return if we found it

				if (
					ancestorClass.isInstance (
						currentObject)
				) {

					return optionalOf (
						ancestorClass.cast (
							currentObject));

				}

				// stop at root

				if (
					rootObjectHelper.objectClass ().isInstance (
						currentObject)
				) {

					return optionalAbsent ();

				}

				// iterate via parent

				currentObject =
					getParentRequired (
						transaction,
						currentObject);

			}

		}

	}

}
