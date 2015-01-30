package wbs.smsapps.manualresponder.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.entity.annotations.CodeField;
import wbs.framework.entity.annotations.DescriptionField;
import wbs.framework.entity.annotations.EphemeralEntity;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.NameField;
import wbs.framework.entity.annotations.ParentField;
import wbs.framework.entity.annotations.ReferenceField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.record.EphemeralRecord;
import wbs.framework.record.Record;
import wbs.sms.keyword.model.KeywordSetRec;
import wbs.sms.route.router.model.RouterRec;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@EphemeralEntity
public
class ManualResponderTemplateRec
	implements EphemeralRecord<ManualResponderTemplateRec> {

	// id

	@GeneratedIdField
	Integer id;

	// identity

	@ParentField
	ManualResponderRec manualResponder;

	@CodeField
	String code;

	// details

	@NameField
	String name;

	@DescriptionField
	String description = "";

	// settings

	@ReferenceField (
		nullable = true)
	RouterRec router;

	@SimpleField
	String number = "";

	@SimpleField
	String defaultText = "";

	@SimpleField
	Boolean customisable = false;

	@ReferenceField (
		nullable = true)
	KeywordSetRec replyKeywordSet;

	@SimpleField
	Integer maximumMessages;

	@SimpleField
	Integer minimumMessageParts;

	@SimpleField
	Boolean splitLong = false;

	@SimpleField
	Boolean applyTemplates = false;

	@SimpleField (
		nullable = true)
	String singleTemplate;

	@SimpleField (
		nullable = true)
	String firstTemplate;

	@SimpleField (
		nullable = true)
	String middleTemplate;

	@SimpleField (
		nullable = true)
	String lastTemplate;

	@SimpleField (
		nullable = true)
	String rules;

	// compare to

	@Override
	public
	int compareTo (
			Record<ManualResponderTemplateRec> otherRecord) {

		ManualResponderTemplateRec other =
			(ManualResponderTemplateRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getManualResponder (),
				other.getManualResponder ())

			.append (
				getCode (),
				other.getCode ())

			.toComparison ();

	}

}
