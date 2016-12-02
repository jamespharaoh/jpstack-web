package wbs.framework.apiclient;

import static wbs.utils.etc.NumberUtils.integerRangeAsSet;

import java.util.List;
import java.util.Map;
import java.util.Set;

public
interface GenericHttpSenderHelper <
	RequestType,
	ResponseType
> {

	// request data setters

	GenericHttpSenderHelper <RequestType, ResponseType> request (
			RequestType request);

	// request information getters

	String url ();
	Map <String, String> requestHeaders ();
	Map <String, List <String>> requestParameters ();
	String requestBody ();

	// work methods

	void verify ();
	void encode ();
	void decode ();

	// options

	public final static
	Set <Long> validStatusCodesDefault =
		integerRangeAsSet (
			200l,
			300l);

	default
	Set <Long> validStatusCodes () {

		return validStatusCodesDefault;

	}

	// response information setters

	GenericHttpSenderHelper <RequestType, ResponseType> responseStatusCode (
			Long responseStatusCode);

	GenericHttpSenderHelper <RequestType, ResponseType> responseStatusReason (
			String responseStatusReason);

	GenericHttpSenderHelper <RequestType, ResponseType> responseHeaders (
			Map <String, String> responseHeaders);

	GenericHttpSenderHelper <RequestType, ResponseType> responseBody (
			String responseBody);

	// response data getter

	ResponseType response ();

}
