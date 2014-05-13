package simulation;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import lob.Order;
import lob.OrderBook;

// TODO BUG upward price movement
// Findings:
// 		- NTs and fundamentals are fine
//		- I think MMs are upward biased

public class Simulation {
	
	private static void testLob() {
		System.out.println("Beginning simulation...\n");
		
		// create limit quotes
		Order quote = new Order(1, true, 2, 100, "offer", 17.4);
		Order quote1 = new Order(2, true, 10, 101, "offer", 17.4);
		Order quote2 = new Order(3, true, 15, 102, "offer", 17.6);
		Order quote3 = new Order(4, true, 5, 103, "bid", 16.89999);
		Order quote4 = new Order(5, true, 5, 104, "bid", 16.9);
		
		// instantiate order book
		OrderBook lob = new OrderBook(0.01);
		
		// View empty book
		print("\n...empty book...\n");
		print(lob.toString());
		
		// Add non-crossing orders
		print("\n...adding limit orders...\n");
		lob.processOrder(quote, false);
		lob.processOrder(quote1, false);
		lob.processOrder(quote2, false);
		lob.processOrder(quote3, false);
		lob.processOrder(quote4, false);
				
		// View the book
		print("\n...populated book...\n");
		print(lob.toString());
		
		// Market order
		print("\n...submitting market order...\n");
		Order quote5 = new Order(6, false, 1, 105, "bid");
		
		lob.processOrder(quote5, false);
		
		// View the book
		print("\n...book after MO...\n");
		print(lob.toString());
		
		// Crossing limit order
		print("\n...submitting limit order that crosses the spread...\n");
		Order quote6 = new Order(7, true, 3, 106, "bid", 100000.0);
		
		lob.processOrder(quote6, false);
		
		// View the book
		print("\n...book after crossing limit order...\n");
		print(lob.toString());
		
		System.out.println("\nFinished simulation...");
	}
	
	
	private static void marketTrial(boolean verbose, String dataDir) {
		System.out.println("Beginning simulation...\n");
		Properties prop = getProperties("config.properties");
		int timesteps = Integer.parseInt(prop.getProperty("timesteps"));
		Market mkt = new Market(prop, dataDir);
//		for (int i=0;i<1000;i++) {
//			mkt.run(timesteps, verbose);
//			mkt.reset();
//		}
//		mkt.writeSimData("simData.csv");
		mkt.run(timesteps, verbose);
		mkt.writeDaysData("trades.csv", "quotes.csv", "mids.csv");
		mkt.reset();
		mkt.writeSimData("simData.csv");
		try {
			Runtime.getRuntime().exec("Rscript " + dataDir + "day_in_pictures.r");
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("\nFinished simulation...");
	}
	
	
	private static Properties getProperties(String filename) {
		FileInputStream in = null;	
		Properties prop = null;
		try {
			in = new FileInputStream(filename);
			prop = new Properties();
			prop.load(in);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return prop;
		
	}
	
	public static void print(String string) {
		System.out.println(string);
	}
	
	public static void main(String[] args) {
		marketTrial(false, "/Users/user/Dropbox/PhD_ICSS/Research/ABM/");
	}
}
