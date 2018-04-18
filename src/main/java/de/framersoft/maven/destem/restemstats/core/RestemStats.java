package de.framersoft.maven.destem.restemstats.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.bittrade.libs.steemj.SteemJ;
import eu.bittrade.libs.steemj.apis.database.models.state.Discussion;
import eu.bittrade.libs.steemj.base.models.AccountName;
import eu.bittrade.libs.steemj.base.models.AccountVote;
import eu.bittrade.libs.steemj.base.models.Permlink;
import eu.bittrade.libs.steemj.base.models.TimePointSec;
import eu.bittrade.libs.steemj.exceptions.SteemCommunicationException;
import eu.bittrade.libs.steemj.exceptions.SteemResponseException;
/**
 * this class is used to get the restem stats  
 * @author fr4mer
 * @since 18.04.2018
 */
public class RestemStats {

	/**
	 * api object to make calls to the Steem-API
	 */
	private SteemJ steem;
	
	/**
	 * Constructor
	 * @param steem
	 * 		the object to make calls to the steem api with
	 */
	public RestemStats(SteemJ steem) {
		this.steem = steem;
	}
	
	/**
	 * gets the restemstats from the start date to the end date
	 * @param start
	 * 		the start date (inclusive)
	 * @param end
	 * 		the end date (exclusive)
	 * @return
	 * 		result object containing the gathered data
	 * @throws SteemCommunicationException
	 * @throws SteemResponseException
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	public RestemStatsResult requestRestemStats(Date start, Date end) throws SteemCommunicationException, SteemResponseException, JsonParseException, JsonMappingException, IOException {
		List<AccountVote> deStemVotes = getAccountVotesBetween(new AccountName("de-stem"), start, end);
        List<AccountVote> steemStemVotes = getAccountVotesBetween(new AccountName("steemstem"), start, end);
        
        //check if the votes from de-stem got voted AFTERWARDS by steemstem
        List<AccountVote> reSteemVotes = new ArrayList<AccountVote>();
        for(AccountVote deStemVote : deStemVotes) {
        	//get same post voted by steemstem
        	AccountVote steemStemVote = findAccountVoteByAuthorPerm(deStemVote.getAuthorperm(), steemStemVotes);
        	
        	//skip the vote if not voted by steemStem
        	if(steemStemVote == null) continue;
        	
        	//check if the steemStemVote was after the deStemVote
        	if(steemStemVote.getTime().getDateTimeAsTimestamp() > deStemVote.getTime().getDateTimeAsTimestamp()) {
        		reSteemVotes.add(steemStemVote);
        	}
        }
        
        //now we have all votes from de-stem that were voted afterwards by steemstem
        //we need to determine if those votes were for posts with de-stem tag
        //and if they are actual blog posts and not comments..
        //.. so we load the "Discussions" for the votes
        List<Discussion> discussions = new ArrayList<Discussion>();
        for(AccountVote vote : reSteemVotes) {
        	AccountName author = getAccountNameFromAuthPerm(vote.getAuthorperm());
        	Permlink permLink = getPermLinkFromAuthPerm(vote.getAuthorperm());
        	
        	Discussion disc = steem.getContent(author, permLink);
        	
        	//blog posts are always on depth 0, so skip others
        	if(disc.getDepth() > 0) continue;
        	
        	//skip all posts that don't use the de-stem tag
        	if(!usesDeStemTag(disc)) continue;
        	
        	discussions.add(disc);
        }
        
        //build list of unique accounts from the discussions
        List<String> uniqueAuthors = getUniqueAuthors(discussions);
        
        //calculate the mean posts per day
        long diff = start.getTime() - end.getTime();
        long days = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
        double meanPostsPerDay = discussions.size() / (double) days;
        
        //store all the gathered data in the result object and return it
        RestemStatsResult result = new RestemStatsResult();
        result.setDiscussions(discussions);
        result.setUniqueAuthors(uniqueAuthors);
        result.setMeanPostsPerDay(meanPostsPerDay);
        return result;
	}
	
	/**
	 * Gets all the votes between the given start and end date of the given
	 * account.
	 * @param accName
	 * 		account name
	 * @param start
	 * 		start date
	 * @param end
	 * 		end date
	 * @return
	 * 		List of votes between start and end date for the given account.
	 * @throws SteemCommunicationException
	 * @throws SteemResponseException
	 */
	 private List<AccountVote> getAccountVotesBetween(AccountName accName, Date start, Date end) throws SteemCommunicationException, SteemResponseException {
    	//get all the votes of the given account
	 	List<AccountVote> votes = steem.getAccountVotes(accName);
    	
	 	//filter for votes between start and end date
    	List<AccountVote> votesBetween = new ArrayList<AccountVote>();
    	for(AccountVote vote : votes) {
    		TimePointSec voteTime = vote.getTime();
    		if(voteTime.getDateTimeAsTimestamp() > start.getTime() && voteTime.getDateTimeAsTimestamp() < end.getTime()) {
    			votesBetween.add(vote);
    		}
    	}
    	return votesBetween;
    }
	 
