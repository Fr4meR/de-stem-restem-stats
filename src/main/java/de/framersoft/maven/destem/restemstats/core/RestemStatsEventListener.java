package de.framersoft.maven.destem.restemstats.core;

public interface RestemStatsEventListener {
	
	public void onVotesLoaded(int deStemVotes, int steemStemVotes);
	
	public void onRestemVotesLoaded(int restemVotes);
	
	public void onDiscussionsLoaded(int discussions);
	
	public void onUniqueAuthorsFound(int uniqueAuthors);
	
	public void onResultReady(RestemStatsResult result);
	
	public void onError(Exception e);
}
