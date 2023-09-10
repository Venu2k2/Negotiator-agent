
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.HashMap;
// import java.util.Vector;
import java.util.List;
import java.lang.Math;
import genius.core.issue.Value;
import genius.core.AgentID;
import genius.core.Bid;
import genius.core.actions.Accept;
import genius.core.actions.Action;
import genius.core.actions.Offer;
import genius.core.parties.AbstractNegotiationParty;
import genius.core.parties.NegotiationInfo;
import genius.core.boaframework.OutcomeSpace;
import genius.core.bidding.BidDetails;
// import genius.core.misc.Range;

/**
 * This is your negotiation party.
 */

public class Group5_Agent extends AbstractNegotiationParty {

	private Bid lastReceivedBid = null;
	private List<BidDetails> bids;

	// private int index = 1 ;
	class Opponents {
		Map<Integer, Map<String, Double>> m = new HashMap<>();
		Map<String, Double> iWeights = new HashMap<>();
		Map<Integer, Double> weights = new HashMap<>();
		double bestOffered = 0;

		public Double getUtil(Bid p) {
			Double x = 0.0;
			HashMap<Integer, Value> h = p.getValues();
			if (iWeights.get(h.get(1).toString()) != null && (weights.get(1) != null)) {
				x = x + iWeights.get(h.get(1).toString()) * weights.get(1);
			}
			if (iWeights.get(h.get(2).toString()) != null && (weights.get(2) != null)) {
				x = x + iWeights.get(h.get(2).toString()) * weights.get(2);
			}
			if (iWeights.get(h.get(3).toString()) != null && (weights.get(3) != null)) {
				x = x + iWeights.get(h.get(3).toString()) * weights.get(3);
			}
			if (iWeights.get(h.get(4).toString()) != null && (weights.get(4) != null)) {
				x = x + iWeights.get(h.get(4).toString()) * weights.get(4);
			}
			return x;
		}

		public void updateweights(Bid p) {
			HashMap<Integer, Value> h = p.getValues();
			for (int i = 0; i < 4; i++) {
				if (m.get(i + 1) != null) {
					Map<String, Double> m1 = m.get(i + 1);
					if (m1.get(h.get(i + 1).toString()) != null) {
						m1.put(h.get(i + 1).toString(), m1.get(h.get(i + 1).toString()) + 1.0);
					} else {
						m1.put(h.get(i + 1).toString(), 1.0);
					}
					m.put(i + 1, m1);
				} else {
					Map<String, Double> m1 = new HashMap<>();
					m1.put(h.get(i + 1).toString(), 1.0);
					m.put(i + 1, m1);
				}
			}
			//System.out.println("YES");
			//System.out.println(m);
			//System.out.println("YES");
			for (int i = 0; i < 4; i++) {
				double sum = 0.0;
				double max = 0.0;
				double cnt = 0;
				Map<String, Double> v = m.get(i + 1);
				for (String name : v.keySet()) {
					sum = sum + v.get(name);
					cnt = cnt + 1;
					if (max <= v.get(name)) {
						max = v.get(name);
					}
				}
				for (String name : v.keySet()) {
					// if (v.get(name) == max) {
					// 	iWeights.put(name, 1.0);
					// } else {
					// 	iWeights.put(name, v.get(name) / (sum - max));
					// }
					iWeights.put(name,v.get(name)/sum);
				}
				m.put(i + 1, v);
			}
			//System.out.println("iweights");
			//System.out.println(iWeights);
			//System.out.println("iweights");
			Map<Integer, Double> values = new HashMap<>();
			for (int i = 0; i < 4; i++) {
				Map<String, Double> v = m.get(i + 1);
				Double sum = 0.0;
				Double value = 0.0;
				for (String name : v.keySet()) {
					sum = sum + v.get(name);
					value = value + v.get(name) * iWeights.get(name);
				}
				values.put(i + 1, value);
			}
			//System.out.println("NO");
			//System.out.println(values);
			//System.out.println("NO");
			Map<Integer, Double> ranks = new HashMap<>();
			for (int i = 1; i < 5; i++) {
				for (int j = 1; j < 5; j++) {
					if (i ==j)
					{
						continue;
					}
					if (values.get(i)/values.get(j) > 1.01) {
						if(ranks.get(j) == null)
						{
							ranks.put(j,0.0);
						}
						continue;
					} else {
						if (values.get(i)/values.get(j) < 0.99) {
							if (ranks.get(j) != null) {
								ranks.put(j, ranks.get(j) + 1);
							} else {
								ranks.put(j, 1.0);
							}
							if(ranks.get(i) == null)
							{
								ranks.put(i,0.0);
							}

						} else {
							if (ranks.get(j) != null) {
								ranks.put(j, ranks.get(j) + 0.5);
							} else {
								ranks.put(j, 0.5);
							}
						}
					}
				}
			}
			//System.out.println("ranks");
			//System.out.println(ranks);
			//System.out.println("ranks");
			for (int i = 1; i < 5; i++) {
				Double x = 0.125;
				//if (ranks.get(i) != null)
				//System.out.println(ranks.toString());
				Double val = x + (ranks.get(i)) / (4 * 3);
				weights.put(i, val);
			}
			//System.out.println("weights");
			//System.out.println(weights);
			//System.out.println("weights");
		}
	}

