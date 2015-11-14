package wbs.clients.apn.chat.infosite.api;

import static wbs.framework.utils.etc.Misc.equal;

import javax.inject.Inject;

import lombok.Cleanup;

import wbs.api.mvc.ApiAction;
import wbs.clients.apn.chat.infosite.model.ChatInfoSiteObjectHelper;
import wbs.clients.apn.chat.infosite.model.ChatInfoSiteRec;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.web.RequestContext;
import wbs.framework.web.Responder;

@PrototypeComponent ("chatInfoSiteViewAction")
public
class ChatInfoSiteViewAction
	extends ApiAction {

	@Inject
	ChatInfoSiteObjectHelper chatInfoSiteHelper;

	@Inject
	Database database;

	@Inject
	RequestContext requestContext;

	@Override
	protected
	Responder goApi () {

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				this);

		ChatInfoSiteRec infoSite =
			chatInfoSiteHelper.find (
				requestContext.requestInt ("chatInfoSiteId"));

		// update first view time

		if (infoSite.getFirstViewTime () == null)
			infoSite.setFirstViewTime (transaction.now ());

		// check the token

		if (! equal (
			infoSite.getToken (),
			requestContext.request ("chatInfoSiteToken"))
		) {

			throw new RuntimeException ("Token mismatch");

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