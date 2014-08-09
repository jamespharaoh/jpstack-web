package wbs.smsapps.broadcast.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.entity.annotations.CodeField;
import wbs.framework.entity.annotations.DeletedField;
import wbs.framework.entity.annotations.DescriptionField;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.MajorEntity;
import wbs.framework.entity.annotations.NameField;
import wbs.framework.entity.annotations.ParentField;
import wbs.framework.entity.annotations.ReferenceField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.record.MajorRecord;
import wbs.framework.record.Record;
import wbs.platform.scaffold.model.SliceRec;
import wbs.sms.number.format.model.NumberFormatRec;
import wbs.sms.number.lookup.model.NumberLookupRec;
import wbs.sms.route.router.model.RouterRec;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@MajorEntity
public
class BroadcastConfigRec
	implements MajorRecord<BroadcastConfigRec> {

	// identity

	@GeneratedIdField
	Integer id;

	@ParentField
	SliceRec slice;

	@CodeField
	String code;

	// details

	@NameField
	String name;

	@DescriptionField
	String description;

	@DeletedField
	Boolean deleted = false;

	// settings

	@ReferenceField (
		nullable = true)
	RouterRec router;

	@ReferenceField (
		nullable = true)
	NumberLookupRec blockNumberLookup;

	@ReferenceField (
		nullable = true)
	NumberFormatRec numberFormat;

	@SimpleField (
		nullable = true)
	String defaultNumbers;

	// statistics

	@SimpleField
	Integer numTotal = 0;

	@SimpleField
	Integer numUnsent = 0;

	@SimpleField
	Integer numScheduled = 0;

	@SimpleField
	Integer numSending = 0;

	@SimpleField
	Integer numSent = 0;

	@SimpleField
	Integer numPartiallySent = 0;

	@SimpleField
	Integer numCancelled = 0;

	// compare to

	@Override
	public
	int compareTo (
			Record<BroadcastConfigRec> otherRecord) {

		BroadcastConfigRec other =
			(BroadcastConfigRec) otherRecord;

		return new CompareToBuilder ()
			.append (getSlice (), other.getSlice ())
			.append (getCode (), other.getCode ())
			.toComparison ();

	}

}
