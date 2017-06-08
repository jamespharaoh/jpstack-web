package wbs.console.forms.core;

import java.util.Map;

import wbs.framework.database.Transaction;

public
interface ConsoleMultiFormType <Container> {

	Class <Container> containerClass ();

	ConsoleMultiForm <Container> buildResponse (
			Transaction parentTransaction,
			Map <String, Object> hints,
			Container value);

	ConsoleMultiForm <Container> buildAction (
			Transaction parentTransaction,
			Map <String, Object> hints,
			Container value);

}
