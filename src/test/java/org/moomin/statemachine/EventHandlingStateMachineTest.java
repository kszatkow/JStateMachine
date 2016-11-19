package org.moomin.statemachine;

import static org.hamcrest.core.StringContains.containsString;
import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.moomin.statemachine.idleactive.KeyWakeupEvent;
import org.moomin.statemachine.idleactive.MouseWakeupEvent;
import org.moomin.statemachine.onoff.OnEvent;

@RunWith(MockitoJUnitRunner.class)
public class EventHandlingStateMachineTest extends StateMachineTestBase {

	private State idleState;

	@Before
	public void setUp() {
		super.setUp();
		
		idleState = addState(mock(State.class));
		addState(mock(State.class));
	}
	
	
	@Test
	public void processUndispatchedEvent() {
		setInitialTransitionAndActivate(idleState);	
		
		// no transition possible
		dispatchThenProcessEventAndCheckActiveState(new MouseWakeupEvent() , idleState);		
		// no transition possible
		dispatchThenProcessEventAndCheckActiveState(new KeyWakeupEvent() , idleState);
	}
	
	@Test 
	public void illegalEventProcessingBeforeActivationTest() {
		exception.expect(IllegalStateException.class);
		exception.expectMessage(containsString("Event processing is not allowed when state machine is inactive"));

		stateMachine.processEvent();
	}

	@Test
	public void illegalEventDispatchBeforeActivationTest() {
		exception.expect(IllegalStateException.class);
		exception.expectMessage(containsString("Event dispatching is not allowed when state machine is inactive"));

		stateMachine.dispatchEvent(new OnEvent());
	}
	
	@Test
	public void illegalInternalEventDispatchBeforeActivationTest() {
		exception.expect(IllegalStateException.class);
		exception.expectMessage(containsString("Event dispatching is not allowed when state machine is inactive"));

		stateMachine.dispatchInternalEvent(new OnEvent());
	}
	
}
