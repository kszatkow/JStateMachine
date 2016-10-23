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
import org.moomin.statemachine.oddeven.EvenNumberGuard;
import org.moomin.statemachine.oddeven.EvenNumberEvent;
import org.moomin.statemachine.oddeven.EvenState;
import org.moomin.statemachine.oddeven.FeedNumberEvent;
import org.moomin.statemachine.oddeven.OddNumberGuard;
import org.moomin.statemachine.oddeven.OddNumberEvent;
import org.moomin.statemachine.oddeven.OddState;
import org.moomin.statemachine.oddeven.ZeroNumberEvent;
import org.moomin.statemachine.oddeven.ZeroNumberGuard;
import org.moomin.statemachine.oddeven.ZeroState;
import org.moomin.statemachine.onoff.OffEvent;
import org.moomin.statemachine.onoff.OffProxyState;
import org.moomin.statemachine.onoff.OffState;
import org.moomin.statemachine.onoff.OnEvent;
import org.moomin.statemachine.onoff.OnOffTransitionGuard;
import org.moomin.statemachine.onoff.OnProxyState;
import org.moomin.statemachine.onoff.OnState;
import org.moomin.statemachine.onoff.Switch;
import org.moomin.statemachine.onoff.TurnOffEffect;
import org.moomin.statemachine.onoff.TurnOnEffect;
import org.moomin.statemachine.phone.ConnectEvent;
import org.moomin.statemachine.phone.ConnectingState;
import org.moomin.statemachine.phone.DialingFinished;
import org.moomin.statemachine.phone.DialingState;
import org.moomin.statemachine.phone.DigitEvent;
import org.moomin.statemachine.phone.FinishDialingEvent;
import org.moomin.statemachine.phone.HangUpEvent;
import org.moomin.statemachine.phone.InvalidNumberEvent;
import org.moomin.statemachine.phone.InvalidNumberState;
import org.moomin.statemachine.phone.LiftReceiverEvent;
import org.moomin.statemachine.phone.PartialDial;
import org.moomin.statemachine.phone.PhoneIdleState;
import org.moomin.statemachine.phone.StartDialing;

/**
 *  TODO:
 *  - thorough testing of SimpleCompositeState, e.g. completion transition
 *  - add proper handling of owning state machines and owning regions
 *  - extract proper interfaces and abstract classes - decide on responsibility split
 *     between Region, State and StateMachine
 *  - resolve other TODOs
 */

public class StateMachineTest {

	private StateMachine stateMachine;
	private Region stateMachineRegion;

	@Before
	public void setUp() throws Exception {
		stateMachine = new StateMachine();
		stateMachineRegion = new PrimitiveStateMachine(stateMachine);
		stateMachine.addRegion(stateMachineRegion);
	}
	
	@Test
	public void initialPseudostateTest() {
		State normalState = addState(State.NULL_STATE);
		
		TransitionTestEffect initialTransitionTestEffect = new TransitionTestEffect();
		setInitialTransition(normalState, initialTransitionTestEffect);
		
		assertFalse(initialTransitionTestEffect.hasBeenExecuted());
		
		stateMachine.activate();
		assertSame(normalState, stateMachineRegion.getActiveState());
		assertTrue(initialTransitionTestEffect.hasBeenExecuted());
	}

	@Test
	public void transitionsWithoutGuardsTest() {
		State offState = addState(new OffState("Off"));
		State onState = addState(new OnState("On"));
		
		addTransition(offState, onState, OnEvent.class);
		addTransition(onState, offState, OffEvent.class);
				
		setInitialTransitionAndActivate(offState);
	
		// off -> on
		dispatchThenProcessEventAndCheckActiveState(new OnEvent() , onState);
		// no transition - unhandled event
		dispatchThenProcessEventAndCheckActiveState(new UnhandledEvent() , onState);
		// on -> off
		dispatchThenProcessEventAndCheckActiveState(new OffEvent() , offState);
		// off -> off
		dispatchThenProcessEventAndCheckActiveState(new OffEvent() , offState);
		// no transition - unhandled event
		dispatchThenProcessEventAndCheckActiveState(new UnhandledEvent() , offState);
		// off -> on
		dispatchThenProcessEventAndCheckActiveState(new OnEvent() , onState);
	}

