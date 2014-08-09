package wbs.framework.entity.model;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;
import wbs.framework.data.annotations.DataAncestor;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataChildren;
import wbs.framework.data.annotations.DataChildrenIndex;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.data.annotations.DataName;
import wbs.framework.data.annotations.DataParent;
import static wbs.framework.utils.etc.Misc.in;
import static wbs.framework.utils.etc.Misc.stringFormat;

@Accessors (fluent = true)
@Data
@ToString (of = { "name" })
@DataClass
public
class ModelField {

	@DataAncestor
	Model model;

	@DataParent
	ModelField parentField;

	@DataName
	String name;

	@DataAttribute
	String label;

	@DataAttribute
	ModelFieldType type;

	@DataAttribute
	boolean parent;

	@DataAttribute
	boolean identity;

	@DataChildren
	List<String> columnNames =
		new ArrayList<String> ();

	@DataAttribute
	Class<?> valueType;

	@DataAttribute
	ParameterizedType parameterizedType;

	@DataAttribute
	Type collectionKeyType;

	@DataAttribute
	Type collectionValueType;

	@DataAttribute
	String sqlType;

	@DataAttribute
	String hibernateTypeHelper;

	@DataAttribute
	Boolean nullable;

	@DataAttribute
	String foreignFieldName;

	@DataAttribute
	String sequenceName;

	@DataAttribute
	String orderBy;

	@DataAttribute
	String where;

	@DataAttribute
	String key;

	@DataAttribute
	String index;

	@DataAttribute
	String element;

	@DataAttribute
	String table;

	@DataAttribute
	String counter;

	@DataChildren
	List<ModelField> fields =
		new ArrayList<ModelField> ();

	@DataChildrenIndex
	Map<String,ModelField> fieldsByName =
		new LinkedHashMap<String,ModelField> ();

	Field field;
	Annotation annotation;

	public
	String columnName () {

		if (columnNames.size () != 1) {

			throw new RuntimeException (
				stringFormat (
					"Field %s has %s columns",
					fullName (),
					columnNames.size ()));

		}

		return columnNames.get (0);

	}

	public
	boolean id () {

		return in (
			type,
			ModelFieldType.generatedId,
			ModelFieldType.assignedId,
			ModelFieldType.foreignId,
			ModelFieldType.compositeId);

	}

	public
	boolean value () {

		return in (
			type,
			ModelFieldType.generatedId,
			ModelFieldType.assignedId,
			ModelFieldType.foreignId,
			ModelFieldType.simple,
			ModelFieldType.name,
			ModelFieldType.description,
			ModelFieldType.deleted,
			ModelFieldType.typeCode,
			ModelFieldType.code,
			ModelFieldType.index,
			ModelFieldType.parentId,
			ModelFieldType.identitySimple);

	}

	public
	boolean reference () {

		return in (
			type,
			ModelFieldType.reference,
			ModelFieldType.parent,
			ModelFieldType.grandParent,
			ModelFieldType.greatGrandParent,
			ModelFieldType.parentType,
			ModelFieldType.type,
			ModelFieldType.identityReference);

	}

	public
	boolean composite () {

		return in (
			type,
			ModelFieldType.compositeId,
			ModelFieldType.component);

	}

	public
	boolean generatedId () {
		return type == ModelFieldType.generatedId;
	}

	public
	boolean assignedId () {
		return type == ModelFieldType.assignedId;
	}

	public
	boolean foreignId () {
		return type == ModelFieldType.foreignId;
	}

	public
	boolean partner () {

		return in (
			type,
			ModelFieldType.master,
			ModelFieldType.slave);

	}

	public
	boolean collection () {
		return type == ModelFieldType.collection;
	}

	public
	boolean link () {
		return type == ModelFieldType.link;
	}

	public
	boolean compositeId () {
		return type == ModelFieldType.compositeId;
	}

	public
	boolean component () {
		return type == ModelFieldType.component;
	}

	public
	String fullName () {

		if (parentField != null) {

			return stringFormat (
				"%s.%s",
				parentField.fullName (),
				name ());

		} else {

			return stringFormat (
				"%s.%s",
				model.objectName (),
				name ());

		}

	}

}
