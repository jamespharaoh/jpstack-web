package wbs.framework.utils.etc;

import static wbs.framework.utils.etc.LogicUtils.ifThenElse;
import static wbs.framework.utils.etc.StringUtils.bytesToString;
import static wbs.framework.utils.etc.StringUtils.joinWithSpace;
import static wbs.framework.utils.etc.StringUtils.stringEqual;
import static wbs.framework.utils.etc.StringUtils.stringFormat;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import fj.data.Either;
import lombok.Cleanup;
import lombok.NonNull;
import lombok.SneakyThrows;

// TODO lots to deprecate here
public
class Misc {

	private final static
	Pattern intPattern =
		Pattern.compile ("[0-9]+");

	@Deprecated
	public static
	boolean isInt (
			String string) {

		if (string == null)
			return false;

		return intPattern
			.matcher (string)
			.matches ();

	}

	public static <Type extends Enum<Type>>
	Type toEnum (
			Class<Type> enumType,
			String name) {

		if (name == null || name.length () == 0)
			return null;

		if (
			stringEqual (
				name,
				"null")
		) {
			return null;
		}

		return Enum.valueOf (enumType, name);

	}

	public static
	Enum<?> toEnumGeneric (
			@NonNull Class<?> enumType,
			@NonNull String name) {

		return (Enum<?>)
			Arrays.stream (enumType.getEnumConstants ())
			.filter (value -> ((Enum<?>) value).name ().equals (name))
			.findFirst ()
			.orElseThrow (() -> new IllegalArgumentException ());

	}

	public static
	String urlEncode (
			String string) {

		try {

			return URLEncoder.encode (
				string,
				"utf8");

		} catch (UnsupportedEncodingException exception) {

			throw new RuntimeException (
				exception);

		}

	}

	public static
	RuntimeException rethrow (
			Throwable throwable) {

		if (throwable instanceof Error) {
			throw (Error) throwable;
		}

		if (throwable instanceof RuntimeException) {
			throw (RuntimeException) throwable;
		}

		throw new RuntimeException (
			throwable);

	}

	public static
	int min (
			int... params) {

		int ret =
			params [0];

		for (int param : params) {

			if (param < ret)
				ret = param;

		}

		return ret;

	}

	public static
	long min (
			long... params) {

		long ret =
			params [0];

		for (
			long param
				: params
		) {

			if (param < ret)
				ret = param;

		}

		return ret;

	}

	public static
	long max (
			long... params) {

		long ret =
			params [0];

		for (
			long param
				: params
		) {

			if (param > ret)
				ret = param;

		}

		return ret;

	}

	public static
	String prettyHour (
			long hour) {

		if (hour < 0 || hour > 23)
			throw new IllegalArgumentException ();

		if (hour == 0)
			return "12am";

		if (hour < 12)
			return "" + hour + "am";

		if (hour == 12)
			return "12pm";

		return "" + (hour - 12) + "pm";

	}

	public static <T>
	Iterable<T> iterable (
			final Iterator<T> iterator) {

		return new Iterable<T> () {

			@Override
			public
			Iterator<T> iterator () {
				return iterator;
			}

		};

	}

	static
	public class TempFile
		implements Closeable {

		private final
		File file;

		private
		TempFile (
				@NonNull String prefix,
				@NonNull String extension) {

			try {

				this.file =
					File.createTempFile (
						"wbs-temp-",
						extension);

			} catch (IOException ioException) {

				throw new RuntimeIoException (
					ioException);

			}

		}

		public
		File file () {
			return file;
		}

		public
		String path () {
			return file.getPath ();
		}

		@Override
		public
		void close () {

			file.delete ();
			
		}

	}

	public static
	TempFile createTempFile (
			@NonNull String extension) {

		return new TempFile (
			"wbs-temp-",
			extension);

	}

	public static
	TempFile createTempFileWithData (
			String extension,
			byte[] data) {

		TempFile tempFile =
			new TempFile (
				"wbs-temp-",
				extension);

		boolean success = false;

		try {

			@Cleanup
			OutputStream outputStream =
				new FileOutputStream (
					tempFile.file ());
	
			success = true;
	
			return tempFile;

		} catch (IOException ioException) {

			throw new RuntimeIoException (
				ioException);

		} finally {

			if (! success) {
				tempFile.close ();
			}

		}

	}

