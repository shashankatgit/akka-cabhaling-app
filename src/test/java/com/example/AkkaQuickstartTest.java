package com.example;

import akka.actor.testkit.typed.javadsl.TestKitJunitResource;
import akka.actor.testkit.typed.javadsl.TestProbe;
import akka.actor.typed.ActorRef;
import pods.cabs.Cab;
import pods.cabs.Globals;
import pods.cabs.Main;
import pods.cabs.RideService;
import pods.cabs.Wallet;
import pods.cabs.models.CabStatus;
import pods.cabs.utils.InitFileReader;
import pods.cabs.utils.Logger;

import java.util.Random;

import org.junit.ClassRule;
import org.junit.Test;

//#definition
public class AkkaQuickstartTest {

	@ClassRule
	public static final TestKitJunitResource testKit = new TestKitJunitResource();
//#definition

	// #test
//    @Test
//    public void testGreeterActorSendingOfGreeting() {
//        TestProbe<Greeter.Greeted> testProbe = testKit.createTestProbe();
//        ActorRef<Greeter.Greet> underTest = testKit.spawn(Greeter.create(), "greeter");
//        underTest.tell(new Greeter.Greet("Charles", testProbe.getRef()));
//        testProbe.expectMessage(new Greeter.Greeted("Charles", underTest));
//    }
	// #test

	@Test
	public void testMainStarted() {
		TestProbe<Main.StartedCommand> mainTestProbe = testKit.createTestProbe();
		ActorRef<Main.MainGenericCommand> underTest = testKit.spawn(Main.create(mainTestProbe.getRef()), "main");
		mainTestProbe.expectMessage(new Main.StartedCommand(true));
		Logger.log("Main Started\n");

		long initBalance = Globals.initReadWrapperObj.walletBalance;

		TestProbe<Wallet.ResponseBalance> walletTestProbe = testKit.createTestProbe();
		Globals.wallets.get("201").tell(new Wallet.GetBalance(walletTestProbe.getRef()));
		walletTestProbe.expectMessage(new Wallet.ResponseBalance(10000));
		Logger.log("Success : Wallet GetBalance\n");

		Globals.wallets.get("201").tell(new Wallet.AddBalance(200));
		Logger.log("Success : Wallet AddBalance\n");

		Globals.wallets.get("201").tell(new Wallet.DeductBalance(200, walletTestProbe.getRef()));
		walletTestProbe.expectMessage(new Wallet.ResponseBalance(10000));
		Logger.log("Success : Wallet DeductBalance\n");

		Globals.wallets.get("201").tell(new Wallet.Reset(walletTestProbe.getRef()));
		walletTestProbe.expectMessage(new Wallet.ResponseBalance(initBalance));
		Logger.log("Success : Wallet Reset\n");

		// Testing for insufficient balance
		Globals.wallets.get("201").tell(new Wallet.DeductBalance(initBalance + 100, walletTestProbe.getRef()));
		walletTestProbe.expectMessage(new Wallet.ResponseBalance(-1));
		Logger.log("Success : Wallet Overdeduction disallowd\n");

		TestProbe<Cab.NumRidesReponse> cabTestProbe = testKit.createTestProbe();
		Globals.cabs.get("101").tell(new Cab.NumRides(cabTestProbe.getRef()));
		cabTestProbe.expectMessage(new Cab.NumRidesReponse(0));
		Logger.log("Success : Cab NumRides functional\n");

		try {			
			Globals.cabs.get("101").tell(new Cab.SignIn(50));
			Thread.sleep(1000);
			Globals.cabs.get("101").tell(new Cab.SignOut());
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		TestProbe<RideService.RideResponse> fufillRideTestProbe = testKit.createTestProbe();
		Globals.rideService.get(0).tell(new RideService.RequestRide("201", 50, 100, fufillRideTestProbe.getRef()));
//		fufillRideTestProbe.expectMessage(new RideService.RideResponse());

	}

}
