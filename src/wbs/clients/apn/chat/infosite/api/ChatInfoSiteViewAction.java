package wbs.clients.apn.chat.infosite.api;

import static wbs.framework.utils.etc.StringUtils.stringNotEqualSafe;

import lombok.Cleanup;

import wbs.api.mvc.ApiAction;
import wbs.clients.apn.chat.infosite.model.ChatInfoSiteObjectHelper;
import wbs.clients.apn.chat.infosite.model.ChatInfoSiteRec;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.web.RequestContext;
import wbs.framework.web.Responder;

@PrototypeComponent ("chatInfoSiteViewAction")
public
class ChatInfoSiteViewAction
	extends ApiAction {

	// singleton dependencies

	@SingletonDependency
	ChatInfoSiteObjectHelper chatInfoSiteHelper;

	@SingletonDependency
	Database database;

	@SingletonDependency
	RequestContext requestContext;

	// implementation

	@Override
	protected
	Responder goApi () {

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				"ChatInfoSiteViewAction.goApi ()",
				this);

		ChatInfoSiteRec infoSite =
			chatInfoSiteHelper.findRequired (
				requestContext.requestIntegerRequired (
					"chatInfoSiteId"));

		// update first view time

		if (infoSite.getFirstViewTime () == null)
			infoSite.setFirstViewTime (transaction.now ());

		// check the token

		if (
			stringNotEqualSafe (
				infoSite.getToken (),
				requestContext.requestStringRequired (
					"chatInfoSiteToken"))
		) {

			throw new RuntimeException (
				"Token mismatch");

		}

		// check the expiry time

		if (transaction.now ()
				.isAfter (infoSite.getExpireTime ())) {

			// update expired counts

			infoSite
				.setLastExpiredTime (transaction.now ())
				.setNumExpired (infoSite.getNumExpired () + 1);

			transaction.commit ();

			// and show expired page

			return responder ("chatInfoSiteExpiredResponder")
				.get ();

		}

		// update successful view counts

		infoSite.setLastViewTime (transaction.now ());
		infoSite.setNumViews (infoSite.getNumViews () + 1);

		transaction.commit ();

		// and show info site

		return responder ("chatInfoSiteViewResponder")
			.get ();

	}

}