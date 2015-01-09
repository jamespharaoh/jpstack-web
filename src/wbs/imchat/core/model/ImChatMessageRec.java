package wbs.imchat.core.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
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
public class ImChatMessageRec 
	implements CommonRecord<ImChatMessageRec>{

	// identity

		@GeneratedIdField
		Integer id;

		@ParentField
		ImChatConversationRec imChatConversation;
		
		@IndexField
		String index;
		
		@Override
		public int compareTo(Record<ImChatMessageRec> o) {
			// TODO Auto-generated method stub
			return 0;
		}

}
