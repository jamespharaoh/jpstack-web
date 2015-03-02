package wbs.clients.apn.chat.bill.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.entity.annotations.EphemeralEntity;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.IdentityReferenceField;
import wbs.framework.entity.annotations.ParentField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.record.MinorRecord;
import wbs.framework.record.Record;
import wbs.sms.network.model.NetworkRec;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@EphemeralEntity
public
class ChatRouteNetworkRec
	implements MinorRecord<ChatRouteNetworkRec> {

	// id

	@GeneratedIdField
	Integer id;

	// identity

	@ParentField
	ChatRouteRec chatRoute;

	@IdentityReferenceField
	NetworkRec network;

	// settings

	@SimpleField
	Integer outRev;

	// compare to

	@Override
	public
	int compareTo (
			Record<ChatRouteNetworkRec> otherRecord) {

		ChatRouteNetworkRec other =
			(ChatRouteNetworkRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getChatRoute (),
				other.getChatRoute ())

			.append (
				getNetwork (),
				other.getNetwork ())

			.toComparison ();

	}

}
