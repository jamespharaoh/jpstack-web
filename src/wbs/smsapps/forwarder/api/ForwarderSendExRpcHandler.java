package wbs.smsapps.forwarder.api;

import static wbs.utils.etc.EnumUtils.enumEqualSafe;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.NumberUtils.moreThanZero;
import static wbs.utils.etc.TypeUtils.genericCastUnchecked;
import static wbs.utils.string.StringUtils.stringFormat;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableList;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.NamedDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.OwnedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.platform.media.logic.MediaLogic;
import wbs.platform.media.model.MediaRec;
import wbs.platform.rpc.core.Rpc;
import wbs.platform.rpc.core.RpcDefinition;
import wbs.platform.rpc.core.RpcElem;
import wbs.platform.rpc.core.RpcHandler;
import wbs.platform.rpc.core.RpcList;
import wbs.platform.rpc.core.RpcResult;
import wbs.platform.rpc.core.RpcSource;
import wbs.platform.rpc.core.RpcStructure;
import wbs.platform.rpc.core.RpcType;

import wbs.smsapps.forwarder.logic.ForwarderLogic;
import wbs.smsapps.forwarder.logic.ForwarderLogicImplementation;
import wbs.smsapps.forwarder.logic.ReportableException;
import wbs.smsapps.forwarder.model.ForwarderRec;

