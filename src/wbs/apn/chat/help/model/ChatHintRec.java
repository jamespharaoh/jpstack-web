package wbs.apn.chat.help.model;

import java.util.Set;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.apn.chat.core.model.ChatRec;
import wbs.framework.entity.annotations.CollectionField;
import wbs.framework.entity.annotations.CommonEntity;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.ParentField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.record.CommonRecord;
import wbs.framework.record.Record;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@CommonEntity
public
class ChatHintRec
	implements CommonRecord<ChatHintRec> {

	// id

	@GeneratedIdField
	Integer id;

	// identity

	@ParentField
	ChatRec chat;

	// settings

	@SimpleField
	Boolean sendIfOnline;

	@SimpleField
	Boolean sendIfOffline;

	@SimpleField
	String text;

	// children

	@CollectionField (
		key = "hint_id")
	Set<ChatHintLogRec> logs;

	// compare to

	@Override
	public
	int compareTo (
			Record<ChatHintRec> otherRecord) {

		ChatHintRec other =
			(ChatHintRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getChat (),
				other.getChat ())

			.append (
				getId (),
				other.getId ())

			.toComparison ();

	}

}