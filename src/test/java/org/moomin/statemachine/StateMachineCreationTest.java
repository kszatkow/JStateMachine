package org.moomin.statemachine;

import static org.hamcrest.core.StringContains.containsString;
import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.moomin.statemachine.onoff.OnEvent;

@RunWith(MockitoJUnitRunner.class)
public class StateMachineCreationTest extends StateMachineTestBase {

	private State idleState;

	@Before
	public void setUp() {
		super.setUp();
		
		idleState = addState(mock(State.class));
		addState(mock(State.class));
	}
	
	@Test
	public void duplicateStatesTest() {
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage(containsString("Duplicate state"));

		addState(idleState);
	}
	
	@Test 
	public void invalidDefaultStateTest() {
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage(containsString("Invalid default state"));
		
		setInitialTransitionAndActivate(mock(State.class));
	}
	
	@Test 
	public void illegalStateAdditionAfterActivationTest() {
		setInitialTransitionAndActivate(idleState);

		exception.expect(IllegalStateException.class);
		exception.expectMessage(containsString("State addition is not allowed when state machine is active"));

		stateMachineRegion.addState(mock(State.class));
	}
	
	@Test 
	public void illegalInitialTransitionSetupAfterActivationTest() {
		setInitialTransitionAndActivate(idleState);
		
		exception.expect(IllegalStateException.class);
		exception.expectMessage(containsString("Initial transition setup is not allowed when state machine is active"));

		setInitialTransition(mock(State.class), null);
	}
	
	@Test 
	public void illegalTransitionAdditionAfterActivationTest() {
		setInitialTransitionAndActivate(idleState);
		
		exception.expect(IllegalStateException.class);
		exception.expectMessage(containsString("Transition addition is not allowed when state machine is active"));

		addTransition(idleState, mock(State.class), OnEvent.class);
	}

	@Test 
	public void illegalRegionAdditionAfterActivationTest() {
		setInitialTransitionAndActivate(idleState);

		exception.expect(IllegalStateException.class);
		exception.expectMessage(containsString("Region addition is not allowed when state machine is active"));

		stateMachine.addRegion(new RegionStateMachine(stateMachine));
	}
	
}
