package wbs.clients.apn.chat.scheme.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.entity.annotations.CodeField;
import wbs.framework.entity.annotations.EphemeralEntity;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.ParentField;
import wbs.framework.entity.annotations.ReferenceField;
import wbs.framework.record.EphemeralRecord;
import wbs.framework.record.Record;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@EphemeralEntity
public
class ChatSchemeMapRec
	implements EphemeralRecord<ChatSchemeMapRec> {

	// id

	@GeneratedIdField
	Integer id;

	// identity

	@ParentField
	ChatSchemeRec parentChatScheme;

	@CodeField
	String prefix;

	// details

	@ReferenceField
	ChatSchemeRec targetChatScheme;

	// compare to

	@Override
	public
	int compareTo (
			Record<ChatSchemeMapRec> otherRecord) {

		ChatSchemeMapRec other =
			(ChatSchemeMapRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getParentChatScheme (),
				other.getParentChatScheme ())

			.append (
				getPrefix (),
				other.getPrefix ())

			.toComparison ();

	}

}