	public static
	int runCommand (
			Logger logger,
			List <String> command)
		throws InterruptedException {

		logger.info (
			stringFormat (
				"Executing %s",
				joinWithSpace (
					command)));

		try {

			Process process =
				Runtime.getRuntime ().exec (
					command.toArray (
						new String [] {}));

			// debug log any error output
	
			@Cleanup
			BufferedReader bufferedReader =
				new BufferedReader (
					new InputStreamReader (
						process.getErrorStream (),
						"utf-8"));
	
			String line;
	
			while ((line = bufferedReader.readLine ()) != null) {
	
				logger.debug (
					line);
	
			}
	
			bufferedReader.close ();

			try {
	
				return process.waitFor ();
	
			} catch (InterruptedException exception) {
	
				process.destroy ();
	
				throw exception;
	
			}

		} catch (IOException ioException) {

			throw new RuntimeIoException (
				ioException);

		}

	}

	public static
	byte[] runFilter (
			Logger logger,
			byte[] data,
			String inExt,
			String outExt,
			String ... command)
		throws InterruptedException {

		return runFilterAdvanced (
			logger,
			data,
			inExt,
			outExt,
			ImmutableList.of (
				ImmutableList.copyOf (
					command)));

	}

	public static
	byte[] runFilterAdvanced (
			Logger logger,
			byte[] data,
			String inExtension,
			String outExtension,
			List <List <String>> commands)
		throws InterruptedException {

		// create the input and output files

		@Cleanup
		TempFile inFile =
			createTempFileWithData (
				inExtension,
				data);

		@Cleanup
		TempFile outFile =
			createTempFile (
				outExtension);

		// stick the filenames into the command

		for (
			List <String> command
				: commands
		) {

			List <String> newCommand =
				command.stream ()

				.map (
					commandItem ->
						ifThenElse (
							stringEqual (
								commandItem,
								"<in>"),
							() -> inFile.path (),
							() -> commandItem))

				.map (
					commandItem ->
						ifThenElse (
							stringEqual (
								commandItem,
								"<out>"),
							() -> outFile.path (),
							() -> commandItem))

				.collect (
					Collectors.toList ());

			// run the command

			int status =
				runCommand (
					logger,
					newCommand);

			if (status != 0)
				throw new RuntimeException ("Command returned " + status);

		}

		// read the output file

		try {

			return FileUtils.readFileToByteArray (
				outFile.file ());

		} catch (IOException ioException) {

			throw new RuntimeIoException (
				ioException);

		}

	}

	public static
	byte[] fromHex (
			@NonNull String hex) {

		byte[] bytes =
			new byte [
				hex.length () / 2];

		for (
			int index = 0;
			index < bytes.length;
			index ++
		) {

			bytes [index] = (byte)
				Integer.parseInt (
					hex.substring (
						2 * index,
						2 * index + 2),
					16);

		}

		return bytes;

	}

	static final
	byte[] HEX_CHAR_TABLE = {
		(byte) '0', (byte) '1', (byte) '2', (byte) '3',
		(byte) '4', (byte) '5', (byte) '6', (byte) '7',
		(byte) '8', (byte) '9', (byte) 'a', (byte) 'b',
		(byte) 'c', (byte) 'd', (byte) 'e', (byte) 'f'
	};

	public static
	String toHex (
			@NonNull byte[] byteValues) {

		byte[] hex =
			new byte [2 * byteValues.length];

		int index = 0;

		for (byte byteValue : byteValues) {

			int intValue =
				byteValue & 0xFF;

			hex [index ++] =
				HEX_CHAR_TABLE [intValue >>> 4];

			hex [index ++] =
				HEX_CHAR_TABLE [intValue & 0xf];

		}

		return bytesToString (
			hex,
			"ASCII");

	}

	private static final
	char[] lowercaseLetters =
		new char [26];

	private static final
	char[] digits =
		new char [10];

	static {

		for (
			int index = 0;
			index < 26;
			index ++
		) {

			lowercaseLetters [index] =
				(char) ('a' + index);

		}

		for (
			int index = 0;
			index < 10;
			index ++
		) {

			digits [index] =
				(char) ('0' + index);

		}

	}

