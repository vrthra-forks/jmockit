/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.emulation.hibernate3.ast.whereClause;

import java.util.*;

import mockit.emulation.hibernate3.ast.*;
import org.junit.*;
import static org.junit.Assert.*;

public final class PrimaryExprTest
{
   @Test
   public void parseConstant()
   {
      Tokens tokens = new Tokens("235");

      Expr expr = PrimaryExpr.parse(tokens);

      assertTrue(expr instanceof ConstantExpr);
      assertFalse(tokens.hasNext());
   }

   @Test
   public void parseParameter()
   {
      Tokens tokens = new Tokens("?");

      Expr expr = PrimaryExpr.parse(tokens);

      assertTrue(expr instanceof PositionalParameterExpr);
      assertEquals(0, ((PositionalParameterExpr) expr).index);
      assertFalse(tokens.hasNext());
   }

   @Test
   public void parseParameterWithIndex()
   {
      Tokens tokens = new Tokens("?3");

      Expr expr = PrimaryExpr.parse(tokens);

      assertTrue(expr instanceof PositionalParameterExpr);
      assertEquals(3, ((PositionalParameterExpr) expr).index);
      assertFalse(tokens.hasNext());
   }

   @Test
   public void parseParameterFollowedBySomething()
   {
      Tokens tokens = new Tokens("?=20");

      Expr expr = PrimaryExpr.parse(tokens);

      assertTrue(expr instanceof PositionalParameterExpr);
      assertEquals(0, ((PositionalParameterExpr) expr).index);
      assertEquals(0, tokens.getPosition());
   }

   @Test
   public void evaluateParameter()
   {
      Map<Object, Object> parameters = new HashMap<Object, Object>();
      parameters.put(0, "test");
      Expr expr = new PositionalParameterExpr(0);

      Object value = expr.evaluate(new QueryEval(parameters));

      assertEquals("test", value);
   }

   @Test
   public void parseNamedParameter()
   {
      Tokens tokens = new Tokens(":orderNo");

      Expr expr = PrimaryExpr.parse(tokens);

      assertTrue(expr instanceof NamedParameterExpr);
      Assert.assertEquals("orderNo", ((NamedParameterExpr) expr).name);
      assertFalse(tokens.hasNext());
   }

   @Test
   public void evaluateNamedParameter()
   {
      Map<String, Object> parameters = new HashMap<String, Object>();
      parameters.put("no", "test");
      Expr expr = new NamedParameterExpr("no");

      Object value = expr.evaluate(new QueryEval(parameters));

      assertEquals("test", value);
   }

   @Test
   public void parseFunctionCall()
   {
      Tokens tokens = new Tokens("trim(abc)");

      Expr expr = PrimaryExpr.parse(tokens);

      assertTrue(expr instanceof FunctionCallExpr);
      FunctionCallExpr fcExpr = (FunctionCallExpr) expr;
      assertEquals("trim", fcExpr.name);
      assertEquals(1, fcExpr.arguments.length);
      assertTrue(fcExpr.arguments[0] instanceof AccessPathExpr);
      assertFalse(tokens.hasNext());
   }

   @Test
   public void evaluateFunctionCall()
   {
      Expr expr = new FunctionCallExpr("abs", new Expr[] { new ConstantExpr(-123) });

      Object value = expr.evaluate(new QueryEval());

      assertEquals(123, value);
   }

   @Test
   public void parseAccessPath()
   {
      Tokens tokens = new Tokens("abc");

      Expr expr = PrimaryExpr.parse(tokens);

      assertTrue(expr instanceof AccessPathExpr);
      AccessPathExpr apExpr = (AccessPathExpr) expr;
      assertEquals(1, apExpr.accessElements.length);
      assertEquals("abc", apExpr.accessElements[0]);
      assertFalse(tokens.hasNext());
   }

   @Test
   public void evaluateAccessPath()
   {
      Map<String, Object> tuple = new HashMap<String, Object>();
      tuple.put("x", new Entity());
      Expr expr = new AccessPathExpr("x.abc");

      Object value = expr.evaluate(new QueryEval(null, tuple));

      assertEquals("test", value);
   }

   public static class Entity
   {
      public String getAbc() { return "test"; }
   }
   
   @Test
   public void parseSomethingElse()
   {
      Tokens tokens = new Tokens(")");

      Expr expr = PrimaryExpr.parse(tokens);

      assertNull(expr);
      assertEquals(-1, tokens.getPosition());
   }
}