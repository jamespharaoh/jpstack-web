package wbs.clients.apn.chat.bill.model;


import java.util.LinkedHashMap;
import java.util.Map;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.clients.apn.chat.core.model.ChatRec;
import wbs.framework.entity.annotations.CollectionField;
import wbs.framework.entity.annotations.EphemeralEntity;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.IdentityReferenceField;
import wbs.framework.entity.annotations.ParentField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.record.CommonRecord;
import wbs.framework.record.Record;
import wbs.sms.route.core.model.RouteRec;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@EphemeralEntity
public
class ChatRouteRec
	implements CommonRecord<ChatRouteRec> {

	// id

	@GeneratedIdField
	Integer id;

	// identity

	@ParentField
	ChatRec chat;

	@IdentityReferenceField
	RouteRec route;

	// settings

	@SimpleField
	Integer mmsCost;

	@SimpleField
	Integer smsCost;

	@SimpleField
	Integer inRev;

	@SimpleField
	Integer outRev;

	// children

	@CollectionField (
		index = "network_id")
	Map<Integer,ChatRouteNetworkRec> chatRouteNetworks =
		new LinkedHashMap<Integer,ChatRouteNetworkRec> ();

	// compare to

	@Override
	public
	int compareTo (
			Record<ChatRouteRec> otherRecord) {

		ChatRouteRec other =
			(ChatRouteRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getChat (),
				other.getChat ())

			.append (
				getRoute (),
				other.getRoute ())

			.toComparison ();

	}

}
