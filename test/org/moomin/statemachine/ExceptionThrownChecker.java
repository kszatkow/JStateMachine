package org.moomin.statemachine;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public abstract class ExceptionThrownChecker {

	private Class<? extends Exception> expectedExceptionType = null;
	
	private String failMessage;
	
	public ExceptionThrownChecker(Class<? extends Exception> expectedExceptionType, String failMessage) {
		this.expectedExceptionType = expectedExceptionType;
		this.failMessage = failMessage;
	}
	
	/* Template method - do action needs to be overridden.
	 * This method check if doAction thrown exception of expectedExceptionType.
	 */
	public void checkExceptionThrownAfterAction() {
		Exception caughtException = null;
		try {
			doAction();
			fail(failMessage);
		} catch (Exception exc) {
			caughtException = exc;
		} finally {
			assertTrue(expectedExceptionType.isInstance(caughtException));
		}
	}
	
	protected abstract void doAction();

}