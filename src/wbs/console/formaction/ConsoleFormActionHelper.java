package wbs.console.formaction;

import static wbs.utils.collection.MapUtils.emptyMap;
import static wbs.utils.etc.Misc.doNothing;
import static wbs.utils.etc.TypeUtils.classInstantiate;
import static wbs.utils.etc.TypeUtils.genericCastUnchecked;
import static wbs.utils.etc.TypeUtils.isSubclassOf;

import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.Map;

import com.google.common.base.Optional;

import lombok.NonNull;

import org.apache.commons.lang3.tuple.Pair;

import wbs.framework.database.Transaction;
import wbs.framework.logging.TaskLogger;

import wbs.utils.string.FormatWriter;

import wbs.web.responder.Responder;

public
interface ConsoleFormActionHelper <FormState> {

	default
	Pair <Boolean, Boolean> canBePerformed () {

		return Pair.of (
			true,
			true);

	}

	default
	void writePreamble (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull FormatWriter formatWriter,
			@NonNull Boolean submit) {

		doNothing ();

	}

	default
	Map <String, String> placeholderValues () {

		return emptyMap ();

	}

	default
	FormState constructFormState () {

		Class <?> formStateClass =
			Arrays.stream (
				getClass ().getGenericInterfaces ())

			.map (
				interfaceType ->
					(ParameterizedType)
					interfaceType)

			.filter (
				interfaceType ->
					isSubclassOf (
						ConsoleFormActionHelper.class,
						(Class <?>) interfaceType.getRawType ()))

			.map (
				interfaceParameterizedType ->
					interfaceParameterizedType
						.getActualTypeArguments () [0])

			.map (
				typeArgumentType ->
					(Class <?>) typeArgumentType)

			.findFirst ()
			.get ();

		return genericCastUnchecked (
			classInstantiate (
				formStateClass));

	}

	default
	void updatePassiveFormState (
			@NonNull FormState formState) {

		doNothing ();

	}

	Optional <Responder> processFormSubmission (
			Transaction transaction,
			FormState formState);

}
