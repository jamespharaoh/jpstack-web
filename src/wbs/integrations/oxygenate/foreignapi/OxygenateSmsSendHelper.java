package wbs.integrations.oxygenate.foreignapi;

import static wbs.utils.collection.CollectionUtils.collectionDoesNotHaveThreeElements;
import static wbs.utils.collection.CollectionUtils.listFirstElementRequired;
import static wbs.utils.collection.CollectionUtils.listFromNullable;
import static wbs.utils.collection.CollectionUtils.listSecondElementRequired;
import static wbs.utils.collection.CollectionUtils.listThirdElementRequired;
import static wbs.utils.collection.CollectionUtils.singletonList;
import static wbs.utils.etc.Misc.doNothing;
import static wbs.utils.string.StringUtils.joinWithSemicolonAndSpace;
import static wbs.utils.string.StringUtils.stringSplitNewline;
import static wbs.web.utils.UrlUtils.urlEncodeParameters;

import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableMap;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.framework.apiclient.GenericHttpSenderHelper;
import wbs.framework.component.annotations.PrototypeComponent;

@Accessors (fluent = true)
@PrototypeComponent ("oxygenateSmsSendHelper")
public
class OxygenateSmsSendHelper
	implements GenericHttpSenderHelper <
		OxygenateSmsSendRequest,
		OxygenateSmsSendResponse
	> {

	// request properties

	@Getter @Setter
	Map <String, String> requestHeaders;

	@Getter @Setter
	Map <String, List <String>> requestParameters;

	@Getter @Setter
	String requestBody;

	@Getter @Setter
	OxygenateSmsSendRequest request;

	// response properties

	@Getter @Setter
	Long responseStatusCode;

	@Getter @Setter
	String responseStatusReason;

	@Getter @Setter
	Map <String, String> responseHeaders;

	@Getter @Setter
	String responseBody;

	@Getter @Setter
	OxygenateSmsSendResponse response;

	// property accessors

	@Override
	public
	String url () {
		return request.relayUrl ();
	}

	// public implementation

	@Override
	public
	void verify () {

		doNothing ();
		
	}

	@Override
	public
	void encode () {

		// construct request headers

		requestHeaders =
			ImmutableMap.<String, String> builder ()

			.put (
				"Content-Type",
				joinWithSemicolonAndSpace (
					"application/x-www-form-urlencoded",
					"charset=UTF-8"))

			.build ();

		// construct request parameters

		requestParameters =
			ImmutableMap.<String, List <String>> builder ()

			.put (
				"Reference",
				singletonList (
					request.reference ()))

			.put (
				"CampaignID",
				listFromNullable (
					request.campaignId ()))

			.put (
				"Username",
				singletonList (
					request.username ()))

			.put (
				"Password",
				singletonList (
					request.password ()))

			.put (
				"Multipart",
				singletonList (
					request.multipart ()))

			.put (
				"Shortcode",
				listFromNullable (
					request.shortcode ()))

			.put (
				"Mask",
				listFromNullable (
					request.mask ()))

			.put (
				"Channel",
				listFromNullable (
					request.channel ()))

			.put (
				"MSISDN",
				singletonList (
					request.msisdn ()))

			.put (
				"Content",
				singletonList (
					request.content ()))

			.put (
				"Premium",
				singletonList (
					request.premium ()))

			.build ();

		// construct request body

		requestBody (
			urlEncodeParameters (
				requestParameters));

	}

	@Override
	public
	void decode () {

		List <String> responseLines =
			stringSplitNewline (
				responseBody ());

		if (
			collectionDoesNotHaveThreeElements (
				responseLines)
		) {

			throw new RuntimeException (
				"Invalid response");

		}

		response (
			new OxygenateSmsSendResponse ()

			.statusCode (
				listFirstElementRequired (
					responseLines))

			.statusMessage (
				listSecondElementRequired (
					responseLines))

			.messageReferences (
				listThirdElementRequired (
					responseLines))

		);
		
	}

}
