package wbs.web.exceptions;

import static wbs.utils.collection.CollectionUtils.emptyList;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.etc.OptionalUtils.optionalOr;

import java.util.List;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.web.misc.HttpStatus;

public
class HttpForbiddenException
	extends HttpClientException {

	public
	HttpForbiddenException (
			@NonNull Optional <String> statusMessage,
			@NonNull List <String> errors) {

		super (
			HttpStatus.httpForbidden,
			optionalOr (
				statusMessage,
				"Forbidden"),
			errors);

	}

	public
	HttpForbiddenException (
			@NonNull String statusMessage) {

		this (
			optionalOf (
				statusMessage),
			emptyList ());

	}

}
