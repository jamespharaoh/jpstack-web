package wbs.smsapps.manualresponder.model;

import java.util.HashSet;
import java.util.Set;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.entity.annotations.CodeField;
import wbs.framework.entity.annotations.CollectionField;
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
import wbs.platform.currency.model.CurrencyRec;
import wbs.platform.scaffold.model.SliceRec;
import wbs.platform.text.model.TextRec;
import wbs.sms.number.list.model.NumberListRec;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@MajorEntity
public
class ManualResponderRec
	implements MajorRecord<ManualResponderRec> {

	// id

	@GeneratedIdField
	Integer id;

	// identity

	@ParentField
	SliceRec slice;

	@CodeField
	String code;

	// details

	@NameField
	String name;

	@DescriptionField
	String description = "";

	@DeletedField
	Boolean deleted = false;

	// settings

	@SimpleField
	Boolean canIgnore = false;

	@SimpleField
	Boolean canSendMultiple = false;

	@ReferenceField (
		nullable = true)
	TextRec infoText;

	@SimpleField
	Boolean showDailyBillInfo = false;

	@SimpleField
	Integer preferredQueueTime = 0;

	@ReferenceField (
		nullable = true)
	CurrencyRec currency;

	@ReferenceField (
		nullable = true)
	NumberListRec unblockNumberList;

	// children

	@CollectionField
	Set<ManualResponderTemplateRec> templates =
		new HashSet<ManualResponderTemplateRec> ();

	// compare to

	@Override
	public
	int compareTo (
			Record<ManualResponderRec> otherRecord) {

		ManualResponderRec other =
			(ManualResponderRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getSlice (),
				other.getSlice ())

			.append (
				getCode (),
				other.getCode ())

			.toComparison ();

	}

}
