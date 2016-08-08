package wbs.framework.logging;

import static wbs.framework.utils.etc.StringUtils.stringFormatArray;

public
class StackFrame {

	static
	ThreadLocal<StackFrame> stackFrameThreadLocal =
		new ThreadLocal<StackFrame> ();

	StackFrame parentFrame;
	Object[] args;

	public
	void close () {

		if (stackFrameThreadLocal.get () != this)
			throw new RuntimeException ();

		stackFrameThreadLocal.set (
			parentFrame);

	}

	public
	void printStackTrace () {

		System.out.println (
			stringFormatArray (
				args));

		parentFrame.printStackTrace ();

	}

	public
	void rethrow (
			Exception exception) {

	}

	public static
	StackFrame enter (
			Object... args) {

		StackFrame oldFrame =
			stackFrameThreadLocal.get ();

		StackFrame newFrame =
			new StackFrame ();

		newFrame.parentFrame =
			oldFrame;

		newFrame.args =
			args;

		stackFrameThreadLocal.set (
			newFrame);

		return newFrame;

	}

}
