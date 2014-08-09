package wbs.psychic.user.core.console;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.record.Record;
import wbs.platform.console.request.ConsoleRequestContext;
import wbs.platform.priv.console.PrivChecker;
import wbs.psychic.bill.model.PsychicBillMode;
import wbs.psychic.bill.model.PsychicUserAccountRec;
import wbs.psychic.user.core.model.PsychicUserObjectHelper;
import wbs.psychic.user.core.model.PsychicUserRec;
import wbs.sms.number.core.console.NumberPlugin;
import wbs.sms.number.core.model.NumberRec;

@SingletonComponent ("psychicUserNumberPlugin")
public
class PsychicUserNumberPlugin
	implements NumberPlugin {

	@Inject
	ConsoleRequestContext requestContext;

	@Inject
	PrivChecker privChecker;

	@Inject
	PsychicUserObjectHelper psychicUserHelper;

	@Override
	public String getName () {
		return "psychic";
	}

	@Override
	public
	List<Link> findLinks (
			NumberRec number,
			boolean activeOnly) {

		List<PsychicUserRec> users =
			psychicUserHelper.find (
				number);

		List<Link> ret =
			new ArrayList<Link> ();

		for (final PsychicUserRec user : users) {

			PsychicUserAccountRec account =
				user.getAccount ();

			final boolean userActive =
				user.getFirstJoinTime () != null
				&& account.getBillMode () != PsychicBillMode.barred;

			if (activeOnly && ! userActive)
				continue;

			ret.add (new Link () {

				@Override
				public NumberPlugin getProvider () {
					return PsychicUserNumberPlugin.this;
				}

				@Override
				public NumberRec getNumber () {
					return user.getNumber ();
				}

				@Override
				public Boolean getActive () {
					return userActive;
				}

				@Override
				public Date getStartTime () {
					return user.getFirstJoinTime () != null
						? user.getFirstJoinTime ().toDate ()
						: user.getCreateTime ().toDate ();
				}

				@Override
				public Date getEndTime () {
					return null;
				}

				@Override
				public Record<?> getParentObject () {
					return user.getPsychic ();
				}

				@Override
				public Record<?> getSubscriptionObject () {
					return user;
				}

				@Override
				public String getType () {
					return user.getFirstJoinTime () != null
						? "registered psychic user"
						: "unregistered psychic user";
				}

				@Override
				public boolean canView () {

					return privChecker.can (
						user.getPsychic (),
						"psychic_user_view");

				}

			});

		}

		return ret;

	}

}
