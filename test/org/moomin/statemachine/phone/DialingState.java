package org.moomin.statemachine.phone;

import org.moomin.statemachine.SimpleCompositeState;
import org.moomin.statemachine.StateMachine;

public class DialingState extends SimpleCompositeState {

	public DialingState(StateMachine owningStateMachine, String string) {
		super(owningStateMachine);
	}

	@Override
	public void onEntryBehaviour() {}
	
	@Override
	public void doActionBehaviour() {}

	@Override
	public void onExitBehaviour() {}

}
