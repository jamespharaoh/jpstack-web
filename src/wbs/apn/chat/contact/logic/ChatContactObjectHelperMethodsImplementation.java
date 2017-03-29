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
import wbs.framework.database.Database;
import wbs.framework.logging.LogContext;
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
	Provider <IdCacheBuilder <Pair <Long, Long>, Long, ChatContactRec>>
	idCacheBuilderProvider;

	// state

	AdvancedCache <Pair <Long, Long>, ChatContactRec> fromAndToUserCache;

	// life cycle

	@LateLifecycleSetup
	public
	void setup (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"setup");

		// from and to user id

		fromAndToUserCache =
			idCacheBuilderProvider.get ()

			.lookupByIdFunction (
				chatContactHelper::find)

			.lookupByKeyFunction (
				key ->

				chatContactHelper.findNoCache (
					chatUserHelper.findRequired (
						key.getLeft ()),
					chatUserHelper.findRequired (
						key.getRight ())))

			.getIdFunction (
				ChatContactRec::getId)

			.createFunction (
				this::findOrCreateReal)

			.build ();

	}

	// public implementation

	@Override
	public
	Optional <ChatContactRec> find (
			@NonNull ChatUserRec fromUser,
			@NonNull ChatUserRec toUser) {

		return fromAndToUserCache.find (
			Pair.of (
				fromUser.getId (),
				toUser.getId ()));

	}

	@Override
	public
	ChatContactRec findOrCreate (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ChatUserRec fromUser,
			@NonNull ChatUserRec toUser) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"findOrCreate");

		return fromAndToUserCache.findOrCreate (
			taskLogger,
			Pair.of (
				fromUser.getId (),
				toUser.getId ()));

	}

	private
	ChatContactRec findOrCreateReal (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ChatUserRec fromUser,
			@NonNull ChatUserRec toUser) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"findOrCreateReal");

		// look for existing

		Optional <ChatContactRec> chatContactOptional =
			chatContactHelper.find (
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
				toUser,
				fromUser);

		// create chat contact

		ChatContactRec chatContact =
			chatContactHelper.insert (
				taskLogger,
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

	// private implementation

	private
	ChatContactRec findOrCreateReal (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Pair <Long, Long> userIds) {

		return findOrCreateReal (
			parentTaskLogger,
			chatUserHelper.findRequired (
				userIds.getLeft ()),
			chatUserHelper.findRequired (
				userIds.getRight ()));

	}

}