package wbs.imchat.core.model;

import org.apache.commons.lang3.builder.CompareToBuilder;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
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
public class ImChatConversationRec
	implements CommonRecord<ImChatConversationRec>{
	
	// identity

	@GeneratedIdField
	Integer id;

	@ParentField
	ImChatCustomerRec imChatCustomer;
	
	@IndexField (
		counter = "numConversations")
	Integer index;
	
	//statistics
	
	@SimpleField
	Integer numMessages = 0;
	
	@Override
	public int compareTo(Record<ImChatConversationRec> otherRecord) {
		
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