	public static
	String prettySize (
			int bytes) {

		int limit = 2;

		if (bytes < limit * 1024)
			return "" + bytes + " bytes";

		if (bytes < limit * 1024 * 1024)
			return "" + (bytes / 1024) + " kilobytes";

		if (bytes < limit * 1024 * 1024 * 1024)
			return "" + (bytes / 1024 / 1024) + " megabytes";

		return "" + (bytes / 1024 / 1024 / 1024) + " gigabytes";

	}

	public static
	RuntimeException todo () {
		return new RuntimeException ("TODO");
	}

	@SneakyThrows (NoSuchAlgorithmException.class)
	public static
	String hashSha1 (
			String string) {

		MessageDigest messageDigest =
			MessageDigest.getInstance ("SHA-1");

		messageDigest.update (
			string.getBytes ());

		return Base64.encodeBase64String (
			messageDigest.digest ());

	}

	public static
	String booleanToString (
			Boolean value,
			String yesString,
			String noString,
			String nullString) {

		if (value == null)
			return nullString;

		if (value == true)
			return yesString;

		if (value == false)
			return noString;

		throw new RuntimeException ();

	}

	public static
	String booleanToYesNo (
			@NonNull Boolean value) {

		return value
			? "yes"
			: "no";

	}

	@Deprecated
	public static
	Boolean stringToBoolean (
			@NonNull String string,
			@NonNull String yesString,
			@NonNull String noString,
			@NonNull String nullString) {

		if (
			stringEqual (
				string,
				yesString)
		) {
			return true;
		}

		if (
			stringEqual (
				string,
				noString)
		) {
			return false;
		}

		if (
			stringEqual (
				string,
				nullString)
		) {
			return null;
		}

		throw new RuntimeException (
			stringFormat (
				"Invalid boolean string: \"%s\"",
				string));

	}

	public static
	void doNothing () {

	}

	public static <Type>
	List <Type> maybeList (
			List<Type> value) {

		return value != null
			? value
			: Collections.<Type>emptyList ();

	}

	public static <Type>
	List <Type> maybeList (
			boolean condition,
			Type value) {

		return condition

			? Collections.<Type>singletonList (
				value)

			: Collections.<Type>emptyList ();

	}

	public static
	boolean isNull (
			Object object) {

		return object == null;

	}

	public static
	boolean isNotNull (
			Object object) {

		return object != null;

	}

	public static
	boolean lessThan (
			int left,
			int right) {

		return left < right;

	}

	public static
	boolean lessThan (
			long left,
			long right) {

		return left < right;

	}

	public static
	int sum (
			int value0,
			int value1) {

		return value0 + value1;

	}

	public static
	long sum (
			long value0,
			long value1) {

		return value0 + value1;

	}

	public static
	boolean isEmpty (
			@NonNull Collection<?> collection) {

		return collection.isEmpty ();

	}

	public static
	boolean isEmpty (
			@NonNull Map<?,?> map) {

		return map.isEmpty ();

	}

	public static
	boolean isNotEmpty (
			Collection<?> collection) {

		return ! collection.isEmpty ();

	}

	public static
	boolean isNotEmpty (
			Map<?,?> collection) {

		return ! collection.isEmpty ();

	}

	public static
	URL stringToUrl (
			String urlString) {

		try {

			return new URL (
				urlString);

		} catch (MalformedURLException exception) {

			throw new RuntimeException (
				exception);

		}

	}

	public static
	boolean doesNotImplement (
			@NonNull Class<?> subjectClass,
			@NonNull Class<?> implementedClass) {

		return ! implementedClass.isAssignableFrom (
			subjectClass);

	}

	public static <Type>
	boolean contains (
			@NonNull Collection<Type> collection,
			@NonNull Type value) {

		return collection.contains (
			value);

	}

	public static <Type>
	boolean contains (
			@NonNull Map<Type,?> map,
			@NonNull Type value) {

		return map.keySet ().contains (
			value);

	}

	public static <Type>
	boolean doesNotContain (
			@NonNull Collection<Type> collection,
			@NonNull Type value) {

		return ! collection.contains (
			value);

	}

	public static <Type>
	Optional<Integer> indexOf (
			@NonNull List<Type> list,
			@NonNull Type value) {

		int index =
			list.indexOf (
				value);

		if (index < 0) {

			return Optional.<Integer>absent ();

		} else {

			return Optional.of (
				index);

		}

	}

	public static <Type>
	Type orNull (
			@NonNull Optional<Type> optional) {

		return optional.orNull ();

	}

