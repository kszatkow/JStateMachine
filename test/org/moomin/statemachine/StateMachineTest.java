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
import org.moomin.statemachine.onoff.OffState;
import org.moomin.statemachine.onoff.OnEvent;
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

public class StateMachineTest {

	private StateMachine stateMachine;

	@Before
	public void setUp() throws Exception {
		stateMachine = new StateMachine();
	}
	
	@Test
	public void initialPseudostateTest() {
		State normalState = addState(State.NULL_STATE);
		
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
	
		// off -> on
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
		sendEventAndCheckCurrentState(new OnEvent() , onState);
		// on -> off
		sendEventAndCheckCurrentState(new OffEvent() , offState);
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
		
		setInitialTransition(idleState, null);
		
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
						stateMachine.addState(State.NULL_STATE);
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
						stateMachine.processEvent(new IdleTimeoutEvent());
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
						stateMachine.getActiveState();
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
		sendEventAndCheckCurrentState(new KeyWakeupEvent() , activeState);
		assertFalse(idleState.hasOnEntryBeenExecuted());
		assertTrue(idleState.hasOnExitBeenExecuted());
		assertTrue(activeState.hasOnActionBeenExecuted());
		
		// transition back to idle state - onEntry of target state executed
		sendEventAndCheckCurrentState(new IdleTimeoutEvent() , idleState);
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
		sendEventAndCheckCurrentState(new FeedNumberEvent(0) , zeroState);
		// zero -> odd
		sendEventAndCheckCurrentState(new FeedNumberEvent(3) , oddState);
		// odd -> odd
		sendEventAndCheckCurrentState(new FeedNumberEvent(5) , oddState);
		// odd -> zero
		sendEventAndCheckCurrentState(new FeedNumberEvent(0) , zeroState);
		// zero -> even
		sendEventAndCheckCurrentState(new FeedNumberEvent(2) , evenState);
		// even -> even
		sendEventAndCheckCurrentState(new FeedNumberEvent(12) , evenState);
		// even -> zero
		sendEventAndCheckCurrentState(new FeedNumberEvent(0) , zeroState);
		// zero -> odd
		sendEventAndCheckCurrentState(new FeedNumberEvent(13) , oddState);
		// odd -> even
		sendEventAndCheckCurrentState(new FeedNumberEvent(18) , evenState);
		// even -> odd
		sendEventAndCheckCurrentState(new FeedNumberEvent(15) , oddState);
	}
	
	@Test
	public void simpleCompositeStateTest() {
		State phoneIdleState = addState(new PhoneIdleState("PhoneIdle"));
		SimpleCompositeState dialingState = addSimpleCompositeState(new DialingState("Dialing"));
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
		
		dialingState.setInitialTransition(new InitialTransition(startDialingSubstate, new InitialTransitionTestEffect()));
		dialingState.addTransition(new Transition(startDialingSubstate, partialDialSubstate, DigitEvent.class));
		dialingState.addTransition(new Transition(partialDialSubstate, partialDialSubstate, DigitEvent.class));
		dialingState.addTransition(new Transition(partialDialSubstate, dialingFinishedSubstate, FinishDialingEvent.class));
		
		setInitialTransitionAndActivate(phoneIdleState);
	
		// PhoneIdle -> Dialing::StartDialing
		sendEventAndCheckCurrentState(new LiftReceiverEvent(), dialingState);
		assertSame(startDialingSubstate, dialingState.getActiveState());
		assertTrue(startDialingSubstate.isDialToneOn());
		
		// Dialing::StartDialing -> Dialing::PartialDial
		sendEventAndCheckCurrentState(new DigitEvent(1) , dialingState);
		assertSame(partialDialSubstate, dialingState.getActiveState());
		assertFalse(startDialingSubstate.isDialToneOn());
		
		// Dialing::PartialDial -> Dialing::PartialDial
		sendEventAndCheckCurrentState(new DigitEvent(2) , dialingState);
		assertSame(partialDialSubstate, dialingState.getActiveState());
		sendEventAndCheckCurrentState(new DigitEvent(3) , dialingState);
		assertSame(partialDialSubstate, dialingState.getActiveState());
		sendEventAndCheckCurrentState(new DigitEvent(4) , dialingState);
		assertSame(partialDialSubstate, dialingState.getActiveState());
		
		// Dialing::PartialDial -> Dialing::DialingFinished
		sendEventAndCheckCurrentState(new FinishDialingEvent() , dialingState);
		assertSame(dialingFinishedSubstate, dialingState.getActiveState());
		
		// Dialing::Finished -> Connecting
		sendEventAndCheckCurrentState(new ConnectEvent() , connectingState);
		
		// Connecting -> PhoneIdle
		sendEventAndCheckCurrentState(new HangUpEvent() , phoneIdleState);
		
		// PhoneIdle -> Dialing::StartDialing
		sendEventAndCheckCurrentState(new LiftReceiverEvent(), dialingState);
		assertSame(startDialingSubstate, dialingState.getActiveState());
		
		// Dialing::PartialDial -> Dialing::PartialDial
		sendEventAndCheckCurrentState(new DigitEvent(2) , dialingState);
		assertSame(partialDialSubstate, dialingState.getActiveState());
		
		// Dialing::PartialDial -> Dialing::DialingFinished
		sendEventAndCheckCurrentState(new FinishDialingEvent() , dialingState);
		assertSame(dialingFinishedSubstate, dialingState.getActiveState());
		
		// Dialing::Finished -> InvalidNumber
		sendEventAndCheckCurrentState(new InvalidNumberEvent() , invalidState);
		
		// InvalidNumber -> PhoneIdle
		sendEventAndCheckCurrentState(new HangUpEvent() , phoneIdleState);
	}

	private State addSubstate(PrimitiveStateMachine compositeState, State substate) {
		compositeState.addState(substate);
		return substate;
	}
	

	private State addState(State state) {
		stateMachine.addState(state);
		return state;
	}
	
	private SimpleCompositeState addSimpleCompositeState(SimpleCompositeState state) {
		return (SimpleCompositeState) addState(state);
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
