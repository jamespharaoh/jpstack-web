package wbs.framework.component.manager;

public
enum ComponentState {
	none,
	creation,
	injection,
	uninitialized,
	active,
	tornDown,
	error;
}