	public static
	String fullClassName (
			@NonNull Class<?> theClass) {

		return theClass.getName ();

	}

	public static
	Optional<String> fullClassName (
			@NonNull Optional<Class<?>> theClass) {

		return theClass.isPresent ()
			? Optional.<String>of (
				theClass.get ().getName ())
			: Optional.<String>absent ();

	}

	public static <Type>
	Type requiredValue (
			@NonNull Type value) {

		return value;

	}

	public static
	String stringTrim (
			@NonNull String source) {

		return source.trim ();

	}

	public static
	Method getMethodRequired (
			@NonNull Class<?> objectClass,
			@NonNull String name,
			@NonNull List<Class<?>> parameterTypes) {

		try {

			return objectClass.getMethod (
				name,
				parameterTypes.toArray (
					new Class<?> [0]));

		} catch (NoSuchMethodException exception) {

			throw new RuntimeException (
				exception);

		}

	}

	public static
	Method getDeclaredMethodRequired (
			@NonNull Class<?> objectClass,
			@NonNull String name,
			@NonNull List<Class<?>> parameterTypes) {

		try {

			return objectClass.getDeclaredMethod (
				name,
				parameterTypes.toArray (
					new Class<?> [0]));

		} catch (NoSuchMethodException exception) {

			throw new RuntimeException (
				exception);

		}

	}

	public static
	Object methodInvoke (
			@NonNull Method method,
			@NonNull Object target,
			@NonNull List<Object> arguments) {

		try {

			return method.invoke (
				target,
				arguments.toArray ());

		} catch (InvocationTargetException exception) {

			Throwable targetException =
				exception.getTargetException ();

			if (targetException instanceof RuntimeException) {

				throw (RuntimeException)
					targetException;

			} else {

				throw new RuntimeException (
					targetException);

			}

		} catch (IllegalAccessException exception) {

			throw new RuntimeException (
				exception);

		}

	}

	public static <Type>
	Type eitherGetLeft (
			@NonNull Either<Type,?> either) {

		return either.left ().value ();

	}

	public static <Type>
	Type getValue (
			@NonNull Either<Type,?> either) {

		return either.left ().value ();

	}

	public static <Type>
	Type eitherGetRight (
			@NonNull Either<?,Type> either) {

		return either.right ().value ();

	}

	public static <Type>
	Type getError (
			@NonNull Either<?,Type> either) {

		return either.right ().value ();

	}

	public static
	boolean isLeft (
			@NonNull Either<?,?> either) {

		return either.isLeft ();

	}

	public static
	boolean isRight (
			@NonNull Either<?,?> either) {

		return either.isRight ();

	}

	public static <LeftType,RightType>
	Either<LeftType,RightType> successResult (
			@NonNull LeftType left) {

		return Either.<LeftType,RightType>left (
			left);

	}

	public static <LeftType,RightType>
	Either<LeftType,RightType> errorResult (
			@NonNull RightType right) {

		return Either.<LeftType,RightType>right (
			right);

	}

	public static
	boolean isError (
			@NonNull Either<?,?> either) {

		return either.isRight ();

	}

	public static
	boolean isSuccess (
			@NonNull Either<?,?> either) {

		return either.isLeft ();

	}

	public static <Left,Right>
	Left requiredSuccess (
			@NonNull Either<Left,Right> either) {

		return either.left ().value ();

	}

	public static <Left,Right>
	Right requiredError (
			@NonNull Either<Left,Right> either) {

		return either.right ().value ();

	}

	public static
	boolean isNotStatic (
			@NonNull Method method) {

		return ! Modifier.isStatic (
			method.getModifiers ());

	}

	public static
	Method getStaticMethodRequired (
			@NonNull Class<?> containingClass,
			@NonNull String methodName,
			@NonNull List<Class<?>> parameterTypes) {

		Method method =
			getDeclaredMethodRequired (
				containingClass,
				methodName,
				parameterTypes);

		if (
			isNotStatic (
				method)
		) {
			throw new RuntimeException ();
		}

		return method;

	}

	public static <Key,Value>
	Map.Entry<Key,Value> mapEntry (
			@NonNull Key key,
			@NonNull Value value) {

		return new AbstractMap.SimpleEntry<Key,Value> (
			key,
			value);

	}

	public static
	RuntimeException shouldNeverHappen () {

		throw new RuntimeException (
			"Should never happen");

	}

}
