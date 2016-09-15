package wbs.apn.chat.core.daemon;

import static wbs.utils.string.StringUtils.joinWithPipe;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.regex.Pattern;

public
class ChatPatterns {

	public final static
	Pattern yes =
		Pattern.compile (
			"\\byes\\b",
			Pattern.CASE_INSENSITIVE);

	public final static
	Pattern gay =
		Pattern.compile (
			stringFormat (
				"\\b(%s)\\b",
				joinWithPipe (
					"gay",
					"lesbian",
					"queer",
					"homo",
					"homosexual",
					"bent",
					"bender",
					"fairy",
					"dyke",
					"fag",
					"faggot",
					"poof")),
			Pattern.CASE_INSENSITIVE);

	public final static
	Pattern straight =
		Pattern.compile (
			"\\b(straight|str8|str 8|breeder)\\b",
			Pattern.CASE_INSENSITIVE);

	public final static
	Pattern bi =
		Pattern.compile (
			"\\b(bi|bisexual|bysexual)\\b",
			Pattern.CASE_INSENSITIVE);

	public final static
	Pattern male =
		Pattern.compile (
			"\\b(male|guy|boy|lad|chap|gentleman|man|fairy|fag|faggot|poof)\\b",
			Pattern.CASE_INSENSITIVE);

	public final static
	Pattern female =
		Pattern.compile (
			"\\b(female|girl|lady|lass|woman|lesbian|dyke|lezza|lez|butch)\\b",
			Pattern.CASE_INSENSITIVE);

	public final static
	Pattern both =
		Pattern.compile (
			"\\b(both|either|curious)\\b",
			Pattern.CASE_INSENSITIVE);

}
