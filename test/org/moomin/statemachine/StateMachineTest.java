package org.moomin.statemachine;

import static org.junit.Assert.*;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.moomin.statemachine.idleactive.ActiveState;
import org.moomin.statemachine.idleactive.IdleState;
import org.moomin.statemachine.idleactive.IdleTimeoutEvent;
import org.moomin.statemachine.idleactive.KeyWakeupEvent;
import org.moomin.statemachine.idleactive.MouseWakeupEvent;
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
import org.moomin.statemachine.onoff.Switch;
import org.moomin.statemachine.onoff.TurnOffEffect;
import org.moomin.statemachine.onoff.TurnOnEffect;

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
		assertSame(offState, stateMachine.getActiveState());
	
		// on -> on
		sendEventAndCheckCurrentState(new OnEvent() , onState);
		// no transition - unhandled event
		sendEventAndCheckCurrentState(new UnhandledEvent() , onState);
		// on -> off
		sendEventAndCheckCurrentState(new OffEvent() , offState);
		// off -> off
		sendEventAndCheckCurrentState(new OffEvent() , offState);
		// no transition - unhandled event
		sendEventAndCheckCurrentState(new UnhandledEvent() , offState);
		// off -> on
		sendEventAndCheckCurrentState(new OnEvent() , onState);
	}

	@Test
	public void transitionsWithGuardsTest() {
		State oddState = addState(new OddState("Odd"));
		State evenState = addState(new EvenState("Even"));
		
		// use two different transition constructors on purpose
		stateMachine.addTransition(new Transition(
				oddState, evenState, 
				FeedNumberEvent.class, 
				new EvenNumberConstraint()));
		stateMachine.addTransition(new Transition(
				evenState, oddState, 
				Collections.singleton(FeedNumberEvent.class),
				new OddNumberConstraint()));
				
		stateMachine.setInitialState(oddState);
		
		// check initial state
		assertSame(oddState, stateMachine.getActiveState());
		
		// odd -> odd
		sendEventAndCheckCurrentState(new FeedNumberEvent(11) , oddState);
		// no transition - unhandled event
		sendEventAndCheckCurrentState(new UnhandledEvent() , oddState);
		// odd -> even
		sendEventAndCheckCurrentState(new FeedNumberEvent(4) , evenState);
		// even -> even
		sendEventAndCheckCurrentState(new FeedNumberEvent(10) , evenState);
		// no transition - unhandled event
		sendEventAndCheckCurrentState(new UnhandledEvent() , evenState);
		// even -> odd
		sendEventAndCheckCurrentState(new FeedNumberEvent(5) , oddState);
	}
	
	@Test
	public void multipleTransitionsFromOneStateTest() {
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
		assertSame(zeroState, stateMachine.getActiveState());
	
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
	
	@Test
	public void transitionEffectTest() {
		State offState = addState(new OffState("Off"));
		State onState = addState(new OnState("On"));
		
		// use two different transition constructors on purpose
		Switch offOnSwitch = new Switch();
		TurnOnEffect turnOnEffect = new TurnOnEffect(offOnSwitch);
		stateMachine.addTransition(
				new Transition(offState, onState, OnEvent.class, turnOnEffect));
		TurnOffEffect turnOffEffect = new TurnOffEffect(offOnSwitch);
		stateMachine.addTransition(
				new Transition(onState, offState, Collections.singletonList(OffEvent.class), turnOffEffect));
				
		stateMachine.setInitialState(offState);
		
		// check initial state
		assertSame(offState, stateMachine.getActiveState());	
		assertEquals(false, offOnSwitch.isOn());
		
		// turn off
		sendEventAndCheckCurrentState(new OffEvent() , offState);
		assertEquals(false, offOnSwitch.isOn());
		
		// turn on
		sendEventAndCheckCurrentState(new OnEvent() , onState);
		assertEquals(true, offOnSwitch.isOn());

		// turn off
		sendEventAndCheckCurrentState(new OffEvent() , offState);
		assertEquals(false, offOnSwitch.isOn());
	}
	
	@Test
	public void transitionWithMultipleTriggersTest() {
		State idleState = addState(new IdleState("Idle"));
		State activeState = addState(new ActiveState("Active"));
		
		Set<Class<? extends Event>> triggerableBy = new HashSet<>();
		triggerableBy.add(KeyWakeupEvent.class);
		triggerableBy.add(MouseWakeupEvent.class);
		stateMachine.addTransition(
				new Transition(idleState, activeState, triggerableBy));
		stateMachine.addTransition(
				new Transition(activeState, idleState, IdleTimeoutEvent.class));
				
		stateMachine.setInitialState(idleState);
		
		// check initial state
		assertSame(idleState, stateMachine.getActiveState());	
		
		// idle -> idle
		sendEventAndCheckCurrentState(new IdleTimeoutEvent() , idleState);		
		// idle -> active (key event)
		sendEventAndCheckCurrentState(new KeyWakeupEvent() , activeState);
		// active -> idle
		sendEventAndCheckCurrentState(new IdleTimeoutEvent() , idleState);	
		// idle -> active (mouse event)
		sendEventAndCheckCurrentState(new MouseWakeupEvent() , activeState);
		// active -> idle
		sendEventAndCheckCurrentState(new IdleTimeoutEvent() , idleState);	
		// no transition - unhandled event
		sendEventAndCheckCurrentState(new UnhandledEvent() , idleState);
	}
	
	@Test
	public void twoStatesNoTransitionTest() {
		State idleState = addState(new IdleState("Idle"));
		addState(new ActiveState("Active"));
		
		stateMachine.setInitialState(idleState);
		
		// check initial state
		assertSame(idleState, stateMachine.getActiveState());	
		
		// no transition possible
		sendEventAndCheckCurrentState(new MouseWakeupEvent() , idleState);		
		// no transition possible
		sendEventAndCheckCurrentState(new KeyWakeupEvent() , idleState);
	}
	
	@Test
	public void illegalSourceOrTargetStateTransitionTest() {
		// legal states
		State idleState = addState(new IdleState("Idle"));
		State activeState = addState(new ActiveState("Active"));
		
		// illegal states
		State offState = new OffState("Off");
		State onState = new OnState("On");
		
		stateMachine.setInitialState(idleState);
		
		// illegal transition - both states illegal
		checkIllegalTransitionAddition(offState, onState, OnEvent.class);
		// illegal transition - source state illegal
		checkIllegalTransitionAddition(offState, activeState, OnEvent.class);
		// illegal transition - target state illegal
		checkIllegalTransitionAddition(idleState, onState, OffEvent.class);
	}

	private void checkIllegalTransitionAddition(
			State source, State target, Class<? extends Event> triggerableBy) {
		Exception expectedException = null;
		try {
			stateMachine.addTransition(
				new Transition(source, target, triggerableBy));
			fail("Exception should have been thrown.");
		} catch (IllegalArgumentException exc) {
			expectedException = exc;
		} finally {
			assertTrue(expectedException instanceof IllegalArgumentException);
		}
	}
	
	private State addState(State state) {
		stateMachine.addState(state);
		return state;
	}
	
	private void sendEventAndCheckCurrentState(Event event, State expectedState) {
		stateMachine.processEvent(event);
		assertSame(expectedState, stateMachine.getActiveState());
	}
}
