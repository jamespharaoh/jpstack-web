package wbs.framework.object;

import static wbs.framework.utils.etc.StringUtils.camelToSpaces;
import static wbs.framework.utils.etc.StringUtils.naivePluralise;
import static wbs.framework.utils.etc.StringUtils.stringFormat;

import javax.inject.Inject;

import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.entity.record.CommonRecord;
import wbs.framework.entity.record.EphemeralRecord;
import wbs.framework.entity.record.EventRecord;
import wbs.framework.entity.record.MajorRecord;
import wbs.framework.entity.record.MinorRecord;
import wbs.framework.entity.record.Record;
import wbs.framework.entity.record.TypeRecord;

@Accessors (fluent = true)
@PrototypeComponent ("objectHelperModelImplementation")
public
class ObjectHelperModelImplementation <RecordType extends Record <RecordType>>
	implements
		ObjectHelperComponent <RecordType>,
		ObjectHelperModelMethods <RecordType> {

	// dependencies

	@Inject
	ObjectTypeRegistry objectTypeRegistry;

	// properties

	@Setter
	ObjectModel <RecordType> model;

	@Setter
	ObjectHelper <RecordType> objectHelper;

	@Setter
	ObjectDatabaseHelper <RecordType> objectDatabaseHelper;

	@Setter
	ObjectManager objectManager;

	// public implementation

	@Override
	public
	String objectName () {
		return model.objectName ();
	}

	@Override
	public
	Long objectTypeId () {
		return model.objectTypeId ();
	}

	@Override
	public
	Class<RecordType> objectClass () {

		@SuppressWarnings ("unchecked")
		Class<RecordType> objectClass =
			(Class<RecordType>)
			model.objectClass ();

		return objectClass;

	}

	@Override
	public
	String objectTypeCode () {

		return model.objectTypeCode ();

	}

	@Override
	public
	Class <? extends Record <?>> parentClass () {

		if (model.isRooted ()) {

			return objectTypeRegistry.rootRecordClass ();

		} else {

			return model.parentClass ();

		}

	}

	@Override
	public
	String parentFieldName () {
		return model.parentField ().name ();
	}

	@Override
	public
	String parentLabel () {
		return model.parentField ().label ();
	}

	@Override
	public
	Boolean parentExists () {
		return model.parentField () != null;
	}

	@Override
	public
	String codeFieldName () {

		if (model.codeField () == null) {

			throw new UnsupportedOperationException (
				stringFormat (
					"Object type %s has no code field",
					model.objectName ()));

		}

		return model.codeField ().name ();

	}

	@Override
	public
	String typeCodeLabel () {

		if (model.typeCodeField () == null) {

			throw new UnsupportedOperationException (
				stringFormat (
					"Object type %s has no type code field",
					model.objectName ()));

		}

		return model.typeCodeField ().label ();

	}

	@Override
	public
	Boolean typeCodeExists () {
		return model.typeCodeField () != null;
	}

	@Override
	public
	String typeCodeFieldName () {

		if (model.typeCodeField () == null) {

			throw new UnsupportedOperationException (
				stringFormat (
					"Object type %s has no type code field",
					model.objectName ()));

		}

		return model.typeCodeField ().name ();

	}

	@Override
	public
	String codeLabel () {

		if (model.codeField () == null) {

			throw new UnsupportedOperationException (
				stringFormat (
					"Object type %s has no code field",
					model.objectName ()));

		}

		return model.codeField ().label ();

	}

	@Override
	public
	Boolean codeExists () {
		return model.codeField () != null;
	}

	@Override
	public
	String indexFieldName () {

		if (model.indexField () == null) {

			throw new UnsupportedOperationException (
				stringFormat (
					"Object type %s has no index field",
					model.objectName ()));

		}

		return model.indexField ().name ();

	}

	@Override
	public
	String indexLabel () {

		if (model.indexField () == null) {

			throw new UnsupportedOperationException (
				stringFormat (
					"Object type %s has no index field",
					model.objectName ()));

		}

		return model.indexField ().label ();

	}

	@Override
	public
	Boolean indexExists () {
		return model.indexField () != null;
	}

	@Override
	public
	String indexCounterFieldName () {
		return model.indexField ().indexCounterFieldName ();
	}

	@Override
	public
	String nameFieldName () {

		if (model.nameField () == null) {

			throw new UnsupportedOperationException (
				stringFormat (
					"Object type %s has no name field",
					model.objectName ()));

		}

		return model.nameField ().name ();

	}

	@Override
	public
	String nameLabel () {

		if (model.nameField () == null) {

			throw new UnsupportedOperationException (
				stringFormat (
					"Object type %s has no name field",
					model.objectName ()));

		}

		return model.nameField ().label ();
	}

	@Override
	public
	Boolean nameExists () {
		return model.nameField () != null;
	}

	@Override
	public
	Boolean nameIsCode () {

		return model.nameField () == null
				&& model.codeField () != null;

	}

	@Override
	public
	Boolean deletedExists () {

		return model.deletedField () != null;

	}

	@Override
	public
	String descriptionFieldName () {

		if (model.descriptionField () == null) {

			throw new UnsupportedOperationException (
				stringFormat (
					"Object type %s has no description field",
					model.objectName ()));

		}

		return model.descriptionField ().name ();

	}

	@Override
	public
	String deletedFieldName () {

		if (model.deletedField () == null) {

			throw new UnsupportedOperationException (
				stringFormat (
					"Object type %s has no deleted field",
					model.objectName ()));

		}

		return model.deletedField ().name ();

	}

	@Override
	public
	String deletedLabel () {

		if (model.deletedField () == null) {

			throw new UnsupportedOperationException (
				stringFormat (
					"Object type %s has no deleted field",
					model.objectName ()));

		}

		return model.deletedField ().label ();

	}

	@Override
	public
	String descriptionLabel () {

		if (model.descriptionField () == null) {

			throw new UnsupportedOperationException (
				stringFormat (
					"Object type %s has no description field",
					model.objectName ()));

		}

		return model.descriptionField ().label ();

	}

	@Override
	public
	Boolean descriptionExists () {
		return model.descriptionField () != null;
	}

	@Override
	public
	boolean major () {
		return MajorRecord.class.isAssignableFrom (
			objectClass ());
	}

	@Override
	public
	boolean minor () {
		return MinorRecord.class.isAssignableFrom (
			objectClass ());
	}

	@Override
	public
	boolean ephemeral () {
		return EphemeralRecord.class.isAssignableFrom (
			objectClass ());
	}

	@Override
	public
	boolean event () {
		return EventRecord.class.isAssignableFrom (
			objectClass ());
	}

	@Override
	public
	boolean common () {
		return CommonRecord.class.isAssignableFrom (
			objectClass ());
	}

	@Override
	public
	boolean type () {
		return TypeRecord.class.isAssignableFrom (
			objectClass ());
	}

	@Override
	public
	String friendlyName () {

		return camelToSpaces (
			objectName ());

	}

	@Override
	public
	String friendlyNamePlural () {

		return naivePluralise (
			friendlyName ());

	}

	@Override
	public
	String shortName () {

		return friendlyName ();

	}

	@Override
	public
	String shortNamePlural () {

		return friendlyNamePlural ();

	}

}
