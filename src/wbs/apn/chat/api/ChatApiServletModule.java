package wbs.apn.chat.api;

import static wbs.framework.utils.etc.Misc.age;
import static wbs.framework.utils.etc.Misc.allOf;
import static wbs.framework.utils.etc.Misc.equal;
import static wbs.framework.utils.etc.Misc.ifNull;
import static wbs.framework.utils.etc.Misc.in;
import static wbs.framework.utils.etc.Misc.instantToDate;
import static wbs.framework.utils.etc.Misc.isNotNull;
import static wbs.framework.utils.etc.Misc.notEqual;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.regex.Matcher;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.servlet.ServletException;

import lombok.Cleanup;
import lombok.extern.log4j.Log4j;

import org.apache.http.HttpStatus;
import org.joda.time.Instant;

import wbs.apn.chat.affiliate.model.ChatAffiliateObjectHelper;
import wbs.apn.chat.affiliate.model.ChatAffiliateRec;
import wbs.apn.chat.bill.logic.ChatCreditLogic;
import wbs.apn.chat.bill.model.ChatUserCreditObjectHelper;
import wbs.apn.chat.bill.model.ChatUserCreditRec;
import wbs.apn.chat.contact.logic.ChatMessageLogic;
import wbs.apn.chat.contact.model.ChatMessageMethod;
import wbs.apn.chat.contact.model.ChatMessageObjectHelper;
import wbs.apn.chat.contact.model.ChatMessageRec;
import wbs.apn.chat.contact.model.ChatMessageSearch;
import wbs.apn.chat.contact.model.ChatMessageStatus;
import wbs.apn.chat.core.logic.ChatMiscLogic;
import wbs.apn.chat.core.model.ChatObjectHelper;
import wbs.apn.chat.core.model.ChatRec;
import wbs.apn.chat.date.logic.ChatDateLogic;
import wbs.apn.chat.scheme.model.ChatSchemeObjectHelper;
import wbs.apn.chat.scheme.model.ChatSchemeRec;
import wbs.apn.chat.user.core.logic.ChatUserLogic;
import wbs.apn.chat.user.core.model.ChatUserDateMode;
import wbs.apn.chat.user.core.model.ChatUserObjectHelper;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.apn.chat.user.core.model.ChatUserType;
import wbs.apn.chat.user.core.model.Gender;
import wbs.apn.chat.user.core.model.Orient;
import wbs.apn.chat.user.image.model.ChatUserImageObjectHelper;
import wbs.apn.chat.user.image.model.ChatUserImageRec;
import wbs.apn.chat.user.image.model.ChatUserImageType;
import wbs.apn.chat.user.info.logic.ChatInfoLogic;
import wbs.apn.chat.user.info.model.ChatProfileFieldRec;
import wbs.apn.chat.user.info.model.ChatProfileFieldValueRec;
import wbs.apn.chat.user.info.model.ChatUserInfoStatus;
import wbs.apn.chat.user.info.model.ChatUserProfileFieldObjectHelper;
import wbs.apn.chat.user.info.model.ChatUserProfileFieldRec;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.object.ObjectManager;
import wbs.framework.utils.cal.CalDate;
import wbs.framework.web.AbstractWebFile;
import wbs.framework.web.PathHandler;
import wbs.framework.web.RegexpPathHandler;
import wbs.framework.web.RequestContext;
import wbs.framework.web.ServletModule;
import wbs.framework.web.WebFile;
import wbs.platform.api.ApiFile;
import wbs.platform.api.WebApiAction;
import wbs.platform.api.WebApiManager;
import wbs.platform.media.logic.MediaLogic;
import wbs.platform.media.model.MediaObjectHelper;
import wbs.platform.media.model.MediaRec;
import wbs.platform.rpc.core.Rpc;
import wbs.platform.rpc.core.RpcChecker;
import wbs.platform.rpc.core.RpcChecker.MapRpcChecker;
import wbs.platform.rpc.core.RpcChecker.PublicMemberRpcChecker;
import wbs.platform.rpc.core.RpcDefinition;
import wbs.platform.rpc.core.RpcElem;
import wbs.platform.rpc.core.RpcException;
import wbs.platform.rpc.core.RpcExport;
import wbs.platform.rpc.core.RpcHandler;
import wbs.platform.rpc.core.RpcHandlerFactory;
import wbs.platform.rpc.core.RpcList;
import wbs.platform.rpc.core.RpcResult;
import wbs.platform.rpc.core.RpcSource;
import wbs.platform.rpc.core.RpcStructure;
import wbs.platform.rpc.core.RpcType;
import wbs.platform.rpc.php.PhpRpcAction;
import wbs.platform.rpc.web.ReusableRpcHandler;
import wbs.platform.rpc.xml.XmlRpcAction;
import wbs.platform.rpc.xml.XmlRpcFile;
import wbs.sms.locator.logic.LocatorLogic;
import wbs.sms.locator.model.EastNorth;
import wbs.sms.locator.model.LongLat;
import wbs.sms.locator.model.MercatorProjection;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.number.core.logic.NumberLogic;
import wbs.sms.number.core.model.NumberRec;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

