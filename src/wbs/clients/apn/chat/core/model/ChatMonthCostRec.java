package wbs.clients.apn.chat.core.model;


import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.entity.annotations.CodeField;
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
class ChatMonthCostRec
	implements CommonRecord<ChatMonthCostRec> {

	// id

	@GeneratedIdField
	Integer id;

	// identity

	@ParentField
	ChatRec chat;

	@CodeField
	String yearMonth;

	// settings

	@SimpleField
	Integer staffCost;

	// compare to

	@Override
	public
	int compareTo (
			Record<ChatMonthCostRec> otherRecord) {

		ChatMonthCostRec other =
			(ChatMonthCostRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getChat (),
				other.getChat ())

			.append (
				getYearMonth (),
				other.getYearMonth ())

			.toComparison ();

	}

}
