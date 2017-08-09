package wbs.console.html;

import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.NumberUtils.moreThanOne;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.etc.OptionalUtils.presentInstances;
import static wbs.utils.string.StringUtils.joinWithSemicolonAndSpace;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.framework.component.annotations.PrototypeComponent;

import wbs.utils.string.FormatWriter;

@PrototypeComponent ("htmlTableCellWriter")
public
class HtmlTableCellWriter {

	// properties

	Optional <String> href =
		optionalAbsent ();

	Optional <String> target =
		optionalAbsent ();

	Optional <Long> columnSpan =
		optionalAbsent ();

	Optional <String> style =
		optionalAbsent ();

	List <String> extra =
		new ArrayList<> ();

	// accessors

	public
	HtmlTableCellWriter href (
			@NonNull String href) {

		if (
			optionalIsPresent (
				this.href)
		) {
			throw new IllegalStateException ();
		}

		this.href =
			optionalOf (
				href);

		return this;

	}

	public
	HtmlTableCellWriter target (
			@NonNull String target) {

		if (
			optionalIsPresent (
				this.target)
		) {
			throw new IllegalStateException ();
		}

		this.target =
			optionalOf (
				target);

		return this;

	}

	public
	HtmlTableCellWriter columnSpan (
			@NonNull Long columnSpan) {

		if (
			optionalIsPresent (
				this.columnSpan)
		) {
			throw new IllegalStateException ();
		}

		this.columnSpan =
			optionalOf (
				columnSpan);

		return this;

	}

	public
	HtmlTableCellWriter style (
			@NonNull String style) {

		if (
			optionalIsPresent (
				this.style)
		) {
			throw new IllegalStateException ();
		}

		this.style =
			optionalOf (
				style);

		return this;

	}

	// implementation

	public
	void write (
			@NonNull FormatWriter formatWriter) {

		formatWriter.writeIndent ();

		formatWriter.writeFormat (
			"<td");

		if (

			optionalIsPresent (
				columnSpan)

			&& moreThanOne (
				columnSpan.get ())

		) {

			formatWriter.writeFormat (
				" colspan=\"%h\"",
				integerToDecimalString (
					columnSpan.get ()));

		}

		formatWriter.writeFormat (
			" style=\"%h\"",
			joinWithSemicolonAndSpace (
				presentInstances (
					optionalOf (
						"cursor: pointer"),
					style)));

		formatWriter.writeFormat (
			" onmouseover=\"this.className='hover'\"");

		formatWriter.writeFormat (
			" onmouseout=\"this.className=null\"");

		if (
			optionalIsPresent (
				href)
		) {

			if (
				optionalIsPresent (
					target)
			) {

				formatWriter.writeFormat (
					" onclick=\"%h\"",
					stringFormat (
						"top.frames ['%j'].location = '%j'",
						target.get (),
						href.get ()));

			} else {

				formatWriter.writeFormat (
					" onclick=\"%h\"",
					stringFormat (
						"window.location = '%j'",
						href.get ()));

			}

		}

		for (
			String extraItem
				: extra
		) {

			formatWriter.writeString (
				extraItem);

		}

		formatWriter.writeFormat (
			">");

		formatWriter.writeNewline ();

		formatWriter.increaseIndent ();

	}

}
