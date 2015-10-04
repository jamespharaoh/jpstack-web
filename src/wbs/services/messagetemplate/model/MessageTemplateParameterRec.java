package wbs.services.messagetemplate.model;

import org.apache.commons.lang3.builder.CompareToBuilder;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import wbs.framework.entity.annotations.CodeField;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.MajorEntity;
import wbs.framework.entity.annotations.NameField;
import wbs.framework.entity.annotations.ParentField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.object.AbstractObjectHooks;
import wbs.framework.record.CommonRecord;
import wbs.framework.record.Record;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id" )
@MajorEntity
public
class MessageTemplateParameterRec
	implements CommonRecord<MessageTemplateParameterRec> {

	// id

	@GeneratedIdField
	Integer id;

	@ParentField
	MessageTemplateTypeRec messageTemplateType;

	// details

	@CodeField
	String code;

	@NameField
	String name;

	@SimpleField
	Boolean required;

	@SimpleField (
		nullable = true)
	Integer length;

	// object hooks

	public static
	class MessageTemplateParameterHooks
		extends AbstractObjectHooks<MessageTemplateParameterRec> {

		@Override
		public
		void beforeInsert (
				MessageTemplateParameterRec messageTemplateParameter) {

			messageTemplateParameter.setCode (
					messageTemplateParameter.getName().toLowerCase());

		}

	}

	// compare to

	@Override
	public
	int compareTo (
			Record<MessageTemplateParameterRec> otherRecord) {

		MessageTemplateParameterRec other =
			(MessageTemplateParameterRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getName (),
				other.getName ())

			.append (
				getMessageTemplateType (),
				other.getMessageTemplateType ())

			.toComparison ();

	}

}