	@Test
	public void simpleStateCompletionTransitionWithoutGuardTest() {
		State offState = addState(new OffState("Off"));
		State onProxyState = addState(new OnProxyState("OnProxy"));
		State onState = addState(new OnState("On"));
		State offProxyState = addState(new OffProxyState("OnProxy"));
		
		addTransition(offState, onProxyState, OnEvent.class);
		addCompletionTransition(onProxyState, onState);
		addTransition(onState, offProxyState, OffEvent.class);
		TransitionTestEffect effect = new TransitionTestEffect();
		addCompletionTransition(offProxyState, offState, effect);
				
		setInitialTransitionAndActivate(offState);
	
		// off -> on
		dispatchThenProcessEventAndCheckActiveState(new OnEvent() , onState);
		// on -> off
		assertFalse(effect.hasBeenExecuted());
		dispatchThenProcessEventAndCheckActiveState(new OffEvent() , offState);
		assertTrue(effect.hasBeenExecuted());
	}
	
	@Test
	public void simpleStateCompletionTransitionWithGuardTest() {
		State offState = addState(new OffState("Off"));
		State onProxyState = addState(new OnProxyState("OnProxy"));
		State onState = addState(new OnState("On"));
		State offProxyState = addState(new OffProxyState("OnProxy"));
		
		OnOffTransitionGuard guard =  new OnOffTransitionGuard();
		addTransition(offState, onProxyState, OnEvent.class);
		addCompletionTransition(onProxyState, onState, guard);
		addTransition(onState, offProxyState, OffEvent.class);
		TransitionTestEffect effect = new TransitionTestEffect();
		addCompletionTransition(offProxyState, offState, guard, effect);
				
		setInitialTransitionAndActivate(offState);
	
		// off -> on unsuccessful - guard evaluates to false
		dispatchThenProcessEventAndCheckActiveState(new OnEvent() , onProxyState);
		// off -> on
		guard.evaluateToTrue();
		dispatchThenProcessEventAndCheckActiveState(new CompletionEvent() , onState);
				
		// on -> off unsuccessful - guard evaluates to false
		guard.evaluateToFalse();
		dispatchThenProcessEventAndCheckActiveState(new OffEvent() , offProxyState);
		assertFalse(effect.hasBeenExecuted());
		// on -> off
		guard.evaluateToTrue();
		dispatchThenProcessEventAndCheckActiveState(new CompletionEvent() , offState);
		assertTrue(effect.hasBeenExecuted());
	}
	
	private void addCompletionTransition(State source, State target) {
		stateMachineRegion.addTransition(
				new CompletionTransition(source, target));
	}

	private void addCompletionTransition(State source, State target, TransitionGuard guard) {
		stateMachineRegion.addTransition(
				new CompletionTransition(source, target, guard));
		
	}
	
	private void addCompletionTransition(State source, State target, TransitionEffect effect) {
		stateMachineRegion.addTransition(
				new CompletionTransition(source, target, effect));
		
	}
	
	private void addCompletionTransition(State source, State target, TransitionGuard guard, TransitionEffect effect) {
		stateMachineRegion.addTransition(
				new CompletionTransition(source, target, guard, effect));
	}
	
	@Test
	public void proxyStateTest() {
		State offState = addState(new OffState("Off"));
		State proxyState = addState(State.NULL_STATE);
		State onState = addState(new OnState("On"));
		
		addTransition(offState, proxyState, OnEvent.class);
		addTransition(proxyState, onState, OnEvent.class);
		addTransition(onState, proxyState, OffEvent.class);
		addTransition(proxyState, offState, OffEvent.class);
				
		setInitialTransitionAndActivate(offState);
	
		// off -> on
		dispatchThenProcessEventAndCheckActiveState(new OnEvent() , onState);
		// on -> off
		dispatchThenProcessEventAndCheckActiveState(new OffEvent() , offState);
	}
	
