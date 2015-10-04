package wbs.services.messagetemplate.model;

import java.util.Set;
import java.util.TreeSet;

import javax.inject.Inject;
import javax.inject.Provider;

import org.apache.commons.lang3.builder.CompareToBuilder;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import wbs.framework.entity.annotations.CodeField;
import wbs.framework.entity.annotations.CollectionField;
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
public class MessageTemplateTypeRec
	implements CommonRecord<MessageTemplateTypeRec> {

	// id

	@GeneratedIdField
	Integer id;

	@CodeField
	String code;

	@ParentField
	MessageTemplateDatabaseRec messageTemplateDatabase;

	// details

	@NameField
	String name;

	@SimpleField
	String defaultValue;

	@SimpleField
	String helpText;

	@SimpleField
	Integer minLength;

	@SimpleField
	Integer maxLength;

	@SimpleField
	MessageTemplateTypeCharset charset;

	@SimpleField
	Integer numParameters = 0;

	// children

	@CollectionField (
		orderBy = "name")
	Set<MessageTemplateParameterRec> messageTemplateParameters =
		new TreeSet<MessageTemplateParameterRec> ();

	// object hooks

	public static
	class MessageTemplateTypeHooks
		extends AbstractObjectHooks<MessageTemplateTypeRec> {

		@Inject
		Provider<MessageTemplateParameterObjectHelper> messageTemplateParameterHelper;

		@Override
		public
		void beforeInsert (
			MessageTemplateTypeRec messageTemplateType) {

			messageTemplateType.setCode (
				messageTemplateType.getName().toLowerCase());

		}

		@Override
		public
		Object getDynamic (
				Record<?> object,
				String name) {

			return null;

		}

	}

	// compare to

	@Override
	public
	int compareTo (
			Record<MessageTemplateTypeRec> otherRecord) {

		MessageTemplateTypeRec other =
			(MessageTemplateTypeRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getCode (),
				other.getCode ())

			.append (
				getMessageTemplateDatabase (),
				other.getMessageTemplateDatabase ())

			.toComparison ();

	}

}
