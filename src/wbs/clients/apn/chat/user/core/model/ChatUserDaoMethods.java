package wbs.clients.apn.chat.user.core.model;

import java.util.List;
import java.util.Map;

import org.joda.time.Instant;

import wbs.clients.apn.chat.affiliate.model.ChatAffiliateRec;
import wbs.clients.apn.chat.category.model.ChatCategoryRec;
import wbs.clients.apn.chat.core.model.ChatRec;
import wbs.sms.number.core.model.NumberRec;

public
interface ChatUserDaoMethods {

	ChatUserRec find (
			ChatRec chat,
			NumberRec number);

	int countOnline (
			ChatRec chat,
			ChatUserType type);

	List<ChatUserRec> findOnline (
			ChatRec chat);

	List<ChatUserRec> findOnline (
			ChatUserType type);

	List<ChatUserRec> findOnlineOrMonitorCategory (
			ChatRec chat,
			ChatCategoryRec category);

	List<ChatUserRec> findWantingBill (
			Instant date);

	List<ChatUserRec> findWantingWarning ();

	List<ChatUserRec> findAdultExpiryLimit (
			Instant now,
			int maxResults);

	List<ChatUserRec> find (
			ChatRec chat,
			ChatUserType type,
			Orient orient,
			Gender gender);

	List<ChatUserRec> findWantingJoinOutbound (
			Instant now);

	List<ChatUserRec> findWantingAdultAd (
			Instant now);

	List<Integer> searchIds (
			Map<String,Object> searchMap);

	List<Integer> searchIds (
			ChatUserSearch search);

	List<ChatUserRec> find (
			ChatAffiliateRec chatAffiliate);

	List<ChatUserRec> findDating (
			ChatRec chat);

	List<ChatUserRec> findWantingAd (
			Instant now);

	List<ChatUserRec> findWantingQuietOutbound (
			Instant now);

}