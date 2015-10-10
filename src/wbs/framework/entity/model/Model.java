package wbs.framework.entity.model;

import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import lombok.Data;
import lombok.NonNull;
import lombok.experimental.Accessors;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataChildren;
import wbs.framework.data.annotations.DataChildrenIndex;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.data.annotations.DataName;
import wbs.framework.data.annotations.DataReference;
import wbs.framework.record.Record;
import wbs.framework.record.RootRecord;
import wbs.framework.utils.etc.BeanLogic;

@Accessors (fluent = true)
@Data
@DataClass
public
final class Model
	implements ModelMethods {

	// identity

	@DataAttribute
	Class<?> objectClass;

	@DataName
	String objectName;

	@DataAttribute
	String objectTypeCode;

	// database stuff

	@DataAttribute
	String tableName;

	@DataAttribute
	Boolean create;

	@DataAttribute
	Boolean mutable;

	// fields

	@DataReference
	ModelField idField;

	@DataReference
	ModelField parentField;

	@DataReference
	ModelField parentTypeField;

	@DataReference
	ModelField parentIdField;

	@DataReference
	ModelField typeCodeField;

	@DataReference
	ModelField codeField;

	@DataReference
	ModelField indexField;

	@DataReference
	ModelField nameField;

	@DataReference
	ModelField descriptionField;

	@DataReference
	ModelField deletedField;

	@DataChildren
	List<ModelField> fields =
		new ArrayList<ModelField> ();

	@DataChildrenIndex
	Map<String,ModelField> fieldsByName =
		new LinkedHashMap<String,ModelField> ();

	// helper

	@DataAttribute
	Class<?> helperClass;

	// methods

	public
	Integer getId (
			@NonNull Record<?> object) {

		return object.getId ();

	}

	public
	String getTypeCode (
			@NonNull Record<?> object) {

		return (String)
			BeanLogic.getProperty (
				object,
				typeCodeField.name ());

	}

	public
	String getCode (
			@NonNull Record<?> object) {

		if (codeField != null) {

			return (String)
				BeanLogic.getProperty (
					object,
					codeField.name ());

		}

		return Integer.toString (
			getId (object));

	}

	public
	String getName (
			@NonNull Record<?> object) {

		return (String)
			BeanLogic.getProperty (
				object,
				nameField.name ());

	}

	public
	String getDescription (
			@NonNull Record<?> object) {

		return (String)
			BeanLogic.getProperty (
				object,
				descriptionField.name ());

	}

	@Override
	public
	Boolean isRoot () {

		return RootRecord.class.isAssignableFrom (
			objectClass ());

	}

	@Override
	public
	Boolean isRooted () {

		return ! isRoot ()
			&& parentTypeField == null
			&& parentField == null;

	}

	@Override
	public
	Boolean canGetParent () {

		return parentField != null;

	}

	@Override
	public
	Boolean parentTypeIsFixed () {

		return parentTypeField == null;

	}

	public
	Record<?> getParent (
			@NonNull Record<?> object) {

		if (! canGetParent ()) {

			throw new UnsupportedOperationException (
				stringFormat (
					"Can't get parent for %s",
					objectName ()));

		}

		return (Record<?>)
			BeanLogic.getProperty (
				object,
				parentField.name ());

	}

	public
	Integer getParentId (
			@NonNull Record<?> object) {

		if (parentTypeIsFixed ()) {

			throw new UnsupportedOperationException (
				stringFormat (
					"Can't get parent id for %s",
					objectName ()));

		}

		return (Integer)
			BeanLogic.getProperty (
				object,
				parentIdField.name ());

	}

	public
	Record<?> getParentType (
			@NonNull Record<?> object) {

		if (parentTypeIsFixed ()) {

			throw new UnsupportedOperationException (
				stringFormat (
					"Can't get parent type for %s",
					objectName ()));

		}

		return (Record<?>)
			BeanLogic.getProperty (
				object,
				parentTypeField.name ());

	}

	public
	Class<?> parentClass () {

		if (parentTypeField != null) {

			throw new UnsupportedOperationException (
				stringFormat (
					"Can't get parent class for %s",
					objectName ()));

		} else if (parentField != null) {

			return parentField.valueType ();

		} else if (isRoot ()) {

			throw new UnsupportedOperationException (
				stringFormat (
					"Can't get parent class for %s",
					objectName ()));

		} else if (isRooted ()) {

			throw new RuntimeException ("TODO");

		} else {

			throw new RuntimeException ();

		}

	}

	@Override
	public
	ModelField field (
			String name) {

		return fieldsByName.get (name);

	}

}
