package de.framersoft.maven.destem.restemstats.core;

public interface RestemVoteValuesEventListener {

	public void onTotalStepsCalculated(int totalSteps);
	
	public void onStep();
	
	public void onError(Exception e);
	
	public void onFinished(double voteSmall, double voteMedium, double voteBig);
}
