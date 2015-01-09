package wbs.imchat.core.model;

import java.util.Random;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import wbs.framework.entity.annotations.CodeField;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.IndexField;
import wbs.framework.entity.annotations.MajorEntity;
import wbs.framework.entity.annotations.ParentField;
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
	
	@IndexField
	String index;
	
	@Override
	public int compareTo(Record<ImChatConversationRec> o) {
		// TODO Auto-generated method stub
		return 0;
	}

}
