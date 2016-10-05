package wbs.framework.apiclient;

import java.util.Map;

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
	String requestBody ();

	// work methods

	void verify ();
	void encode ();
	void decode ();

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
