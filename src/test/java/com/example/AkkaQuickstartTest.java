package com.example;

import akka.actor.testkit.typed.javadsl.TestKitJunitResource;
import akka.actor.testkit.typed.javadsl.TestProbe;
import akka.actor.typed.ActorRef;
import pods.cabs.Globals;
import pods.cabs.Main;
import pods.cabs.Wallet;
import pods.cabs.utils.InitFileReader;
import pods.cabs.utils.Logger;

import org.junit.ClassRule;
import org.junit.Test;

//#definition
public class AkkaQuickstartTest {

    @ClassRule
    public static final TestKitJunitResource testKit = new TestKitJunitResource();
//#definition

    //#test
//    @Test
//    public void testGreeterActorSendingOfGreeting() {
//        TestProbe<Greeter.Greeted> testProbe = testKit.createTestProbe();
//        ActorRef<Greeter.Greet> underTest = testKit.spawn(Greeter.create(), "greeter");
//        underTest.tell(new Greeter.Greet("Charles", testProbe.getRef()));
//        testProbe.expectMessage(new Greeter.Greeted("Charles", underTest));
//    }
    //#test
    
    @Test
    public void testMainStarted() {
		  TestProbe<Main.StartedCommand> mainTestProbe = testKit.createTestProbe();
		  ActorRef<Main.MainGenericCommand> underTest = testKit.spawn(Main.create(mainTestProbe.getRef()), "main");
		  mainTestProbe.expectMessage(new Main.StartedCommand(true));
		  Logger.log("Main Started\n");
		  
		  
		  TestProbe<Wallet.ResponseBalance> walletTestProbe = testKit.createTestProbe();
		  Globals.wallets.get("201").tell(new Wallet.GetBalance(walletTestProbe.getRef()));		  
		  walletTestProbe.expectMessage(new Wallet.ResponseBalance(10000));
		  Logger.log("GetBalance success\n");
		  
		  
		  Globals.wallets.get("201").tell(new Wallet.AddBalance(200));		  
		  Logger.log("AddBalance success\n");

		  
		  Globals.wallets.get("201").tell(new Wallet.DeductBalance(200,walletTestProbe.getRef()));		  
		  walletTestProbe.expectMessage(new Wallet.ResponseBalance(10000));		
		  Logger.log("DeductBalance success\n");    
		  
		  
		  Globals.wallets.get("201").tell(new Wallet.Reset(walletTestProbe.getRef()));		  
		  walletTestProbe.expectMessage(new Wallet.ResponseBalance(10000));		
		  Logger.log("Reset success\n");
    }
    
}