	@Test
	public void transitionsWithGuardsTest() {
		State oddState = addState(new OddState("Odd"));
		State evenState = addState(new EvenState("Even"));
		
		// use two different transition constructors on purpose
		addTransition(oddState, evenState, FeedNumberEvent.class, new EvenNumberGuard());
		addTransition(evenState, oddState, Collections.singleton(FeedNumberEvent.class), new OddNumberGuard());
				
		setInitialTransitionAndActivate(oddState);
		
		// odd -> odd
		dispatchThenProcessEventAndCheckActiveState(new FeedNumberEvent(11) , oddState);
		// no transition - unhandled event
		dispatchThenProcessEventAndCheckActiveState(new UnhandledEvent() , oddState);
		// odd -> even
		dispatchThenProcessEventAndCheckActiveState(new FeedNumberEvent(4) , evenState);
		// even -> even
		dispatchThenProcessEventAndCheckActiveState(new FeedNumberEvent(10) , evenState);
		// no transition - unhandled event
		dispatchThenProcessEventAndCheckActiveState(new UnhandledEvent() , evenState);
		// even -> odd
		dispatchThenProcessEventAndCheckActiveState(new FeedNumberEvent(5) , oddState);
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
		dispatchThenProcessEventAndCheckActiveState(new ZeroNumberEvent() , zeroState);
		// zero -> odd
		dispatchThenProcessEventAndCheckActiveState(new OddNumberEvent() , oddState);
		// odd -> odd
		dispatchThenProcessEventAndCheckActiveState(new OddNumberEvent() , oddState);
		// odd -> zero
		dispatchThenProcessEventAndCheckActiveState(new ZeroNumberEvent() , zeroState);
		// zero -> even
		dispatchThenProcessEventAndCheckActiveState(new EvenNumberEvent() , evenState);
		// even -> even
		dispatchThenProcessEventAndCheckActiveState(new EvenNumberEvent() , evenState);
		// even -> zero
		dispatchThenProcessEventAndCheckActiveState(new ZeroNumberEvent() , zeroState);
		// zero -> odd
		dispatchThenProcessEventAndCheckActiveState(new OddNumberEvent() , oddState);
		// odd -> even
		dispatchThenProcessEventAndCheckActiveState(new EvenNumberEvent() , evenState);
		// even -> odd
		dispatchThenProcessEventAndCheckActiveState(new OddNumberEvent() , oddState);
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
		dispatchThenProcessEventAndCheckActiveState(new OffEvent(), offState);
		assertEquals(false, offOnSwitch.isOn());
		
		// turn on
		dispatchThenProcessEventAndCheckActiveState(new OnEvent(), onState);
		assertEquals(true, offOnSwitch.isOn());

		// turn off
		dispatchThenProcessEventAndCheckActiveState(new OffEvent(), offState);
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
		dispatchThenProcessEventAndCheckActiveState(new IdleTimeoutEvent(), idleState);		
		// idle -> active (key event)
		dispatchThenProcessEventAndCheckActiveState(new KeyWakeupEvent(), activeState);
		// active -> idle
		dispatchThenProcessEventAndCheckActiveState(new IdleTimeoutEvent(), idleState);	
		// idle -> active (mouse event)
		dispatchThenProcessEventAndCheckActiveState(new MouseWakeupEvent(), activeState);
		// active -> idle
		dispatchThenProcessEventAndCheckActiveState(new IdleTimeoutEvent(), idleState);	
		// no transition - unhandled event
		dispatchThenProcessEventAndCheckActiveState(new UnhandledEvent(), idleState);
	}
	
	@Test
	public void twoStatesNoTransitionTest() {
		State idleState = addState(new IdleState("Idle"));
		State activeState = addState(new ActiveState("Active"));
		
		addTransition(idleState, activeState, KeyWakeupEvent.class);
		addTransition(activeState, idleState, IdleTimeoutEvent.class);
		
		setInitialTransitionAndActivate(idleState);	
		
		// no event dispatched - no transition
		stateMachine.processEvent();
		assertSame(idleState, stateMachineRegion.getActiveState());
	}
	
	@Test
	public void processUndispatchedEvent() {
		State idleState = addState(new IdleState("Idle"));
		addState(new ActiveState("Active"));
		
		setInitialTransitionAndActivate(idleState);	
		
		// no transition possible
		dispatchThenProcessEventAndCheckActiveState(new MouseWakeupEvent() , idleState);		
		// no transition possible
		dispatchThenProcessEventAndCheckActiveState(new KeyWakeupEvent() , idleState);
	}
	
	@Test
	public void illegalSourceOrTargetStateTransitionTest() {
		// legal states
		State idleState = addState(new IdleState("Idle"));
		State activeState = addState(new ActiveState("Active"));
		
		// illegal states
		State offState = new OffState("Off");
		State onState = new OnState("On");
		
		setInitialTransition(idleState, null);
		
		// prepare exception thrown checker
		ExceptionThrownIllegalTransitionChecker illegalTransitionChecker = new ExceptionThrownIllegalTransitionChecker(
				IllegalArgumentException.class, 
				"Exception should have been thrown - illegal transition added.",
				stateMachineRegion);
		
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
		
		// invalid default state chosen - exception thrown
		invalidDefaultStateChecker.checkExceptionThrownAfterAction();
	}
	
