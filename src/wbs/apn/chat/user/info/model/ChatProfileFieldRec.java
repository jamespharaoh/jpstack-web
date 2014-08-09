package wbs.apn.chat.user.info.model;

import java.util.Map;
import java.util.TreeMap;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.apn.chat.core.model.ChatRec;
import wbs.framework.entity.annotations.CodeField;
import wbs.framework.entity.annotations.CollectionField;
import wbs.framework.entity.annotations.EphemeralEntity;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.ParentField;
import wbs.framework.record.EphemeralRecord;
import wbs.framework.record.Record;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@EphemeralEntity
public
class ChatProfileFieldRec
	implements EphemeralRecord<ChatProfileFieldRec> {

	@GeneratedIdField
	Integer id;

	@ParentField
	ChatRec chat;

	@CodeField
	String code;

	// children

	@CollectionField (
		index = "code")
	Map<String,ChatProfileFieldValueRec> values =
		new TreeMap<String,ChatProfileFieldValueRec> ();

	// compare to

	@Override
	public
	int compareTo (
			Record<ChatProfileFieldRec> otherRecord) {

		ChatProfileFieldRec other =
			(ChatProfileFieldRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getChat (),
				other.getChat ())

			.append (
				getCode (),
				other.getCode ())

			.toComparison ();

	}

}
