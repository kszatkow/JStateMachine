package org.moomin.statemachine;

import static org.mockito.Mockito.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.moomin.statemachine.idleactive.IdleTimeoutEvent;
import org.moomin.statemachine.idleactive.KeyWakeupEvent;

@RunWith(MockitoJUnitRunner.class)
public class StateBehaviourStateMachineTest extends StateMachineTestBase {

	@Test
	public void stateBehavioursTest() {
		State idleState = addState(spy(State.class));
		State activeState = addState(spy(State.class));
		
		addTransition(idleState, activeState, KeyWakeupEvent.class);
		addTransition(activeState, idleState, IdleTimeoutEvent.class);
		
		verify(idleState, never()).onEntry(any());
		verify(idleState, never()).onExit();
		
		setInitialTransitionAndActivate(idleState);
		verify(idleState).onEntry(any());
		verify(idleState, never()).onExit();
		
		// initial state - active state onAction not executed yet
		verify(activeState, never()).doAction();
		
		// transition to active state - onExit of source state and onAction of target state executed
		dispatchThenProcessEventAndCheckActiveState(new KeyWakeupEvent() , activeState);
		verify(idleState).onEntry(any());
		verify(idleState).onExit();
		verify(activeState).doAction();
		
		// transition back to idle state - onEntry of target state executed
		IdleTimeoutEvent idleTimeoutEvent = new IdleTimeoutEvent();
		dispatchThenProcessEventAndCheckActiveState(idleTimeoutEvent , idleState);
		verify(idleState).onEntry(idleTimeoutEvent);
	}
	
	
}