	@Test 
	public void illegalInitialTransitionSetupAfterActivationTest() {
		State idleState = addState(new IdleState("Idle"));
		addState(new ActiveState("Active"));
		
		setInitialTransitionAndActivate(idleState);
		
		// prepare exception thrown checker
		ExceptionThrownChecker illegalStateAdditionChecker = new ExceptionThrownChecker(
				IllegalStateException.class, 
				"Exception should have been thrown - illegal initial transition setup.") {
					@Override
					protected void doAction() {
						stateMachineRegion.addState(State.NULL_STATE);
					}
		};
		
		// illegal action taken - exception thrown
		illegalStateAdditionChecker.checkExceptionThrownAfterAction();
	}
	
	@Test 
	public void illegalStateAdditionAfterActivationTest() {
		State idleState = addState(new IdleState("Idle"));
		addState(new ActiveState("Active"));
		
		setInitialTransitionAndActivate(idleState);
		
		// prepare exception thrown checker
		ExceptionThrownChecker illegalStateAdditionChecker = new ExceptionThrownChecker(
				IllegalStateException.class, 
				"Exception should have been thrown - illegal state addition.") {
					@Override
					protected void doAction() {
						setInitialTransition(new OnState(""), null);
					}
		};
		
		// illegal action taken - exception thrown
		illegalStateAdditionChecker.checkExceptionThrownAfterAction();
	}
	
	@Test 
	public void illegalTransitionAdditionAfterActivationTest() {
		State idleState = addState(new IdleState("Idle"));
		addState(new ActiveState("Active"));
		
		setInitialTransitionAndActivate(idleState);
		
		// prepare exception thrown checker
		ExceptionThrownChecker illegalTransitionAdditionChecker = new ExceptionThrownChecker(
				IllegalStateException.class, 
				"Exception should have been thrown - illegal transition addition.") {
					@Override
					protected void doAction() {
						addTransition(idleState, new OnState(""), OnEvent.class);
					}
		};
		
		// illegal action taken - exception thrown
		illegalTransitionAdditionChecker.checkExceptionThrownAfterAction();
	}

	@Test 
	public void illegalDoubleActivationTest() {
		State idleState = addState(new IdleState("Idle"));
		addState(new ActiveState("Active"));
		
		setInitialTransitionAndActivate(idleState);
		
		// prepare exception thrown checker
		ExceptionThrownChecker illegalDoubleActivationChecker = new ExceptionThrownChecker(
				IllegalStateException.class, 
				"Exception should have been thrown - illegal double activation.") {
					@Override
					protected void doAction() {
						stateMachine.activate();
					}
		};
		
		// illegal action taken - exception thrown
		illegalDoubleActivationChecker.checkExceptionThrownAfterAction();
	}
	
	@Test 
	public void illegalEventProcessingBeforeActivationTest() {
		addState(new IdleState("Idle"));
		addState(new ActiveState("Active"));
		
		// prepare exception thrown checker
		ExceptionThrownChecker illegalTransitionAdditionChecker = new ExceptionThrownChecker(
				IllegalStateException.class, 
				"Exception should have been thrown - illegal event processing.") {
					@Override
					protected void doAction() {
						stateMachine.processEvent();
					}
		};
		
		// illegal action taken - exception thrown
		illegalTransitionAdditionChecker.checkExceptionThrownAfterAction();
	}
	
	@Test 
	public void illegalActiveStateBeforeActivationTest() {
		addState(new IdleState("Idle"));
		addState(new ActiveState("Active"));
		
		// prepare exception thrown checker
		ExceptionThrownChecker illegalTransitionAdditionChecker = new ExceptionThrownChecker(
				IllegalStateException.class, 
				"Exception should have been thrown - illegal active state read.") {
					@Override
					protected void doAction() {
						stateMachineRegion.getActiveState();
					}
		};
		
		// illegal action taken - exception thrown
		illegalTransitionAdditionChecker.checkExceptionThrownAfterAction();
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
		dispatchThenProcessEventAndCheckActiveState(new KeyWakeupEvent() , activeState);
		assertFalse(idleState.hasOnEntryBeenExecuted());
		assertTrue(idleState.hasOnExitBeenExecuted());
		assertTrue(activeState.hasOnActionBeenExecuted());
		
		// transition back to idle state - onEntry of target state executed
		dispatchThenProcessEventAndCheckActiveState(new IdleTimeoutEvent() , idleState);
		assertTrue(idleState.hasOnEntryBeenExecuted());
	}
	
