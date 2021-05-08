package com.example;

import static org.junit.Assert.assertTrue;

import akka.actor.testkit.typed.javadsl.TestKitJunitResource;
import akka.actor.testkit.typed.javadsl.TestProbe;
import akka.actor.typed.ActorRef;
import pods.cabs.Cab;
import pods.cabs.Globals;
import pods.cabs.RideService;
import pods.cabs.Wallet;
import pods.cabs.utils.Logger;

public class TestInterface {
	
	public TestKitJunitResource testKit;
	
	public TestInterface(TestKitJunitResource testKit) {
		super();
		this.testKit = testKit;
	}
	
	void resetAllCabs() {
		TestProbe<Cab.NumRidesReponse> testProbe = testKit.createTestProbe();
		for (ActorRef<Cab.Command> cabActor : Globals.cabs.values()) {
			cabActor.tell(new Cab.Reset(testProbe.getRef()));
			Cab.NumRidesReponse response = testProbe.receiveMessage();
		}
	}
	
	void resetAllWallets() {
		TestProbe<Wallet.ResponseBalance> testProbe = testKit.createTestProbe();
		for (ActorRef<Wallet.Command> walletActor : Globals.wallets.values()) {
			walletActor.tell(new Wallet.Reset(testProbe.getRef()));
			Wallet.ResponseBalance response = testProbe.receiveMessage();
		}
	}
	
	void resetAll() {
		resetAllCabs();
		resetAllWallets();
	}
 
	void walletAdd(String custId, long amountToAdd) {
		Globals.wallets.get(custId).tell(new Wallet.AddBalance(amountToAdd));
		Logger.logTestSuccess("Added " + amountToAdd + " to wallet-"+custId);
	}
	
	void walletDeduct(String custId, long amountToDeduct, long expectedBalance) {
		TestProbe<Wallet.ResponseBalance> testProbe = testKit.createTestProbe();
		Globals.wallets.get(custId).tell(new Wallet.DeductBalance(amountToDeduct, testProbe.getRef()));
		testProbe.expectMessage(new Wallet.ResponseBalance(expectedBalance));
		
		Logger.logTestSuccess("Deducted " + amountToDeduct + " from wallet-"+custId);
	}
	
	long walletGetBalanceTest(String custId, long amountToDeduct, long expectedBalance, boolean doTest) {
		TestProbe<Wallet.ResponseBalance> testProbe = testKit.createTestProbe();
		Globals.wallets.get(custId).tell(new Wallet.DeductBalance(amountToDeduct, testProbe.getRef()));
		
		if(doTest) {
			Wallet.ResponseBalance walletResponse =  testProbe.receiveMessage();
			testProbe.expectMessage(new Wallet.ResponseBalance(expectedBalance));
			return expectedBalance;
		}
		Wallet.ResponseBalance walletResponse =  testProbe.receiveMessage();
		return walletResponse.balance;
	}
	
	
	
}
