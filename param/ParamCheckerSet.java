package wbs.console.param;

import static wbs.utils.etc.OptionalUtils.optionalOrNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableMap;

import lombok.Getter;

import wbs.console.request.ConsoleRequestContext;

public
class ParamCheckerSet {

	@Getter
	Map<String,ParamChecker<?>> paramCheckers;

	public
	ParamCheckerSet (
			Map<String,ParamChecker<?>> paramCheckers) {

		this.paramCheckers =
			ImmutableMap.copyOf (
				paramCheckers);

	}

	public
	Map<String,Object> apply (
			ConsoleRequestContext requestContext) {

		Map<String,Object> ret =
			new HashMap<String,Object> ();

		List<String> errors =
			new ArrayList<String> ();

		for (
			Map.Entry <String, ParamChecker<?>> ent
				: paramCheckers.entrySet ()
		) {

			String key =
				ent.getKey ();

			ParamChecker <?> paramChecker =
				ent.getValue ();

			try {

				Object value =
					paramChecker.get (
						optionalOrNull (
							requestContext.parameter (
								key)));

				if (ret != null)
					ret.put (key, value);

			} catch (ParamFormatException e) {

				errors.add (e.getMessage ());
				ret = null;

			}

		}

		if (ret == null) {

			for (String error : errors)
				requestContext.addError (error);

		}

		return ret;

	}

}