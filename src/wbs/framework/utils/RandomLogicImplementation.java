package wbs.framework.utils;

import static wbs.framework.utils.etc.Misc.joinWithoutSeparator;

import java.util.List;
import java.util.Random;

import javax.annotation.PostConstruct;

import wbs.framework.application.annotations.SingletonComponent;

@SingletonComponent ("randomLogic")
public
class RandomLogicImplementation
	implements RandomLogic {

	// state

	Random random;

	// life cycle

	@PostConstruct
	public
	void setup () {

		random =
			new Random ();

	}

	// implementation

	@Override
	public
	int randomInteger (
			int limit) {

		return random.nextInt (
			limit);

	}

	@Override
	public
	String generateString (
			String chars,
			int length) {

		StringBuilder stringBuilder =
			new StringBuilder ();

		for (int i = 0; i < length; i ++) {

			stringBuilder.append (
				chars.charAt (
					randomInteger (
						chars.length ())));

		}

		return stringBuilder.toString ();

	}

	@Override
	public
	String generateLowercase (
			int length) {

		return generateString (
			"abcdefghijklmnopqrstuvwxyz",
			length);

	}

	@Override
	public
	String generateUppercase (
			int length) {

		return generateString (
			"ABCDEFGHIJKLMNOPQRSTUVWXYZ",
			length);

	}

	@Override
	public
	String generateNumeric (
			int length) {

		return generateString (
			"0123456789",
			length);

	}

	@Override
	public
	String generateNumericNoZero (
			int length) {

		return joinWithoutSeparator (
			generateString ("123456789", 1),
			generateNumeric (length - 1));

	}

	@Override
	public <Type>
	Type sample (
			Type[] options) {

		int index =
			randomInteger (
				options.length);

		return options [index];

	}

	@Override
	public <Type>
	Type sample (
			List<Type> options) {

		if (options.isEmpty ()) {

			throw new IllegalArgumentException (
				"Options cannot be empty");

		}

		int index =
			randomInteger (
				options.size ());

		return options.get (
			index);

	}

}
