package wbs.web.exceptions;

import java.util.List;

import lombok.NonNull;

public
class HttpClientException
	extends HttpStatusException {

	public
	HttpClientException (
			@NonNull Long statusCode,
			@NonNull String statusMessage,
			@NonNull List <String> errors) {

		super (
			statusCode,
			statusMessage,
			errors);

	}

}
