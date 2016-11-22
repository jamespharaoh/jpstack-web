package wbs.web.exceptions;

import java.util.List;

import lombok.NonNull;

import wbs.web.misc.HttpStatus;

public
class HttpUnprocessableEntityException
	extends HttpClientErrorException {

	public
	HttpUnprocessableEntityException (
			@NonNull String statusMessage,
			@NonNull List <String> errors) {

		super (
			HttpStatus.httpUnprocessableEntity,
			statusMessage,
			errors);

	}

}
