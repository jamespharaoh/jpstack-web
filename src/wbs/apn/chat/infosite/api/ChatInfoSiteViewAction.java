package wbs.apn.chat.infosite.api;

import static wbs.utils.string.StringUtils.stringNotEqualSafe;

import lombok.NonNull;

import wbs.api.mvc.ApiAction;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.OwnedTransaction;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.apn.chat.infosite.model.ChatInfoSiteObjectHelper;
import wbs.apn.chat.infosite.model.ChatInfoSiteRec;
import wbs.web.context.RequestContext;
import wbs.web.responder.Responder;

@PrototypeComponent ("chatInfoSiteViewAction")
public
class ChatInfoSiteViewAction
	extends ApiAction {

	// singleton dependencies

	@SingletonDependency
	ChatInfoSiteObjectHelper chatInfoSiteHelper;

	@SingletonDependency
	Database database;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	RequestContext requestContext;

	// implementation

	@Override
	protected
	Responder goApi (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"goApi");

		try (

			OwnedTransaction transaction =
				database.beginReadWrite (
					taskLogger,
					"ChatInfoSiteViewAction.goApi ()",
					this);

		) {

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

				return responder (
					taskLogger,
					"chatInfoSiteExpiredResponder");

			}

			// update successful view counts

			infoSite.setLastViewTime (transaction.now ());
			infoSite.setNumViews (infoSite.getNumViews () + 1);

			transaction.commit ();

			// and show info site

			return responder (
				taskLogger,
				"chatInfoSiteViewResponder");

		}

	}

}