	@Test
	public void choicePseudostateTest() {
		State zeroState = addState(new ZeroState("Zero"));
		State oddState = addState(new OddState("Odd"));
		State evenState = addState(new EvenState("Even"));
		State checkParity = addState(new ChoiceState("CheckParity"));
		
		addTransition(zeroState, checkParity, FeedNumberEvent.class);
		addTransition(oddState, checkParity, FeedNumberEvent.class);
		addTransition(evenState, checkParity, FeedNumberEvent.class);
		addTransition(checkParity, evenState, FeedNumberEvent.class, new EvenNumberGuard());
		addTransition(checkParity, oddState, FeedNumberEvent.class, new OddNumberGuard());
		addTransition(checkParity, zeroState, FeedNumberEvent.class, new ZeroNumberGuard());
		
		setInitialTransitionAndActivate(zeroState);
	
		// zero -> zero
		dispatchThenProcessEventAndCheckActiveState(new FeedNumberEvent(0) , zeroState);
		// zero -> odd
		dispatchThenProcessEventAndCheckActiveState(new FeedNumberEvent(3) , oddState);
		// odd -> odd
		dispatchThenProcessEventAndCheckActiveState(new FeedNumberEvent(5) , oddState);
		// odd -> zero
		dispatchThenProcessEventAndCheckActiveState(new FeedNumberEvent(0) , zeroState);
		// zero -> even
		dispatchThenProcessEventAndCheckActiveState(new FeedNumberEvent(2) , evenState);
		// even -> even
		dispatchThenProcessEventAndCheckActiveState(new FeedNumberEvent(12) , evenState);
		// even -> zero
		dispatchThenProcessEventAndCheckActiveState(new FeedNumberEvent(0) , zeroState);
		// zero -> odd
		dispatchThenProcessEventAndCheckActiveState(new FeedNumberEvent(13) , oddState);
		// odd -> even
		dispatchThenProcessEventAndCheckActiveState(new FeedNumberEvent(18) , evenState);
		// even -> odd
		dispatchThenProcessEventAndCheckActiveState(new FeedNumberEvent(15) , oddState);
	}
	
