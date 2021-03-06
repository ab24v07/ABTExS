
package traders;

import java.util.ArrayList;
import java.util.LinkedList; 
import java.util.Properties;

import lob.*;

/**
 * <p>
 * These agents simultaneously post an order on each side of the book, 
 * maintaining an approximately neutral position throughout the day. They make 
 * their income from the difference between their bids and offers. If one or 
 * both limit orders is executed, it will be replaced by a new one the next time 
 * the market maker is chosen to trade. Each round, the market maker updates 
 * a prediction for the sign of the next period���s order using a simple $w$
 * period rolling-mean estimate. When a market maker predicts that a buy order 
 * will arrive next, she will set her sell limit order volume to a uniformly 
 * distributed random number between v_min and v_max and her buy limit 
 * order volume to 1.<br/>
 * 
 * When to update orders:
 * ATM 
 * If (my prediction has changed since last time) OR (order executed) {
 * 		delete both and resubmit
 * }
 * 
 * Prices:
 * ATM
 * set to current best
 * 
 * MUST RUN UPDATE BEFORE SUBMITORDERS()!!!
 * 
 * @author Ash Booth
 *
 */

public class MarketMaker extends Trader {

	private final int rollMeanLen;
	private final int vMin; 
	private final int vMax;
	private final int vMinus;
	
	private double lastSignPred;
	private double nextSignPred;
	private LinkedList<Integer> lastOrderSigns = new LinkedList<Integer>();
	
	/**
	 * @param tId 
	 * @param cash
	 * @param numAssets
	 * @param rollMeanLen	param w in paper window length of rolling mean
	 * @param vMin			see paper
	 * @param vMax			see paper
	 * @param vMinus		see paper
	 */
	public MarketMaker(int tId, Properties prop) {
		super(tId, prop);
		this.rollMeanLen = Integer.valueOf(prop.getProperty("MM_rollMeanLen"));
		this.vMin = Integer.valueOf(prop.getProperty("MM_vMin"));
		this.vMax = Integer.valueOf(prop.getProperty("MM_vMax"));
		this.vMinus = Integer.valueOf(prop.getProperty("MM_vMinus"));
	}

//	@Override
//	public ArrayList<Order> getOrders(OrderBook lob, int time, boolean verbose) {
//		ArrayList<Order> ordersToGo = new ArrayList<Order>();
//		if ( (this.ordersInBook.size() != 2)  || (nextSignPred != lastSignPred) ) {
//			// remove all current orders from lob
//			for(Integer orderId: this.ordersInBook.keySet()) {
//				String side = this.ordersInBook.get(orderId).getSide();
//				lob.cancelOrder(side, orderId, time);
//			}
//			this.ordersInBook.clear();
//			
//			// Submit new bid and offer
//			double bidPrice = lob.getBestBid();
//			double offerPrice = lob.getBestOffer();
//			int bidQty, offerQty;
//			if (this.nextSignPred > 0) {
//				// we predict a buy next
//				offerQty = (vMin + generator.nextInt(vMax-vMin+1));
//				bidQty = vMinus;
//			} else {
//				// we predict a sell next
//				bidQty = (vMin + generator.nextInt(vMax-vMin+1));
//				offerQty = vMinus;
//			}
//			ordersToGo.add(new Order(time, true, bidQty, tId, "bid", bidPrice));
//			ordersToGo.add(new Order(time, true, offerQty, tId, "offer", offerPrice));
//		}
//		return ordersToGo;
//	}
	
	@Override
	public ArrayList<Order> getOrders(OrderBook lob, int time, boolean verbose) {
		ArrayList<Order> ordersToGo = new ArrayList<Order>();
		
			
		// remove all current orders from lob
		for(Integer orderId: this.ordersInBook.keySet()) {
			String side = this.ordersInBook.get(orderId).getSide();
			lob.cancelOrder(side, orderId, time);
		}
		this.ordersInBook.clear();
			
		// Submit new bid and offer
		double bidPrice = lob.getBestBid();
		double offerPrice = lob.getBestOffer();
		int bidQty, offerQty;
		
		offerQty = (vMin + generator.nextInt(vMax-vMin+1));
		bidQty = (vMin + generator.nextInt(vMax-vMin+1));

		ordersToGo.add(new Order(time, true, bidQty, tId, "bid", bidPrice));
		ordersToGo.add(new Order(time, true, offerQty, tId, "offer", offerPrice));
		return ordersToGo;
	}


	@Override
	public void update(OrderBook lob) {
		// Update rolling mean estimate of order sign
		if (lastOrderSigns.size() >= rollMeanLen) {
			lastOrderSigns.removeFirst();
		}
		lastOrderSigns.add(lob.getLastOrderSign());
		this.predictNextOrderSign();
	}
	
	private void predictNextOrderSign() {
		this.lastSignPred = this.nextSignPred;
		double sum = 0;
		for (int i : lastOrderSigns) {
			sum += i;
		}
		if (sum > 0) {
			this.nextSignPred = 1;
		} else if (sum < 0){
			this.nextSignPred = -1;
		}else {
			this.nextSignPred = this.generator.nextBoolean() ? 1 : -1;
		}
	}

	@Override
	protected void iTraded(boolean bought, double price, int qty) {

	}
	

}
