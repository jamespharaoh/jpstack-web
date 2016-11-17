package wbs.web.exceptions;

import java.util.List;

import lombok.NonNull;

public
class HttpClientErrorException
	extends HttpStatusException {

	public
	HttpClientErrorException (
			@NonNull Long statusCode,
			@NonNull String statusMessage,
			@NonNull List <String> errors) {

		super (
			statusCode,
			statusMessage,
			errors);

	}

}
