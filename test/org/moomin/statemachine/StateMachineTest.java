package org.moomin.statemachine;

import static org.junit.Assert.*;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.moomin.statemachine.idleactive.ActiveState;
import org.moomin.statemachine.idleactive.ActiveStateWithDoActionBehaviour;
import org.moomin.statemachine.idleactive.IdleState;
import org.moomin.statemachine.idleactive.IdleStateWithOnEntryAndOnExitBehaviours;
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
	public void initialPseudostateTest() {
		State normalState = addState(new NoBehaviourState());
		
		InitialTransitionTestEffect initialTransitionTestEffect = new InitialTransitionTestEffect();
		setInitialTransition(normalState, initialTransitionTestEffect);
		
		assertFalse(initialTransitionTestEffect.hasBeenExecuted());
		
		stateMachine.activate();
		assertSame(normalState, stateMachine.getActiveState());
		assertTrue(initialTransitionTestEffect.hasBeenExecuted());
	}

	@Test
	public void transitionsWithoutGuardsTest() {
		State offState = addState(new OffState("Off"));
		State onState = addState(new OnState("On"));
		
		addTransition(offState, onState, OnEvent.class);
		addTransition(onState, offState, OffEvent.class);
				
		setInitialTransitionAndActivate(offState);
	
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
		addTransition(oddState, evenState, FeedNumberEvent.class, new EvenNumberConstraint());
		addTransition(evenState, oddState, Collections.singleton(FeedNumberEvent.class), new OddNumberConstraint());
				
		setInitialTransitionAndActivate(oddState);
		
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
		
		addTransition(zeroState, oddState, OddNumberEvent.class);
		addTransition(zeroState, evenState, EvenNumberEvent.class);
		addTransition(oddState, evenState, EvenNumberEvent.class);
		addTransition(oddState, zeroState, ZeroNumberEvent.class);
		addTransition(evenState, oddState, OddNumberEvent.class);
		addTransition(evenState, zeroState, ZeroNumberEvent.class);
		
		setInitialTransitionAndActivate(zeroState);
	
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
		addTransition(offState, onState, OnEvent.class, turnOnEffect);
		TurnOffEffect turnOffEffect = new TurnOffEffect(offOnSwitch);
		addTransition(onState, offState, Collections.singletonList(OffEvent.class), turnOffEffect);
				
		setInitialTransitionAndActivate(offState);	
		assertEquals(false, offOnSwitch.isOn());
		
		// turn off
		sendEventAndCheckCurrentState(new OffEvent(), offState);
		assertEquals(false, offOnSwitch.isOn());
		
		// turn on
		sendEventAndCheckCurrentState(new OnEvent(), onState);
		assertEquals(true, offOnSwitch.isOn());

		// turn off
		sendEventAndCheckCurrentState(new OffEvent(), offState);
		assertEquals(false, offOnSwitch.isOn());
	}
	
	@Test
	public void transitionWithMultipleTriggersTest() {
		State idleState = addState(new IdleState("Idle"));
		State activeState = addState(new ActiveState("Active"));
		
		Set<Class<? extends Event>> triggerableBy = new HashSet<>();
		triggerableBy.add(KeyWakeupEvent.class);
		triggerableBy.add(MouseWakeupEvent.class);
		addTransition(idleState, activeState, triggerableBy);
		addTransition(activeState, idleState, IdleTimeoutEvent.class);
				
		setInitialTransitionAndActivate(idleState);	
		
		// idle -> idle
		sendEventAndCheckCurrentState(new IdleTimeoutEvent(), idleState);		
		// idle -> active (key event)
		sendEventAndCheckCurrentState(new KeyWakeupEvent(), activeState);
		// active -> idle
		sendEventAndCheckCurrentState(new IdleTimeoutEvent(), idleState);	
		// idle -> active (mouse event)
		sendEventAndCheckCurrentState(new MouseWakeupEvent(), activeState);
		// active -> idle
		sendEventAndCheckCurrentState(new IdleTimeoutEvent(), idleState);	
		// no transition - unhandled event
		sendEventAndCheckCurrentState(new UnhandledEvent(), idleState);
	}
	
	@Test
	public void twoStatesNoTransitionTest() {
		State idleState = addState(new IdleState("Idle"));
		addState(new ActiveState("Active"));
		
		setInitialTransitionAndActivate(idleState);	
		
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
		
		setInitialTransitionAndActivate(idleState);
		
		// prepare exception thrown checker
		ExceptionThrownIllegalTransitionChecker illegalTransitionChecker = new ExceptionThrownIllegalTransitionChecker(
				IllegalArgumentException.class, 
				"Exception should have been thrown - illegal transition added.",
				stateMachine);
		
		// illegal transition - both states illegal
		illegalTransitionChecker.setIllegalTransition(new Transition(offState, onState, OnEvent.class));
		illegalTransitionChecker.checkExceptionThrownAfterAction();
		
		// illegal transition - source state illegal
		illegalTransitionChecker.setIllegalTransition(new Transition(offState, activeState, OnEvent.class));
		illegalTransitionChecker.checkExceptionThrownAfterAction();
		
		// illegal transition - target state illegal
		illegalTransitionChecker.setIllegalTransition(new Transition(idleState, onState, OffEvent.class));
		illegalTransitionChecker.checkExceptionThrownAfterAction();
	}

	@Test
	public void duplicateStatesTest() {
		// legal states
		State idleState = addState(new IdleState("Idle"));
		addState(new ActiveState("Active"));
		
		// prepare exception thrown checker
		ExceptionThrownChecker duplicateStateChecker = new ExceptionThrownChecker(
				IllegalArgumentException.class, 
				"Exception should have been thrown - duplicate state added.") {
					@Override
					protected void doAction() {
						addState(idleState);
					}
		};
		
		// duplicate state added - exception thrown
		duplicateStateChecker.checkExceptionThrownAfterAction();
	}
	
	@Test 
	public void invalidDefaultStateTest() {
		// legal states
		addState(new IdleState("Idle"));
		addState(new ActiveState("Active"));
		
		// prepare exception thrown checker
		ExceptionThrownChecker invalidDefaultStateChecker = new ExceptionThrownChecker(
				IllegalArgumentException.class, 
				"Exception should have been thrown - invalid default state set.") {
					@Override
					protected void doAction() {
						setInitialTransitionAndActivate(new OnState("On"));
					}
		};
		
		// duplicate state added - exception thrown
		invalidDefaultStateChecker.checkExceptionThrownAfterAction();
	}
	
	@Test
	public void stateBehavioursTest() {
		IdleStateWithOnEntryAndOnExitBehaviours idleState = 
				(IdleStateWithOnEntryAndOnExitBehaviours) addState(new IdleStateWithOnEntryAndOnExitBehaviours("Idle"));
		ActiveStateWithDoActionBehaviour activeState = 
				(ActiveStateWithDoActionBehaviour) addState(new ActiveStateWithDoActionBehaviour("ActiveState"));
		
		addTransition(idleState, activeState, KeyWakeupEvent.class);
		addTransition(activeState, idleState, IdleTimeoutEvent.class);
		
		assertFalse(idleState.hasOnEntryBeenExecuted());
		assertFalse(idleState.hasOnExitBeenExecuted());
		
		setInitialTransitionAndActivate(idleState);
		assertTrue(idleState.hasOnEntryBeenExecuted());
		assertFalse(idleState.hasOnExitBeenExecuted());
		
		idleState.clearExecutionStateFlags();
		
		// initial state - active state onAction not executed yet
		assertFalse(activeState.hasOnActionBeenExecuted());
		
		// transition to active state - onExit of source state and onAction of target state executed
		sendEventAndCheckCurrentState(new KeyWakeupEvent() , activeState);
		assertFalse(idleState.hasOnEntryBeenExecuted());
		assertTrue(idleState.hasOnExitBeenExecuted());
		assertTrue(activeState.hasOnActionBeenExecuted());
		
		// transition back to idle state - onEntry of target state executed
		sendEventAndCheckCurrentState(new IdleTimeoutEvent() , idleState);
		assertTrue(idleState.hasOnEntryBeenExecuted());
	}
	
	private State addState(State state) {
		stateMachine.addState(state);
		return state;
	}
	
	private void addTransition(State source, State target, Class<? extends Event> triggerableBy) {
		stateMachine.addTransition(
				new Transition(source, target, triggerableBy));
	}

	private void addTransition(State source, State target, Collection<Class<? extends Event>> triggerableBy) {
		stateMachine.addTransition(
				new Transition(source, target, triggerableBy));
	}
	
	private void addTransition(State source, State target, Class<? extends Event> triggerableBy, TransitionEffect effect) {
		stateMachine.addTransition(
				new Transition(source, target, triggerableBy, effect));
	}

	private void addTransition(State source, State target, Collection<Class<? extends Event>> triggerableBy, TransitionEffect effect) {
		stateMachine.addTransition(
				new Transition(source, target, triggerableBy, effect));
	}
	
	private void addTransition(State source, State target, Class<? extends Event> triggerableBy, TransitionGuard guard) {
		stateMachine.addTransition(new Transition(source, target, triggerableBy, guard));
	}
	
	private void addTransition(State source, State target, Set<Class<? extends Event>> triggerableBy, TransitionGuard guard) {
		stateMachine.addTransition(new Transition(source, target, triggerableBy, guard));
	}
	
	private void setInitialTransitionAndActivate(State initialState) {
		setInitialTransition(
				initialState,
				new TransitionEffect() {
						@Override
						public void execute() {
							// empty on purpose
						}
				});
		stateMachine.activate();
	}
	
	private void setInitialTransition(State target, TransitionEffect effect) {
		stateMachine.setInitialTransition(new InitialTransition(target, effect));
		
	}
	
	private void sendEventAndCheckCurrentState(Event event, State expectedState) {
		stateMachine.processEvent(event);
		assertSame(expectedState, stateMachine.getActiveState());
	}
}
