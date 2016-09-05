package wbs.framework.utils.formatwriter;

import static wbs.framework.utils.etc.Misc.doNothing;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors (fluent = true)
public
class NullFormatWriter
	implements FormatWriter {

	// properties

	@Getter @Setter
	String indentString = "\t";

	@Getter @Setter
	long indentSize = 0l;

	// implementation

	@Override
	public
	void writeString (
			@NonNull String string) {

		doNothing ();

	}

	@Override
	public
	void writeCharacter (
			int character) {

		doNothing ();

	}

	@Override
	public
	void close () {

		doNothing ();

	}

}
