package wbs.platform.console.html;

import static wbs.framework.utils.etc.Misc.stringFormat;
import lombok.EqualsAndHashCode;

/**
 * Immutable class to represent <link ...> elements in an html document.
 */
@EqualsAndHashCode
public
class HtmlLink {

	String charset;
	String href;
	String hrefLang;
	String type;
	String rel;
	String rev;
	String media;

	/**
	 * Sole public constructor.
	 */
	public
	HtmlLink (
			String newCharset,
			String newHref,
			String newHrefLang,
			String newType,
			String newRel,
			String newRev,
			String newMedia) {

		charset = newCharset;
		href = newHref;
		hrefLang = newHrefLang;
		type = newType;
		rel = newRel;
		rev = newRev;
		media = newMedia;
	}

	/**
	 * Static factory method to construct css stylesheet references.
	 */
	public static
	HtmlLink cssStyle (
			String href) {

		return new HtmlLink (
			null,
			href,
			null,
			"text/css",
			"stylesheet",
			null,
			null);

	}

	public static
	HtmlLink icon (
			String href) {

		return new HtmlLink (
			null,
			href,
			null,
			null,
			"icon",
			null,
			null);

	}

	public static
	HtmlLink shortcutIcon (
			String href) {

		return new HtmlLink (
			null,
			href,
			null,
			null,
			"shortcut icon",
			null,
			null);

	}

	/**
	 * Returns the html string representation of the appropriate link element.
	 */
	@Override
	public
	String toString () {

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
					href));

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

}
