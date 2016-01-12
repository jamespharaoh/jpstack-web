package wbs.smsapps.forwarder.api;

import static wbs.framework.utils.etc.Misc.equal;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import lombok.Cleanup;
import lombok.extern.log4j.Log4j;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import wbs.api.mvc.ApiFile;
import wbs.api.mvc.WebApiAction;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.web.PathHandler;
import wbs.framework.web.ServletModule;
import wbs.framework.web.WebFile;
import wbs.platform.media.logic.MediaLogic;
import wbs.platform.media.model.MediaRec;
import wbs.platform.rpc.core.Rpc;
import wbs.platform.rpc.core.RpcChecker;
import wbs.platform.rpc.core.RpcDefinition;
import wbs.platform.rpc.core.RpcElem;
import wbs.platform.rpc.core.RpcException;
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
import wbs.sms.message.core.model.MessageStatus;
import wbs.smsapps.forwarder.logic.ForwarderLogic;
import wbs.smsapps.forwarder.logic.ForwarderLogicImplementation;
import wbs.smsapps.forwarder.logic.ReportableException;
import wbs.smsapps.forwarder.model.ForwarderMessageInObjectHelper;
import wbs.smsapps.forwarder.model.ForwarderMessageInRec;
import wbs.smsapps.forwarder.model.ForwarderMessageOutObjectHelper;
import wbs.smsapps.forwarder.model.ForwarderMessageOutRec;
import wbs.smsapps.forwarder.model.ForwarderMessageOutReportRec;
import wbs.smsapps.forwarder.model.ForwarderRec;

