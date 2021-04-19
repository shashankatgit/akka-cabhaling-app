package pods.cabs;

import java.util.ArrayList;
import java.util.HashMap;

import akka.actor.typed.ActorRef;
import pods.cabs.Cab.CabGenericCommand;
import pods.cabs.Main.MainGenericCommand;


public class Globals {
	static HashMap<String,ActorRef<Cab.CabGenericCommand>> cabs; //Cab Ids are keys
	static HashMap<String,ActorRef<Wallet.WalletGenericCommand>> wallets;
	
	static ArrayList<RideService> rideService;
	
	static {
		cabs = new HashMap<>();
		wallets = new HashMap<>();
		rideService = new ArrayList<>();
	}
}
