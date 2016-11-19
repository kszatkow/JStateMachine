package org.moomin.statemachine;

import static org.hamcrest.core.StringContains.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.moomin.statemachine.onoff.OffEvent;
import org.moomin.statemachine.onoff.OnEvent;

@RunWith(MockitoJUnitRunner.class)
public class ActivatableStateMachineTest extends StateMachineTestBase {

	private State offState;
	private State onState;

	@Before
	public void setUp() {
		super.setUp();
		
		offState = addState(spy(State.class));
		onState = addState(spy(State.class));
	}
	
	
	@Test
	public void activateDeactivateTest() {
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
	
	
	@Test 
	public void illegalDoubleActivationTest() {
		setInitialTransitionAndActivate(onState);
		
		exception.expect(IllegalStateException.class);
		exception.expectMessage(containsString("Already active"));

		stateMachine.activate();
	}
	
	@Test 
	public void illegalDoubleDeactivationTest() {
		setInitialTransitionAndActivate(onState);
		stateMachine.deactivate();

		exception.expect(IllegalStateException.class);
		exception.expectMessage(containsString("Already inactive"));

		stateMachine.deactivate();
	}
	
	@Test 
	public void illegalActiveStateBeforeActivationTest() {
		exception.expect(IllegalStateException.class);
		exception.expectMessage(containsString("State machine is inactive"));

		stateMachineRegion.activeState();
	}
	
}
