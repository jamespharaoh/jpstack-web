package wbs.apn.chat.bill.model;

import static wbs.framework.utils.etc.Misc.stringFormat;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.apn.chat.core.model.ChatRec;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.framework.entity.annotations.EphemeralEntity;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.IdentityReferenceField;
import wbs.framework.entity.annotations.ParentField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.record.EphemeralRecord;
import wbs.framework.record.Record;
import wbs.sms.network.model.NetworkRec;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@EphemeralEntity
public
class ChatNetworkRec
	implements EphemeralRecord<ChatNetworkRec> {

	// id

	@GeneratedIdField
	Integer id;

	// identity

	@ParentField
	ChatRec chat;

	@IdentityReferenceField
	NetworkRec network;

	// settings

	@SimpleField
	Boolean allowReverseBill = false;

	@SimpleField
	Boolean allowPrePay = false;

	// compare to

	@Override
	public
	int compareTo (
			Record<ChatNetworkRec> otherRecord) {

		ChatNetworkRec other =
			(ChatNetworkRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getChat (),
				other.getChat ())

			.append (
				getNetwork (),
				other.getNetwork ())

			.toComparison ();

	}

	// object helper methods

	public static
	interface ChatNetworkObjectHelperMethods {

		ChatNetworkRec forUserRequired (
			ChatUserRec chatUser);

	}

	// object helper implementation

	public static
	class ChatNetworkObjectHelperImplementation
		implements ChatNetworkObjectHelperMethods {

		@Inject
		Provider<ChatNetworkObjectHelper> chatNetworkHelper;

		@Override
		public
		ChatNetworkRec forUserRequired (
				ChatUserRec chatUser) {

			ChatNetworkRec chatNetwork =
				chatNetworkHelper.get ().find (
					chatUser.getChat (),
					chatUser.getNumber ().getNetwork ());

			if (chatNetwork == null) {

				throw new RuntimeException (
					stringFormat (
						"No chat network for %s (%s)",
						chatUser,
						chatUser.getNumber ().getNetwork ()));

			}

			return chatNetwork;

		}

	}

	// dao methods

	public static
	interface ChatNetworkDaoMethods {

		ChatNetworkRec find (
				ChatRec chat,
				NetworkRec network);

	}

}