@Log4j
@SingletonComponent ("forwarderApiModule")
public
class ForwarderApiModule
	implements ServletModule {

	@Inject
	Database database;

	@Inject
	ForwarderApiLogic forwarderApiLogic;

	@Inject
	ForwarderMessageInObjectHelper forwarderMessageInHelper;

	@Inject
	ForwarderMessageOutObjectHelper forwarderMessageOutHelper;

	@Inject
	ForwarderLogic forwarderLogic;

	@Inject
	ForwarderQueryExMessageChecker forwarderQueryExMessageChecker;

	@Inject
	MediaLogic mediaLogic;

	@Inject
	@Named ("forwarderPeekExRequestDef")
	RpcDefinition forwarderPeekExRequestDef;

	@Inject
	@Named ("forwarderQueryExRequestDef")
	RpcDefinition forwarderQueryExRequestDef;

	@Inject
	Provider<ApiFile> apiFile;

	@Inject
	Provider<PhpRpcAction> phpRpcAction;

	@Inject
	Provider<XmlRpcAction> xmlRpcAction;

	// ========================================================= servlet module

	@Override
	public
	Map<String,PathHandler> paths () {

		return new HashMap<String,PathHandler> ();

	}

	@Override
	public
	Map<String,WebFile> files () {

		return ImmutableMap.<String,WebFile>builder ()

			.put (
				"/forwarder/control",
				apiFile.get ()
					.getActionName ("forwarderInAction")
					.postActionName ("forwarderInAction"))

			.put (
				"/forwarder/in",
				apiFile.get ()
					.getActionName ("forwarderInAction")
					.postActionName ("forwarderInAction"))

			.put (
				"/forwarder/out",
				apiFile.get ()
					.getActionName ("forwarderOutAction")
					.postActionName ("forwarderOutAction"))

			// ---------- php

			.put (
				"/forwarder/php/send",
				apiFile.get ()
					.postApiAction (sendPhpAction))

			.put (
				"/forwarder/php/sendEx",
				apiFile.get ()
					.postApiAction (sendExPhpAction))

			.put (
				"/forwarder/php/queryEx",
				apiFile.get ()
					.postApiAction (queryExPhpAction))

			.put (
				"/forwarder/php/peekEx",
				apiFile.get ()
					.postApiAction (peekExPhpAction))

			.put (
				"/forwarder/php/unqueueEx",
				apiFile.get ()
					.postApiAction (unqueueExPhpAction))

			// ---------- xml

			.put (
				"/forwarder/xml/send",
				apiFile.get ()
					.postApiAction (sendXmlAction))

			.put (
				"/forwarder/xml/sendEx",
				apiFile.get ()
					.postApiAction (sendExXmlAction))

			.put (
				"/forwarder/xml/queryEx",
				apiFile.get ()
					.postApiAction (queryExXmlAction))

			.put (
				"/forwarder/xml/peekEx",
				apiFile.get ()
					.postApiAction (peekExXmlAction))

			.put (
				"/forwarder/xml/unqueueEx",
				apiFile.get ()
					.postApiAction (unqueueExXmlAction))

			.build ();

	}

	// =================================================== after properties set

	@PostConstruct
	public
	void afterPropertiesSet () {
		initRpcHandlers ();
		initActions ();
	}

	// =========================================================== rpc handlers

	ReusableRpcHandler
		sendRpcHandler,
		sendExRpcHandler,
		queryExRpcHandler,
		peekExRpcHandler;

	private
	void initRpcHandlers () {

		sendRpcHandler =
			new RpcHandlerFactory (
				SendRpcHandler.class,
				this);

		sendExRpcHandler =
			new RpcHandlerFactory (
				SendExRpcHandler.class,
				this);

		queryExRpcHandler =
			new RpcHandlerFactory (
				QueryExRpcHandler.class,
				this);

		peekExRpcHandler =
			new RpcHandlerFactory (
				PeekExRpcHandler.class,
				this);

	}

	// ============================================================ actions

	WebApiAction

		sendPhpAction,
		sendExPhpAction,
		queryExPhpAction,
		peekExPhpAction,
		unqueueExPhpAction,

		sendXmlAction,
		sendExXmlAction,
		queryExXmlAction,
		peekExXmlAction,
		unqueueExXmlAction;

	private
	void initActions () {

		// php

		sendPhpAction =
			phpRpcAction.get ().rpcHandler (
				sendRpcHandler);

		sendExPhpAction =
			phpRpcAction.get ().rpcHandler (
				sendExRpcHandler);

		queryExPhpAction =
			phpRpcAction.get ().rpcHandler (
				queryExRpcHandler);

		peekExPhpAction =
			phpRpcAction.get ().rpcHandler (
				peekExRpcHandler);

		unqueueExPhpAction =
			phpRpcAction.get ().rpcHandlerName (
				"forwarderUnqueueExRpcHandler");

		// xml

		sendXmlAction =
			xmlRpcAction.get ().rpcHandler (
				sendRpcHandler);

		sendExXmlAction =
			xmlRpcAction.get ().rpcHandler (
				sendExRpcHandler);

		queryExXmlAction =
			xmlRpcAction.get ().rpcHandler (
				queryExRpcHandler);

		peekExXmlAction =
			xmlRpcAction.get ().rpcHandler (
				peekExRpcHandler);

		unqueueExXmlAction =
			xmlRpcAction.get ().rpcHandlerName (
				"forwarderUnqueueExRpcHandler");

	}

	// ============================================================ send rpc
	// handler

	private final static
	RpcDefinition sendRequestDef =
		Rpc.rpcDefinition ("forwarder-send-request", RpcType.rStructure,
			Rpc.rpcDefinition ("slice", RpcType.rString),
			Rpc.rpcDefinition ("forwarder", RpcType.rString),
			Rpc.rpcDefinition ("password", RpcType.rString),
			Rpc.rpcDefinition ("num-from", RpcType.rString),
			Rpc.rpcDefinition ("num-to", RpcType.rString, RpcChecker.stringNumeric),
			Rpc.rpcDefinition ("message", RpcType.rString),
			Rpc.rpcDefinition ("route", "free", RpcType.rString),
			Rpc.rpcDefinition ("service", "default", RpcType.rString),
			Rpc.rpcDefinition ("client-id", null, RpcType.rString),
			Rpc.rpcDefinition ("reply-to-server-id", null, RpcType.rInteger),
			Rpc.rpcDefinition ("pri", 0, RpcType.rInteger));

	public
	class SendRpcHandler
		implements RpcHandler {

		String numTo;
		String numFrom;
		String message;
		String clientId;
		String route;
		String service;
		Integer replyToServerId;
		Long pri;

		ForwarderRec forwarder;
		ForwarderLogicImplementation.SendTemplate sendTemplate;

		List<String> errors =
			new ArrayList<String> ();

		@Override
		public
		RpcResult handle (
				RpcSource source) {

			@Cleanup
			Transaction transaction =
				database.beginReadWrite (
					this);

			// authenticate

			forwarder =
				forwarderApiLogic.rpcAuth (source);

			// get params

			getParams (source);

			// bail on any request-invalid classErrors

			if (errors.iterator ().hasNext ()) {

				return Rpc.rpcError (
					"forwarder-send-response",
					Rpc.stRequestInvalid,
					"request-invalid",
					errors);

			}

			// create template

			createTemplate ();

			// check template

			if (checkTemplate ()) {

				// and send

				sendTemplate ();

				// return success

				transaction.commit ();

				return Rpc.rpcSuccess (
					"Message queued for sending",
					"forwarer-send-response",
					Rpc.rpcElem (
						"server-id",
						sendTemplate.parts.get (0).forwarderMessageOut.getId ()));

			} else {

				// return failure

				throw new RuntimeException (
						"Need to elaborate on error here...");

			}

		}

		private
		void getParams (
				RpcSource source) {

			Map<String,Object> params =
				forwarderApiLogic.unsafeMapStringObject (
					source.obtain (
						sendRequestDef,
						errors,
						true));

			numTo =
				(String)
				params.get ("num-to");

			numFrom =
				(String)
				params.get ("num-from");

			message =
				(String)
				params.get("message");

			clientId = (String) params.get("client-id");
			route = (String) params.get("route");
			service = (String) params.get("service");
			replyToServerId = (Integer) params.get("reply-to-server-id");
			pri = (Long) params.get("pri");

		}

		private
		void createTemplate () {

			sendTemplate =
				new ForwarderLogicImplementation.SendTemplate ();

			sendTemplate.forwarder =
				forwarder;

			sendTemplate.fmInId =
				replyToServerId;

			ForwarderLogicImplementation.SendPart part =
				new ForwarderLogicImplementation.SendPart ();

			sendTemplate.parts.add (part);

			part.message = message;
			part.numFrom = numFrom;
			part.numTo = numTo;
			part.routeCode = route;
			part.serviceCode = service;
			part.clientId = clientId;
			part.pri = pri;

		}

		private
		boolean checkTemplate () {

			return forwarderLogic.sendTemplateCheck (
				sendTemplate);

		}

		private
		void sendTemplate () {

			forwarderLogic.sendTemplateSend (
				sendTemplate);

		}

	}

	// ============================================================ send ex rpc
	// handler

	private static
	enum MessageTypeEnum {

		sms,
		wapPush,
		mms

	}

	private final static
	Map<String,Object> messageTypeEnumMap =
		ImmutableMap.<String,Object>builder ()
			.put ("sms", MessageTypeEnum.sms)
			.put ("wap-push", MessageTypeEnum.wapPush)
			.put ("mms", MessageTypeEnum.mms)
			.build ();

	private final static
	RpcDefinition sendExRequestDef =
		Rpc.rpcDefinition ("forwarder-send-ex-request", RpcType.rStructure,
			Rpc.rpcDefinition ("slice", RpcType.rString),
			Rpc.rpcDefinition ("forwarder", RpcType.rString),
			Rpc.rpcDefinition ("password", RpcType.rString),
			Rpc.rpcDefinition ("allow-partial", false, RpcType.rBoolean),
			Rpc.rpcDefinition ("message-chains", RpcType.rList,
				Rpc.rpcDefinition ("message-chain", RpcType.rStructure,
				Rpc.rpcDefinition ("reply-to-server-id", null, RpcType.rInteger),
					Rpc.rpcDefinition ("unqueueExMessages", RpcType.rList,
						Rpc.rpcDefinition ("message", RpcType.rStructure,

							Rpc.rpcDefinition (
								"type",
								MessageTypeEnum.sms,
								RpcType.rString,
								Rpc.rpcEnumChecker (
									messageTypeEnumMap)),

							Rpc.rpcDefinition (
								"num-from",
								RpcType.rString),

							Rpc.rpcDefinition (
								"num-to",
								RpcType.rString,
								RpcChecker.stringNumeric),

							Rpc.rpcDefinition (
								"message",
								RpcType.rString),

							Rpc.rpcDefinition (
								"url",
								null,
								RpcType.rString),

							Rpc.rpcDefinition (
								"route",
								"free",
								RpcType.rString),

							Rpc.rpcDefinition (
								"client-id",
								null,
								RpcType.rString),

							Rpc.rpcDefinition (
								"reply-to-server-id",
								null,
								RpcType.rInteger),

							Rpc.rpcDefinition (
								"pri",
								0,
								RpcType.rInteger),

							Rpc.rpcDefinition (
								"service",
								"default",
								RpcType.rString),

							Rpc.rpcDefinition (
								"reports",
								false,
								RpcType.rBoolean),

							Rpc.rpcDefinition (
								"retry-days",
								null,
								RpcType.rInteger),

							Rpc.rpcDefinition (
								"tags",
								new HashSet<String> (),
								RpcType.rList,
								Rpc.rpcSetChecker (),

								Rpc.rpcDefinition (
									"tag",
									RpcType.rString)),

							Rpc.rpcDefinition (
								"medias",
								null,
								RpcType.rList,

								Rpc.rpcDefinition (
									"media",
									RpcType.rStructure,

									Rpc.rpcDefinition (
										"url",
										RpcType.rString),

									Rpc.rpcDefinition (
										"message",
										null,
										RpcType.rString))),

							Rpc.rpcDefinition (
								"network-id",
								null,
								RpcType.rInteger))))));

	private static
	class SendExMessageChain {

		Integer replyToServerId;

		List<SendExMessage> messages =
			new ArrayList<SendExMessage> ();

		ForwarderLogicImplementation.SendTemplate sendTemplate;

		List<String> errors =
			new ArrayList<String> ();

		boolean ok =
			false;

	}

	private static
	class SendExMessage {

		MessageTypeEnum type;
		String numTo;
		String numFrom;
		String message;
		String url;
		String clientId;
		String route;
		String service;
		Long pri;
		Long retryDays;
		Set<String> tags;
		ForwarderLogicImplementation.SendPart part;
		Collection<MediaRec> medias = null;
		String subject;

	}

	public
	class SendExRpcHandler
		implements RpcHandler {

		// the authenticated forwarder
		ForwarderRec forwarder;

		// here we compile top-level status unqueueExMessages

		List<String> errors =
			new ArrayList<String> ();

		// extracted top-level params

		boolean allowPartial;

		// per message-chain stuff

		List<SendExMessageChain> messageChains =
			new ArrayList<SendExMessageChain>();

		// We remember the first message chain to give a SendError, this is used
		// to form the main request result in case
		// of a partial failure cancelling the whole request.

		ForwarderLogicImplementation.SendError firstError = null;

		// keep track of the result of the check stage, and whether we have
		// decided to cancel the whole request.

		boolean someSuccess = false;
		boolean someFailed = false;

		boolean cancel = false;

		@Override
		public
		RpcResult handle (
				RpcSource source) {

			@Cleanup
			Transaction transaction =
				database.beginReadWrite (
					this);

			// authenticate

			forwarder =
				forwarderApiLogic.rpcAuth (source);

			// get params

			getParams (source);

			// bail on any request-invalid classErrors

			if (errors.size () > 0) {

				return Rpc.rpcError (
					"forwarder-send-ex-response",
					Rpc.stRequestInvalid,
					"request-invalid",
					errors);

			}

			// fill in the SendTemplates

			createTemplate ();

			// check the template is ok

			checkTemplate ();

			// send the message (if appropriate)

			if (! cancel)
				sendTemplate ();

			// return

			collectErrors ();

			transaction.commit ();

			return makeResult ();

		}

		private
		Collection<MediaRec> getMedias (
				List<Map<String, Object>> mpList)
			throws ReportableException {

			Collection<MediaRec> medias =
				new ArrayList<MediaRec> ();

			try {

				for (Map<String,Object> mp
						: mpList) {

					String url = (String) mp.get("url");

					if (url == null)
						throw new ReportableException ("Media url is null");

					// process

					String filename =
						stringFormat (
							"%s.jpeg",
							url.hashCode ());

					medias.add (
						mediaLogic.createMediaFromImageRequired (
							getImageContent (url),
							"image/jpeg",
							filename));

					String message =
						(String)
						mp.get ("message");

					if (message != null) {

						// TODO wtf is this?

						char pound =
							'\u00A3';

						message =
							message.replaceAll (
								"&pound;",
								"" + pound);

						String txtFilename =
							stringFormat (
								"%s.txt",
								message.hashCode ());

						medias.add (
							mediaLogic.createTextMedia (
								message,
								"text/plain",
								txtFilename));

					}

				}

			} catch (Exception e) {
				throw new ReportableException(e.getMessage());
			}

			return medias;
		}

		private
		byte[] getImageContent (
				String inurl)
			throws ReportableException {

			try {

				URL url =
					new URL (inurl);

				URLConnection yc =
					url.openConnection ();

				yc.setReadTimeout (
					30 * 1000);

				InputStream inputStream =
					yc.getInputStream ();

				BufferedInputStream bufferedInputStream =
					new BufferedInputStream (inputStream);

				ByteArrayOutputStream byteArrayOutputStream =
					new ByteArrayOutputStream();

				byte[] buf = new byte[8192];
				int numread;
				while ((numread = bufferedInputStream.read(buf)) > 0) {
					byteArrayOutputStream.write(buf, 0, numread);
				}
				bufferedInputStream.close();
				return byteArrayOutputStream.toByteArray();
			} catch (Exception e) {
				throw new ReportableException(e.getMessage());
			}

		}

		/**
		 * Extracts input parameters from the RpcSource.
		 */
		private
		void getParams (
				RpcSource source) {

			Map<String,Object> params =
				forwarderApiLogic.unsafeMapStringObject (
					source.obtain (
						sendExRequestDef,
						errors,
						true));

			allowPartial =
				(Boolean)
				params.get ("allow-partial");

			List<Map<String,Object>> messageChainPartList =
				forwarderApiLogic.unsafeListMapStringObject (
					params.get ("message-chains"));

			if (messageChainPartList != null)

				for (Map<String,Object> messageChainPart
						: messageChainPartList) {

					SendExMessageChain sendExMessageChain =
						new SendExMessageChain ();

					messageChains.add (sendExMessageChain);

					sendExMessageChain.replyToServerId =
						(Integer) params.get ("reply-to-server-id");

					List<Map<String,Object>> mpList =
						forwarderApiLogic.unsafeListMapStringObject (
							messageChainPart.get ("unqueueExMessages"));

					if (mpList != null)

						for (Map<String,Object> mp
								: mpList) {

							SendExMessage sendExMessage =
								new SendExMessage ();

							sendExMessageChain.messages.add (
								sendExMessage);

							sendExMessage.type =
								(MessageTypeEnum)
								mp.get ("type");

							sendExMessage.numTo =
								(String)
								mp.get ("num-to");

							sendExMessage.numFrom = (String) mp.get("num-from");
							sendExMessage.message = (String) mp.get("message");
							char pound = '\u00A3';
							sendExMessage.message = sendExMessage.message.replaceAll("&pound;", ""
									+ pound);
							sendExMessage.url = (String) mp.get("url");
							sendExMessage.clientId = (String) mp.get("client-id");
							sendExMessage.route = (String) mp.get("route");
							sendExMessage.service = (String) mp.get("service");
							sendExMessage.pri = (Long) mp.get("pri");
							sendExMessage.retryDays = (Long) mp.get("retry-days");

							@SuppressWarnings ("unchecked")
							Set<String> tagsTemp =
								(Set<String>)
								mp.get ("tags");

							sendExMessage.tags = tagsTemp;

							if (sendExMessage.type == MessageTypeEnum.mms) {

								try {

									List<Map<String,Object>> mediaList =
										forwarderApiLogic.unsafeListMapStringObject (
											mp.get ("medias"));

									if (mediaList == null) {

										errors.add (
											"Must provide media list for mms type.");

										break;

									}
									sendExMessage.medias = getMedias(mediaList);
									sendExMessage.subject = sendExMessage.message;

								} catch (ReportableException e) {
									errors.add("Error: " + e.getMessage());
								}

							}

							switch (sendExMessage.type) {

							case mms:

								if (sendExMessage.url != null)
									errors
											.add("Parameter should not be set when message type is mms: url");

								break;

							case sms:

								if (sendExMessage.url != null)
									errors
											.add("Parameter should not be set when message type is sms: url");

								break;

							case wapPush:

								if (sendExMessage.url == null)
									errors
											.add("Parameter must be set when message type is wap push: url");

								break;

							}
						}
				}
		}

		/**
		 * Creates SendTemplates for each message chain.
		 */
		private
		void createTemplate () {

			for (SendExMessageChain sendExMessageChain
					: messageChains) {

				sendExMessageChain.sendTemplate =
					new ForwarderLogicImplementation.SendTemplate ();

				sendExMessageChain.sendTemplate.forwarder =
					forwarder;

				sendExMessageChain.sendTemplate.fmInId =
					sendExMessageChain.replyToServerId;

				for (SendExMessage sendExMessage
						: sendExMessageChain.messages) {

					ForwarderLogicImplementation.SendPart part =
						new ForwarderLogicImplementation.SendPart ();

					part.message = sendExMessage.message;
					part.url = sendExMessage.url;
					part.numFrom = sendExMessage.numFrom;
					part.numTo = sendExMessage.numTo;
					part.routeCode = sendExMessage.route;
					part.serviceCode = sendExMessage.service;
					part.clientId = sendExMessage.clientId;
					part.pri = sendExMessage.pri;
					part.retryDays = sendExMessage.retryDays;
					part.tags = sendExMessage.tags;
					part.medias = sendExMessage.medias;
					part.subject = sendExMessage.subject;
					sendExMessageChain.sendTemplate.parts.add(part);
					sendExMessage.part = part;

				}

			}

		}

		/**
		 * Calls forwarderUtils to check each template, keeping track of
		 * failures and successes etc...
		 */
		private
		void checkTemplate () {

			for (
				int index = 0;
				index < messageChains.size ();
				index ++
			) {

				SendExMessageChain sendExMessageChain =
					messageChains.get (index);

				// call sendTemplateCheck

				if (forwarderLogic.sendTemplateCheck (
						sendExMessageChain.sendTemplate)) {

					someSuccess = true;

					sendExMessageChain.ok = true;

				} else {

					someFailed = true;

					if (firstError == null) {

						firstError =
							sendExMessageChain.sendTemplate.sendError;

					}

				}

			}

			if (someFailed && ! allowPartial)
				cancel = true;

		}

		private
		void sendTemplate () {

			for (SendExMessageChain sendExMessasgeChain
					: messageChains) {

				if (! sendExMessasgeChain.ok)
					continue;

				forwarderLogic.sendTemplateSend (
					sendExMessasgeChain.sendTemplate);

			}

		}

		private
		void collectErrors () {

			// do main status message

			if (someFailed && ! cancel) {

				errors.add (
					"Partial success");

			} else if (! someFailed) {

				errors.add (
					"All unqueueExMessages sent");

			}

			// then collect lower-level unqueueExMessages

			for (
				int index = 0;
				index < messageChains.size ();
				index ++
			) {

				SendExMessageChain sendExMessageChain =
					messageChains.get (index);

				for (String error
						: sendExMessageChain.sendTemplate.errors) {

					sendExMessageChain.errors.add (error);

					errors.add (
						String.format (
							"Chain %d: %s",
							index,
							error));

				}

				for (int j = 0; j < sendExMessageChain.messages.size(); j++) {
					SendExMessage sem = sendExMessageChain.messages.get(j);
					for (String error : sem.part.errors) {
						sendExMessageChain.errors.add(String.format("Message %d: %s", j,
								error));
						errors.add(String.format("Chain %d: Message %d: %s", index,
								j, error));
					}
				}
			}
		}

		private
		RpcResult makeResult () {

			// collect message chain results

			RpcList messageChainsPart =
				Rpc.rpcList (
					"message-chains",
					"message-chain",
					RpcType.rStructure);

			for (
				int i = 0;
				i < messageChains.size ();
				i ++
			) {

				messageChainsPart.add (
					sendExMessageChainToResultMessageChainPart (
						messageChains.get (i)));
			}

			if (! someFailed) {

				// success

				return new RpcResult (
					"forwarder-send-ex-response",
					Rpc.stSuccess,
					"success",
					ImmutableList.of ("All message chains sent"),
					messageChainsPart);

			} else if (someFailed && cancel) {

				// cancelled due to failure

				return new RpcResult (
					"forwarder-send-ex-response",
					forwarderSendErrorToStatus (firstError),
					forwarderSendErrorToStatusCode (firstError),
					errors,
					messageChainsPart);

			} else if (someFailed && ! cancel) {

				// partial success

				return new RpcResult (
					"forwarder-send-ex-response",
					Rpc.stPartialSuccess,
					"partial-success",
					errors,
					messageChainsPart);
			}

			throw new RuntimeException();
		}

		private
		RpcElem sendExMessageChainToResultMessageChainPart (
				SendExMessageChain sendExMessageChain) {

			// construct unqueueExMessages part

			RpcList messagesPart =
				Rpc.rpcList (
					"unqueueExMessages",
					"message",
					RpcType.rStructure);

			for (int i = 0; i < sendExMessageChain.messages.size (); i++) {

				messagesPart.add (
					sendExMessageToResultMessagePart (
						sendExMessageChain.messages.get (i)));

			}

			// constrcut message chain part

			if (sendExMessageChain.sendTemplate.sendError == null && cancel) {

				// cancelled due to other error

				return Rpc.rpcStruct ("message-chain",
					Rpc.rpcElem ("status", Rpc.stCancelled),
					Rpc.rpcElem ("status-code", "cancelled"),
					Rpc.rpcElem ("status-message", "Cancelled"),
					Rpc.rpcList ("status-unqueueExMessages", "status-message",
						ImmutableList.<String>of (
							"Cancelled due to other error")),
					Rpc.rpcElem ("success", false),
					messagesPart);

			} else if (sendExMessageChain.sendTemplate.sendError == null && ! cancel) {

				// success

				return Rpc.rpcStruct ("message-chain",
					Rpc.rpcElem ("status", Rpc.stSuccess),
					Rpc.rpcElem ("status-code", "success"),
					Rpc.rpcElem ("status-message", "Success"),
					Rpc.rpcList ("status-unqueueExMessages", "status-message",
						ImmutableList.<String>of (
							"Message chain sent")),
					Rpc.rpcElem ("success", true),
					messagesPart);

			} else if (sendExMessageChain.sendTemplate.sendError != null) {

				// error

				return Rpc.rpcStruct ("message-chain",

					Rpc.rpcElem ("status",
						forwarderSendErrorToStatus (
							sendExMessageChain.sendTemplate.sendError)),

					Rpc.rpcElem (
						"status-code",
						forwarderSendErrorToStatusCode (
							sendExMessageChain.sendTemplate.sendError)),

					Rpc.rpcElem (
						"status-message",
						forwarderSendErrorToStatusMessage (
							sendExMessageChain.sendTemplate.sendError)),

					Rpc.rpcList (
						"status-unqueueExMessages",
						"status-message",
						sendExMessageChain.errors),

					Rpc.rpcElem (
						"success",
						false),

					messagesPart);

			}

			throw new RuntimeException ();

		}

		private
		RpcElem sendExMessageToResultMessagePart (
				SendExMessage sem) {

			RpcStructure ret =
				Rpc.rpcStruct ("message");

			// include client id if supplied

			if (sem.clientId != null)
				ret.add (Rpc.rpcElem ("client-id", sem.clientId));

			if (sem.part.sendError == null && cancel) {

				// cancelled

				ret.add (
					Rpc.rpcElem ("status", Rpc.stCancelled),
					Rpc.rpcElem ("status-code", "cancelled"),
					Rpc.rpcElem ("status-message", "Cancelled"),
					Rpc.rpcList ("status-unqueueExMessages", "status-message",
						ImmutableList.<String>of (
							"Cancelled due to other error")),
					Rpc.rpcElem ("success", false));

			} else if (sem.part.sendError == null && !cancel) {

				// success

				ret.add (
					Rpc.rpcElem ("server-id", sem.part.forwarderMessageOut.getId ()),
					Rpc.rpcElem ("status", Rpc.stSuccess),
					Rpc.rpcElem ("status-code", "success"),
					Rpc.rpcElem ("status-message", "Message sent"),
					Rpc.rpcList ("status-unqueueExMessages", "status-message",
						ImmutableList.<String>of (
							"Message sent")),
					Rpc.rpcElem ("success", true));

			} else if (sem.part.sendError != null) {

				// error

				ret.add (

					Rpc.rpcElem (
						"status",
						forwarderSendErrorToStatus (sem.part.sendError)),

					Rpc.rpcElem (
						"status-code",
						forwarderSendErrorToStatusCode (sem.part.sendError)),

					Rpc.rpcElem (
						"status-message",
						forwarderSendErrorToStatusMessage (sem.part.sendError)),

					Rpc.rpcList (
						"status-unqueueExMessages",
						"status-message",
						sem.part.errors),

					Rpc.rpcElem (
						"success",
						false));

			} else {
				throw new RuntimeException();
			}

			return ret;
		}

		private
		int forwarderSendErrorToStatus (
				ForwarderLogicImplementation.SendError sendError) {

			switch (sendError) {

			case missingReplyToMessageId:
				return ForwarderApiConstants.stReplyToServerIdRequired;

			case tooManyReplies:
				return ForwarderApiConstants.stRepliesMaxExceeded;

			case invalidReplyToMessageId:
				return ForwarderApiConstants.stReplyToServerIdInvalid;

			case invalidRoute:
				return ForwarderApiConstants.stRouteInvalid;

			case invalidNumFrom:
				return ForwarderApiConstants.stNumFromInvalid;

			case missingClientId:
				return ForwarderApiConstants.stClientIdRequired;

			case reusedClientId:
				return ForwarderApiConstants.stClientIdReused;

			default:
				throw new RuntimeException ();

			}

		}

		private
		String forwarderSendErrorToStatusCode (
				ForwarderLogicImplementation.SendError sendError) {

			switch (sendError) {

			case missingReplyToMessageId:
				return "reply-to-server-id-required";

			case tooManyReplies:
				return "replies-max-exceeded";

			case invalidReplyToMessageId:
				return "reply-to-server-id-invalid";

			case invalidRoute:
				return "route-invalid";

			case invalidNumFrom:
				return "num-from-invalid";

			case missingClientId:
				return "client-id-required";

			case reusedClientId:
				return "client-id-reused";

			default:
				throw new RuntimeException ();

			}

		}

		private
		String forwarderSendErrorToStatusMessage (
				ForwarderLogicImplementation.SendError sendError) {

			switch (sendError) {

			case missingReplyToMessageId:
				return "Reply-to-server-id must be supplied";

			case tooManyReplies:
				return "Maximum reply counters exceeded for this message";

			case invalidReplyToMessageId:
				return "Reply-to-server-id is invalid";

			case invalidRoute:
				return "Route is invalid";

			case invalidNumFrom:
				return "Num-from is invalid for this route";

			case missingClientId:
				return "Client-id required";

			case reusedClientId:
				return "Client-id reused or sent in different order";

			default:
				throw new RuntimeException ();

			}

		}
	}

	private static
	class QueryExMessage {

		String clientId;
		Integer serverId;
		ForwarderMessageOutRec fmOut;

	}

	public
	class QueryExRpcHandler
		implements RpcHandler {

		ForwarderRec forwarder;

		List<String> errors =
			new ArrayList<String> ();

		List<QueryExMessage> messages =
			new ArrayList<QueryExMessage> ();

		@Override
		public
		RpcResult handle (
				RpcSource source) {

			@Cleanup
			Transaction transaction =
				database.beginReadWrite (
					this);

			// authenticate

			forwarder =
				forwarderApiLogic.rpcAuth (source);

			// get params

			getParams (source);

			// bail on any request-invalid classErrors

			if (errors.iterator ().hasNext ()) {

				return Rpc.rpcError (
					"forwarder-query-ex-response",
					Rpc.stRequestInvalid,
					"request-invalid",
					errors);

			}

			// do the stuff

			findMessages ();
			checkIdsMatch ();

			// return

			transaction.commit ();

			return makeSuccess ();

		}

		private
		void getParams (
				RpcSource source) {

			Map<String,Object> params =
				forwarderApiLogic.unsafeMapStringObject (
					source.obtain (
						forwarderQueryExRequestDef,
						errors,
						true));

			List<Map<String,Object>> mpList =
				forwarderApiLogic.unsafeListMapStringObject (
					params.get ("unqueueExMessages"));

			if (mpList == null)
				return;

			for (Map<String,Object> mp
					: mpList) {

				if (mp == null)
					continue;

				QueryExMessage queryExMessage =
					new QueryExMessage ();

				queryExMessage.clientId =
					(String)
					mp.get ("client-id");

				queryExMessage.serverId =
					(Integer)
					mp.get ("server-id");

				messages.add(queryExMessage);

			}

		}

		private
		void findMessages () {

			for (
				int i = 0;
				i < messages.size ();
				i ++
			) {

				QueryExMessage queryExMessage =
					messages.get (i);

				if (queryExMessage.serverId != null) {

					queryExMessage.fmOut =
						forwarderMessageOutHelper.find (
							queryExMessage.serverId);

				}

				if (queryExMessage.clientId != null
						&& queryExMessage.fmOut == null) {

					queryExMessage.fmOut =
						forwarderMessageOutHelper.findByOtherId (
							forwarder,
							queryExMessage.clientId);

				}

				if (queryExMessage.fmOut == null)
					errors.add ("Message " + i + " not found");

			}

			if (errors.size() > 0) {

				throw new RpcException (
					"forwarder-query-ex-response",
					ForwarderApiConstants.stMessageNotFound,
					"message-not-found",
					errors);

			}

		}

		private
		void checkIdsMatch () {

			for (
				int index = 0;
				index < messages.size ();
				index ++
			) {

				QueryExMessage queryExMessage =
					messages.get (index);

				if (queryExMessage.serverId == null
						|| queryExMessage.clientId == null)
					continue;

				if (! equal (
						queryExMessage.serverId,
						queryExMessage.fmOut.getId ())
					|| ! equal (
						queryExMessage.clientId,
						queryExMessage.fmOut.getOtherId ())) {

					errors.add ("Message " + index + " id mismatch");

				}

			}

			if (errors.size () > 0) {

				throw new RpcException (
					"forwarder-query-ex-response",
					ForwarderApiConstants.stMessageIdMismatch,
					"message-id-mismatch",
					errors);

			}

		}

		private
		RpcResult makeSuccess () {

			RpcList messagesPart =
				Rpc.rpcList (
					"unqueueExMessages",
					"message",
					RpcType.rStructure);

			for (QueryExMessage queryExMessage
					: messages) {

				MessageStatus messageStatus =
					queryExMessage.fmOut.getMessage ().getStatus ();

				ForwarderMessageStatus forwarderMessageStatus =
					statusMap.get (messageStatus);

				messagesPart.add (
					Rpc.rpcStruct (
						"message",

						Rpc.rpcElem (
							"server-id",
							queryExMessage.fmOut.getId ()),

						Rpc.rpcElem (
							"client-id",
							queryExMessage.fmOut.getOtherId ()),

						Rpc.rpcElem (
							"message-status",
							forwarderMessageStatus.status),

						Rpc.rpcElem (
							"message-status-code",
							forwarderMessageStatus.statusCode)));

			}

			return Rpc.rpcSuccess (
				"Success",
				"forwarder-query-ex-response",
				messagesPart);

		}

	}

	private
	enum ForwarderMessageStatus {

		pending (
			0x000,
			"pending"),

		sent (
			0x001,
			"sent"),

		sentUpstream (
			0x002,
			"sent-upstream"),

		cancelled (
			0x100,
			"cancelled"),

		failed (
			0x101,
			"failed"),

		undelivered (
			0x102,
			"undelivered"),

		reportTimedOut (
			0x103,
			"report-timed-out"),

		delivered(0x200, "delivered");

		private final
		int status;

		private final
		String statusCode;

		private
		ForwarderMessageStatus (
				int newStatus,
				String newStatusCode) {

			status = newStatus;
			statusCode = newStatusCode;

		}

	}

	private static
	Map<MessageStatus,ForwarderMessageStatus> statusMap =
		ImmutableMap.<MessageStatus,ForwarderMessageStatus>builder ()

			.put (
				MessageStatus.blacklisted,
				ForwarderMessageStatus.undelivered)

			.put (
				MessageStatus.held,
				ForwarderMessageStatus.pending)

			.put (
				MessageStatus.pending,
				ForwarderMessageStatus.pending)

			.put (
				MessageStatus.cancelled,
				ForwarderMessageStatus.cancelled)

			.put (
				MessageStatus.failed,
				ForwarderMessageStatus.failed)

			.put (
				MessageStatus.sent,
				ForwarderMessageStatus.sent)

			.put (
				MessageStatus.delivered,
				ForwarderMessageStatus.delivered)

			.put (
				MessageStatus.undelivered,
				ForwarderMessageStatus.undelivered)

			.put (
				MessageStatus.submitted,
				ForwarderMessageStatus.sentUpstream)

			.put (
				MessageStatus.reportTimedOut,
				ForwarderMessageStatus.reportTimedOut)

			.build ();

	// ============================================================ peek ex rpc
	// handler

	public
	class PeekExRpcHandler
		implements RpcHandler {

		Boolean getMessages;
		Boolean getReports;
		Integer maxResults;
		Boolean advancedReporting;

		ForwarderRec forwarder;

		List<String> errors =
			new ArrayList<String> ();

		@Override
		public
		RpcResult handle (
				RpcSource source) {

			@Cleanup
			Transaction transaction =
				database.beginReadOnly (
					this);

			// authenticate

			forwarder =
				forwarderApiLogic.rpcAuth (source);

			// get params

			getParams (source);

			// bail on any request-invalid classErrors

			if (errors.iterator ().hasNext ())
				return Rpc.rpcError (
					"forwarder-peek-ex-ersponse",
					Rpc.stRequestInvalid,
					"request-invalid",
					errors);

			// find stuff

			RpcResult result =
				makeSuccess ();

			// commit

			return result;

		}

		private
		void getParams (
				RpcSource source) {

			Map<String,Object> params =
				forwarderApiLogic.unsafeMapStringObject (
					source.obtain (
						forwarderPeekExRequestDef,
						errors,
						true));

			getMessages = (Boolean)
				params.get ("get-unqueueExMessages");

			getReports = (Boolean)
				params.get ("get-reports");

			maxResults = (Integer)
				params.get ("max-results");

			advancedReporting = (Boolean)
				params.get ("advanced-reporting");

		}

		private
		RpcResult makeSuccess () {

			RpcList messagesPart =
				Rpc.rpcList (
					"unqueueExMessages",
					"message",
					RpcType.rStructure);

			RpcList reportsPart =
				Rpc.rpcList (
					"reports",
					"report",
					RpcType.rStructure);

			RpcList advancedReportsPart =
				Rpc.rpcList (
					"advancedreports",
					"advancedreport",
					RpcType.rStructure);

			// get a list of pending unqueueExMessages

			if (getMessages) {

				List<ForwarderMessageInRec> pendingMessageList =
					forwarderMessageInHelper.findPendingLimit (
						forwarder,
						maxResults);

				for (ForwarderMessageInRec forwarderMessageIn
						: pendingMessageList) {

					RpcStructure message;

					messagesPart.add (message =

						Rpc.rpcStruct (
							"message",

							Rpc.rpcElem (
								"server-id",
								forwarderMessageIn
									.getId ()),

							Rpc.rpcElem (
								"num-from",
								forwarderMessageIn
									.getMessage ()
									.getNumFrom ()),

							Rpc.rpcElem (
								"num-to",
								forwarderMessageIn
									.getMessage ()
									.getNumTo ()),

							Rpc.rpcElem (
								"message",
								forwarderMessageIn
									.getMessage ()
									.getText ()
									.getText ())));

					if (forwarderMessageIn
							.getMessage ()
							.getNetwork ()
							.getId () != 0) {

						message.add (
							Rpc.rpcElem (
								"network-id",
								forwarderMessageIn
									.getMessage ()
									.getNetwork ()
									.getId ()));

					}

				}

			}

			// get a list of pending reports

			if (getReports) {

				List<ForwarderMessageOutRec> pendingReportList =
					forwarderMessageOutHelper.findPendingLimit (
						forwarder,
						maxResults);

				// create the return data list

				for (ForwarderMessageOutRec forwarderMessageOut
						: pendingReportList) {

					log.debug ("fmo.id=" + forwarderMessageOut.getId ());

					ForwarderMessageOutReportRec forwarderMessageOutReport =
						forwarderMessageOut.getReports ().get (
							(int) (long)
							forwarderMessageOut.getReportIndexPending ());

					ForwarderMessageStatus forwarderMessageStatus =
						statusMap.get (
							forwarderMessageOutReport.getNewMessageStatus ());

					RpcStructure struct =
						Rpc.rpcStruct ("report",
							Rpc.rpcElem ("server-id", forwarderMessageOut.getId ()),
							Rpc.rpcElem ("client-id", forwarderMessageOut.getOtherId ()),
							Rpc.rpcElem ("report-id", forwarderMessageOutReport.getId ()),
							Rpc.rpcElem ("message-status", forwarderMessageStatus.status),
							Rpc.rpcElem ("message-status-code", forwarderMessageStatus.statusCode));

					if (advancedReporting != null && advancedReporting) {

						struct.add (
							Rpc.rpcElem (
								"advanced-message-status",
								forwarderMessageOutReport.getNewMessageStatus ().getOrdinal ()));

					}

					reportsPart.add (struct);

				}

			}

			if (advancedReporting != null && advancedReporting) {

				/*
				List<ForwarderMessageOutRec> pendingReportList =
					forwarderDao.findForwarderMessageOutsPendingByForwarderIdLimitAdvanced (
						forwarder.getId (), maxResults);

				for (ForwarderMessageOutRec fmo : pendingReportList) {

					MessageRec message =
						fmo.getMessage ();

					Set<MessageReportRec> reports =
						message.getReports ();

					for (MessageReportRec mr : reports) {

						MessageReportCodeRec codeRec = mr.getMessageReportCode ();
						if (codeRec == null)
							break;

						RpcStructure struct =
							rpcStruct ("advancedreport",
								rpcElem ("server-id", fmo.getId ()),
								rpcElem ("client-id", fmo.getOtherId ()),
								rpcElem ("report-id", mr.getId ()),
								// dr details
								rpcElem ("type", messageReportTypes.get (codeRec.getType ())),
								rpcElem ("permanent", codeRec.getPermanent ()),
								rpcElem ("status", codeRec.getStatus ()),
								rpcElem ("success", codeRec.getSuccess ()));

						if (codeRec.getStatusType () != null)
							struct.add (
								rpcElem ("statustype", codeRec.getStatusType ()));

						if (codeRec.getReason () != null)
							struct.add (
								rpcElem ("reason", codeRec.getReason ()));

						if (codeRec.getDescription () != null)
							struct.add (
								rpcElem ("description", codeRec.getDescription ()));

						advancedReportsPart.add (struct);

					}

					//fmo.setMessageReportPending (false);

				}
				*/

			}

			// and return it in a map

			if (advancedReporting != null && advancedReporting) {

				return Rpc.rpcSuccess (
					"forwarder-peek-ex-response",
					"Success",
					messagesPart,
					reportsPart,
					advancedReportsPart);

			} else {

				return Rpc.rpcSuccess (
					"forwarder-peek-ex-response",
					"Success",
					messagesPart,
					reportsPart);

			}

		}

	}

}
