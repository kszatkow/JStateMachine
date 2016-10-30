package org.moomin.statemachine;

public abstract class SimpleState extends State {

	@Override
	public final boolean consumeEvent(Event event) {
		return false;
	}
	
	@Override
	protected final void doActivate() {
		// empty on purpose
	}

	@Override
	protected final void doDeactivate() {
		// empty on purpose
	}

	public void doAction() {
		doActionBehaviour();
		containingStateMachine().dispatchInternalEvent(new CompletionEvent(this));
	}
	
}
