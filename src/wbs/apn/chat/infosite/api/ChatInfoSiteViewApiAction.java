package wbs.apn.chat.infosite.api;

import static wbs.utils.etc.NullUtils.isNull;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.string.StringUtils.stringNotEqualSafe;

import javax.inject.Provider;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.api.mvc.ApiAction;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.OwnedTransaction;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.apn.chat.infosite.model.ChatInfoSiteObjectHelper;
import wbs.apn.chat.infosite.model.ChatInfoSiteRec;
import wbs.web.context.RequestContext;
import wbs.web.responder.WebResponder;

@PrototypeComponent ("chatInfoSiteViewApiAction")
public final
class ChatInfoSiteViewApiAction
	implements ApiAction {

	// singleton dependencies

	@SingletonDependency
	ChatInfoSiteObjectHelper chatInfoSiteHelper;

	@SingletonDependency
	Database database;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	RequestContext requestContext;

	// prototype dependencies

	@PrototypeDependency
	Provider <ChatInfoSiteExpiredResponder> expiredResponderProvider;

	@PrototypeDependency
	Provider <ChatInfoSiteViewResponder> viewResponderProvider;

	// implementation

	@Override
	public
	Optional <WebResponder> handle (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTransaction transaction =
				database.beginReadWrite (
					logContext,
					parentTaskLogger,
					"handle");

		) {

			ChatInfoSiteRec infoSite =
				chatInfoSiteHelper.findRequired (
					transaction,
					requestContext.requestIntegerRequired (
						"chatInfoSiteId"));

			// update first view time

			if (
				isNull (
					infoSite.getFirstViewTime ())
			) {

				infoSite

					.setFirstViewTime (
						transaction.now ());

			}

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

				return optionalOf (
					expiredResponderProvider.get ());

			}

			// update successful view counts

			infoSite

				.setLastViewTime (
					transaction.now ())

				.setNumViews (
					infoSite.getNumViews () + 1)

			;

			transaction.commit ();

			// and show info site

			return optionalOf (
				viewResponderProvider.get ());

		}

	}

}