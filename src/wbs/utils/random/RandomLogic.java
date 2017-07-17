package wbs.utils.random;

import java.util.List;

public
interface RandomLogic {

	int randomJavaInteger (
			int limit);

	long randomInteger (
			long limit);

	boolean randomBoolean (
			long numerator,
			long denominator);

	String generateString (
			String chars,
			int length);

	String generateUppercase (
			int length);

	String generateLowercase (
			int length);

	String generateNumeric (
			int length);

	String generateNumericNoZero (
			int length);

	<Type>
	Type sample (
			Type[] options);

	<Type>
	Type sample (
			List <Type> options);

	<Type>
	List <Type> shuffleToList (
			Iterable <Type> input);

}
