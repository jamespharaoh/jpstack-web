package wbs.apn.chat.contact.logic;

import static wbs.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.utils.etc.OptionalUtils.optionalOrNull;

import javax.inject.Provider;

import com.google.common.base.Optional;

import lombok.NonNull;

import org.apache.commons.lang3.tuple.Pair;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.LateLifecycleSetup;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.annotations.WeakSingletonDependency;
import wbs.framework.database.CloseableTransaction;
import wbs.framework.database.Database;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

import wbs.utils.cache.AdvancedCache;
import wbs.utils.cache.IdCacheBuilder;

import wbs.apn.chat.contact.model.ChatContactObjectHelper;
import wbs.apn.chat.contact.model.ChatContactObjectHelperMethods;
import wbs.apn.chat.contact.model.ChatContactRec;
import wbs.apn.chat.user.core.model.ChatUserObjectHelper;
import wbs.apn.chat.user.core.model.ChatUserRec;

public
class ChatContactObjectHelperMethodsImplementation
	implements ChatContactObjectHelperMethods {

	// singleton dependencies

	@WeakSingletonDependency
	ChatContactObjectHelper chatContactHelper;

	@WeakSingletonDependency
	ChatUserObjectHelper chatUserHelper;

	@SingletonDependency
	Database database;

	@ClassSingletonDependency
	LogContext logContext;

	// prototype dependencies

	@PrototypeDependency
	Provider <IdCacheBuilder <
		CloseableTransaction,
		Pair <Long, Long>,
		Long,
		ChatContactRec
	>> idCacheBuilderProvider;

	// state

	AdvancedCache <CloseableTransaction, Pair <Long, Long>, ChatContactRec>
		fromAndToUserCache;

	// life cycle

	@LateLifecycleSetup
	public
	void setup (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"setup");

		) {

			// from and to user id

			fromAndToUserCache =
				idCacheBuilderProvider.get ()

				.lookupByIdFunction (
					chatContactHelper::find)

				.lookupByKeyFunction (
					(context, key) ->
						chatContactHelper.findNoCache (
							context,
							chatUserHelper.findRequired (
								context,
								key.getLeft ()),
							chatUserHelper.findRequired (
								context,
								key.getRight ())))

				.getIdFunction (
					ChatContactRec::getId)

				.createFunction (
					this::findOrCreateReal)

				.wrapperFunction (
					CloseableTransaction::genericWrapper)

				.build (
					taskLogger);

		}

	}

	// public implementation

	@Override
	public
	Optional <ChatContactRec> find (
			@NonNull Transaction parentTransaction,
			@NonNull ChatUserRec fromUser,
			@NonNull ChatUserRec toUser) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"find");

		) {

			return fromAndToUserCache.find (
				transaction,
				Pair.of (
					fromUser.getId (),
					toUser.getId ()));

		}

	}

	@Override
	public
	ChatContactRec findOrCreate (
			@NonNull Transaction parentTransaction,
			@NonNull ChatUserRec fromUser,
			@NonNull ChatUserRec toUser) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"findOrCreate");

		) {

			return fromAndToUserCache.findOrCreate (
				transaction,
				Pair.of (
					fromUser.getId (),
					toUser.getId ()));

		}

	}

	private
	ChatContactRec findOrCreateReal (
			@NonNull Transaction parentTransaction,
			@NonNull ChatUserRec fromUser,
			@NonNull ChatUserRec toUser) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"findOrCreateReal");

		) {

			// look for existing

			Optional <ChatContactRec> chatContactOptional =
				chatContactHelper.find (
					transaction,
					fromUser,
					toUser);

			if (
				optionalIsPresent (
					chatContactOptional)
			) {

				return chatContactOptional.get ();

			}

			// find inverse

			Optional <ChatContactRec> chatContactInverseOptional =
				chatContactHelper.find (
					transaction,
					toUser,
					fromUser);

			// create chat contact

			ChatContactRec chatContact =
				chatContactHelper.insert (
					transaction,
					chatContactHelper.createInstance ()

				.setFromUser (
					fromUser)

				.setToUser (
					toUser)

				.setChat (
					fromUser.getChat ())

				.setInverseChatContact (
					optionalOrNull (
						chatContactInverseOptional))

			);

			// update inverse

			if (
				optionalIsPresent (
					chatContactInverseOptional)
			) {

				chatContactInverseOptional.get ()

					.setInverseChatContact (
						chatContact);

			}

			// and return

			return chatContact;

		}

	}

	// private implementation

	private
	ChatContactRec findOrCreateReal (
			@NonNull Transaction parentTransaction,
			@NonNull Pair <Long, Long> userIds) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"findOrCreateReal");

		) {

			return findOrCreateReal (
				transaction,
				chatUserHelper.findRequired (
					transaction,
					userIds.getLeft ()),
				chatUserHelper.findRequired (
					transaction,
					userIds.getRight ()));

		}

	}

}