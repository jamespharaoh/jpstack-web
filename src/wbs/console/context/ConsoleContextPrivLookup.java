package wbs.console.context;

import static wbs.framework.utils.etc.StringUtils.joinWithoutSeparator;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.lookup.BooleanLookup;
import wbs.framework.component.annotations.PrototypeComponent;

@Accessors (fluent = true)
@PrototypeComponent ("consoleContextPrivLookup")
public
class ConsoleContextPrivLookup
		implements BooleanLookup {

	@Getter @Setter
	List<String> privKeys =
		new ArrayList<String> ();

	public
	ConsoleContextPrivLookup addPrivKey (
			String privKey) {

		if (privKey == null)
			return this;

		privKeys.add (
			privKey);

		return this;

	}

	@Override
	public
	boolean lookup (
			ConsoleContextStuff stuff) {

		for (String privKey : privKeys) {

			if (stuff.can (privKey))
				return true;

		}

		return privKeys.isEmpty ();

	}

	@Override
	public
	String describe () {

		return joinWithoutSeparator (
			privKeys);

	}

}
