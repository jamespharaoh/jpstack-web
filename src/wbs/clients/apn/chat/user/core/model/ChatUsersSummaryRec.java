package wbs.clients.apn.chat.user.core.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.clients.apn.chat.core.model.ChatRec;
import wbs.framework.entity.annotations.CommonEntity;
import wbs.framework.entity.annotations.ForeignIdField;
import wbs.framework.entity.annotations.MasterField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.record.CommonRecord;
import wbs.framework.record.Record;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@CommonEntity (create = false)
public
class ChatUsersSummaryRec
	implements CommonRecord<ChatUsersSummaryRec> {

	// id

	@ForeignIdField (
		field = "chat")
	Integer id;

	// identity

	@MasterField
	ChatRec chat;

	// stats

	@SimpleField
	Integer numUsersGayMale;

	@SimpleField
	Integer numUsersGayFemale;

	@SimpleField
	Integer numUsersBiMale;

	@SimpleField
	Integer numUsersBiFemale;

	@SimpleField
	Integer numUsersStraightMale;

	@SimpleField
	Integer numUsersStraightFemale;

	@SimpleField
	Integer numMonitorsGayMale;

	@SimpleField
	Integer numMonitorsGayFemale;

	@SimpleField
	Integer numMonitorsBiMale;

	@SimpleField
	Integer numMonitorsBiFemale;

	@SimpleField
	Integer numMonitorsStraightMale;

	@SimpleField
	Integer numMonitorsStraightFemale;

	@SimpleField
	Integer numJoinedLastDay;

	@SimpleField
	Integer numJoinedLastWeek;

	@SimpleField
	Integer numJoinedLastMonth;

	@SimpleField
	Integer numOnlineLastDay;

	@SimpleField
	Integer numOnlineLastWeek;

	@SimpleField
	Integer numOnlineLastMonth;

	// compare to

	@Override
	public
	int compareTo (
			Record<ChatUsersSummaryRec> otherRecord) {

		ChatUsersSummaryRec other =
			(ChatUsersSummaryRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getId (),
				other.getId ())

			.toComparison ();

	}

}