@Log4j
@SingletonComponent ("chatApiServletModule")
public
class ChatApiServletModule
	implements ServletModule {

	@Inject
	ChatAffiliateObjectHelper chatAffiliateHelper;

	@Inject
	ChatCreditLogic chatCreditLogic;

	@Inject
	ChatDateLogic chatDateLogic;

	@Inject
	ChatInfoLogic chatInfoLogic;

	@Inject
	ChatMessageObjectHelper chatMessageHelper;

	@Inject
	ChatMessageLogic chatMessageLogic;

	@Inject
	ChatMiscLogic chatMiscLogic;

	@Inject
	ChatObjectHelper chatHelper;

	@Inject
	ChatSchemeObjectHelper chatSchemeHelper;

	@Inject
	ChatUserCreditObjectHelper chatUserCreditHelper;

	@Inject
	ChatUserObjectHelper chatUserHelper;

	@Inject
	ChatUserProfileFieldObjectHelper chatUserProfileFieldHelper;

	@Inject
	ChatUserLogic chatUserLogic;

	@Inject
	ChatUserImageObjectHelper chatUserImageHelper;

	@Inject
	Database database;

	@Inject
	LocatorLogic locatorLogic;

	@Inject
	MediaObjectHelper mediaHelper;

	@Inject
	MediaLogic mediaLogic;

	@Inject
	NumberLogic numberLogic;

	@Inject
	ObjectManager objectManager;

	@Inject
	RequestContext requestContext;

	@Inject
	WebApiManager webApiManager;

	@Inject
	@Named
	MercatorProjection ukNationalGrid;

	@Inject
	Provider<ApiFile> apiFile;

	@Inject
	Provider<PhpRpcAction> phpRpcAction;

	@Inject
	Provider<XmlRpcAction> xmlRpcAction;

	@Inject
	Provider<XmlRpcFile> xmlRpcFile;

	// ================================= servlet module

	WebFile mediaFile =
		new AbstractWebFile () {

		@Override
		public
		void doGet ()
			throws
				ServletException,
				IOException {

			String format =
				(String) requestContext.request ("format");

			int mediaId =
				requestContext.requestInt ("mediaId");

			@Cleanup
			Transaction transaction =
				database.beginReadOnly ();

			MediaRec media =
				mediaHelper.find (
					mediaId);

			if (media == null) {

				requestContext.status (
					HttpStatus.SC_NOT_FOUND);

				return;

			}

			byte[] data =
				media.getContent ().getData ();

			String mimeType =
				media.getMediaType ().getMimeType ();

			if (format != null
					&& ! equal (format, "orig")) {

				if (mediaLogic.isVideo (mimeType)) {

					if (
						mediaLogic.videoProfileNames ().contains (
							format)
					) {

						data =
							mediaLogic.videoConvert (
								format,
								data);

					} else {

						log.warn (
							stringFormat (
								"Unable to convert %s to format %s",
								mimeType,
								format));

						requestContext.status (
							HttpStatus.SC_NOT_FOUND);

						return;

					}

				} else {

					log.warn (
						stringFormat (
							"Unable to convert %s",
							 mimeType));

					requestContext.status (
						HttpStatus.SC_NOT_FOUND);

					return;

				}

			}

			requestContext.setHeader (
				"Content-Type",
				media.getMediaType ().getMimeType ());

			requestContext.setHeader (
				"Content-Length",
				Integer.toString (data.length));

			OutputStream out =
				requestContext.outputStream ();

			out.write (data);

		}

	};

	RegexpPathHandler.Entry mediaEntry =
		new RegexpPathHandler.Entry (
			"/([0-9]+)(?:(?:\\.([a-z0-9]+))?\\.([a-z0-9]+))?") {

		@Override
		protected
		WebFile handle (
				Matcher matcher) {

			requestContext.request (
				"mediaId",
				Integer.parseInt (
					matcher.group (1)));

			requestContext.request (
				"format",
				ifNull (
					matcher.group (2),
					matcher.group (3)));

			return mediaFile;

		}

	};

	@Override
	public
	Map<String,PathHandler> paths () {

		Map<String,PathHandler> ret =
			new HashMap<String,PathHandler> ();

		ret.put (
			"/chat/media",
			new RegexpPathHandler (mediaEntry));

		return ret;

	}

	@Override
	public
	Map<String,WebFile> files () {

		Map<String,WebFile> ret =
			new HashMap<String,WebFile> ();

		for (Class<? extends RpcHandler> handlerClass
				: handlerClasses) {

			String name =
				handlerClass
					.getAnnotation (RpcExport.class)
					.value ();

			ret.put (
				"/chat/php/" + name,
				apiFile.get ()
					.postHandler (
						webApiManager.makeWebApiActionRequestHandler (
							actions.get ("php_" + name))));

			ret.put (
				"/chat/xml/" + name,
				xmlRpcFile.get ()
					.action (actions.get ("xml_" + name)));

		}

		return ret;

	}

	// ================================= init stuff

	@PostConstruct
	public
	void initStuff () {

		initRpcHandlers ();
		initActions ();

	}

	// ================================= rpc handlers

	Map<String,ReusableRpcHandler> handlers =
		new HashMap<String,ReusableRpcHandler> ();

	private
	void initRpcHandlers () {

		for (Class<? extends RpcHandler> handlerClass
				: handlerClasses) {

			String name =
				handlerClass
					.getAnnotation (RpcExport.class)
					.value ();

			handlers.put (
				name,
				new RpcHandlerFactory (
					handlerClass,
					this));

		}

	}

	// ================================= actions

	private static
	List<Class<? extends RpcHandler>> handlerClasses =
		new ArrayList<Class<? extends RpcHandler>> ();

	private static
	void registerRpcHandlerClasses (
			Class<?>... handlerClasses) {

		for (Class<?> uncastHandlerClass
				: handlerClasses) {

			Class<? extends RpcHandler> handlerClass =
				uncastHandlerClass.asSubclass (RpcHandler.class);

			if (handlerClass.getAnnotation (RpcExport.class) == null) {

				log.warn ("Unable to register " + handlerClass);

				continue;

			}

			ChatApiServletModule
				.handlerClasses
				.add (handlerClass);

		}

	}

	Map<String,WebApiAction> actions =
		new HashMap<String,WebApiAction> ();

	private
	void initActions () {

		for (Class<? extends RpcHandler> handlerClass
				: handlerClasses) {

			String name =
				handlerClass.getAnnotation (RpcExport.class).value ();

			actions.put (
				"php_" + name,
				phpRpcAction.get ()
					.rpcHandler (handlers.get (name)));

			actions.put (
				"xml_" + name,
				xmlRpcAction.get ()
					.rpcHandler (handlers.get (name)));

		}

	}

	// ================================= enum maps

	public static
	enum DeviceType { iphone, web };

	private final static
	Map<String,DeviceType> deviceTypeEnumMap =
		ImmutableMap.<String,DeviceType>builder ()
			.put ("iphone", DeviceType.iphone)
			.put ("web", DeviceType.web)
			.build ();

	private final static
	Map<String,ChatUserType> typeEnumMap =
		ImmutableMap.<String,ChatUserType>builder ()
			.put ("user", ChatUserType.user)
			.put ("monitor", ChatUserType.monitor)
			.build ();

	private final static
	Map<String,Gender> genderEnumMap =
		ImmutableMap.<String,Gender>builder ()
			.put ("male", Gender.male)
			.put ("female", Gender.female)
			.build ();

	private final static
	Map<String,Orient> orientEnumMap =
		ImmutableMap.<String,Orient>builder ()
			.put ("gay", Orient.gay)
			.put ("straight", Orient.straight)
			.put ("bi", Orient.bi)
			.build ();

	private final static
	Map<String,ChatUserDateMode> dateModeEnumMap =
		ImmutableMap.<String,ChatUserDateMode>builder ()
			.put ("none", ChatUserDateMode.none)
			.put ("text", ChatUserDateMode.text)
			.put ("image", ChatUserDateMode.photo)
			.build ();

	private final static
	Map<ChatUserDateMode,String> dateModeMuneMap =
		ImmutableMap.<ChatUserDateMode,String>builder ()
			.put (ChatUserDateMode.none, "none")
			.put (ChatUserDateMode.text, "text")
			.put (ChatUserDateMode.photo, "image")
			.build ();

	private final static
	Map<String,ChatUserImageType> imageTypeEnumMap =
		ImmutableMap.<String,ChatUserImageType>builder ()
			.put ("image", ChatUserImageType.image)
			.put ("video", ChatUserImageType.video)
			.put ("audio", ChatUserImageType.audio)
			.build ();

	private final static
	Map<String,ChatMessageMethod> messageMethodEnumMap =
		ImmutableMap.<String,ChatMessageMethod>builder ()
			.put ("iphone", ChatMessageMethod.iphone)
			.put ("sms", ChatMessageMethod.sms)
			.put ("web", ChatMessageMethod.web)
			.build ();

	private final static
	Map<ChatUserInfoStatus,String> chatUserInfoStatusMuneMap =
		ImmutableMap.<ChatUserInfoStatus,String>builder ()
			.put (ChatUserInfoStatus.set, "set")
			.put (ChatUserInfoStatus.autoEdited, "auto-edited")
			.put (ChatUserInfoStatus.moderatorPending, "moderator-pending")
			.put (ChatUserInfoStatus.moderatorApproved, "moderator-approved")
			.put (ChatUserInfoStatus.moderatorRejected, "moderator-rejected")
			.put (ChatUserInfoStatus.moderatorAutoEdited, "moderator-auto-edited")
			.put (ChatUserInfoStatus.moderatorEdited, "moderator-edited")
			.put (ChatUserInfoStatus.console, "console")
			.build ();

	// ============================================================ custom rpc status codes

	private final static
	int

		stChargesNotConfirmed = 0x5100,
		stDobNotConfirmed = 0x5101,
		stDobTooYoung = 0x5102,
		stCredit = 0x5103,
		stBarred = 0x5104,
		stRecipientNotFound = 0x5105,
		stAffiliateNotFound = 0x5106,
		stInvalidMessageId = 0x5107,
		stLoggedOut = 0x5108,
		stSendAmountCountMismatch = 0x5109;

	// ============================================================ profiles rpc handler

	private final static
	RpcDefinition profilesRequestDef =
		Rpc.rpcDefinition ("chat-profiles-request", RpcType.rStructure,
			Rpc.rpcDefinition ("chat-id", RpcType.rInteger),
			Rpc.rpcDefinition ("number", null, RpcType.rString),
			Rpc.rpcDefinition ("limit", null, RpcType.rInteger),
			Rpc.rpcDefinition ("codes", null, RpcType.rList, Rpc.rpcSetChecker (),
				Rpc.rpcDefinition ("code", null, RpcType.rString)),
			Rpc.rpcDefinition ("last-action", null, RpcType.rInteger),
			Rpc.rpcDefinition ("has-image", null, RpcType.rBoolean),
			Rpc.rpcDefinition ("has-video", null, RpcType.rBoolean),
			Rpc.rpcDefinition ("has-audio", null, RpcType.rBoolean),
			Rpc.rpcDefinition ("is-dating", null, RpcType.rBoolean),
			Rpc.rpcDefinition ("is-online", null, RpcType.rBoolean),
			Rpc.rpcDefinition ("types", null, RpcType.rList, Rpc.rpcSetChecker (),
				Rpc.rpcDefinition ("type", RpcType.rString, Rpc.rpcEnumChecker (typeEnumMap))),
			Rpc.rpcDefinition ("genders", null, RpcType.rList, Rpc.rpcSetChecker (),
				Rpc.rpcDefinition ("gender", RpcType.rString, Rpc.rpcEnumChecker (genderEnumMap))),
			Rpc.rpcDefinition ("orients", null, RpcType.rList, Rpc.rpcSetChecker (),
				Rpc.rpcDefinition ("orient", RpcType.rString, Rpc.rpcEnumChecker (orientEnumMap))));

	@RpcExport ("profiles")
	public
	class ProfilesRpcHandler
		implements RpcHandler {

		List<String> errors =
			new ArrayList<String> ();

		Integer chatId;
		String number;
		Integer limit;

		Set<String> codes;
		Integer lastAction;
		Boolean hasImage;
		Boolean hasVideo;
		Boolean hasAudio;
		Boolean isDating;
		Boolean isOnline;
		Set<ChatUserType> types;
		Set<Gender> genders;
		Set<Orient> orients;

		ChatUserRec myUser;

		@Override
		public
		RpcResult handle (
				RpcSource source) {

			@Cleanup
			Transaction transaction =
				database.beginReadOnly ();

			// get params

			getParams (source);

			// bail on any request-invalid classErrors

			if (errors.iterator ().hasNext ())
				return Rpc.rpcError (
					"chat-profiles-response",
					Rpc.stRequestInvalid,
					"request-invalid",
					errors);

			// return

			return makeResponse ();

		}

		@SuppressWarnings ("unchecked")
		private
		void getParams (
				RpcSource source) {

			Map<String,Object> params =
				(Map<String,Object>)
				source.obtain (
					profilesRequestDef,
					errors,
					true);

			if (params == null)
				return;

			chatId = (Integer) params.get ("chat-id");
			number = (String) params.get ("number");
			limit = (Integer) params.get ("limit");

			codes = (Set<String>) params.get ("codes");
			lastAction = (Integer) params.get ("last-action");
			hasImage = (Boolean) params.get ("has-image");
			hasVideo = (Boolean) params.get ("has-video");
			hasAudio = (Boolean) params.get ("has-audio");
			isDating = (Boolean) params.get ("is-dating");
			isOnline = (Boolean) params.get ("is-online");
			types = (Set<ChatUserType>) params.get ("types");
			genders = (Set<Gender>) params.get ("genders");
			orients = (Set<Orient>) params.get ("orients");

		}

		private
		RpcResult makeResponse () {

			if (number != null) {

				ChatRec chat =
					chatHelper.find (
						chatId);

				NumberRec numberRec =
					numberLogic.findOrCreateNumber (
						number);

				myUser =
					chatUserHelper.find (
						chat,
						numberRec);

				if (myUser == null || ! equal (myUser.getChat ().getId (), chatId)) {

					return Rpc.rpcError (
						"chat-profiles-response",
						Rpc.stRequestInvalid,
						"request-invalid",
						"Number not recognised");

				}

			}

			LongLat myLongLat =
				myUser != null
					? myUser.getLocLongLat ()
					: null;

			// find users

			Map<String,Object> searchMap =
				new LinkedHashMap<String,Object> ();

			searchMap.put ("chatId", chatId);
			searchMap.put ("hasGender", true);
			searchMap.put ("hasOrient", true);
			searchMap.put ("blockAll", false);
			searchMap.put ("barred", false);
			searchMap.put ("notDeleted", true);

			if (codes != null)
				searchMap.put ("codeIn", codes);
			if (lastAction != null)
				searchMap.put ("lastActionAfter",
					new Timestamp (System.currentTimeMillis () - (long) lastAction * 1000L));
			if (types != null)
				searchMap.put ("typeIn", types);
			if (hasImage != null)
				searchMap.put ("hasImage", hasImage);
			if (hasVideo != null)
				searchMap.put ("hasVideo", hasVideo);
			if (hasAudio != null)
				searchMap.put ("hasAudio", hasAudio);
			if (isDating != null)
				searchMap.put ("hasDateMode", isDating);
			if (isOnline != null)
				searchMap.put ("online", isOnline);
			if (genders != null)
				searchMap.put ("genderIn", genders);
			if (orients != null)
				searchMap.put ("orientIn", orients);

			long nanoTime = System.nanoTime ();

			List<Integer> userIds =
				chatUserHelper.searchIds (
					searchMap);

			log.info ("Search took " + (int) ((System.nanoTime () - nanoTime) / 1000000) + "ms");

			long time =
				System.currentTimeMillis ();

			long time0;

			long timeA = 0;

			long timeB = 0;

			// build list
			// MercatorProjection osgb = locatorDao.findMercatorProjectionByCode
			// ("uk_national_grid");

			MercatorProjection osgb =
				ukNationalGrid;

			RpcList profiles =
				Rpc.rpcList (
					"profiles",
					"profile",
					RpcType.rStructure);

			for (Integer userId : userIds) {

				ChatUserRec user =
					chatUserHelper.find (
						userId);

				// ignore system chat user, unless they are asked for specifically
				if (myUser != null
						&& user == myUser.getChat ().getSystemChatUser ()
						&& codes != null)
					continue;

				// ignore specifically excluded users, unless specifically requested
				if (user.getHiddenFromChatTube ()
						&& codes != null)
					continue;

				RpcStructure profile =
					Rpc.rpcStruct ("profile",
						Rpc.rpcElem ("code", user.getCode ()),
						Rpc.rpcElem ("online", user.getOnline ()),
						Rpc.rpcElem ("gender", user.getGender ().toString ()),
						Rpc.rpcElem ("orient", user.getOrient ().toString ()));

				/*if (user.getNumber () != null)
					profile.add (
						rpcElem ("number", user.getNumber ().getNumber ()));*/

				if (user.getName() != null)
					profile.add (
						Rpc.rpcElem ("name", user.getName ()));

				if (user.getInfoText () != null)
					profile.add (
						Rpc.rpcElem ("info", user.getInfoText ().getText ()));

				if (user.getMainChatUserImage () != null)
					profile.add (
						Rpc.rpcElem ("media-id", user.getMainChatUserImage ().getMedia ().getId ()));

				if (user.getChatUserImageList ().size () > 0) {

					RpcList images =
						new RpcList (
							"images",
							"image",
							RpcType.rStructure);

					for (ChatUserImageRec cui
							: user.getChatUserImageList ()) {

						RpcStructure image =
							Rpc.rpcStruct ("image",
								Rpc.rpcElem ("media-id", cui.getMedia ().getId ()),
								Rpc.rpcElem ("classification", "unknown"),
								Rpc.rpcElem ("selected", cui == user.getMainChatUserImage ()));

						if (cui.getFullMedia () != null) {

							image.add (
								Rpc.rpcElem (
								"full-media-id",
								cui.getFullMedia ().getId ()));

						}

						images.add (image);

					}

					profile.add (images);

				}

				if (user.getChatUserVideoList ().size () > 0) {
					RpcList videos = new RpcList ("videos", "video", RpcType.rStructure);
					for (ChatUserImageRec cui: user.getChatUserVideoList ()) {

						RpcStructure video =
							Rpc.rpcStruct ("video",
								Rpc.rpcElem ("media-id", cui.getMedia ().getId ()),
								Rpc.rpcElem ("classification", "unknown"),
								Rpc.rpcElem ("selected", cui == user.getMainChatUserVideo ()));

						if (cui.getFullMedia () != null) {

							video.add (
								Rpc.rpcElem ("full-media-id", cui.getFullMedia ().getId ()),
								Rpc.rpcElem ("full-media-filename", cui.getFullMedia ().getFilename ()),
								Rpc.rpcElem ("full-media-mime-type", cui.getFullMedia ().getMediaType ().getMimeType ()));

						}

						videos.add (video);

					}

					profile.add (videos);

				}

				if (user.getChatUserAudioList ().size () > 0) {

					RpcList videos =
						new RpcList (
							"audios",
							"audio",
							RpcType.rStructure);

					for (ChatUserImageRec cui: user.getChatUserAudioList ()) {

						videos.add (
							Rpc.rpcStruct ("audio",
								Rpc.rpcElem ("media-id", cui.getMedia ().getId ()),
								Rpc.rpcElem ("classification", "unknown"),
								Rpc.rpcElem ("selected", cui == user.getMainChatUserAudio ())));

					}

					profile.add (videos);

				}

				if (! user.getProfileFields ().isEmpty ()) {

					RpcList profileFields =
						Rpc.rpcList (
							"profile-fields",
							"profile-field",
							RpcType.rStructure);

					for (ChatUserProfileFieldRec userField
							: user.getProfileFields ().values ()) {

						profileFields.add (

							Rpc.rpcStruct (
								"profile-field",

								Rpc.rpcElem (
									"name",
									userField.getChatProfileField ().getCode ()),

								Rpc.rpcElem (
									"value",
									userField.getChatProfileFieldValue ().getCode ())));

					}

					profile.add (profileFields);

				}

				if (user.getDob () != null) {

					profile.add (
						Rpc.rpcElem (
							"age",
							age (
								TimeZone.getDefault (),
								user.getDob ().toDate ().getTime (),
								System.currentTimeMillis ())));

				}

				time0 =
					System.currentTimeMillis ();

				timeA +=
					+ time0
					- time;

				time =
					time0;

				if (user.getLocLongLat () != null) {

					LongLat longLat =
						user.getLocLongLat ();

					EastNorth eastNorth =
						locatorLogic.longLatToEastNorth (
							osgb,
							user.getLocLongLat ());

					profile.add (

						Rpc.rpcElem (
							"longitude",
							longLat.getLongitude ()),

						Rpc.rpcElem (
							"latitude",
							longLat.getLatitude ()),

						Rpc.rpcElem (
							"easting",
							eastNorth.getEasting ()),

						Rpc.rpcElem (
							"northing",
							eastNorth.getNorthing ()));

					if (myLongLat != null) {

						profile.add (

							Rpc.rpcElem (
								"distance-in-metres",
								locatorLogic.distanceMetres (
									myLongLat,
									longLat)),

							Rpc.rpcElem (
								"distance-in-miles",
								locatorLogic.distanceMiles (
									myLongLat,
									longLat)));

					}

				}

				time0 =
					System.currentTimeMillis ();

				timeB +=
					+ time0
					- time;

				time =
					time0;

				profiles.add (
					profile);

			}

			if (myLongLat != null) {

				Collections.sort (
					profiles.getValue (),
					new Comparator<Object> () {

						@Override
						public
						int compare (
								Object o1,
								Object o2) {

							RpcStructure s1 =
								(RpcStructure) o1;

							RpcStructure s2 =
								(RpcStructure) o2;

							RpcElem e1 =
								s1.getValue ().get ("distance-in-metres");

							RpcElem e2 =
								s2.getValue ().get ("distance-in-metres");

							double d1 =
								e1 != null
									? (Double) e1.getValue ()
									: Double.MAX_VALUE;

							double d2 =
								e2 != null
									? (Double) e2.getValue ()
									: Double.MAX_VALUE;

							if (d1 < d2)
								return -1;

							if (d2 < d1)
								return 1;

							return 0;

						}

					}

				);

			}

			if (limit != null) {

				while (profiles.getValue ().size () > limit) {

					profiles.getValue ().remove (
						profiles.getValue ().size () - 1);

				}

			}

			time0 =
				System.currentTimeMillis ();

			timeA +=
				+ time0
				- time;

			log.info ("timeA = " + timeA);
			log.info ("timeB = " + timeB);

			// return

			return Rpc.rpcSuccess (
				"chat-profiles-response",
				"Listing chat user profiles",
				profiles);

		}

	}

	// ============================================================ media rpc handler

	private final static
	RpcDefinition mediaRequestDef =

		Rpc.rpcDefinition (
			"chat-media-request",
			RpcType.rStructure,

			Rpc.rpcDefinition (
				"media-ids",
				null,
				RpcType.rList,
				Rpc.rpcSetChecker (),

				Rpc.rpcDefinition (
					"media-id",
					RpcType.rInteger)));

	@RpcExport ("media")
	public
	class MediaRpcHandler
		implements RpcHandler {

		List<String> errors =
			new ArrayList<String> ();

		Set<Integer> mediaIds;

		@Override
		public
		RpcResult handle (
				RpcSource source) {

			@Cleanup
			Transaction transaction =
				database.beginReadOnly ();

			// get params

			getParams (source);

			// bail on any request-invalid errors

			if (errors.iterator ().hasNext ()) {

				return Rpc.rpcError (
					"chat-media-response",
					Rpc.stRequestInvalid,
					"request-invalid",
					errors);

			}

			// return

			return makeResponse ();

		}

		private void getParams (
				RpcSource source) {

			@SuppressWarnings ("unchecked")
			Map<String,Object> params = (Map<String,Object>)
				source.obtain (
					mediaRequestDef,
					errors,
					true);

			if (params == null)
				return;

			@SuppressWarnings ("unchecked")
			Set<Integer> mediaIdsTemp = (Set<Integer>)
				params.get ("media-ids");

			mediaIds =
				mediaIdsTemp;

		}

		private RpcResult makeResponse () {

			// build list

			RpcList medias =
				Rpc.rpcList (
					"medias",
					"media",
					RpcType.rStructure);

			for (Integer mediaId : mediaIds) {

				MediaRec mediaRec =
					mediaHelper.find (
						mediaId);

				RpcStructure media =
					Rpc.rpcStruct ("media",
						Rpc.rpcElem ("mediaId", mediaId),
						Rpc.rpcElem ("found", mediaRec != null));

				if (mediaRec != null) {

					media.add (
						Rpc.rpcElem (
							"content",
							mediaRec.getContent ().getData ()));

				}

				medias.add (
					media);

			}

			// return

			return Rpc.rpcSuccess (
				"chat-media-response",
				"Retrieving media files",
				medias);

		}

	}

	// ============================================================ profile rpc handler

	private final static RpcDefinition profileRequestDef =
		Rpc.rpcDefinition ("chat-profile-request", RpcType.rStructure,
			Rpc.rpcDefinition ("chat-id", RpcType.rInteger),
			Rpc.rpcDefinition ("number", RpcType.rString),
			Rpc.rpcDefinition ("scheme-code", null, RpcType.rString),
			Rpc.rpcDefinition ("affiliate-code", null, RpcType.rString),
			Rpc.rpcDefinition ("name", null, RpcType.rString),
			Rpc.rpcDefinition ("gender", null, RpcType.rString, Rpc.rpcEnumChecker (genderEnumMap)),
			Rpc.rpcDefinition ("orient", null, RpcType.rString, Rpc.rpcEnumChecker (orientEnumMap)),
			Rpc.rpcDefinition ("info", null, RpcType.rString),
			Rpc.rpcDefinition ("dob", null, RpcType.rDate),
			Rpc.rpcDefinition ("location", null, RpcType.rString),
			Rpc.rpcDefinition ("longitude", null, RpcType.rFloat),
			Rpc.rpcDefinition ("latitude", null, RpcType.rFloat),
			Rpc.rpcDefinition ("date-mode", null, RpcType.rString, Rpc.rpcEnumChecker (dateModeEnumMap)),
			Rpc.rpcDefinition ("date-radius-miles", null, RpcType.rInteger),
			Rpc.rpcDefinition ("date-start-hour", null, RpcType.rInteger),
			Rpc.rpcDefinition ("date-end-hour", null, RpcType.rInteger),
			Rpc.rpcDefinition ("date-daily-max", null, RpcType.rInteger),
			Rpc.rpcDefinition ("image", null, RpcType.rBinary),
			Rpc.rpcDefinition ("email", null, RpcType.rString),
			Rpc.rpcDefinition ("jigsaw-application-identifier", null, RpcType.rString),
			Rpc.rpcDefinition ("jigsaw-token", null, RpcType.rString),
			Rpc.rpcDefinition ("charges-confirmed", null, RpcType.rBoolean),
			Rpc.rpcDefinition ("profile-fields", null, RpcType.rList, new MapRpcChecker ("name", "value"),
				Rpc.rpcDefinition ("profile-field", RpcType.rStructure,
					Rpc.rpcDefinition ("name", RpcType.rString),
					Rpc.rpcDefinition ("value", RpcType.rString))));

	@RpcExport ("profile")
	public
	class ProfileRpcHandler
		implements RpcHandler {

		List<String> errors =
			new ArrayList<String> ();

		List<String> errorCodes =
			new ArrayList<String> ();

		Integer chatId;
		String number;
		String schemeCode;
		String affiliateCode;
		String name;
		Gender gender;
		Orient orient;
		String info;
		CalDate dob;
		String location;
		Double longitude, latitude;
		ChatUserDateMode dateMode;
		Integer dateRadius;
		Integer dateStartHour;
		Integer dateEndHour;
		Integer dateDailyMax;
		Boolean chargesConfirmed;
		byte[] image;
		String email;
		String jigsawApplicationIdentifier;
		String jigsawToken;
		Map<String,String> profileFields;

		ChatUserRec chatUser;

		@Override
		public
		RpcResult handle (
				RpcSource source) {

			@Cleanup
			Transaction transaction =
				database.beginReadWrite ();

			// get params

			getParams (source);

			// bail on any request-invalid classErrors

			if (errors.iterator ().hasNext ()) {

				return Rpc.rpcError (
					"chat-profile-response",
					Rpc.stRequestInvalid,
					"request-invalid",
					errors);

			}

			// do updates

			doUpdates (transaction);

			// commit

			transaction.commit ();

			// return

			return makeResponse ();

		}

		private
		void getParams (
				RpcSource source) {

			@SuppressWarnings ("unchecked")
			Map<String,Object> params =
				(Map<String, Object>)
				source.obtain (
					profileRequestDef,
					errors,
					true);

			if (params == null)
				return;

			chatId = (Integer) params.get ("chat-id");
			number = (String) params.get ("number");
			schemeCode = (String) params.get ("scheme-code");
			affiliateCode = (String) params.get ("affiliate-code");
			name = (String) params.get ("name");
			gender = (Gender) params.get ("gender");
			orient = (Orient) params.get ("orient");
			info = (String) params.get ("info");
			dob = (CalDate) params.get ("dob");
			location = (String) params.get ("location");
			longitude = (Double) params.get ("longitude");
			latitude = (Double) params.get ("latitude");
			dateMode = (ChatUserDateMode) params.get ("date-mode");
			dateRadius = (Integer) params.get ("date-radius");
			dateStartHour = (Integer) params.get ("date-start-hour");
			dateEndHour = (Integer) params.get ("date-end-hour");
			dateDailyMax = (Integer) params.get ("date-daily-max");
			chargesConfirmed = (Boolean) params.get ("charges-confirmed");
			image = (byte[]) params.get ("image");
			email = (String) params.get ("email");
			jigsawApplicationIdentifier = (String) params.get ("jigsaw-application-identifier");
			jigsawToken = (String) params.get ("jigsaw-token");

			@SuppressWarnings ("unchecked")
			Map<String,String> profileFieldsTemp =
				(Map<String,String>)
				params.get ("profile-fields");

			profileFields =
				profileFieldsTemp;

			if ((schemeCode != null
						|| affiliateCode != null)
					&& (schemeCode == null
						|| affiliateCode == null)) {

				errors.add (
					"scheme-code and affiliate-code must be set together");

			}

		}

		private
		void doUpdates (
				Transaction transaction) {

			ChatRec chat =
				chatHelper.find (
					chatId);

			NumberRec numberRec =
				numberLogic.findOrCreateNumber (
					number);

			chatUser =
				chatUserHelper.findOrCreate (
					chat,
					numberRec);

			if (schemeCode != null) {

				ChatSchemeRec scheme =
					chatSchemeHelper.findByCode (
						objectManager.getGlobalId (chat),
						schemeCode);

				if (scheme == null) {

					throw new RpcException (
						Rpc.rpcError (
							"chat-profile-response",
							stAffiliateNotFound,
							"affiliate-not-found",
							"The affiliate specified can not be found"));

				}

				ChatAffiliateRec affiliate =
					chatAffiliateHelper.findByCode (
						scheme,
						affiliateCode);

				if (affiliate == null) {

					throw new RpcException (
						Rpc.rpcError (
							"chat-profile-response",
							stAffiliateNotFound,
							"affiliate-not-found",
							"The affiliate specified can not be found"));

				}

				chatUserLogic.setAffiliate (
					chatUser,
					affiliate);

			}

			if (name != null)
				chatMiscLogic.chatUserSetName (
					chatUser,
					name,
					null);

			if (gender != null)
				chatUser.setGender (gender);

			if (orient != null)
				chatUser.setOrient (orient);

			if (info != null)
				chatInfoLogic.chatUserSetInfo (
					chatUser,
					info,
					null);

			if (dob != null) {

				if (chatUser.getDob () != null) {

					errorCodes.add (
						"dob-already-set");

					errors.add (
						"Date of birth cannot be changed");

				}

				chatUser.setDob (
					dob.toLocalDate ());

			}

			if (location != null) {

				if (! chatUserLogic.setPlace (
						chatUser,
						location,
						null)) {

					errorCodes.add ("location-invalid");

					errors.add ("Invalid location");

				}

			}

			if (longitude != null
					&& latitude != null) {

				chatUser.setLocLongLat (
					new LongLat (longitude, latitude));

			} else if (longitude != null || latitude != null) {

				throw new RpcException (
					Rpc.rpcError (
						"chat-profile-response",
						Rpc.stRequestInvalid,
						"request-invalid",
						"Longitude and latitude must both be specified"));

			}

			chatDateLogic.userDateStuff (
				chatUser,
				null,
				null,
				dateMode,
				dateRadius,
				dateStartHour,
				dateEndHour,
				dateDailyMax,
				false);

			if (chargesConfirmed != null && chargesConfirmed)
				chatUser.setChargesConfirmed (true);

			if (image != null) {

				chatUserLogic.setPhoto (
					chatUser,
					image,
					Optional.<String>absent (),
					Optional.<String>absent (),
					Optional.<MessageRec>absent (),
					false);

			}

			if (email != null)
				chatUser.setEmail (email);

			if (jigsawApplicationIdentifier != null) {

				chatUser

					.setJigsawApplicationIdentifier (
						jigsawApplicationIdentifier);

			}

			if (jigsawToken != null) {

				chatUser

					.setJigsawToken (
						jigsawToken);

			}

			if (profileFields != null) {

				for (Map.Entry<String,String> profileFieldEntry
						: profileFields.entrySet ()) {

					// lookup field

					ChatProfileFieldRec field =
						chat.getProfileFields ().get (
							profileFieldEntry.getKey ());

					if (field == null) {

						throw new RpcException (
							"chat-profile-response",
							Rpc.stRequestInvalid,
							"request-invalid",
							stringFormat (
								"Profile field name not recognised: %s",
								profileFieldEntry.getKey ()));

					}

					// lookup value

					ChatProfileFieldValueRec value =
						field.getValues ()
							.get (profileFieldEntry.getValue ());

					if (! equal (profileFieldEntry.getValue (), "") && value == null) {

						throw new RpcException (
							"chat-profile-response",
							Rpc.stRequestInvalid,
							"request-invalid",
							stringFormat (
								"Profile field value not recognised: %s, for ",
								profileFieldEntry.getValue (),
								"field: %s",
								profileFieldEntry.getKey ()));

					}

					ChatUserProfileFieldRec userField =
						chatUser.getProfileFields ()
							.get (field.getId ());

					if (userField == null && value != null) {

						// new field

						userField =
							chatUserProfileFieldHelper.insert (
								new ChatUserProfileFieldRec ()
									.setChatUser (chatUser)
									.setChatProfileField (field)
									.setChatProfileFieldValue (value));

					} else if (
						userField != null
						&& value != null
						&& userField.getChatProfileFieldValue () != value
					) {

						// change field

						userField.setChatProfileFieldValue (value);

					} else if (
						userField != null
						&& value == null
					) {

						// remove field

						chatUserProfileFieldHelper.remove (
							userField);

					}

					transaction.flush ();

					transaction.refresh (chatUser);

				}

			}

		}

		private RpcResult makeResponse () {

			// enumerate update classErrors

			RpcList updateErrorCodes =
				Rpc.rpcList (
					"update-error-codes",
					"update-error-code",
					RpcType.rString);

			for (String errorCode
					: errorCodes) {

				updateErrorCodes.add (
					Rpc.rpcElem (
						"update-error-code",
						errorCode));

			}

			// create profile

			RpcStructure profile =

				Rpc.rpcStruct (
					"profile",

					Rpc.rpcElem (
						"code",
						chatUser.getCode ()),

					Rpc.rpcElem (
						"number",
						chatUser.getNumber ().getNumber ()),

					Rpc.rpcElem (
						"date-mode",
						dateModeMuneMap.get (
							chatUser.getDateMode ())),

					Rpc.rpcElem (
						"date-radius",
						chatUser.getDateRadius ()),

					Rpc.rpcElem (
						"date-start-hour",
						chatUser.getDateStartHour ()),

					Rpc.rpcElem (
						"date-end-hour",
						chatUser.getDateEndHour ()),

					Rpc.rpcElem (
						"date-daily-max",
						chatUser.getDateDailyMax ()),

					Rpc.rpcElem (
						"charges-confirmed",
						chatUser.getChargesConfirmed ()));

			if (chatUser.getGender() != null)
				profile.add (Rpc.rpcElem ("gender", chatUser.getGender ().toString ()));

			if (chatUser.getOrient() != null)
				profile.add (Rpc.rpcElem ("orient", chatUser.getOrient ().toString ()));

			if (chatUser.getName() != null)
				profile.add (Rpc.rpcElem ("name", chatUser.getName ()));

			if (chatUser.getNewChatUserName () != null) {

				profile.add (
					Rpc.rpcElem (
						"name-new",
						chatUser
							.getNewChatUserName ()
							.getOriginalName ()));

			}

			if (chatUser.getInfoText () != null)
				profile.add (Rpc.rpcElem ("info", chatUser.getInfoText ().getText ()));

			if (chatUser.getNewChatUserInfo () != null) {

				profile.add (
					Rpc.rpcElem (
						"info-new",
						chatUser
							.getNewChatUserInfo ()
							.getOriginalText ()
							.getText ()));

			}

			if (chatUser.getDob () != null) {

				profile.add (
					Rpc.rpcElem (
						"dob",
						CalDate.forLocalDate (
							chatUser.getDob ())));

			}

			if (chatUser.getMainChatUserImage () != null) {

				profile.add (
					Rpc.rpcElem (
						"media-id",
						chatUser
							.getMainChatUserImage ()
							.getMedia ()
							.getId ()));

			}

			if (chatUser.getLocPlace () != null) {

				profile.add (
					Rpc.rpcElem (
						"location",
						chatUser
							.getLocPlace ()));

			}

			if (chatUser.getLocPlaceLongLat () != null) {

				LongLat longLat =
					chatUser.getLocPlaceLongLat ();

				EastNorth eastNorth =
					locatorLogic.longLatToEastNorth (
						ukNationalGrid,
						longLat);

				profile.add (

					Rpc.rpcElem (
						"longitude",
						longLat.getLongitude ()),

					Rpc.rpcElem (
						"latitude",
						longLat.getLatitude ()),

					Rpc.rpcElem (
						"easting",
						eastNorth.getEasting ()),

					Rpc.rpcElem (
						"northing",
						eastNorth.getNorthing ()));

			}

			if (! chatUser.getProfileFields ().isEmpty ()) {

				RpcList profileFields =
					Rpc.rpcList (
						"profile-fields",
						"profile-field",
						RpcType.rStructure);

				for (ChatUserProfileFieldRec userField
						: chatUser.getProfileFields ().values ()) {

					profileFields.add (

						Rpc.rpcStruct (
							"profile-field",

							Rpc.rpcElem (
								"name",
								userField
									.getChatProfileField ()
									.getCode ()),

							Rpc.rpcElem (
								"value",
								userField
									.getChatProfileFieldValue ()
									.getCode ())));

				}

				profile.add (
					profileFields);

			}

			if (chatUser.getEmail () != null) {

				profile.add (
					Rpc.rpcElem (
						"email",
						chatUser.getEmail ()));

			}

			// return

			return Rpc.rpcSuccess (
				"chat-profile-response",
				"Updated and returning chat user profile",
				updateErrorCodes,
				profile);

		}

	}

	// ============================================= profile delete rpc handler

	private final static
	RpcDefinition profileDeleteRequestDef =

		Rpc.rpcDefinition (
			"chat-profile-delete-request",
			RpcType.rStructure,

			Rpc.rpcDefinition (
				"chat-id",
				RpcType.rInteger),

			Rpc.rpcDefinition (
				"number",
				RpcType.rString));

	@RpcExport ("profileDelete")
	public
	class ProfileDeleteRpcHandler
		implements RpcHandler {

		List<String> errors =
			new ArrayList<String> ();

		List<String> errorCodes =
			new ArrayList<String> ();

		Integer chatId;
		String number;

		ChatUserRec chatUser;

		@Override
		public
		RpcResult handle (
				RpcSource source) {

			@Cleanup
			Transaction transaction =
				database.beginReadWrite ();

			// get params

			getParams (source);

			// bail on any request-invalid classErrors

			if (errors.iterator ().hasNext ()) {

				return Rpc.rpcError (
					"chat-delete-response",
					Rpc.stRequestInvalid,
					"request-invalid",
					errors);

			}

			// do updates

			doUpdates (transaction);

			// commit

			transaction.commit ();

			// return

			return makeResponse ();

		}

		@SuppressWarnings ("unchecked")
		private
		void getParams (
				RpcSource source) {

			Map<String,Object> params =
				(Map<String,Object>)
				source.obtain (
					profileDeleteRequestDef,
					errors,
					true);

			if (params == null)
				return;

			chatId =
				(Integer)
				params.get ("chat-id");

			number =
				(String)
				params.get ("number");

		}

		private
		void doUpdates (
				Transaction transaction) {

			ChatRec chat =
				chatHelper.find (chatId);

			NumberRec numberRec =
				numberLogic.findOrCreateNumber (number);

			chatUser =
				chatUserHelper.find (
					chat,
					numberRec);

			if (chatUser == null)
				return;

			chatUser.setNumber (null);

			log.info ("Delete chat user " + chatUser.getId () + ", number was " + number);

		}

		private
		RpcResult makeResponse () {

			return Rpc.rpcSuccess (
				"chat-delete-profile-response",
				"Delete chat user");

		}

	}

	// =============================================== message send rpc handler

	public static
	class MessageSendAttachment {

		public
		String type;

		public
		byte[] data;

		public
		String filename;

	}

	private final static
	RpcDefinition messageSendRequestDef =

		Rpc.rpcDefinition (
			"chat-message-send-request",
			RpcType.rStructure,

			Rpc.rpcDefinition (
				"chat-id",
				RpcType.rInteger),

			Rpc.rpcDefinition (
				"number",
				RpcType.rString),

			Rpc.rpcDefinition (
				"to-code",
				RpcType.rString),

			Rpc.rpcDefinition (
				"message",
				RpcType.rString),

			Rpc.rpcDefinition (
				"source",
				ChatMessageMethod.api,
				RpcType.rString,
				Rpc.rpcEnumChecker (messageMethodEnumMap)),

			Rpc.rpcDefinition (
				"attachments",
				null,
				RpcType.rList,

				Rpc.rpcDefinition (
					"attachment",
					RpcType.rStructure,
					new PublicMemberRpcChecker (
						MessageSendAttachment.class),

					Rpc.rpcDefinition (
						"type",
						RpcType.rString),

					Rpc.rpcDefinition (
						"data",
						RpcType.rBinary),

					Rpc.rpcDefinition (
						"filename",
						RpcType.rString))));

	@RpcExport ("messageSend")
	public
	class MessageSendRpcHandler
		implements RpcHandler {

		List<String> errors =
			new ArrayList<String> ();

		Integer chatId;
		String number;
		String toCode;
		String message;

		ChatMessageMethod source;

		List<MessageSendAttachment> attachments;

		@Override
		public
		RpcResult handle (
				RpcSource source) {

			@Cleanup
			Transaction transaction =
				database.beginReadWrite ();

			// get params

			getParams (source);

			// bail on any request-invalid classErrors

			if (errors.iterator ().hasNext ()) {

				return Rpc.rpcError (
					"chat-message-send-response",
					Rpc.stRequestInvalid,
					"request-invalid",
					errors);

			}

			// do it

			doIt ();

			// commit

			transaction.commit ();

			// return

			return Rpc.rpcSuccess (
				"chat-message-send-response",
				"Message sent");

		}

		@SuppressWarnings ("unchecked")
		private void getParams (RpcSource source) {

			Map<String,Object> params = (Map<String,Object>)
				source.obtain (messageSendRequestDef, errors, true);

			if (params == null)
				return;

			chatId = (Integer) params.get ("chat-id");
			number = (String) params.get ("number");
			toCode = (String) params.get ("to-code");
			message = (String) params.get ("message");
			this.source = (ChatMessageMethod) params.get ("source");
			attachments = (List<MessageSendAttachment>) params.get ("attachments");
		}

		private
		void doIt () {

			ChatRec chat =
				chatHelper.find (chatId);

			NumberRec numberRec =
				numberLogic.findOrCreateNumber (number);

			ChatUserRec fromUser =
				chatUserHelper.findOrCreate (
					chat,
					numberRec);

			ChatUserRec toUser =
				chatUserHelper.findByCode (
					chat,
					toCode);

			// check the user is not barred
			if (fromUser.getBarred ())
				throw new RpcException (
					Rpc.rpcError (
						"chat-message-send-response",
						stBarred,
						"barred",
						"This profile has been barred"));

			// check if the user is barred through credit

			if (! chatCreditLogic.userSpendCheck (
					fromUser,
					true,
					null,
					false))

				throw new RpcException (
					Rpc.rpcError (
						"chat-message-send-response",
						stCredit,
						"credit",
						"This profile has been barred due to billing problems"));

			// check the age has been set up

			if (! chatUserLogic.gotDob (fromUser)) {

				throw new RpcException (
					Rpc.rpcError (
						"chat-message-send-response",
						stDobNotConfirmed,
						"dob-not-confirmed",
						"This profile has no date of birth"));

			}

			// check the age is ok

			if (! chatUserLogic.dobOk (fromUser)) {

				throw new RpcException (
					Rpc.rpcError (
						"chat-message-send-response",
						stDobTooYoung,
						"dob-too-young",
						"This profile has a too-young date of birth"));

			}

			// check the charges have been confirmed

			if (fromUser.getDeliveryMethod () != ChatMessageMethod.iphone
					&& ! fromUser.getChargesConfirmed ()) {

				throw new RpcException (
					Rpc.rpcError (
						"chat-message-send-response",
						stChargesNotConfirmed,
						"charges-not-confirmed",
						"This profile has not had the charges confirmed"));

			}

			// check the user can be found

			if (toUser == null) {

				throw new RpcException (
					Rpc.rpcError (
						"chat-message-send-response",
						stRecipientNotFound,
						"recipient-not-found",
						"The specified recipient does not exist"));

			}

			// turn the images into media

			List<MediaRec> medias =
				new ArrayList<MediaRec> ();

			if (attachments != null) {

				for (MessageSendAttachment attachment
						: attachments) {

					MediaRec media =
						mediaLogic.createMediaFromImage (
							attachment.data,
							attachment.type,
							attachment.filename);

					medias.add (media);

				}

			}

			// send the message
			chatMessageLogic.chatMessageSendFromUser (
				fromUser,
				toUser,
				message,
				null,
				source,
				medias);
		}
	}

	// ============================================================ message poll rpc handler

	final static
	RpcDefinition messagePollRequestDef =
		Rpc.rpcDefinition ("chat-message-poll-request", RpcType.rStructure,
			Rpc.rpcDefinition ("chat-id", RpcType.rInteger),
			Rpc.rpcDefinition ("number", RpcType.rString),
			Rpc.rpcDefinition ("got-delivery-id", null, RpcType.rInteger),
			Rpc.rpcDefinition ("ignore-credit", false, RpcType.rBoolean),
			Rpc.rpcDefinition ("login", false, RpcType.rBoolean),
			Rpc.rpcDefinition ("logout", false, RpcType.rBoolean),
			Rpc.rpcDefinition ("device-type", RpcType.rString, Rpc.rpcEnumChecker (deviceTypeEnumMap)));

	@RpcExport ("messagePoll")
	public
	class MessagePollRpcHandler
		implements RpcHandler {

		List<String> errors =
			new ArrayList<String> ();

		Integer chatId;
		String number;
		Integer gotDeliveryId;
		Boolean ignoreCredit;
		Boolean login;
		Boolean logout;
		DeviceType deviceType;

		RpcList respMessages;

		@Override
		public
		RpcResult handle (
				RpcSource source) {

			@Cleanup
			Transaction transaction =
				database.beginReadWrite ();

			// get params

			getParams (source);

			// bail on any request-invalid classErrors

			if (errors.iterator ().hasNext ()) {

				return Rpc.rpcError (
					"chat-message-poll-response",
					Rpc.stRequestInvalid,
					"request-invalid",
					errors);

			}

			// do it

			doIt (
				transaction.now ());

			// commit

			transaction.commit ();

			// return

			return Rpc.rpcSuccess (
				"chat-message-poll-response",
				"Message poll successful",
				respMessages);

		}

		@SuppressWarnings ("unchecked")
		private void getParams (RpcSource source) {

			Map<String,Object> params = (Map<String,Object>)
				source.obtain (messagePollRequestDef, errors, true);

			if (params == null)
				return;

			chatId = (Integer) params.get ("chat-id");
			number = (String) params.get ("number");
			gotDeliveryId = (Integer) params.get ("got-delivery-id");
			ignoreCredit = (Boolean) params.get ("ignore-credit");
			login = (Boolean) params.get ("login");
			logout = (Boolean) params.get ("logout");
			deviceType = (DeviceType) params.get ("device-type");
		}

		private
		void doIt (
				Instant now) {

			ChatRec chat =
				chatHelper.find (chatId);

			NumberRec numberRec =
				numberLogic.findOrCreateNumber (number);

			ChatUserRec chatUser =
				chatUserHelper.findOrCreate (
					chat,
					numberRec);

			// check the user is not barred

			if (chatUser.getBarred ()) {

				throw new RpcException (
					Rpc.rpcError (
						"chat-message-poll-response",
						stBarred,
						"barred",
						"This profile has been barred"));

			}

			// check if the user is barred through credit

			if (ignoreCredit == null || ! ignoreCredit) {

				if (! chatCreditLogic.userSpendCheck (
						chatUser,
						true,
						null,
						false)) {

					throw new RpcException (
						Rpc.rpcError (
							"chat-message-poll-response",
							stCredit,
							"credit",
							"This profile has been barred due to billing problems"));

				}

			}

			// check and/or update the delivery method

			ChatMessageMethod method;

			switch (deviceType) {

			case iphone:
				method = ChatMessageMethod.iphone;
				break;

			case web:
				method = ChatMessageMethod.web;
				break;

			default:

				throw new RpcException (
					Rpc.rpcError (
						"chat-message-poll-response",
						Rpc.stRequestInvalid,
						"request-invalid",
						"The device type is not valid"));

			}

			if (login) {

				chatMiscLogic.userJoin (
					chatUser,
					false,
					null,
					method);

			}

			if (chatUser.getDeliveryMethod () != method) {

				throw new RpcException (
					Rpc.rpcError (
						"chat-message-poll-response",
						stLoggedOut,
						"logged-out",
						"Not logged on via this device type"));

			}

			if (logout) {

				chatUserLogic.logoff (
					chatUser,
					true);

			}

			// update the last message poll

			chatUser

				.setLastMessagePoll (
					instantToDate (now));

			// update the last poll message id

			if (gotDeliveryId != null) {

				List<ChatMessageRec> chatMessages =
					chatMessageHelper.search (
						new ChatMessageSearch ()

					.toUserId (
						chatUser.getId ())

					.deliveryId (
						gotDeliveryId));

				if (chatMessages.isEmpty ()
						|| chatMessages.get (0).getToUser () != chatUser) {

					throw new RpcException (
						Rpc.rpcError (
							"chat-message-poll-response",
							stInvalidMessageId,
							"invalid-message-id",
							"The value for got-delivery-id not valid"));

				}

				if (chatUser.getLastMessagePollId () == null
						|| gotDeliveryId > chatUser.getLastMessagePollId ())
					chatUser.setLastMessagePollId (gotDeliveryId);

			}

			// retrieve all newer messages

			respMessages =
				Rpc.rpcList (
					"messages",
					"message",
					RpcType.rStructure);

			List<ChatMessageRec> messages =
				chatMessageHelper.search (
					new ChatMessageSearch ()
						.toUserId (chatUser.getId ())
						.method (method)
						.statusIn (
							ImmutableSet.<ChatMessageStatus>of (
								ChatMessageStatus.sent,
								ChatMessageStatus.moderatorApproved,
								ChatMessageStatus.moderatorAutoEdited,
								ChatMessageStatus.moderatorEdited))
						.deliveryIdGreaterThan (chatUser.getLastMessagePollId ())
						.orderBy (ChatMessageSearch.Order.deliveryId));

			for (ChatMessageRec message : messages) {

				if (message.getDeliveryId () == null
						|| message.getDeliveryId () <= 0) {

					throw new RuntimeException (
						"Message " + message.getId ());

				}

				RpcStructure respMessage =
					Rpc.rpcStruct (
						"message",

						Rpc.rpcElem ("id", message.getId ()),

						Rpc.rpcElem ("delivery-id", message.getDeliveryId ()),

						Rpc.rpcElem ("from-type", message.getFromUser ().getType ().toString ()),

						Rpc.rpcElem ("from-code", message.getFromUser ().getCode ()),

						Rpc.rpcElem ("to-code", message.getToUser ().getCode ()),

						Rpc.rpcElem ("timestamp", message.getTimestamp ().getTime ()),

						Rpc.rpcElem ("text", message.getEditedText ().getText ()));

				if (! message.getMedias ().isEmpty ()) {

					RpcList attachments =
						Rpc.rpcList (
							"attachments",
							"attachment",
							RpcType.rStructure);

					for (MediaRec media : message.getMedias ()) {

						attachments.add (
							Rpc.rpcStruct ("attachment",
								Rpc.rpcElem ("type", media.getMediaType ().getMimeType ()),
								Rpc.rpcElem ("media-id", media.getId ())));

					}

					respMessage.add (attachments);

				}

				respMessages.add (respMessage);

			}
		}
	}

	// ============================================================ image update rpc handler

	private final static
	RpcDefinition imageUpdateRequestDef =
		Rpc.rpcDefinition ("chat-image-update-request", RpcType.rStructure,
			Rpc.rpcDefinition ("chat-id", RpcType.rInteger),
			Rpc.rpcDefinition ("number", RpcType.rString),
			Rpc.rpcDefinition ("type", RpcType.rString, Rpc.rpcEnumChecker (imageTypeEnumMap)),
			Rpc.rpcDefinition ("add", null, RpcType.rList,
				Rpc.rpcDefinition ("image-data", RpcType.rBinary)),
			Rpc.rpcDefinition ("add-files", null, RpcType.rList,
				Rpc.rpcDefinition ("add-file", RpcType.rStructure, new PublicMemberRpcChecker (ImageUpdateAdd.class),
					Rpc.rpcDefinition ("filename", null, RpcType.rString),
					Rpc.rpcDefinition ("mime-type", null, RpcType.rString),
					Rpc.rpcDefinition ("image-data", RpcType.rBinary))),
			Rpc.rpcDefinition ("reorder", null, RpcType.rList,
				Rpc.rpcDefinition ("image-id", RpcType.rInteger)),
			Rpc.rpcDefinition ("delete", null, RpcType.rList,
				Rpc.rpcDefinition ("image-id", RpcType.rInteger)),
			Rpc.rpcDefinition ("selected-image-id", null, RpcType.rInteger));

	public static
	class ImageUpdateAdd {

		public
		String filename;

		public
		String mimeType;

		public
		byte[] imageData;

	}

	@RpcExport ("imageUpdate")
	public
	class ImageUpdateRpcHandler
		implements RpcHandler {

		List<String> errors =
			new ArrayList<String> ();

		Integer chatId;
		String number;
		ChatUserImageType type;
		List<ImageUpdateAdd> add;
		List<Integer> reorder;
		List<Integer> delete;
		Integer selectedImageId;

		RpcList respImages;
		RpcList respOtherImages;

		@Override
		public
		RpcResult handle (
				RpcSource source) {

			@Cleanup
			Transaction transaction =
				database.beginReadWrite ();

			// get params

			getParams (source);

			// bail on any request-invalid classErrors

			if (errors.iterator ().hasNext ()) {

				return Rpc.rpcError (
					"chat-image-update-response",
					Rpc.stRequestInvalid,
					"request-invalid",
					errors);

			}

			// do it

			doIt (transaction);

			// commit

			transaction.commit ();

			// return

			return Rpc.rpcSuccess (
				"chat-image-update-response",
				"Image update successful",
				respImages,
				respOtherImages);

		}

		@SuppressWarnings ("unchecked")
		private void getParams (RpcSource source) {

			Map<String,Object> params = (Map<String,Object>)
				source.obtain (imageUpdateRequestDef, errors, true);

			if (params == null)
				return;

			chatId = (Integer) params.get ("chat-id");
			number = (String) params.get ("number");
			type = (ChatUserImageType) params.get ("type");
			add = new ArrayList<ImageUpdateAdd> ();
			if (params.get ("add-files") != null)
				add.addAll ((List<ImageUpdateAdd>) params.get ("add-files"));
			if (params.get ("add") != null) {
				for (byte[] data : (List<byte[]>) params.get ("add")) {
					ImageUpdateAdd imageUpdateAdd = new ImageUpdateAdd ();
					imageUpdateAdd.imageData = data;
					add.add (imageUpdateAdd);
				}
			}
			reorder = (List<Integer>) params.get ("reorder");
			delete = (List<Integer>) params.get ("delete");
			selectedImageId = (Integer) params.get ("selected-image-id");
		}

		private void doIt (
				Transaction transaction) {

			ChatRec chat =
				chatHelper.find (
					chatId);

			NumberRec numberRec =
				numberLogic.findOrCreateNumber (number);

			ChatUserRec chatUser =
				chatUserHelper.findOrCreate (
					chat,
					numberRec);

			List<ChatUserImageRec> images =
				chatUser.getChatUserImageListByType (type);

			// delete images

			if (delete != null) {

				// check the delete image ids are valid

				Set<Integer> userImageIds = new HashSet<Integer> ();
				for (ChatUserImageRec image : images) userImageIds.add (image.getId ());
				Set<Integer> requestImageIds = new HashSet<Integer> ();
				for (Integer imageId : delete) {

					if (requestImageIds.contains (imageId)) {

						throw new RpcException (
							Rpc.rpcError (
								"chat-update-image-response",
								Rpc.stRequestInvalid,
								"request-invalid",
								"The delete image ids contains a duplicate"));

					}

					if (! userImageIds.contains (imageId)) {

						throw new RpcException (
							Rpc.rpcError (
								"chat-update-image-response",
								Rpc.stRequestInvalid,
								"request-invalid",
								"The delete image ids contains an invalid image id"));

					}

					requestImageIds.add (imageId);

				}

				// perform the deletions
				int i = 0;
				for (ChatUserImageRec image : images) {
					if (chatUser.getMainChatUserImageByType (type) == image)
						chatUser.setMainChatUserImageByType (type, null);
					image.setIndex (requestImageIds.contains (image.getId ()) ? null : i++);
				}
				transaction.flush ();
				transaction.refresh (chatUser);
				images = chatUser.getChatUserImageListByType (type);
			}

			// reorder images
			if (reorder != null) {

				// check the reorder image ids are valid
				Set<Integer> userImageIds = new HashSet<Integer> ();
				for (ChatUserImageRec image : images) userImageIds.add (image.getId ());
				Set<Integer> requestImageIds = new HashSet<Integer> ();

				for (Integer imageId : reorder) {

					if (requestImageIds.contains (imageId)) {

						throw new RpcException (
							Rpc.rpcError (
								"chat-update-image-response",
								Rpc.stRequestInvalid,
								"request-invalid",
								"The reorder image ids contains a duplicate"));

					}

					requestImageIds.add (imageId);

				}

				if (! equal (userImageIds, requestImageIds)) {

					throw new RpcException (
						Rpc.rpcError (
							"chat-update-image-response",
							Rpc.stRequestInvalid,
							"request-invalid",
							"The reorder image ids do not match the current list"));

				}

				// perform the reorder

				for (ChatUserImageRec image : images)
					image.setIndex (null);

				transaction.flush ();

				int index = 0;

				for (Integer chatUserImageId : reorder) {

					ChatUserImageRec image =
						chatUserImageHelper.find (
							chatUserImageId);

					image.setIndex (index ++);

				}

				transaction.flush ();

				transaction.refresh (chatUser);

				images =
					chatUser.getChatUserImageListByType (
						type);

			}

			// add images

			if (add != null) {

				for (ImageUpdateAdd imageUpdateAdd : add) {

					chatUserLogic.setImage (
						chatUser,
						type,
						imageUpdateAdd.imageData,
						imageUpdateAdd.filename,
						imageUpdateAdd.mimeType,
						null,
						true);

				}

				transaction.flush ();
				transaction.refresh (chatUser);

				images =
					chatUser.getChatUserImageListByType (type);

			}

			// set selected image
			if (selectedImageId != null) {
				ChatUserImageRec selectedCui = null;
				for (ChatUserImageRec cui : chatUser.getChatUserImageListByType (type)) {
					if (! equal (cui.getId (), selectedImageId)) continue;
					selectedCui = cui;
				}

				if (selectedCui == null) {

					throw new RpcException (
						Rpc.rpcError (
							"chat-update-image-response",
							Rpc.stRequestInvalid,
							"request-invalid",
							"The new selected image id does not exist"));

				}

				chatUser.setMainChatUserImageByType (
					type,
					selectedCui);

			}

			// retrieve all images

			respImages =
				Rpc.rpcList ("images", "image", RpcType.rStructure);

			for (ChatUserImageRec cui : chatUser.getChatUserImageListByType (type)) {

				RpcStructure respImage =
					Rpc.rpcStruct ("image",
						Rpc.rpcElem ("image-id", cui.getId ()),
						Rpc.rpcElem ("media-id", cui.getMedia ().getId ()),
						Rpc.rpcElem ("classification", cui.getClassification ()),
						Rpc.rpcElem ("selected", cui == chatUser.getMainChatUserImageByType (type)),
						Rpc.rpcElem ("status", chatUserInfoStatusMuneMap.get (cui.getStatus ())),
						Rpc.rpcElem ("creation-time", cui.getTimestamp ().getTime ()));

				if (cui.getFullMedia () != null) {

					respImage.add (
						Rpc.rpcElem ("full-media-id", cui.getFullMedia ().getId ()),
						Rpc.rpcElem ("full-media-filename", cui.getFullMedia ().getFilename ()),
						Rpc.rpcElem ("full-media-mime-type", cui.getFullMedia ().getMediaType ().getMimeType ()));

				}

				if (cui.getModerationTime () != null) {

					respImage.add (
						Rpc.rpcElem ("moderation-time", cui.getModerationTime ().getTime ()));

				}

				respImages.add (respImage);
			}

			// retrieve all images

			respOtherImages =
				Rpc.rpcList ("other-images", "image", RpcType.rStructure);

			for (ChatUserImageRec cui : chatUser.getChatUserImages ()) {

				if (cui.getType () != type) continue;

				if (! in (cui.getStatus (),
						ChatUserInfoStatus.moderatorPending,
						ChatUserInfoStatus.moderatorRejected))
					continue;

				RpcStructure respImage =
					Rpc.rpcStruct ("image",
						Rpc.rpcElem ("image-id", cui.getId ()),
						Rpc.rpcElem ("media-id", cui.getMedia ().getId ()),
						Rpc.rpcElem ("classification", cui.getClassification ()),
						Rpc.rpcElem ("selected", cui == chatUser.getMainChatUserImageByType (type)),
						Rpc.rpcElem ("status", chatUserInfoStatusMuneMap.get (cui.getStatus ())),
						Rpc.rpcElem ("creation-time", cui.getTimestamp ().getTime ()));

				if (cui.getFullMedia () != null) {

					respImage.add (
						Rpc.rpcElem ("full-media-id", cui.getFullMedia ().getId ()),
						Rpc.rpcElem ("full-media-filename", cui.getFullMedia ().getFilename ()),
						Rpc.rpcElem ("full-media-mime-type", cui.getFullMedia ().getMediaType ().getMimeType ()));

				}

				if (cui.getModerationTime () != null) {

					respImage.add (
						Rpc.rpcElem ("moderation-time", cui.getModerationTime ().getTime ()));

				}

				respOtherImages.add (respImage);

			}

		}

	}

	// ============================================================ credit rpc handler

	private final static
	RpcDefinition creditRequestDef =
		Rpc.rpcDefinition ("chat-credit-request", RpcType.rStructure,
			Rpc.rpcDefinition ("chat-id", RpcType.rInteger),
			Rpc.rpcDefinition ("number", RpcType.rString),
			Rpc.rpcDefinition ("send-count", null, RpcType.rInteger, RpcChecker.integerZeroOrMore),
			Rpc.rpcDefinition ("send-amount", null, RpcType.rInteger, RpcChecker.integerZeroOrMore),
			Rpc.rpcDefinition ("credit-amount", null, RpcType.rInteger),
			Rpc.rpcDefinition ("bill-amount", null, RpcType.rInteger),
			Rpc.rpcDefinition ("details", null, RpcType.rString));

	@RpcExport ("credit")
	public
	class CreditRpcHandler
		implements RpcHandler {

		List<String> errors =
			new ArrayList<String> ();

		Integer chatId;
		String number;

		Integer sendCount;
		Integer sendAmount;
		Integer creditAmount;
		Integer billAmount;
		String details;

		int routeCharge;

		@Override
		public
		RpcResult handle (
				RpcSource source) {

			@Cleanup
			Transaction transaction =
				database.beginReadWrite ();

			// get params

			getParams (source);

			// bail on any request-invalid classErrors

			if (errors.iterator ().hasNext ()) {

				return Rpc.rpcError (
					"chat-credit-response",
					Rpc.stRequestInvalid,
					"request-invalid",
					errors);

			}

			// do it

			doIt (transaction);

			// commit

			transaction.commit ();

			// return

			return Rpc.rpcSuccess (
				"chat-credit-response",
				"Credit successful",

				Rpc.rpcElem (
					"route-charge",
					routeCharge));

		}

		@SuppressWarnings ("unchecked")
		private void getParams (RpcSource source) {

			Map<String,Object> params = (Map<String,Object>)
				source.obtain (creditRequestDef, errors, true);

			if (params == null)
				return;

			chatId = (Integer) params.get ("chat-id");
			number = (String) params.get ("number");
			sendCount = (Integer) params.get ("send-count");
			sendAmount = (Integer) params.get ("send-amount");
			creditAmount = (Integer) params.get ("credit-amount");
			billAmount = (Integer) params.get ("bill-amount");
			details = (String) params.get ("details");
		}

		private void doIt (
				Transaction transaction) {

			ChatRec chat =
				chatHelper.find (
					chatId);

			NumberRec numberRec =
				numberLogic.findOrCreateNumber (
					number);

			ChatUserRec chatUser =
				chatUserHelper.findOrCreate (
					chat,
					numberRec);

			ChatSchemeRec chatScheme =
				chatUser.getChatScheme ();

			routeCharge =
				chatScheme.getRbBillRoute ().getOutCharge ();

			// set count from amount if not specified

			if (sendCount == null && sendAmount != null)
				sendCount = sendAmount / routeCharge;

			// check send count and amount match

			if (
				allOf (
					isNotNull (sendCount),
					isNotNull (sendAmount),
					notEqual (
						sendCount * routeCharge,
						sendAmount))
			) {

				throw new RpcException (
					"chat-credit-response",
					stSendAmountCountMismatch,
					"send-amount-count-mismatch",
					stringFormat (
						"Send count %s and send amount %s mismatch. Route ",
						sendCount,
						sendAmount,
						"charge is %s.",
						routeCharge));

			}

			// send bills

			if (sendCount != null) {

				for (int i = 0; i < sendCount; i++)
					chatCreditLogic.userBillReal (
						chatUser,
						true);

			}

			// update credit

			if (creditAmount != null || billAmount != null) {

				chatUserCreditHelper.insert (
					new ChatUserCreditRec ()

					.setChatUser (
						chatUser)

					.setCreditAmount (
						ifNull (creditAmount, 0))

					.setBillAmount (
						ifNull (billAmount, 0))

					.setGift (
						ifNull (billAmount, 0) == 0)

					.setDetails (
						ifNull (details, ""))

				);

				chatUser

					.setCredit (
						chatUser.getCredit ()
						+ creditAmount)

					.incCreditBought (
						creditAmount);

			}

		}

	}

	// ================================= register handlers

	static {

		registerRpcHandlerClasses (
			MediaRpcHandler.class,
			MessageSendRpcHandler.class,
			MessagePollRpcHandler.class,
			ProfileRpcHandler.class,
			ProfilesRpcHandler.class,
			ImageUpdateRpcHandler.class,
			CreditRpcHandler.class,
			ProfileDeleteRpcHandler.class);

	}

}
