package wbs.services.messagetemplate.model;

import java.util.Set;
import java.util.TreeSet;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.entity.annotations.CodeField;
import wbs.framework.entity.annotations.CollectionField;
import wbs.framework.entity.annotations.DescriptionField;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.MajorEntity;
import wbs.framework.entity.annotations.NameField;
import wbs.framework.entity.annotations.ParentField;
import wbs.framework.object.AbstractObjectHooks;
import wbs.framework.record.MajorRecord;
import wbs.framework.record.Record;
import wbs.platform.scaffold.model.SliceRec;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id" )
@MajorEntity
public
class MessageTemplateDatabaseRec
	implements MajorRecord<MessageTemplateDatabaseRec> {

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
	String description;

	@CollectionField (
			orderBy = "id")
		Set<MessageTemplateTypeRec> messageTemplateTypes =
			new TreeSet<MessageTemplateTypeRec> ();

	// object hooks

	public static
	class MessageTemplateDatabaseHooks
		extends AbstractObjectHooks<MessageTemplateDatabaseRec> {

		@Override
		public
		void beforeInsert (
			MessageTemplateDatabaseRec messageTemplateDatabase) {

			messageTemplateDatabase.setCode (
				messageTemplateDatabase.getName().toLowerCase());

		}

	}

	// compare to

	@Override
	public
	int compareTo (
			Record<MessageTemplateDatabaseRec> otherRecord) {

		MessageTemplateDatabaseRec other =
			(MessageTemplateDatabaseRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getCode (),
				other.getCode ())

			.append (
				getSlice (),
				other.getSlice ())

			.toComparison ();

	}
}