	public Map<AgentID, Opponents> agents;

	@Override
	public void init(NegotiationInfo info) {

		super.init(info);

		System.out.println("Discount Factor is " + getUtilitySpace().getDiscountFactor());
		System.out.println("Reservation Value is " + getUtilitySpace().getReservationValueUndiscounted());
		OutcomeSpace outcomeSpace = new OutcomeSpace(utilitySpace);
		bids = outcomeSpace.getAllOutcomes();
		Collections.sort(bids, new Comparator<BidDetails>() {
			public int compare(BidDetails o1, BidDetails o2) {
				return Double.compare(o2.getMyUndiscountedUtil(), o1.getMyUndiscountedUtil());
			}
		});
		agents = new HashMap<>();
		// System.out.println(bids.toString()) ;
	}

	@Override
	public Action chooseAction(List<Class<? extends Action>> validActions) {

		// BidDetails curr = null;
		double time = getTimeLine().getTime();

		if (time < 0.1) {
			System.out.println("0.1");
			if (lastReceivedBid != null) {
				if (agents.get(getPartyId()) == null) {
					Opponents b = new Opponents();
					agents.put(getPartyId(), b);
					// System.out.println(getPartyId());
					// System.out.println(agents.toString());
					agents.get(getPartyId()).updateweights(lastReceivedBid);
				} else {
					agents.get(getPartyId()).updateweights(lastReceivedBid);
				}
				// return new Accept(getPartyId(),lastReceivedBid);
				if (agents.get(getPartyId()).bestOffered < getUtility(lastReceivedBid)) {
					//System.out.println(getUtility(lastReceivedBid));
					
					
					//System.out.println(getUtility(lastReceivedBid));
					agents.get(getPartyId()).bestOffered = getUtility(lastReceivedBid);
				}
				Bid offer = bids.get(0).getBid();
				return new Offer(getPartyId(), offer);
			} else {
				Bid offer = bids.get(0).getBid();
				return new Offer(getPartyId(), offer);
			}
		} else if (time < 0.5) {
			System.out.println("0.5");
			if (lastReceivedBid != null) {
				if (agents.get(getPartyId()) == null) {
					Opponents b = new Opponents();
					agents.put(getPartyId(), b);
					agents.get(getPartyId()).updateweights(lastReceivedBid);
				} else {
					agents.get(getPartyId()).updateweights(lastReceivedBid);
				}
				//System.out.println(getUtility(lastReceivedBid));
				if (agents.get(getPartyId()).bestOffered < getUtility(lastReceivedBid)) {
					agents.get(getPartyId()).bestOffered = getUtility(lastReceivedBid);
					//System.out.println(getUtility(lastReceivedBid));
				}
				double val = 0.98 - 0.05 * time * time;
				int index = 0;
				for (int i = 0; i < bids.size(); i++) {
					Bid p = bids.get(i).getBid();
					if (val > getUtility(p)) {
						index = i - 1;
						break;
					}
				}
				Bid offer = bids.get(index).getBid();
				return new Offer(getPartyId(), offer);
			}
		} else if (time < 0.9) {
			System.out.println("0.9");
			if (agents.get(getPartyId()) == null) {
				Opponents b = new Opponents();
				agents.put(getPartyId(), b);
				agents.get(getPartyId()).updateweights(lastReceivedBid);
			} else {
				agents.get(getPartyId()).updateweights(lastReceivedBid);
			}
			// System.out.println(agents.get(getPartyId()));
			if (agents.get(getPartyId()).bestOffered < getUtility(lastReceivedBid)) {
				agents.get(getPartyId()).bestOffered = getUtility(lastReceivedBid);
				//System.out.println(getUtility(lastReceivedBid));
			}
			double best = agents.get(getPartyId()).bestOffered;
			double maxi = 0.4;
			if (maxi < best) {
				maxi = best;
			}
			double q = 0;
			if (maxi != 1) {
				System.out.println("Maxi = " + maxi );
				q = 1 + 2 * ((maxi - 0.4) / (1 - maxi));
				
			}
			if (q != 0) {
				q = 1 / q;
			}
			System.out.println("Maxi = " + maxi + "Q = " + q);
			double blah = 0.4;
			if (blah < best)
			{
				blah = best;
			}
			double exp = q * best + (1 - q) * (0.5+(blah/2));			
			System.out.println("Time =" +time*60 +"best = " + best);
			double val = exp + (1 - exp) * (1 - time*time);
			if (val < getUtility(lastReceivedBid)) {
				return new Accept(getPartyId(), lastReceivedBid);
			} else {
				Bid p = bids.get(0).getBid();
				Bid p1 = bids.get(0).getBid();
				double maximum = 0;
				for (int i = 0; i < bids.size(); i++) {
					double max = 0;
					max = max + (2 - 0.2 * time * time) * getUtility(bids.get(i).getBid());
					for (AgentID x : agents.keySet()) {
						max = max + agents.get(x).getUtil(bids.get(i).getBid());
					}
					if (maximum < max) {
						p = bids.get(i).getBid();
						maximum = max;
					}
					Bid x = bids.get(i).getBid();
					if (getUtility(x) < 0.98 - 0.05 * time * time) {
						p1 = bids.get(i - 1).getBid();
					}
					if (getUtility(p) < getUtility(p1)) {
						p = p1;
					}
				}
				return new Offer(getPartyId(), p);
			}
		} else {
			System.out.println("else");
			if (time >= 0.98) {
				System.out.println("0.98");
				if (agents.get(getPartyId()) == null) {
					Opponents b = new Opponents();
					agents.put(getPartyId(), b);
					agents.get(getPartyId()).updateweights(lastReceivedBid);
				} else {
					agents.get(getPartyId()).updateweights(lastReceivedBid);
				}
				if (agents.get(getPartyId()).bestOffered < getUtility(lastReceivedBid)) {
					agents.get(getPartyId()).bestOffered = getUtility(lastReceivedBid);
					
				}
				if (0.4 <= getUtility(lastReceivedBid)) {
					new Accept(getPartyId(), lastReceivedBid);
				} else {
					Bid p = bids.get(0).getBid();
					double maximum = 0;
					for (int i = 0; i < bids.size(); i++) {
						double max = 0;
						max = max + (2 - 0.2 * time * time) * getUtility(bids.get(i).getBid());
						for (AgentID x : agents.keySet()) {
							System.out.println("Last bids values : " + agents.get(x).getUtil(bids.get(i).getBid()));
							max = max + agents.get(x).getUtil(bids.get(i).getBid());
						}
						if (maximum < max) {
							p = bids.get(i).getBid();
							maximum = max;
							System.out.println("Final max joint = " + max);
						}
					}
					System.out.println("Maximum = " + maximum);
					return new Offer(getPartyId(), p);
				}
				
			}
			if (agents.get(getPartyId()) == null) {
				Opponents b = new Opponents();
				agents.put(getPartyId(), b);
				agents.get(getPartyId()).updateweights(lastReceivedBid);
			} else {
				agents.get(getPartyId()).updateweights(lastReceivedBid);
			}
			if (agents.get(getPartyId()).bestOffered < getUtility(lastReceivedBid)) {
				agents.get(getPartyId()).bestOffered = getUtility(lastReceivedBid);
			}
			double best = agents.get(getPartyId()).bestOffered;
			double maxi = 0.4;
			if (maxi < best) {
				maxi = best;
			}
			double q = 0;
			System.out.println(q);
			System.out.println("best=" + best);
			if (maxi != 1) {
				q = 1 + 2 * ((maxi - 0.4) / (1 - maxi));
			}
			if (q != 0) {
				q = 1 / q;
			}
			double blah = 0.4;
			if (blah < best)
			{
				blah = best;
			}
			double exp = q * blah + (1 - q) * (0.5+(blah/2));
			double val = exp + (1 - exp) * (1 - time*time);
			if (val < getUtility(lastReceivedBid)) {
				System.out.print(exp + ",");
				System.out.println(val);
				return new Accept(getPartyId(), lastReceivedBid);
			}
			Bid p = bids.get(0).getBid();
			double maximum = 0;
			for (int i = 0; i < bids.size(); i++) {

				double max = 0;
				max = max + (2 - 0.2 * time * time) * getUtility(bids.get(i).getBid());
				for (AgentID x : agents.keySet()) {
					max = max + agents.get(x).getUtil(bids.get(i).getBid());
				}
				if (maximum < max) {
					p = bids.get(i).getBid();
					maximum = max;
				}
			}
			return new Offer(getPartyId(), p);
		}
		return null;
	}

	@Override
	public void receiveMessage(AgentID sender, Action action) {
		super.receiveMessage(sender, action);
		if (action instanceof Offer) {
			lastReceivedBid = ((Offer) action).getBid();
		}
	}

	@Override
	public String getDescription() {
		return "D4: group 5 agent";
	}

}
