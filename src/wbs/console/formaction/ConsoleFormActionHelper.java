package wbs.console.formaction;

import static wbs.utils.collection.CollectionUtils.emptyList;
import static wbs.utils.collection.MapUtils.emptyMap;
import static wbs.utils.etc.Misc.doNothing;
import static wbs.utils.etc.TypeUtils.classInstantiate;
import static wbs.utils.etc.TypeUtils.genericCastUnchecked;
import static wbs.utils.etc.TypeUtils.isSubclassOf;

import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.google.common.base.Optional;

import lombok.Data;
import lombok.NonNull;
import lombok.experimental.Accessors;

import wbs.framework.database.Transaction;
import wbs.framework.logging.TaskLogger;

import wbs.utils.string.FormatWriter;

import wbs.web.responder.Responder;

public
interface ConsoleFormActionHelper <FormState, History> {

	default
	Permissions canBePerformed (
			@NonNull TaskLogger parentTaskLogger) {

		return new Permissions ()
			.canView (true)
			.canPerform (true);

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
	Map <String, Object> formHints () {

		return emptyMap ();

	}

	default
	void updatePassiveFormState (
			@NonNull FormState formState) {

		doNothing ();

	}

	Optional <Responder> processFormSubmission (
			TaskLogger parentTaskLogger,
			Transaction transaction,
			FormState formState);

	default
	List <History> history () {

		return emptyList ();

	}

	@Accessors (fluent = true)
	@Data
	public static
	class Permissions {

		Boolean canView;
		Boolean canPerform;

	}

}
