package wbs.apn.chat.user.core.model;

import static wbs.utils.etc.OptionalUtils.optionalGetRequired;

import java.util.List;

import com.google.common.base.Optional;

import lombok.NonNull;

import org.joda.time.Instant;

import wbs.framework.database.Transaction;

import wbs.sms.number.core.model.NumberRec;

import wbs.apn.chat.affiliate.model.ChatAffiliateRec;
import wbs.apn.chat.category.model.ChatCategoryRec;
import wbs.apn.chat.core.model.ChatRec;

public
interface ChatUserDaoMethods {

	Optional <ChatUserRec> find (
			Transaction parentTransaction,
			ChatRec chat,
			NumberRec number);

	default
	ChatUserRec findRequired (
			@NonNull Transaction parentTransaction,
			@NonNull ChatRec chat,
			@NonNull NumberRec number) {

		return optionalGetRequired (
			find (
				parentTransaction,
				chat,
				number));

	}

	Long countOnline (
			Transaction parentTransaction,
			ChatRec chat,
			ChatUserType type);

	List <ChatUserRec> findOnline (
			Transaction parentTransaction,
			ChatRec chat);

	List <ChatUserRec> findOnline (
			Transaction parentTransaction,
			ChatUserType type);

	List <ChatUserRec> findOnlineOrMonitorCategory (
			Transaction parentTransaction,
			ChatRec chat,
			ChatCategoryRec category);

	List <ChatUserRec> findWantingBill (
			Transaction parentTransaction,
			ChatRec chat,
			Instant lastAction,
			Long maximumCredit);

	default
	List <ChatUserRec> findWantingBill (
			@NonNull Transaction parentTransaction,
			@NonNull ChatRec chat,
			@NonNull Instant lastAction) {

		return findWantingBill (
			parentTransaction,
			chat,
			lastAction,
			0l);

	}

	List <ChatUserRec> findWantingWarning (
			Transaction parentTransaction);

	List <ChatUserRec> findAdultExpiryLimit (
			Transaction parentTransaction,
			Instant now,
			Long maxResults);

	List <ChatUserRec> find (
			Transaction parentTransaction,
			ChatRec chat,
			ChatUserType type,
			Orient orient,
			Gender gender);

	List <ChatUserRec> findWantingJoinOutbound (
			Transaction parentTransaction,
			Instant now);

	List <ChatUserRec> findWantingAdultAd (
			Transaction parentTransaction,
			Instant now);

	List <Long> searchIds (
			Transaction parentTransaction,
			ChatUserSearch search);

	List <ChatUserRec> find (
			Transaction parentTransaction,
			ChatAffiliateRec chatAffiliate);

	List <ChatUserRec> findDating (
			Transaction parentTransaction,
			ChatRec chat);

	List <ChatUserRec> findWantingAd (
			Transaction parentTransaction,
			Instant now);

	List <ChatUserRec> findWantingQuietOutbound (
			Transaction parentTransaction,
			Instant now);

}