package wbs.smsapps.subscription.hibernate;

import java.sql.Types;

import org.hibernate.type.CustomType;
import org.hibernate.type.Type;

import wbs.framework.hibernate.EnumUserType;
import wbs.smsapps.subscription.model.SubscriptionStatus;

public
class SubscriptionStatusType
	extends EnumUserType<Integer,SubscriptionStatus> {

	{

		sqlType (Types.INTEGER);
		enumClass (SubscriptionStatus.class);

		add (0, SubscriptionStatus.notSent);
		add (1, SubscriptionStatus.cancelled);
		add (2, SubscriptionStatus.sent);
		add (3, SubscriptionStatus.scheduled);
		add (4, SubscriptionStatus.sentAutomatically);

	}

	public final static
	Type INSTANCE =
		new CustomType (
			new SubscriptionStatusType ());

}
