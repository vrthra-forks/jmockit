/*
 * Copyright (c) 2006-2012 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package org.jmock.samples.sniper;

import org.junit.*;

import mockit.*;

public final class AuctionSniper_JMockit_Test
{
   final Money increment = new Money(2);
   final Money maximumBid = new Money(20);
   final Money beatableBid = new Money(10);
   final Money unbeatableBid = maximumBid.add(new Money(1));

   @Mocked Auction auction;
   @Mocked AuctionSniperListener listener;

   AuctionSniper sniper;

   @Before
   public void init()
   {
      sniper = new AuctionSniper(auction, increment, maximumBid, listener);
   }

   @Test
   public void triesToBeatTheLatestHighestBid() throws Exception
   {
      final Money expectedBid = beatableBid.add(increment);

      new Expectations() {{ auction.bid(expectedBid); }};

      sniper.bidAccepted(beatableBid);
   }

   @Test
   public void willNotBidPriceGreaterThanMaximum() throws Exception
   {
      new Expectations() {{ auction.bid((Money) any); times = 0; }};

      sniper.bidAccepted(unbeatableBid);
   }

   @Test
   public void willLimitBidToMaximum() throws Exception
   {
      new Expectations() {{ auction.bid(maximumBid); }};

      sniper.bidAccepted(maximumBid.subtract(new Money(1)));
   }

   @Test
   public void willAnnounceItHasFinishedIfPriceGoesAboveMaximum()
   {
      new Expectations() {{ listener.sniperFinished(sniper); }};

      sniper.bidAccepted(unbeatableBid);
   }

   @Test
   public void catchesExceptionsAndReportsThemToErrorListener() throws Exception
   {
      final AuctionException exception = new AuctionException("test");

      new Expectations() {{
         auction.bid((Money) any); result = exception;
         listener.sniperBidFailed(sniper, exception);
      }};

      sniper.bidAccepted(beatableBid);
   }
}