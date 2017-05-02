package wbs.console.forms;

import com.google.common.base.Optional;

import wbs.framework.database.Transaction;

public
interface FormFieldAccessor <Container, Native> {

	Optional <Native> read (
			Transaction parentTransaction,
			Container container);

	void write (
			Transaction parentTransaction,
			Container container,
			Optional <Native> nativeValue);

}
