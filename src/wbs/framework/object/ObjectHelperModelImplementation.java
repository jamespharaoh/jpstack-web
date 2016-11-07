package wbs.framework.object;

import static wbs.utils.string.StringUtils.camelToSpaces;
import static wbs.utils.string.StringUtils.naivePluralise;
import static wbs.utils.string.StringUtils.stringFormat;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.annotations.WeakSingletonDependency;
import wbs.framework.entity.model.ModelField;
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
class ObjectHelperModelImplementation <
	RecordType extends Record <RecordType>
>
	implements
		ObjectHelperComponent <RecordType>,
		ObjectHelperModelMethods <RecordType> {

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
	String objectName () {
		return objectModel.objectName ();
	}

	@Override
	public
	Long objectTypeId () {
		return objectModel.objectTypeId ();
	}

	@Override
	public
	Class <RecordType> objectClass () {

		Class <RecordType> objectClass =
			objectModel.objectClass ();

		return objectClass;

	}

	@Override
	public
	String objectTypeCode () {

		return objectModel.objectTypeCode ();

	}

	@Override
	public
	Class <? extends Record <?>> parentClass () {

		if (objectModel.isRooted ()) {

			return objectTypeRegistry.rootRecordClass ();

		} else {

			return objectModel.parentClass ();

		}

	}

	@Override
	public
	ModelField parentField () {
		return objectModel.parentField ();
	}

	@Override
	public
	String parentFieldName () {
		return objectModel.parentField ().name ();
	}

	@Override
	public
	String parentLabel () {
		return objectModel.parentField ().label ();
	}

	@Override
	public
	Boolean parentExists () {
		return objectModel.parentField () != null;
	}

	@Override
	public
	ModelField codeField () {
		return objectModel.codeField ();
	}

	@Override
	public
	String codeFieldName () {

		if (objectModel.codeField () == null) {

			throw new UnsupportedOperationException (
				stringFormat (
					"Object type %s has no code field",
					objectModel.objectName ()));

		}

		return objectModel.codeField ().name ();

	}

	@Override
	public
	ModelField typeCodeField () {
		return objectModel.typeCodeField ();
	}

	@Override
	public
	String typeCodeLabel () {

		if (objectModel.typeCodeField () == null) {

			throw new UnsupportedOperationException (
				stringFormat (
					"Object type %s has no type code field",
					objectModel.objectName ()));

		}

		return objectModel.typeCodeField ().label ();

	}

	@Override
	public
	Boolean typeCodeExists () {
		return objectModel.typeCodeField () != null;
	}

	@Override
	public
	String typeCodeFieldName () {

		if (objectModel.typeCodeField () == null) {

			throw new UnsupportedOperationException (
				stringFormat (
					"Object type %s has no type code field",
					objectModel.objectName ()));

		}

		return objectModel.typeCodeField ().name ();

	}

	@Override
	public
	String codeLabel () {

		if (objectModel.codeField () == null) {

			throw new UnsupportedOperationException (
				stringFormat (
					"Object type %s has no code field",
					objectModel.objectName ()));

		}

		return objectModel.codeField ().label ();

	}

	@Override
	public
	Boolean codeExists () {
		return objectModel.codeField () != null;
	}

	@Override
	public
	ModelField indexField () {
		return objectModel.indexField ();
	}

	@Override
	public
	String indexFieldName () {

		if (objectModel.indexField () == null) {

			throw new UnsupportedOperationException (
				stringFormat (
					"Object type %s has no index field",
					objectModel.objectName ()));

		}

		return objectModel.indexField ().name ();

	}

	@Override
	public
	String indexLabel () {

		if (objectModel.indexField () == null) {

			throw new UnsupportedOperationException (
				stringFormat (
					"Object type %s has no index field",
					objectModel.objectName ()));

		}

		return objectModel.indexField ().label ();

	}

	@Override
	public
	Boolean indexExists () {
		return objectModel.indexField () != null;
	}

	@Override
	public
	String indexCounterFieldName () {
		return objectModel.indexField ().indexCounterFieldName ();
	}

	@Override
	public
	ModelField nameField () {
		return objectModel.nameField ();
	}

	@Override
	public
	String nameFieldName () {

		if (objectModel.nameField () == null) {

			throw new UnsupportedOperationException (
				stringFormat (
					"Object type %s has no name field",
					objectModel.objectName ()));

		}

		return objectModel.nameField ().name ();

	}

	@Override
	public
	String nameLabel () {

		if (objectModel.nameField () == null) {

			throw new UnsupportedOperationException (
				stringFormat (
					"Object type %s has no name field",
					objectModel.objectName ()));

		}

		return objectModel.nameField ().label ();
	}

	@Override
	public
	Boolean nameExists () {
		return objectModel.nameField () != null;
	}

	@Override
	public
	Boolean nameIsCode () {

		return objectModel.nameField () == null
				&& objectModel.codeField () != null;

	}

	@Override
	public
	ModelField deletedField () {
		return objectModel.deletedField ();
	}

	@Override
	public
	Boolean deletedExists () {

		return objectModel.deletedField () != null;

	}

	@Override
	public
	ModelField descriptionField () {
		return objectModel.descriptionField ();
	}

	@Override
	public
	String descriptionFieldName () {

		if (objectModel.descriptionField () == null) {

			throw new UnsupportedOperationException (
				stringFormat (
					"Object type %s has no description field",
					objectModel.objectName ()));

		}

		return objectModel.descriptionField ().name ();

	}

	@Override
	public
	String deletedFieldName () {

		if (objectModel.deletedField () == null) {

			throw new UnsupportedOperationException (
				stringFormat (
					"Object type %s has no deleted field",
					objectModel.objectName ()));

		}

		return objectModel.deletedField ().name ();

	}

	@Override
	public
	String deletedLabel () {

		if (objectModel.deletedField () == null) {

			throw new UnsupportedOperationException (
				stringFormat (
					"Object type %s has no deleted field",
					objectModel.objectName ()));

		}

		return objectModel.deletedField ().label ();

	}

	@Override
	public
	String descriptionLabel () {

		if (objectModel.descriptionField () == null) {

			throw new UnsupportedOperationException (
				stringFormat (
					"Object type %s has no description field",
					objectModel.objectName ()));

		}

		return objectModel.descriptionField ().label ();

	}

	@Override
	public
	Boolean descriptionExists () {
		return objectModel.descriptionField () != null;
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
	ModelField field (
			@NonNull String name) {

		return objectModel.field (
			name);

	}

	@Override
	public
	String shortNamePlural () {

		return friendlyNamePlural ();

	}

	@Override
	public
	boolean isRoot () {
		return objectModel.isRoot ();
	}

	@Override
	public
	boolean isRooted () {
		return objectModel.isRooted ();
	}

	@Override
	public
	boolean canGetParent () {
		return objectModel.canGetParent ();
	}

	@Override
	public
	boolean parentTypeIsFixed () {
		return objectModel.parentTypeIsFixed ();
	}

	@Override
	public
	ModelField timestampField () {
		return objectModel.timestampField ();
	}

	@Override
	public
	String timestampFieldName () {
		return objectModel.timestampField ().name ();
	}

}