@PrototypeComponent ("forwarderSendExRpcHandler")
public
class ForwarderSendExRpcHandler
	implements RpcHandler {

	// singleton dependencies

	@SingletonDependency
	Database database;

	@SingletonDependency
	ForwarderApiLogic forwarderApiLogic;

	@SingletonDependency
	ForwarderLogic forwarderLogic;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	MediaLogic mediaLogic;

	@SingletonDependency
	@NamedDependency ("forwarderSendExRequestDef")
	RpcDefinition sendExRequestDef;

	// state

	ForwarderRec forwarder;

	List <String> errors =
		new ArrayList<> ();

	boolean allowPartial;

	List <ForwarderSendExMessageChain> messageChains =
		new ArrayList<>();

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
			@NonNull TaskLogger parentTaskLogger,
			@NonNull RpcSource source) {

		try (

			OwnedTransaction transaction =
				database.beginReadWrite (
					logContext,
					parentTaskLogger,
					"SendExRpcHandler.handle");

		) {

			// authenticate

			forwarder =
				forwarderApiLogic.rpcAuth (
					transaction,
					source);

			// get params

			getParams (
				transaction,
				source);

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

			checkTemplate (
				transaction);

			// send the message (if appropriate)

			if (! cancel) {

				sendTemplate (
					transaction);

			}

			// return

			collectErrors ();

			transaction.commit ();

			return makeResult ();

		}

	}

	private
	Collection <MediaRec> getMedias (
			@NonNull Transaction parentTransaction,
			@NonNull List <Map <String, Object>> mpList)
		throws ReportableException {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"SendExRpcHandler.getMedias");

		) {

			Collection <MediaRec> medias =
				new ArrayList<> ();

			try {

				for (
					Map <String, Object> mp
						: mpList
				) {

					String url = (String) mp.get("url");

					if (url == null) {

						throw new ReportableException (
							"Media url is null");

					}

					// process

					String filename =
						stringFormat (
							"%s.jpeg",
							integerToDecimalString (
								url.hashCode ()));

					medias.add (
						mediaLogic.createMediaFromImageRequired (
							transaction,
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
								integerToDecimalString (
									message.hashCode ()));

						medias.add (
							mediaLogic.createTextMedia (
								transaction,
								message,
								"text/plain",
								txtFilename));

					}

				}

			} catch (Exception e) {

				throw new ReportableException (
					e.getMessage ());

			}

			return medias;

		}

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

			try (

				InputStream inputStream =
					yc.getInputStream ();

				BufferedInputStream bufferedInputStream =
					new BufferedInputStream (inputStream);

				ByteArrayOutputStream byteArrayOutputStream =
					new ByteArrayOutputStream ();

			) {

				byte[] byteBuffer =
					new byte [8192];

				int numread;

				while (
					moreThanZero (
						numread =
							bufferedInputStream.read (
								byteBuffer))
				) {

					byteArrayOutputStream.write (
						byteBuffer,
						0,
						numread);

				}

				return byteArrayOutputStream.toByteArray ();

			}

		} catch (Exception e) {

			throw new ReportableException(e.getMessage());

		}

	}

	/**
	 * Extracts input parameters from the RpcSource.
	 */
	private
	void getParams (
			@NonNull Transaction parentTransaction,
			@NonNull RpcSource source) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"getParams");

		) {

			Map <String, Object> params =
				genericCastUnchecked (
					source.obtain (
						sendExRequestDef,
						errors,
						true));

			allowPartial =
				(Boolean)
				params.get (
					"allow-partial");

			List <Map <String, Object>> messageChainPartList =
				genericCastUnchecked (
					params.get (
						"message-chains"));

			if (messageChainPartList != null) {

				for (
					Map <String, Object> messageChainPart
						: messageChainPartList
				) {

					ForwarderSendExMessageChain sendExMessageChain =
						new ForwarderSendExMessageChain ();

					messageChains.add (
						sendExMessageChain);

					sendExMessageChain.replyToServerId =
						genericCastUnchecked (
							params.get (
								"reply-to-server-id"));

					List <Map <String, Object>> mpList =
						genericCastUnchecked (
							messageChainPart.get (
								"unqueueExMessages"));

					if (mpList != null) {

						for (
							Map <String, Object> mp
								: mpList
						) {

							ForwarderSendExMessage sendExMessage =
								new ForwarderSendExMessage ();

							sendExMessageChain.messages.add (
								sendExMessage);

							sendExMessage.type =
								(ForwarderMessageType)
								mp.get ("type");

							sendExMessage.numTo =
								(String)
								mp.get ("num-to");

							sendExMessage.numFrom =
								(String)
								mp.get ("num-from");

							sendExMessage.message =
								(String)
								mp.get ("message");

							char pound = '\u00A3';

							sendExMessage.message =
								sendExMessage.message.replaceAll (
									"&pound;", "" + pound);

							sendExMessage.url =
								(String)
								mp.get ("url");

							sendExMessage.clientId =
								(String)
								mp.get ("client-id");

							sendExMessage.route =
								(String)
								mp.get ("route");

							sendExMessage.service =
								(String)
								mp.get ("service");

							sendExMessage.pri =
								(Long)
								mp.get ("pri");

							sendExMessage.retryDays =
								(Long)
								mp.get ("retry-days");

							Set <String> tagsTemp =
								genericCastUnchecked (
									mp.get ("tags"));

							sendExMessage.tags = tagsTemp;

							if (
								enumEqualSafe (
									sendExMessage.type,
									ForwarderMessageType.mms)
							) {

								try {

									List <Map <String, Object>> mediaList =
										genericCastUnchecked (
											mp.get ("medias"));

									if (mediaList == null) {

										errors.add (
											stringFormat (
												"Must provide media list for ",
												"mms type."));

										break;

									}

									sendExMessage.medias =
										getMedias (
											transaction,
											mediaList);

									sendExMessage.subject =
										sendExMessage.message;

								} catch (ReportableException e) {

									errors.add (
										"Error: " + e.getMessage ());

								}

							}

							switch (sendExMessage.type) {

							case mms:

								if (sendExMessage.url != null) {

									errors.add (
										stringFormat (
											"Parameter should not be set when ",
											"message type is mms: url"));

								}

								break;

							case sms:

								if (sendExMessage.url != null) {

									errors.add (
										stringFormat (
											"Parameter should not be set when ",
											"message type is sms: url"));

								}

								break;

							case wapPush:

								if (sendExMessage.url == null) {

									errors.add (
										stringFormat (
											"Parameter must be set when ",
											"message type is wap push: url"));

								}

								break;

							}

						}

					}

				}

			}

		}

	}

	/**
	 * Creates SendTemplates for each message chain.
	 */
	private
	void createTemplate () {

		for (
			ForwarderSendExMessageChain sendExMessageChain
				: messageChains
		) {

			sendExMessageChain.sendTemplate =
				new ForwarderLogicImplementation.SendTemplate ();

			sendExMessageChain.sendTemplate.forwarder =
				forwarder;

			sendExMessageChain.sendTemplate.fmInId =
				sendExMessageChain.replyToServerId;

			for (
				ForwarderSendExMessage sendExMessage
					: sendExMessageChain.messages
			) {

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
				sendExMessageChain.sendTemplate.parts.add (part);
				sendExMessage.part = part;

			}

		}

	}

	/**
	 * Calls forwarderUtils to check each template, keeping track of
	 * failures and successes etc...
	 */
	private
	void checkTemplate (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"checkTemplate");

		) {

			for (
				int index = 0;
				index < messageChains.size ();
				index ++
			) {

				ForwarderSendExMessageChain sendExMessageChain =
					messageChains.get (
						index);

				// call sendTemplateCheck

				if (
					forwarderLogic.sendTemplateCheck (
						transaction,
						sendExMessageChain.sendTemplate)
				) {

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

			if (someFailed && ! allowPartial) {
				cancel = true;
			}

		}

	}

	private
	void sendTemplate (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"sendTemplate");

		) {

			for (
				ForwarderSendExMessageChain sendExMessasgeChain
					: messageChains
			) {

				if (! sendExMessasgeChain.ok)
					continue;

				forwarderLogic.sendTemplateSend (
					transaction,
					sendExMessasgeChain.sendTemplate);

			}

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

			ForwarderSendExMessageChain sendExMessageChain =
				messageChains.get (index);

			for (
				String error
					: sendExMessageChain.sendTemplate.errors
			) {

				sendExMessageChain.errors.add (
					error);

				errors.add (
					stringFormat (
						"Chain %s: %s",
						integerToDecimalString (
							index),
						error));

			}

			for (int j = 0; j < sendExMessageChain.messages.size(); j++) {

				ForwarderSendExMessage sem =
					sendExMessageChain.messages.get (j);

				for (String error : sem.part.errors) {

					sendExMessageChain.errors.add (
						String.format (
							"Message %d: %s",
							j,
							error));

					errors.add (
						String.format (
							"Chain %d: Message %d: %s",
							index,
							j,
							error));

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
			ForwarderSendExMessageChain sendExMessageChain) {

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

		if (
			sendExMessageChain.sendTemplate.sendError == null
			&& cancel
		) {

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

		} else if (
			sendExMessageChain.sendTemplate.sendError == null
			&& ! cancel
		) {

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
			ForwarderSendExMessage sem) {

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

				Rpc.rpcElem (
					"server-id",
					sem.part.forwarderMessageOut.getId ()),

				Rpc.rpcElem (
					"status",
					Rpc.stSuccess),

				Rpc.rpcElem (
					"status-code",
					"success"),

				Rpc.rpcElem (
					"status-message",
					"Message sent"),

				Rpc.rpcList (
					"status-unqueueExMessages",
					"status-message",
					ImmutableList.of (
						"Message sent")),

				Rpc.rpcElem (
					"success",
					true));

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
