package wbs.apn.chat.user.core.console;

import java.util.ArrayList;
import java.util.List;

import lombok.NonNull;

import org.joda.time.Instant;

import wbs.console.priv.UserPrivChecker;
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.entity.record.Record;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.sms.number.core.console.NumberPlugin;
import wbs.sms.number.core.model.NumberRec;

import wbs.apn.chat.user.core.model.ChatUserDao;
import wbs.apn.chat.user.core.model.ChatUserDateMode;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.apn.chat.user.core.model.ChatUserSearch;

@SingletonComponent ("chatUserNumberLinkProvider")
public
class ChatUserNumberLinkProvider
	implements NumberPlugin {

	// singleton dependencies

	@SingletonDependency
	ChatUserDao chatUserDao;

	@SingletonDependency
	ChatUserConsoleHelper chatUserHelper;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	UserPrivChecker privChecker;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	// details

	@Override
	public
	String getName () {
		return "chatUser";
	}

	// implementation

	@Override
	public
	List <Link> findLinks (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull NumberRec number,
			boolean active) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"findLinks");

		) {

			// find relevant subs

			List <Long> chatUserIds =
				chatUserHelper.searchIds (
					taskLogger,
					new ChatUserSearch ()

				.numberId (
					number.getId ())

			);

			// create advices

			List<Link> advices =
				new ArrayList<Link> ();

			for (
				Long chatUserId
					: chatUserIds
			) {

				final ChatUserRec chatUser =
					chatUserHelper.findRequired (
						chatUserId);

				advices.add (
					new Link () {

					@Override
					public
					NumberPlugin getProvider () {
						return ChatUserNumberLinkProvider.this;
					}

					@Override
					public
					NumberRec getNumber () {
						return chatUser.getNumber ();
					}

					@Override
					public
					Boolean getActive () {
						return !chatUser.getBlockAll ();
					}

					@Override
					public
					Instant getStartTime () {
						return chatUser.getFirstJoin ();
					}

					@Override
					public
					Instant getEndTime () {
						return null;
					}

					@Override
					public
					Record<?> getParentObject () {
						return chatUser.getChat ();
					}

					@Override
					public
					Record<?> getSubscriptionObject () {
						return chatUser;
					}

					@Override
					public
					String getType () {
						return "registered chat user";
					}

					@Override
					public
					boolean canView (
							@NonNull TaskLogger parentTaskLogger) {

						try (

							TaskLogger taskLogger =
								logContext.nestTaskLogger (
									parentTaskLogger,
									"canView");

						) {

							return privChecker.canRecursive (
								taskLogger,
								chatUser.getChat (),
								"chat_user_create",
								"chat_user_view",
								"user_admin",
								"user_credit");

						}

					}

				});

				if (chatUser.getOnline ()) {

					advices.add (
						new Link () {

						@Override
						public
						NumberPlugin getProvider () {
							return ChatUserNumberLinkProvider.this;
						}

						@Override
						public
						NumberRec getNumber () {
							return chatUser.getNumber ();
						}

						@Override
						public
						Boolean getActive () {
							return ! chatUser.getBlockAll ();
						}

						@Override
						public
						Instant getStartTime () {
							return chatUser.getLastJoin ();
						}

						@Override
						public
						Instant getEndTime () {
							return null;
						}

						@Override
						public
						Record<?> getParentObject () {
							return chatUser.getChat ();
						}

						@Override
						public
						Record<?> getSubscriptionObject () {
							return chatUser;
						}

						@Override
						public
						String getType () {
							return "online chat user";
						}

						@Override
						public
						boolean canView (
								@NonNull TaskLogger parentTaskLogger) {

							try (

								TaskLogger taskLogger =
									logContext.nestTaskLogger (
										parentTaskLogger,
										"canView");

							) {

								return privChecker.canRecursive (
									taskLogger,
									chatUser.getChat (),
									"chat_user_create",
									"chat_user_view",
									"user_admin",
									"user_credit");

							}

						}

					});

				}

				if (chatUser.getDateMode() != ChatUserDateMode.none) {

					advices.add (
						new Link () {

						@Override
						public
						NumberPlugin getProvider () {
							return ChatUserNumberLinkProvider.this;
						}

						@Override
						public
						NumberRec getNumber () {
							return chatUser.getNumber ();
						}

						@Override
						public
						Boolean getActive () {
							return true;
						}

						@Override
						public
						Instant getStartTime () {
							return chatUser.getLastJoin ();
						}

						@Override
						public
						Instant getEndTime () {
							return null;
						}

						@Override
						public
						Record<?> getParentObject () {
							return chatUser.getChat ();
						}

						@Override
						public
						Record<?> getSubscriptionObject () {
							return chatUser;
						}

						@Override
						public
						String getType () {

							switch (chatUser.getDateMode ()) {

							case text:
								return "text dating user";

							case photo:
								return "photo dating user";

							default:
								throw new RuntimeException (
									chatUser.getDateMode ().toString ());

							}

						}

						@Override
						public
						boolean canView (
								@NonNull TaskLogger parentTaskLogger) {

							try (

								TaskLogger taskLogger =
									logContext.nestTaskLogger (
										parentTaskLogger,
										"canView");

							) {

								return privChecker.canRecursive (
									taskLogger,
									chatUser.getChat (),
									"chat_user_create",
									"chat_user_view",
									"user_admin",
									"user_credit");

							}

						}

					});

				}

			}

			return advices;

		}

	}

}
