package wbs.framework.utils;

import java.util.List;

public
interface RandomLogic {

	int randomInteger (
			int limit);

	long randomInteger (
			long limit);

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
			List<Type> options);

}
