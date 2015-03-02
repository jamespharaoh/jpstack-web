package wbs.clients.apn.chat.affiliate.console;

import lombok.Data;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.clients.apn.chat.affiliate.model.ChatAffiliateRec;

@Accessors (fluent = true)
@Data
public
final class ChatAffiliateWithNewUserCount
	implements Comparable<ChatAffiliateWithNewUserCount> {

	ChatAffiliateRec chatAffiliate;

	int newUsers;

	@Override
	public
	int compareTo (
			ChatAffiliateWithNewUserCount other) {

		return new CompareToBuilder ()

			.append (
				other.newUsers (),
				newUsers ())

			.append (
				chatAffiliate (),
				other.chatAffiliate ())

			.toComparison ();

	}

}
