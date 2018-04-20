package de.framersoft.maven.destem.restemstats.core;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import eu.bittrade.libs.steemj.SteemJ;
import eu.bittrade.libs.steemj.base.models.AccountName;
import eu.bittrade.libs.steemj.base.models.ExtendedAccount;
import eu.bittrade.libs.steemj.base.models.Price;
import eu.bittrade.libs.steemj.base.models.RewardFund;
import eu.bittrade.libs.steemj.enums.RewardFundType;
import eu.bittrade.libs.steemj.exceptions.SteemCommunicationException;
import eu.bittrade.libs.steemj.exceptions.SteemResponseException;

public class RestemVoteValues extends Thread {
	/**
	 * api object to make calls to the Steem-API
	 */
	private SteemJ steem;
	
	private List<RestemVoteValuesEventListener> eventListeners = new ArrayList<RestemVoteValuesEventListener>();
	
	private RewardFund rewardFund;
	
	private Price currentMedianHistoryPrice;
	
	/**
	 * constructor
	 * @param steem
	 * 		api object
	 */
	public RestemVoteValues(SteemJ steem) {
		this.steem = steem;
	}
	
	@Override
	public void run() {
		try {			
			Map<String, Double> voteValues = getVotingValues();
			
			double voteSmall = calculateTotalVoteValue("data/voting-weights-small.csv", "data/curation-trail.csv", voteValues);
			double voteMedium = calculateTotalVoteValue("data/voting-weights-medium.csv", "data/curation-trail.csv", voteValues);
			double voteBig = calculateTotalVoteValue("data/voting-weights-big.csv", "data/curation-trail.csv", voteValues);
			
			for(RestemVoteValuesEventListener listener : eventListeners) {
				listener.onFinished(voteSmall, voteMedium, voteBig);
			}
		} catch (IOException | SteemCommunicationException | SteemResponseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
     * Calculates the worth (rounded to 2 decimal places) of a vote for an account.
     * @param account
     * 		the account
     * @param useCurrentVotingPower
     * 		use the current voting power of the account?
     * @param voteWeight
     * 		the weight of the vote
     * @return
     * 		the worth of the vote
     * @throws SteemCommunicationException
     * @throws SteemResponseException
     * @see https://steemit.com/steem/@yabapmatt/how-to-calculate-the-value-of-a-vote
     */
    private double calculateVoteWorth(AccountName account, boolean useCurrentVotingPower, double voteWeight) throws SteemCommunicationException, SteemResponseException {
    	double voteWorth = 0.0;
    	
    	List<ExtendedAccount> accounts = steem.getAccounts(Arrays.asList(account));
        RewardFund rewardFund = getRewardFund();
        Price price = getCurrentMedianHistoryPrice();
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
        	voteWorth = x3 * 2 / 100 * voteWeight;
        	if(useCurrentVotingPower) {
        		voteWorth *= (deStem.getVotingPower() / 10000.0);
        	}
        	voteWorth = Math.round(voteWorth * 100.0) / 100.0; 
        }
        
        return voteWorth;
    }
    
    
    private double calculateTotalVoteValue(String mainAccountVotingDataFile, String curationTrailDataFile, Map<String, Double> votingValues) throws FileNotFoundException, IOException {
        Map<String, Double> votingAccounts = new HashMap<String, Double>();
        
        //main accounts
        try(BufferedReader br = new BufferedReader(new FileReader(mainAccountVotingDataFile))){
        	String line;
        	boolean skipFirstLine = true;
        	while((line = br.readLine()) != null) {
        		if(skipFirstLine) {
        			skipFirstLine = false;
        			continue;
        		}
        		
        		String[] splits = line.split(";");
        		votingAccounts.put(splits[0], Double.parseDouble(splits[1]));
        	}
        }
        
        //curation trail accounts
        try(BufferedReader br = new BufferedReader(new FileReader(curationTrailDataFile))){
        	String line;
        	boolean skipFirstLine = true;
        	while((line = br.readLine()) != null) {
        		if(skipFirstLine) {
        			skipFirstLine = false;
        			continue;
        		}
        		
        		String[] splits = line.split(";");
        		
        		if(votingAccounts.containsKey(splits[2])) {
        			votingAccounts.put(splits[0], Double.parseDouble(splits[1]) * votingAccounts.get(splits[2]));
        		}
        	}
        }
        
        	
        double sum = 0.0;
        for(String accountName : votingAccounts.keySet()) {
        	double voteValue100 = votingValues.get(accountName);
        	double votingWeight = votingAccounts.get(accountName);
        	
        	double voteWorth = votingWeight * voteValue100;
        	sum += voteWorth;
        }
        return sum;
    }
    
    public void addEventListener(RestemVoteValuesEventListener listener) {
    	eventListeners.add(listener);
    }
    
    private RewardFund getRewardFund() throws SteemCommunicationException, SteemResponseException {
    	if(rewardFund == null) {
    		rewardFund = steem.getRewardFund(RewardFundType.POST);
    	}
    	
    	return rewardFund;
    }
    
    private Price getCurrentMedianHistoryPrice() throws SteemCommunicationException, SteemResponseException {
    	if(currentMedianHistoryPrice == null) {
    		currentMedianHistoryPrice = steem.getCurrentMedianHistoryPrice();
    	}
    	
    	return currentMedianHistoryPrice;
    }
    
    private Map<String, Double> getVotingValues() throws FileNotFoundException, IOException, SteemCommunicationException, SteemResponseException {
    	Set<String> accountNames = getAllAccountNamesFromFiles();
    	
    	for(RestemVoteValuesEventListener listener : eventListeners) {
    		listener.onTotalStepsCalculated(accountNames.size());
    	}
    	
    	//load 100% votes for the accounts
    	Map<String, Double> votingWorths = new HashMap<String, Double>();
    	for(String accountName : accountNames) {
    		double voteValues = calculateVoteWorth(new AccountName(accountName), true, 1);
    		votingWorths.put(accountName, voteValues);
    		
    		for(RestemVoteValuesEventListener listener : eventListeners) {
        		listener.onStep();
        	}
    	}
    	
    	return votingWorths;
    }
    
    private Set<String> getAllAccountNamesFromFiles() throws FileNotFoundException, IOException{
    	Set<String> accountNames = new HashSet<String>();
    	
    	//main accounts
    	Set<String> mainAccountFiles = new HashSet<String>();
    	mainAccountFiles.add("data/voting-weights-small.csv");
    	mainAccountFiles.add("data/voting-weights-medium.csv");
    	mainAccountFiles.add("data/voting-weights-big.csv");
    	
    	for(String file : mainAccountFiles) {
	        try(BufferedReader br = new BufferedReader(new FileReader(file))){
	        	String line;
	        	boolean skipFirstLine = true;
	        	while((line = br.readLine()) != null) {
	        		if(skipFirstLine) {
	        			skipFirstLine = false;
	        			continue;
	        		}
	        		
	        		String[] splits = line.split(";");
	        		accountNames.add(splits[0]);
	        	}
	        }
    	}
    	
    	//curation trail accounts
    	try(BufferedReader br = new BufferedReader(new FileReader("data/curation-trail.csv"))){
        	String line;
        	boolean skipFirstLine = true;
        	while((line = br.readLine()) != null) {
        		if(skipFirstLine) {
        			skipFirstLine = false;
        			continue;
        		}
        		
        		String[] splits = line.split(";");
        		accountNames.add(splits[0]);
        	}
        }
    	
    	return accountNames;
    }
}
