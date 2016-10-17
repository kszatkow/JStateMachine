package org.moomin.statemachine;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.moomin.statemachine.oddeven.EvenNumberConstraint;
import org.moomin.statemachine.oddeven.EvenNumberEvent;
import org.moomin.statemachine.oddeven.EvenState;
import org.moomin.statemachine.oddeven.FeedNumberEvent;
import org.moomin.statemachine.oddeven.OddNumberConstraint;
import org.moomin.statemachine.oddeven.OddNumberEvent;
import org.moomin.statemachine.oddeven.OddState;
import org.moomin.statemachine.oddeven.ZeroNumberEvent;
import org.moomin.statemachine.oddeven.ZeroState;
import org.moomin.statemachine.onoff.OffEvent;
import org.moomin.statemachine.onoff.OffState;
import org.moomin.statemachine.onoff.OnEvent;
import org.moomin.statemachine.onoff.OnState;

public class StateMachineTest {

	private StateMachine stateMachine;

	@Before
	public void setUp() throws Exception {
		stateMachine = new StateMachine();
	}
	
	@Test
	public void transitionsWithoutGuardsTest() {
		State offState = addState(new OffState("Off"));
		State onState = addState(new OnState("On"));
		
		stateMachine.addTransition(
				new Transition(offState, onState, OnEvent.class));
		stateMachine.addTransition(
				new Transition(onState, offState, OffEvent.class));
				
		stateMachine.setInitialState(offState);
		
		// check initial state
		assertSame(offState, stateMachine.getCurrentState());
	
		// check on state transitions
		sendEventAndCheckCurrentState(new OnEvent() , onState);
		sendEventAndCheckCurrentState(new UnhandledEvent() , onState);
		sendEventAndCheckCurrentState(new OffEvent() , offState);

		// check off state transitions
		sendEventAndCheckCurrentState(new OffEvent() , offState);
		sendEventAndCheckCurrentState(new UnhandledEvent() , offState);
		sendEventAndCheckCurrentState(new OnEvent() , onState);
	}

	@Test
	public void transitionsWithGuardsTest() {
		State oddState = addState(new OddState("Odd"));
		State evenState = addState(new EvenState("Even"));
		
		stateMachine.addTransition(new Transition(
				oddState, evenState, 
				FeedNumberEvent.class, 
				new EvenNumberConstraint()));
		stateMachine.addTransition(new Transition(
				evenState, oddState, 
				FeedNumberEvent.class,
				new OddNumberConstraint()));
				
		stateMachine.setInitialState(oddState);
		
		// check initial state
		assertSame(oddState, stateMachine.getCurrentState());
		
		// check odd state transitions
		sendEventAndCheckCurrentState(new FeedNumberEvent(11) , oddState);
		sendEventAndCheckCurrentState(new UnhandledEvent() , oddState);
		sendEventAndCheckCurrentState(new FeedNumberEvent(4) , evenState);
		
		// check even state transitions
		sendEventAndCheckCurrentState(new FeedNumberEvent(10) , evenState);
		sendEventAndCheckCurrentState(new UnhandledEvent() , evenState);
		sendEventAndCheckCurrentState(new FeedNumberEvent(5) , oddState);
	}
	
	@Test
	public void multipleTransitionsFromOneStateTestTest() {
		State zeroState = addState(new ZeroState("Zero"));
		State oddState = addState(new OddState("Odd"));
		State evenState = addState(new EvenState("Even"));
		
		stateMachine.addTransition(
				new Transition(zeroState, oddState, OddNumberEvent.class));
		stateMachine.addTransition(
				new Transition(zeroState, evenState, EvenNumberEvent.class));
		stateMachine.addTransition(
				new Transition(oddState, evenState, EvenNumberEvent.class));
		stateMachine.addTransition(
				new Transition(oddState, zeroState, ZeroNumberEvent.class));
		stateMachine.addTransition(
				new Transition(evenState, oddState, OddNumberEvent.class));
		stateMachine.addTransition(
				new Transition(evenState, zeroState, ZeroNumberEvent.class));
		
		stateMachine.setInitialState(zeroState);
		
		// check initial state
		assertSame(zeroState, stateMachine.getCurrentState());
	
		// check transitions
		// zero -> zero
		sendEventAndCheckCurrentState(new ZeroNumberEvent() , zeroState);
		// zero -> odd
		sendEventAndCheckCurrentState(new OddNumberEvent() , oddState);
		// odd -> odd
		sendEventAndCheckCurrentState(new OddNumberEvent() , oddState);
		// odd -> zero
		sendEventAndCheckCurrentState(new ZeroNumberEvent() , zeroState);
		// zero -> even
		sendEventAndCheckCurrentState(new EvenNumberEvent() , evenState);
		// even -> even
		sendEventAndCheckCurrentState(new EvenNumberEvent() , evenState);
		// even -> zero
		sendEventAndCheckCurrentState(new ZeroNumberEvent() , zeroState);
		// zero -> odd
		sendEventAndCheckCurrentState(new OddNumberEvent() , oddState);
		// odd -> even
		sendEventAndCheckCurrentState(new EvenNumberEvent() , evenState);
		// even -> odd
		sendEventAndCheckCurrentState(new OddNumberEvent() , oddState);
	}
	
	private State addState(State state) {
		stateMachine.addState(state);
		return state;
	}
	
	private void sendEventAndCheckCurrentState(Event event, State expectedState) {
		stateMachine.processEvent(event);
		assertSame(expectedState, stateMachine.getCurrentState());
	}
}
