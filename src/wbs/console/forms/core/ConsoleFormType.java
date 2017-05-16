package wbs.console.forms.core;

import static wbs.utils.collection.CollectionUtils.emptyList;
import static wbs.utils.etc.OptionalUtils.optionalOrElseRequired;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.framework.database.Transaction;

public
interface ConsoleFormType <Container> {

	// ---------- build action

	ConsoleForm <Container> buildAction (
			Transaction parentTransaction,
			Map <String, Object> hints,
			List <Container> objects);

	ConsoleForm <Container> buildAction (
			Transaction parentTransaction,
			Map <String, Object> hints);

	default
	ConsoleForm <Container> buildAction (
			@NonNull Transaction parentTransaction,
			@NonNull Map <String, Object> hints,
			@NonNull Optional <List <Container>> objects) {

		return buildAction (
			parentTransaction,
			hints,
			optionalOrElseRequired (
				objects,
				() -> emptyList ()));

	}

	default
	ConsoleForm <Container> buildAction (
			@NonNull Transaction parentTransaction,
			@NonNull Map <String, Object> hints,
			@NonNull Container object) {

		return buildAction (
			parentTransaction,
			hints,
			new ArrayList <Container> (
				Collections.singleton (
					object)));

	}

	// ---------- build response

	ConsoleForm <Container> buildResponse (
			Transaction parentTransaction,
			Map <String, Object> hints,
			List <Container> objects);

	ConsoleForm <Container> buildResponse (
			Transaction parentTransaction,
			Map <String, Object> hints);

	default
	ConsoleForm <Container> buildResponse (
			@NonNull Transaction parentTransaction,
			@NonNull Map <String, Object> hints,
			@NonNull Optional <List <Container>> objects) {

		return buildResponse (
			parentTransaction,
			hints,
			optionalOrElseRequired (
				objects,
				() -> emptyList ()));

	}

	default
	ConsoleForm <Container> buildResponse (
			@NonNull Transaction parentTransaction,
			@NonNull Map <String, Object> hints,
			@NonNull Container object) {

		return buildResponse (
			parentTransaction,
			hints,
			new ArrayList <Container> (
				Collections.singleton (
					object)));

	}

}
