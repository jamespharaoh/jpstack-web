package wbs.apn.chat.user.join.daemon;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.apn.chat.affiliate.model.ChatAffiliateRec;
import wbs.apn.chat.core.model.ChatRec;
import wbs.apn.chat.scheme.model.ChatSchemeRec;
import wbs.apn.chat.user.core.model.Gender;
import wbs.apn.chat.user.core.model.Orient;
import wbs.apn.chat.user.join.daemon.ChatJoiner.JoinType;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.object.ObjectManager;
import wbs.sms.command.model.CommandObjectHelper;
import wbs.sms.command.model.CommandRec;
import wbs.sms.message.inbox.daemon.CommandHandler;
import wbs.sms.message.inbox.logic.InboxLogic;
import wbs.sms.message.inbox.model.InboxAttemptRec;
import wbs.sms.message.inbox.model.InboxRec;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

@Accessors (fluent = true)
@PrototypeComponent ("chatJoinCommand")
public
class ChatJoinCommand
	implements CommandHandler {

	// dependencies

	@Inject
	CommandObjectHelper commandHelper;

	@Inject
	Database database;

	@Inject
	InboxLogic inboxLogic;

	@Inject
	ObjectManager objectManager;

	// prototype dependencies

	@Inject
	Provider<ChatJoiner> chatJoinerProvider;

	// properties

	@Getter @Setter
	InboxRec inbox;

	@Getter @Setter
	CommandRec command;

	@Getter @Setter
	Optional<Integer> commandRef;

	@Getter @Setter
	String rest;

	// details

	@Override
	public
	String[] getCommandTypes () {

		return new String [] {

			"chat.join_info",
			"chat.join_age",
			"chat.join_location",
			"chat.join_prefs",
			"chat.join_next",
			"chat.join_dob",
			"chat.join_charges",
			"chat.join_pics",
			"chat.join_videos",

			"chat.chat_gay_male",
			"chat.chat_gay_female",
			"chat.chat_straight_male",
			"chat.chat_straight_female",
			"chat.chat_bi_male",
			"chat.chat_bi_female",

			"chat_scheme.chat_info",
			"chat_scheme.chat_dob",
			"chat_scheme.chat_location",
			"chat_scheme.chat_gender",
			"chat_scheme.chat_gender_other",
			"chat_scheme.chat_next",
			"chat_scheme.chat_pics",
			"chat_scheme.chat_charges",
			"chat_scheme.date_info",
			"chat_scheme.date_dob",
			"chat_scheme.date_location",
			"chat_scheme.date_gender",
			"chat_scheme.date_gender_other",
			"chat_scheme.date_charges",

			"chat_affiliate.date_set_photo"

		};

	}

	@Override
	public
	InboxAttemptRec handle () {

		Object parent =
			objectManager.getParent (command);

		if (parent instanceof ChatRec) {

			ChatRec chat =
				(ChatRec) parent;

			JoinType joinType =
				commandCodeToJoinType.get (
					command.getCode ());

			if (joinType == null)
				throw new RuntimeException ();

			Gender gender =
				commandCodeToGender.get (
					command.getCode ());

			Orient orient =
				commandCodeToOrient.get (
					command.getCode ());

			return chatJoinerProvider.get ()

				.chatId (
					chat.getId ())

				.joinType (
					joinType)

				.gender (
					gender)

				.orient (
					orient)

				.inbox (
					inbox)

				.rest (
					rest)

				.handleInbox (
					command);

		}

		if (parent instanceof ChatSchemeRec) {

			ChatSchemeRec chatScheme =
				(ChatSchemeRec) parent;

			ChatRec chat =
				chatScheme.getChat ();

			JoinType joinType =
				commandCodeToJoinType.get (command.getCode ());

			if (joinType == null)
				throw new RuntimeException ();

			return chatJoinerProvider.get ()

				.chatId (
					chat.getId ())

				.chatSchemeId (
					chatScheme.getId ())

				.joinType (
					joinType)

				.inbox (
					inbox)

				.rest (
					rest)

				.handleInbox (
					command);

		}

		if (parent instanceof ChatAffiliateRec) {

			ChatAffiliateRec chatAffiliate =
				(ChatAffiliateRec) parent;

			ChatSchemeRec chatScheme =
				chatAffiliate.getChatScheme ();

			ChatRec chat =
				chatScheme.getChat ();

			JoinType joinType =
				commandCodeToJoinType.get (command.getCode ());

			if (joinType == null)
				throw new RuntimeException ();

			return chatJoinerProvider.get ()

				.chatId (
					chat.getId ())

				.joinType (
					joinType)

				.chatAffiliateId (
					chatAffiliate.getId ())

				.chatSchemeId (
					chatScheme.getId ())

				.inbox (
					inbox)

				.rest (
					rest)

				.handleInbox (
					command);

		}

		throw new RuntimeException ();

	}

	Map<String,JoinType> commandCodeToJoinType =
		ImmutableMap.<String,JoinType>builder ()
			.put ("join_info", JoinType.chatSetInfo)
			.put ("join_age", JoinType.chatAge)
			.put ("join_location", JoinType.chatLocation)
			.put ("join_prefs", JoinType.chatPrefs)
			.put ("join_next", JoinType.chatNext)
			.put ("join_dob", JoinType.chatDob)
			.put ("join_charges", JoinType.chatCharges)
			.put ("join_pics", JoinType.chatPics)
			.put ("join_videos", JoinType.chatVideos)
			.put ("chat_gay_male", JoinType.chatSetInfo)
			.put ("chat_gay_female", JoinType.chatSetInfo)
			.put ("chat_straight_male", JoinType.chatSetInfo)
			.put ("chat_straight_female", JoinType.chatSetInfo)
			.put ("chat_bi_male", JoinType.chatSetInfo)
			.put ("chat_bi_female", JoinType.chatSetInfo)
			.put ("chat_info", JoinType.chatSetInfo)
			.put ("chat_dob", JoinType.chatDob)
			.put ("chat_location", JoinType.chatLocation)
			.put ("chat_gender", JoinType.chatGender)
			.put ("chat_gender_other", JoinType.chatGenderOther)
			.put ("chat_next", JoinType.chatNext)
			.put ("chat_pics", JoinType.chatPics)
			.put ("chat_charges", JoinType.chatCharges)
			.put ("date_info", JoinType.dateSetInfo)
			.put ("date_dob", JoinType.dateDob)
			.put ("date_location", JoinType.dateLocation)
			.put ("date_gender", JoinType.dateGender)
			.put ("date_gender_other", JoinType.dateGenderOther)
			.put ("date_charges", JoinType.dateCharges)
			.put ("date_set_photo", JoinType.dateSetPhoto)
			.build ();

	Map<String,Gender> commandCodeToGender =
		ImmutableMap.<String,Gender>builder ()
			.put ("chat_gay_male", Gender.male)
			.put ("chat_gay_female", Gender.female)
			.put ("chat_straight_male", Gender.male)
			.put ("chat_straight_female", Gender.female)
			.put ("chat_bi_male", Gender.male)
			.put ("chat_bi_female", Gender.female)
			.build ();

	Map<String,Orient> commandCodeToOrient =
		ImmutableMap.<String,Orient>builder ()
			.put ("chat_gay_male", Orient.gay)
			.put ("chat_gay_female", Orient.gay)
			.put ("chat_straight_male", Orient.straight)
			.put ("chat_straight_female", Orient.straight)
			.put ("chat_bi_male", Orient.bi)
			.put ("chat_bi_female", Orient.bi)
			.build ();

}
