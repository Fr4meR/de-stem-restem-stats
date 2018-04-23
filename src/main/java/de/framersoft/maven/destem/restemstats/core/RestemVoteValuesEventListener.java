package de.framersoft.maven.destem.restemstats.core;
/**
 * event listener for the restem vote values thread
 * @author fr4mer
 *
 */
public interface RestemVoteValuesEventListener {

	/**
	 * gets calles as the total amounts of steps that are done
	 * in the threads is calculated
	 * @param totalSteps
	 * 		the number of total steps
	 */
	public void onTotalStepsCalculated(int totalSteps);
	
	/**
	 * gets calles if one step is done
	 */
	public void onStep();
	
	/**
	 * gets calles if an exception occurs
	 * @param e
	 * 		the exception
	 */
	public void onException(Exception e);
	
	/**
	 * gets calles when the thread is finished
	 * @param voteSmall
	 * 		the value of the small vote
	 * @param voteMedium
	 * 		the value of the medium vote
	 * @param voteBig
	 * 		the value of the big vote
	 */
	public void onFinished(double voteSmall, double voteMedium, double voteBig);
}
