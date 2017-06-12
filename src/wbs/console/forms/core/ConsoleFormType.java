package wbs.console.forms.core;

import static wbs.utils.etc.TypeUtils.classNotEqual;
import static wbs.utils.etc.TypeUtils.genericCastUnchecked;

import java.util.List;
import java.util.Map;

import lombok.NonNull;

import wbs.framework.database.Transaction;

public
interface ConsoleFormType <Container> {

	// ---------- accessors

	Class <Container> containerClass ();

	String formName ();

	// ---------- build action

	ConsoleForm <Container> buildAction (
			Transaction parentTransaction,
			Map <String, Object> hints,
			Container value);

	ConsoleForm <Container> buildAction (
			Transaction parentTransaction,
			Map <String, Object> hints,
			List <Container> values);

	ConsoleForm <Container> buildActionWithParent (
			Transaction parentTransaction,
			Map <String, Object> hints,
			Object parent,
			Container value);

	ConsoleForm <Container> buildActionWithParent (
			Transaction parentTransaction,
			Map <String, Object> hints,
			Object parent,
			List <Container> values);

	// ---------- build response

	ConsoleForm <Container> buildResponse (
			Transaction parentTransaction,
			Map <String, Object> hints,
			Container value);

	ConsoleForm <Container> buildResponse (
			Transaction parentTransaction,
			Map <String, Object> hints,
			List <Container> values);

	ConsoleForm <Container> buildResponseWithParent (
			Transaction parentTransaction,
			Map <String, Object> hints,
			Object parent,
			Container value);

	ConsoleForm <Container> buildResponseWithParent (
			Transaction parentTransaction,
			Map <String, Object> hints,
			Object parent,
			List <Container> values);

	// ---------- misc

	default <NewContainer>
	ConsoleFormType <NewContainer> cast (
			@NonNull Class <NewContainer> newContainerClass) {

		if (
			classNotEqual (
				containerClass (),
				newContainerClass)
		) {
			throw new ClassCastException ();
		}

		return genericCastUnchecked (
			this);

	}

}
