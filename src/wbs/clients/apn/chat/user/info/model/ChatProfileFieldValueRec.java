package wbs.clients.apn.chat.user.info.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.IdentityReferenceField;
import wbs.framework.entity.annotations.IdentitySimpleField;
import wbs.framework.entity.annotations.MajorEntity;
import wbs.framework.record.MajorRecord;
import wbs.framework.record.Record;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@MajorEntity
public
class ChatProfileFieldValueRec
	implements MajorRecord<ChatProfileFieldValueRec> {

	@GeneratedIdField
	Integer id;

	@IdentityReferenceField
	ChatProfileFieldRec chatProfileField;

	@IdentitySimpleField
	String code = null;

	// compare to

	@Override
	public
	int compareTo (
			Record<ChatProfileFieldValueRec> otherRecord) {

		ChatProfileFieldValueRec other =
			(ChatProfileFieldValueRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getChatProfileField (),
				other.getChatProfileField ())

			.append (
				getCode (),
				other.getCode ())

			.toComparison ();

	}

}
