package wbs.apn.chat.namednote.model;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.apn.chat.core.model.ChatRec;
import wbs.framework.entity.annotations.CommonEntity;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.IndexField;
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
class ChatNoteNameRec
	implements CommonRecord<ChatNoteNameRec> {

	@GeneratedIdField
	Integer id;

	@ParentField
	ChatRec chat;

	@IndexField
	Integer index;

	@SimpleField
	String name;

	@SimpleField
	Boolean deleted;

	@Override
	public
	int compareTo (
			Record<ChatNoteNameRec> otherRecord) {

		ChatNoteNameRec other =
			(ChatNoteNameRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getChat (),
				other.getChat ())

			.append (
				getIndex (),
				other.getIndex ())

			.toComparison ();


	}

	// dao methods

	public static
	interface ChatNoteNameDaoMethods {

		List<ChatNoteNameRec> findNotDeleted (
				ChatRec chat);

	}

}