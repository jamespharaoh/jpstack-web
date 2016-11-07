package wbs.framework.object;

import static wbs.utils.etc.Misc.doNothing;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.TypeUtils.isNotInstanceOf;
import static wbs.utils.string.StringUtils.stringFormat;

import com.google.common.base.Optional;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.annotations.WeakSingletonDependency;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.entity.record.Record;
import wbs.utils.etc.PropertyUtils;

@Accessors (fluent = true)
@PrototypeComponent ("objectHelperPropertyImplementation")
public
class ObjectHelperPropertyImplementation <
	RecordType extends Record <RecordType>
>
	implements
		ObjectHelperComponent <RecordType>,
		ObjectHelperPropertyMethods <RecordType> {

	// singleton dependencies

	@WeakSingletonDependency
	ObjectManager objectManager;

	@SingletonDependency
	ObjectTypeRegistry objectTypeRegistry;

	// properties

	@Getter @Setter
	ObjectModel <RecordType> objectModel;

	@Getter @Setter
	ObjectHelper <RecordType> objectHelper;

	@Getter @Setter
	ObjectDatabaseHelper <RecordType> objectDatabaseHelper;

	// public implementation

	@Override
	public
	String getName (
			@NonNull RecordType object) {

		return objectModel.getName (
			object);

	}

	@Override
	public
	String getTypeCode (
			@NonNull RecordType object) {

		return objectModel.getTypeCode (
			object);

	}

	@Override
	public
	String getCode (
			@NonNull RecordType object) {

		return objectModel.getCode (
			object);

	}

	@Override
	public
	String getDescription (
			@NonNull RecordType object) {

		return objectModel.getDescription (
			object);

	}

	@Override
	public
	Record <?> getParentType (
			@NonNull RecordType object) {

		return objectModel.getParentType (
			object);

	}

	@Override
	public
	Long getParentTypeId (
			@NonNull RecordType object) {

		if (
			! objectModel.objectClass ().isInstance (
				object)
		) {

			throw new IllegalArgumentException ();

		} else if (objectModel.isRoot ()) {

			throw new UnsupportedOperationException ();

		} else if (objectModel.parentTypeId () != null) {

			return objectModel.parentTypeId ();

		} else {

			Record <?> parentType =
				objectModel.getParentType (
					object);

			return parentType.getId ();

		}

	}

	@Override
	public
	Long getParentId (
			@NonNull RecordType object) {

		if (
			isNotInstanceOf (
				objectModel.objectClass (),
				object)
		) {

			throw new IllegalArgumentException ();

		} else if (objectModel.isRoot ()) {

			throw new UnsupportedOperationException ();

		} else if (objectModel.isRooted ()) {

			return 0l;

		} else if (objectModel.canGetParent ()) {

			Record <?> parent =
				getParent (
					object);

			return parent.getId ();

		} else {

			return (Long)
				PropertyUtils.getProperty (
					object,
					objectModel.parentIdField ().name ());

		}

	}

	@Override
	public
	void setParent (
			@NonNull RecordType object,
			@NonNull Record <?> parent) {

		PropertyUtils.setProperty (
			object,
			objectModel.parentField ().name (),
			parent);

		// TODO grand parent etc

	}

	@Override
	public
	GlobalId getParentGlobalId (
			@NonNull RecordType object) {

		if (objectModel.isRoot ()) {

			return null;

		} else {

			return new GlobalId (
				getParentTypeId (
					object),
				getParentId (
					object));

		}

	}

	@Override
	public
	Record <?> getParent (
			@NonNull RecordType object) {

		if (objectModel.isRoot ()) {

			return null;

		} else if (objectModel.isRooted ()) {

			ObjectHelper <?> rootHelper =
				objectManager.objectHelperForClassRequired (
					objectTypeRegistry.rootRecordClass ());

			return rootHelper.findRequired (
				0l);

		} else if (objectModel.canGetParent ()) {

			Record <?> parent =
				objectModel.getParent (
					object);

			if (parent == null) {

				throw new RuntimeException (
					stringFormat (
						"Failed to get parent of %s with id %s",
						objectModel.objectName (),
						integerToDecimalString (
							object.getId ())));

			}

			return parent;

		} else {

			Record <?> parentObjectType =
				(Record <?>)
				objectModel.getParentType (
					object);

			Long parentObjectId =
				objectModel.getParentId (
					object);

			if (parentObjectId == null) {

				throw new RuntimeException (
					stringFormat (
						"Failed to get parent id of %s with id %s",
						objectModel.objectName (),
						integerToDecimalString (
							object.getId ())));

			}

			ObjectHelper <?> parentHelper=
				objectManager.objectHelperForTypeId (
					parentObjectType.getId ());

			if (parentHelper == null) {

				throw new RuntimeException (
					stringFormat (
						"No object helper provider for %s, ",
						integerToDecimalString (
							parentObjectType.getId ()),
						"parent of %s (%s)",
						objectModel.objectName (),
						integerToDecimalString (
							object.getId ())));

			}

			Optional<? extends Record<?>> parentOptional =
				parentHelper.find (
					parentObjectId);

			if (
				optionalIsNotPresent (
					parentOptional)
			) {

				throw new RuntimeException (
					stringFormat (
						"Can't find %s with id %s",
						parentHelper.objectName (),
						integerToDecimalString (
							parentObjectId)));

			}

			return optionalGetRequired (
				parentOptional);

		}

	}

	@Override
	public
	GlobalId getGlobalId (
			@NonNull RecordType object) {

		return new GlobalId (
			objectModel.objectTypeId (),
			object.getId ());

	}

	@Override
	public
	Boolean getDeleted (
			@NonNull RecordType object,
			boolean checkParents) {

		Record <?> currentObject =
			object;

		ObjectHelper <?> currentHelper =
			objectHelper;

		for (;;) {

			// root is never deleted

			if (currentHelper.isRoot ()) {
				return false;
			}

			// check our deleted flag

			try {

				boolean deletedProperty =
					(Boolean)
					PropertyUtils.getProperty (
						object,
						"deleted");

				if (deletedProperty) {
					return true;
				}

			} catch (Exception exception) {

				doNothing ();

			}

			// try parent

			if (! checkParents) {
				return false;
			}

			if (currentHelper.isRooted ()) {
				return false;
			}

			if (currentHelper.canGetParent ()) {

				currentObject =
					currentHelper.getParentGeneric (
						currentObject);

				currentHelper =
					objectManager.objectHelperForObjectRequired (
						currentObject);

			} else {

				Record <?> parentType =
					(Record <?>)
					objectHelper.getParentTypeGeneric (
						currentObject);

				Long parentObjectId =
					getParentIdGeneric (
						currentObject);

				currentHelper =
					objectManager.objectHelperForTypeId (
						parentType.getId ());

				currentObject =
					currentHelper.findRequired (
						parentObjectId);

			}

		}

	}

	@Override
	public
	Object getDynamic (
			@NonNull RecordType object,
			@NonNull String name) {

		return objectModel.hooks ().getDynamic (
			 object,
			 name);

	}

	@Override
	public
	void setDynamic (
			@NonNull RecordType object,
			@NonNull String name,
			@NonNull Optional <?> valueOptional) {

		objectModel.hooks ().setDynamic (
			object,
			name,
			valueOptional);

	}

}
