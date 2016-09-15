package wbs.apn.chat.contact.logic;

import static wbs.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.utils.etc.OptionalUtils.optionalOrNull;

import javax.inject.Provider;

import com.google.common.base.Optional;

import lombok.NonNull;

import org.apache.commons.lang3.tuple.Pair;

import wbs.apn.chat.contact.model.ChatContactObjectHelperMethods;
import wbs.apn.chat.contact.model.ChatContactObjectHelper;
import wbs.apn.chat.contact.model.ChatContactRec;
import wbs.apn.chat.user.core.model.ChatUserObjectHelper;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.framework.component.annotations.LateLifecycleSetup;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.annotations.WeakSingletonDependency;
import wbs.framework.database.Database;
import wbs.utils.cache.AdvancedCache;
import wbs.utils.cache.IdCacheBuilder;

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

	// prototype dependencies

	@PrototypeDependency
	Provider <IdCacheBuilder <Pair <Long, Long>, Long, ChatContactRec>>
	idCacheBuilderProvider;

	// state

	AdvancedCache <Pair <Long, Long>, ChatContactRec> fromAndToUserCache;

	// life cycle

	@LateLifecycleSetup
	public
	void setup () {

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
				key ->

				findOrCreateReal (
					chatUserHelper.findRequired (
						key.getLeft ()),
					chatUserHelper.findRequired (
						key.getRight ())))

			.build ();

	}

	// implementation

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
			@NonNull ChatUserRec fromUser,
			@NonNull ChatUserRec toUser) {

		return fromAndToUserCache.findOrCreate (
			Pair.of (
				fromUser.getId (),
				toUser.getId ()));

	}

	private
	ChatContactRec findOrCreateReal (
			@NonNull ChatUserRec fromUser,
			@NonNull ChatUserRec toUser) {

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