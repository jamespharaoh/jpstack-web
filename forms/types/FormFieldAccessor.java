package wbs.console.forms.types;

import com.google.common.base.Optional;

import wbs.framework.database.Transaction;

public
interface FormFieldAccessor <Container, Native> {

	Optional <Native> read (
			Transaction parentTransaction,
			Container container);

	Optional <String> write (
			Transaction parentTransaction,
			Container container,
			Optional <Native> nativeValue);

}