	@Test
	public void simpleCompositeStateTest() {
		State phoneIdleState = addState(new PhoneIdleState("PhoneIdle"));
		SimpleCompositeState dialingState = addSimpleCompositeState(new DialingState(stateMachine, "Dialing"));
		State invalidState = addState(new InvalidNumberState("InvalidNumber"));
		State connectingState = addState(new ConnectingState("Connecting"));
		
		addTransition(phoneIdleState, dialingState, LiftReceiverEvent.class);
		addTransition(dialingState, connectingState, ConnectEvent.class);
		addTransition(dialingState, invalidState, InvalidNumberEvent.class);
		addTransition(invalidState, phoneIdleState, HangUpEvent.class);
		addTransition(connectingState, phoneIdleState, HangUpEvent.class);
		
		StartDialing startDialingSubstate = (StartDialing) addSubstate(dialingState, new StartDialing("StartDialing"));
		State partialDialSubstate = addSubstate(dialingState, new PartialDial("PartialDial"));
		State dialingFinishedSubstate = addSubstate(dialingState, new DialingFinished("DialingFinished"));
		
		dialingState.setInitialTransition(new InitialTransition(startDialingSubstate, new TransitionTestEffect()));
		dialingState.addTransition(new Transition(startDialingSubstate, partialDialSubstate, DigitEvent.class));
		dialingState.addTransition(new Transition(partialDialSubstate, partialDialSubstate, DigitEvent.class));
		dialingState.addTransition(new Transition(partialDialSubstate, dialingFinishedSubstate, FinishDialingEvent.class));
		
		setInitialTransitionAndActivate(phoneIdleState);
	
		// PhoneIdle -> Dialing::StartDialing
		dispatchThenProcessEventAndCheckActiveState(new LiftReceiverEvent(), dialingState);
		assertSame(startDialingSubstate, dialingState.getActiveState());
		assertTrue(startDialingSubstate.isDialToneOn());
		
		// Dialing::StartDialing -> Dialing::PartialDial
		dispatchThenProcessEventAndCheckActiveState(new DigitEvent(1) , dialingState);
		assertSame(partialDialSubstate, dialingState.getActiveState());
		assertFalse(startDialingSubstate.isDialToneOn());
		
		// Dialing::PartialDial -> Dialing::PartialDial
		dispatchThenProcessEventAndCheckActiveState(new DigitEvent(2) , dialingState);
		assertSame(partialDialSubstate, dialingState.getActiveState());
		dispatchThenProcessEventAndCheckActiveState(new DigitEvent(3) , dialingState);
		assertSame(partialDialSubstate, dialingState.getActiveState());
		dispatchThenProcessEventAndCheckActiveState(new DigitEvent(4) , dialingState);
		assertSame(partialDialSubstate, dialingState.getActiveState());
		
		// Dialing::PartialDial -> Dialing::DialingFinished
		dispatchThenProcessEventAndCheckActiveState(new FinishDialingEvent() , dialingState);
		assertSame(dialingFinishedSubstate, dialingState.getActiveState());
		
		// Dialing::Finished -> Connecting
		dispatchThenProcessEventAndCheckActiveState(new ConnectEvent() , connectingState);
		
		// Connecting -> PhoneIdle
		dispatchThenProcessEventAndCheckActiveState(new HangUpEvent() , phoneIdleState);
		
		// PhoneIdle -> Dialing::StartDialing
		dispatchThenProcessEventAndCheckActiveState(new LiftReceiverEvent(), dialingState);
		assertSame(startDialingSubstate, dialingState.getActiveState());
		
		// Dialing::PartialDial -> Dialing::PartialDial
		dispatchThenProcessEventAndCheckActiveState(new DigitEvent(2) , dialingState);
		assertSame(partialDialSubstate, dialingState.getActiveState());
		
		// Dialing::PartialDial -> Dialing::DialingFinished
		dispatchThenProcessEventAndCheckActiveState(new FinishDialingEvent() , dialingState);
		assertSame(dialingFinishedSubstate, dialingState.getActiveState());
		
		// Dialing::Finished -> InvalidNumber
		dispatchThenProcessEventAndCheckActiveState(new InvalidNumberEvent() , invalidState);
		
		// InvalidNumber -> PhoneIdle
		dispatchThenProcessEventAndCheckActiveState(new HangUpEvent() , phoneIdleState);
	}

	private State addSubstate(SimpleCompositeState compositeState, State substate) {
		compositeState.addState(substate);
		return substate;
	}
	

	private State addState(State state) {
		stateMachineRegion.addState(state);
		return state;
	}
	
	private SimpleCompositeState addSimpleCompositeState(SimpleCompositeState state) {
		return (SimpleCompositeState) addState(state);
	}
	
	private void addTransition(State source, State target, Class<? extends Event> triggerableBy) {
		stateMachineRegion.addTransition(
				new Transition(source, target, triggerableBy));
	}

	private void addTransition(State source, State target, Collection<Class<? extends Event>> triggerableBy) {
		stateMachineRegion.addTransition(
				new Transition(source, target, triggerableBy));
	}
	
	private void addTransition(State source, State target, Class<? extends Event> triggerableBy, TransitionEffect effect) {
		stateMachineRegion.addTransition(
				new Transition(source, target, triggerableBy, effect));
	}

	private void addTransition(State source, State target, Collection<Class<? extends Event>> triggerableBy, TransitionEffect effect) {
		stateMachineRegion.addTransition(
				new Transition(source, target, triggerableBy, effect));
	}
	
	private void addTransition(State source, State target, Class<? extends Event> triggerableBy, TransitionGuard guard) {
		stateMachineRegion.addTransition(new Transition(source, target, triggerableBy, guard));
	}
	
	private void addTransition(State source, State target, Set<Class<? extends Event>> triggerableBy, TransitionGuard guard) {
		stateMachineRegion.addTransition(new Transition(source, target, triggerableBy, guard));
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
		stateMachineRegion.setInitialTransition(new InitialTransition(target, effect));
	}
	
	private void dispatchThenProcessEventAndCheckActiveState(Event event, State expectedState) {
		stateMachine.dispatchEvent(event);
		stateMachine.processEvent();
		assertSame(expectedState, stateMachineRegion.getActiveState());
	}
}