	 /**
	  * Finds a vote with the given authorperm.
	  * @param authorPerm
	  * 	the authorperm to search for
	  * @param votes
	  * 	the collection of votes to search in
	  * @return
	  * 	if the vote is found it will be returned, if not
	  * 	the method will return <code>null</code>
	  */
	 private AccountVote findAccountVoteByAuthorPerm(String authorPerm, Collection<AccountVote> votes) {
    	for(AccountVote vote : votes) {
    		if(vote.getAuthorperm().equals(authorPerm)) {
    			return vote;
    		}
    	}
    	return null;
	 }
	 
	 /**
	  * Extracts the author name from an authPerm string.
	  * @param authPerm
	  * 	the authperm string to extract the author name from.
	  * @return
	  * 	the autor name
	  */
	 private AccountName getAccountNameFromAuthPerm(String authPerm) {
    	String[] splits = authPerm.split("/");
    	return new AccountName(splits[0]);
    }
	
	/**
	 * extracts the permlink part from an authperm string
	 * @param authPerm
	 * 		the authperm string to get the permlink part from
	 * @return
	 * 		the permlink
	 */
    private Permlink getPermLinkFromAuthPerm(String authPerm) {
    	String[] splits = authPerm.split("/");
    	
    	StringJoiner permLinkString = new StringJoiner("/");
    	for(int i = 1; i < splits.length; i++) {
    		permLinkString.add(splits[i]);
    	}
    	
    	return new Permlink(permLinkString.toString());
    }
    
    /**
     * Gets the Set of tags used for a discusscion (post)
     * @param disc
     * 		the discussion to get the tags for
     * @return
     * 		a set containing all the tags used
     * @throws JsonParseException
     * @throws JsonMappingException
     * @throws IOException
     */
    private Set<String> getTagsFromDiscussion(Discussion disc) throws JsonParseException, JsonMappingException, IOException{
    	Set<String> tags = new HashSet<String>();
    	
    	//use jackson to extract the tags from the meta data of the discussion
    	//SteemJ currently doesn't support this directly..
    	ObjectMapper mapper = new ObjectMapper();
    	JsonNode jsonMeta = mapper.readValue(disc.getJsonMetadata(), JsonNode.class);
    	JsonNode jsonTags = jsonMeta.get("tags");
    	
    	if(jsonTags.isArray()) {
    	Iterator<JsonNode> tagIterator = jsonTags.elements();
    		while(tagIterator.hasNext()) {
    			JsonNode tag = tagIterator.next();
    			tags.add(tag.asText());
    		}
    	}
    	
    	return tags;
    }
    
    /**
     * Checks if the given discussion uses the tag "de-stem"
     * @param disc
     * 		the discussion to check if it uses the tag "de-stem"
     * @return
     * 		<code>true</code> if the discussion uses the tag "de-stem".
     * 		<code>false</code> if the tag isn't used.
     * @throws JsonParseException
     * @throws JsonMappingException
     * @throws IOException
     */
    private boolean usesDeStemTag(Discussion disc) throws JsonParseException, JsonMappingException, IOException {
    	Set<String> tags = getTagsFromDiscussion(disc);
    	return tags.contains("de-stem");
    }
    
    /**
     * Creates a list of unique users from a collection of discussions.
     * @param discussions
     * 		the collection of discussions
     * @return
     * 		A lexicographically sorted list of unique user names.
     */
    private List<String> getUniqueAuthors(Collection<Discussion> discussions){
    	//filter for unique authers
    	List<String> uniqueAuthors = new ArrayList<String>();
    	for(Discussion disc : discussions) {
        	if(!uniqueAuthors.contains(disc.getAuthor().getName())) {
        		uniqueAuthors.add(disc.getAuthor().getName());
        	}
        }
    	
    	//sort the list
    	uniqueAuthors.sort(new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				return o1.compareTo(o2);
			}
		});
    	
    	return uniqueAuthors;
    }
}
