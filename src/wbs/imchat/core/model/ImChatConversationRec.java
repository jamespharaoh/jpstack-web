package wbs.imchat.core.model;

import java.util.Set;
import java.util.TreeSet;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.joda.time.Instant;

import wbs.framework.entity.annotations.CollectionField;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.IndexField;
import wbs.framework.entity.annotations.MajorEntity;
import wbs.framework.entity.annotations.ParentField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.record.CommonRecord;
import wbs.framework.record.Record;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id" )
@MajorEntity
public
class ImChatConversationRec
	implements CommonRecord<ImChatConversationRec>{

	// id

	@GeneratedIdField
	Integer id;

	// identity

	@ParentField
	ImChatCustomerRec imChatCustomer;

	@IndexField (
		counter = "numConversations")
	Integer index;

	@CollectionField (
		orderBy = "index")
	Set<ImChatMessageRec> imChatMessages =
		new TreeSet<ImChatMessageRec> ();

	// statistics

	@SimpleField
	Integer numMessages = 0;

	@SimpleField
	Instant startTime;

	// compare to

	@Override
	public
	int compareTo (
			Record<ImChatConversationRec> otherRecord) {

		ImChatConversationRec other =
			(ImChatConversationRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getImChatCustomer (),
				other.getImChatCustomer ())

			.append (
				getIndex (),
				other.getIndex ())

			.toComparison ();

	}

}
