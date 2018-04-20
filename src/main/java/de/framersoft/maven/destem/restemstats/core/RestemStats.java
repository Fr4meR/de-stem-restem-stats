package de.framersoft.maven.destem.restemstats.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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
import eu.bittrade.libs.steemj.base.models.ExtendedAccount;
import eu.bittrade.libs.steemj.base.models.Permlink;
import eu.bittrade.libs.steemj.base.models.Price;
import eu.bittrade.libs.steemj.base.models.RewardFund;
import eu.bittrade.libs.steemj.base.models.TimePointSec;
import eu.bittrade.libs.steemj.enums.RewardFundType;
import eu.bittrade.libs.steemj.exceptions.SteemCommunicationException;
import eu.bittrade.libs.steemj.exceptions.SteemResponseException;
/**
 * this class is used to get the restem stats  
 * @author fr4mer
 * @since 18.04.2018
 */
public class RestemStats extends Thread{

	/**
	 * api object to make calls to the Steem-API
	 */
	private SteemJ steem;
	
	private Date start;
	private Date end;
	
	private List<RestemStatsEventListener> eventListeners = new ArrayList<RestemStatsEventListener>();
	
	/**
	 * Constructor
	 * @param steem
	 * 		the object to make calls to the steem api with
	 */
	public RestemStats(SteemJ steem, Date start, Date end) {
		this.steem = steem;
		this.start = start;
		this.end = end;
	}

	@Override
	public void run(){
		try {
			List<AccountVote> deStemVotes = getAccountVotesBetween(new AccountName("de-stem"), start, end);
	        List<AccountVote> steemStemVotes = getAccountVotesBetween(new AccountName("steemstem"), start, end);
	        
	        for(RestemStatsEventListener listener : eventListeners) {
	        	listener.onVotesLoaded(deStemVotes.size(), steemStemVotes.size());
	        }
	        
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
	        
	        for(RestemStatsEventListener listener : eventListeners) {
	        	listener.onRestemVotesLoaded(reSteemVotes.size());
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
	        
	        for(RestemStatsEventListener listener : eventListeners) {
	        	listener.onDiscussionsLoaded(discussions.size());
	        }
	        
	        //build list of unique accounts from the discussions
	        List<String> uniqueAuthors = getUniqueAuthors(discussions);
	        
	        for(RestemStatsEventListener listener : eventListeners) {
	        	listener.onUniqueAuthorsFound(uniqueAuthors.size());
	        }
	        
	        //calculate the mean posts per day
	        long diff = end.getTime() - start.getTime();
	        long days = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
	        double meanPostsPerDay = discussions.size() / (double) days;
	
	        System.out.println(calculateVoteWorth(new AccountName("de-stem"), 1, 0.9));
	        System.out.println(calculateVoteWorth(new AccountName("steemstem"), 1, 1));
	        System.out.println(calculateVoteWorth(new AccountName("curie"), 1, 1));
	        System.out.println(calculateVoteWorth(new AccountName("sco"), 1, 1));
	        
	        
	        //store all the gathered data in the result object and publish it in the callbacks
	        RestemStatsResult result = new RestemStatsResult();
	        result.setDiscussions(discussions);
	        result.setUniqueAuthors(uniqueAuthors);
	        result.setMeanPostsPerDay(meanPostsPerDay);
	        for(RestemStatsEventListener listener : eventListeners) {
	        	listener.onResultReady(result);
	        }
		}
		catch(Exception e) {
			for(RestemStatsEventListener listener : eventListeners) {
	        	listener.onError(e);
	        }
		}
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
    
    public void addListener(RestemStatsEventListener listener) {
    	eventListeners.add(listener);
    }
    
    /**
     * Calculates the worth (rounded to 2 decimal places) of a vote for an account.
     * @param account
     * 		the account
     * @param votePower
     * 		the voting power of the account
     * @param voteWeight
     * 		the weight of the vote
     * @return
     * 		the worth of the vote
     * @throws SteemCommunicationException
     * @throws SteemResponseException
     * @see https://steemit.com/steem/@yabapmatt/how-to-calculate-the-value-of-a-vote
     */
    private double calculateVoteWorth(AccountName account, double votePower, double voteWeight) throws SteemCommunicationException, SteemResponseException {
    	double voteWorth = 0.0;
    	
    	List<ExtendedAccount> accounts = steem.getAccounts(Arrays.asList(account));
        RewardFund rewardFund = steem.getRewardFund(RewardFundType.POST);
        Price price = steem.getCurrentMedianHistoryPrice();
        if(accounts.size() == 1) {
        	ExtendedAccount deStem = accounts.get(0);
        	double vestingShares = deStem.getVestingShares().getAmount();
        	double receivedShares = deStem.getReceivedVestingShares().getAmount();
        	double delegatedShares = deStem.getDelegatedVestingShares().getAmount();
        	
        	double vestsTotal = vestingShares + receivedShares - delegatedShares;

        	
        	double recentClaims = rewardFund.getRecentClaims().doubleValue();
        	double x1 = vestsTotal / recentClaims;
        	double rewardPoolBalance = rewardFund.getRewardBalance().getAmount();
        	double x2 = x1 * rewardPoolBalance / 1000;
        	double sbdPerSteem = (double) price.getBase().getAmount() / (double) price.getQuote().getAmount();
        	double x3 = x2 * sbdPerSteem;
        	voteWorth = x3 * 2 / 100 * votePower * voteWeight;
        	voteWorth = Math.round(voteWorth * 100.0) / 100.0; 
        }
        
        return voteWorth;
    }
}
