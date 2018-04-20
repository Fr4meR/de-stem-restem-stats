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

	private double smallVoteWorth;
	private double mediumVoteWorth;
	private double bigVoteWorth;
	
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

	/**
	 * @return the smallVoteWorth
	 */
	public double getSmallVoteWorth() {
		return smallVoteWorth;
	}

	/**
	 * @param smallVoteWorth the smallVoteWorth to set
	 */
	public void setSmallVoteWorth(double smallVoteWorth) {
		this.smallVoteWorth = smallVoteWorth;
	}

	/**
	 * @return the mediumVoteWorth
	 */
	public double getMediumVoteWorth() {
		return mediumVoteWorth;
	}

	/**
	 * @param mediumVoteWorth the mediumVoteWorth to set
	 */
	public void setMediumVoteWorth(double mediumVoteWorth) {
		this.mediumVoteWorth = mediumVoteWorth;
	}

	/**
	 * @return the bigVoteWorth
	 */
	public double getBigVoteWorth() {
		return bigVoteWorth;
	}

	/**
	 * @param bigVoteWorth the bigVoteWorth to set
	 */
	public void setBigVoteWorth(double bigVoteWorth) {
		this.bigVoteWorth = bigVoteWorth;
	}
}
