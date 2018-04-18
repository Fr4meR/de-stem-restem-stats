package de.framersoft.maven.destem.restemstats.core;

import java.util.List;

import eu.bittrade.libs.steemj.apis.database.models.state.Discussion;
/**
 * Result object containing the restemstats data.
 * @author fr4mer
 * @since 18.04.2018
 */
public class RestemStatsResult {

	/**
	 * the discussions
	 */
	private List<Discussion> discussions;
	
	/**
	 * list of unique authors 
	 */
	private List<String> uniqueAuthors;
	
	/**
	 * mean posts per day
	 */
	private double meanPostsPerDay;

	/**
	 * @return the discussions
	 */
	public List<Discussion> getDiscussions() {
		return discussions;
	}

	/**
	 * @param discussions the discussions to set
	 */
	public void setDiscussions(List<Discussion> discussions) {
		this.discussions = discussions;
	}

	/**
	 * @return the uniqueAuthors
	 */
	public List<String> getUniqueAuthors() {
		return uniqueAuthors;
	}

	/**
	 * @param uniqueAuthors the uniqueAuthors to set
	 */
	public void setUniqueAuthors(List<String> uniqueAuthors) {
		this.uniqueAuthors = uniqueAuthors;
	}

	/**
	 * @return the meanPostsPerDay
	 */
	public double getMeanPostsPerDay() {
		return meanPostsPerDay;
	}

	/**
	 * @param meanPostsPerDay the meanPostsPerDay to set
	 */
	public void setMeanPostsPerDay(double meanPostsPerDay) {
		this.meanPostsPerDay = meanPostsPerDay;
	}
	
	/**
	 * @return
	 * 		the number of posts
	 */
	public int getNumberOfPosts() {
		return discussions.size();
	}
}
