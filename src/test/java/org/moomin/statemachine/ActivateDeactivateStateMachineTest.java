package org.moomin.statemachine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.spy;

import org.junit.Test;
import org.moomin.statemachine.onoff.OffEvent;
import org.moomin.statemachine.onoff.OnEvent;

public class ActivateDeactivateStateMachineTest extends StateMachineTestBase {

	@Test
	public void activateDeactivateTest() {
		State offState = addState(spy(State.class));
		State onState = addState(spy(State.class));
		
		addTransition(offState, onState, OnEvent.class);
		addTransition(onState, offState, OffEvent.class);
				
		assertFalse(stateMachine.isActive());
		setInitialTransitionAndActivate(offState);
		assertTrue(stateMachine.isActive());
		
		// off -> on
		dispatchThenProcessEventAndCheckActiveState(new OnEvent() , onState);
		assertTrue(stateMachine.isActive());
		
		// deactivate - machine goes into default state
		stateMachine.deactivate();
		assertFalse(stateMachine.isActive());
		
		// activate again
		stateMachine.activate();
		assertTrue(stateMachine.isActive());
		assertEquals(offState, stateMachineRegion.activeState());
		
		// off -> on
		dispatchThenProcessEventAndCheckActiveState(new OnEvent() , onState);
	}
	
}
