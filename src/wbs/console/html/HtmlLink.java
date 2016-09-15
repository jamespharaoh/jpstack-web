package wbs.console.html;

import static wbs.utils.string.StringUtils.stringFormat;

import lombok.Builder;
import lombok.experimental.Accessors;

import wbs.console.request.ConsoleRequestContext;

@Accessors (fluent = true)
@Builder
public
class HtmlLink {

	public static
	enum Resolution {

		absolute,
		application;

	}

	Resolution resolution;

	String charset;
	String href;
	String hrefLang;
	String type;
	String rel;
	String rev;
	String media;

	public static
	HtmlLink applicationCssStyle (
			String href) {

		return HtmlLink.builder ()

			.resolution (
				Resolution.application)

			.href (
				href)

			.type (
				"text/css")

			.rel (
				"stylesheet")

			.build ();

	}

	public static
	HtmlLink applicationIcon (
			String href) {

		return HtmlLink.builder ()

			.resolution (
				Resolution.application)

			.href (
				href)

			.rel (
				"icon")

			.build ();

	}

	public static
	HtmlLink applicationShortcutIcon (
			String href) {

		return HtmlLink.builder ()

			.resolution (
				Resolution.application)

			.href (
				href)

			.rel (
				"shortcut icon")

			.build ();

	}

	public
	String render (
			ConsoleRequestContext requestContext) {

		StringBuilder stringBuilder =
			new StringBuilder ();

		stringBuilder.append (
			"<link");

		if (charset != null) {

			stringBuilder.append (
				stringFormat (
					" charset=\"%h\"",
					charset));

		}

		if (href != null) {

			stringBuilder.append (
				stringFormat (
					" href=\"%h\"",
					resolveHref (requestContext)));

		}

		if (hrefLang != null) {

			stringBuilder.append (
				stringFormat (
					" hreflang=\"%h\"",
					hrefLang));

		}

		if (type != null) {

			stringBuilder.append (
				stringFormat (
					" type=\"%h\"",
					type));

		}

		if (rel != null) {

			stringBuilder.append (
				stringFormat (
					" rel=\"%h\"",
					rel));

		}

		if (rev != null) {

			stringBuilder.append (
				stringFormat (
					" rev=\"%h\"",
					rev));

		}

		if (media != null) {

			stringBuilder.append (
				stringFormat (
					" media=\"%h\"",
					media));

		}

		stringBuilder.append (
			">");

		return stringBuilder.toString ();

	}

	public
	String resolveHref (
			ConsoleRequestContext requestContext) {

		switch (resolution) {

		case absolute:

			return href;

		case application:

			return requestContext.resolveApplicationUrl (
				href);

		default:

			throw new RuntimeException ();

		}

	}

}
