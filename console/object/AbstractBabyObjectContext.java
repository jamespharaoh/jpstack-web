package wbs.platform.console.object;

import java.util.Map;

import wbs.platform.console.context.ConsoleContext;
import wbs.platform.console.context.ConsoleContextStuff;

public abstract
class AbstractBabyObjectContext
	extends AbstractObjectContext {

	@Override
	public abstract
	String title ();

	@Override
	public
	String localPathForStuff (
			ConsoleContextStuff stuff) {

		return parentContext ().localPathForStuff (stuff);

	}

	@Override
	public
	String titleForStuff (
			ConsoleContextStuff stuff) {

		return title ();

	}

	@Override
	public
	void initContext (
			PathSupply pathParts,
			ConsoleContextStuff contextStuff) {

		ConsoleContext parentContext =
			parentContext ();

		parentContext.initContext (
			pathParts,
			contextStuff);

		postInitHook (
			contextStuff);

		if (stuff () != null) {

			for (Map.Entry<String,? extends Object> ent
					: stuff ().entrySet ()) {

				contextStuff.set (
					ent.getKey (),
					ent.getValue ());

			}

		}

	}

	protected
	void postInitHook (
			ConsoleContextStuff contextStuff) {

	}

}
