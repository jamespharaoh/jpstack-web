package wbs.web.exceptions;

import static wbs.utils.etc.OptionalUtils.optionalOr;

import java.util.List;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.web.misc.HttpStatus;

public
class HttpBadRequestException
	extends HttpClientErrorException {

	public
	HttpBadRequestException (
			@NonNull Optional <String> statusMessage,
			@NonNull List <String> errors) {

		super (
			HttpStatus.httpBadRequest,
			optionalOr (
				statusMessage,
				"Bad request"),
			errors);

	}

}
