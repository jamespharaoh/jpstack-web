package wbs.clients.apn.chat.user.core.console;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.Instant;

import wbs.clients.apn.chat.user.core.model.ChatUserDao;
import wbs.clients.apn.chat.user.core.model.ChatUserDateMode;
import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.clients.apn.chat.user.core.model.ChatUserSearch;
import wbs.console.priv.UserPrivChecker;
import wbs.console.request.ConsoleRequestContext;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.entity.record.Record;
import wbs.sms.number.core.console.NumberPlugin;
import wbs.sms.number.core.model.NumberRec;

@SingletonComponent ("chatUserNumberLinkProvider")
public
class ChatUserNumberLinkProvider
	implements NumberPlugin {

	// singleton dependencies

	@SingletonDependency
	ChatUserDao chatUserDao;

	@SingletonDependency
	ChatUserConsoleHelper chatUserHelper;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	@SingletonDependency
	UserPrivChecker privChecker;

	// details

	@Override
	public
	String getName () {
		return "chatUser";
	}

	// implementation

	@Override
	public
	List<Link> findLinks (
			NumberRec number,
			boolean active) {

		// find relevant subs

		List<Long> chatUserIds =
			chatUserHelper.searchIds (
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
				boolean canView () {

					return privChecker.canRecursive (
						chatUser.getChat (),
						"chat_user_create",
						"chat_user_view",
						"user_admin",
						"user_credit");

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
					boolean canView () {

						return privChecker.canRecursive (
							chatUser.getChat (),
							"chat_user_create",
							"chat_user_view",
							"user_admin",
							"user_credit");

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
					boolean canView () {

						return privChecker.canRecursive (
							chatUser.getChat (),
							"chat_user_create",
							"chat_user_view",
							"user_admin",
							"user_credit");

					}

				});

			}

		}

		return advices;

	}

}
