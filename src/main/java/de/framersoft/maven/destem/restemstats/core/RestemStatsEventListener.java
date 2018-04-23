package de.framersoft.maven.destem.restemstats.core;
/**
 * event listener for the RestemStats Thread
 * @author fr4mer
 */
public interface RestemStatsEventListener {
	
	/**
	 * gets called when the votes are loaded
	 * @param deStemVotes
	 * 		the number of de-stem
	 * @param steemStemVotes
	 * 		the number of steemstem votes
	 */
	public void onVotesLoaded(int deStemVotes, int steemStemVotes);
	
	/**
	 * gets calles when the restem votes are loaded
	 * @param restemVotes
	 * 		the number of restem votes
	 */
	public void onRestemVotesLoaded(int restemVotes);
	
	/**
	 * gets called when the restem-discussions are loaded
	 * @param discussions
	 * 		the number of restem-discussions
	 */
	public void onDiscussionsLoaded(int discussions);
	
	/**
	 * gets called when the unique authors are loaded
	 * @param uniqueAuthors
	 * 		the number of unique authors
	 */
	public void onUniqueAuthorsFound(int uniqueAuthors);
	
	/**
	 * gets calles when the result is ready
	 * @param result
	 * 		the result object
	 */
	public void onResultReady(RestemStatsResult result);
	
	/**
	 * gets calles when an error occurs
	 * @param e
	 * 		the exception that occured
	 */
	public void onException(Exception e);
}
