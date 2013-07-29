/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.emulation.hibernate3.ast;

import java.io.*;
import java.text.*;
import java.util.*;

public final class Tokens
{
   private static final DecimalFormat NUM_FORMAT = new DecimalFormat();
   private static final List<String> KEYWORDS = Arrays.asList(
      "and", "as", "asc", "ascending", "between", "by", "desc", "descending", "distinct", "fetch",
      "from", "full", "group", "having", "in", "inner", "join", "left", "like", "member", "new",
      "not", "of", "or", "order", "outer", "right", "select", "where");

   static {
      NUM_FORMAT.setGroupingUsed(false);

      DecimalFormatSymbols symbols = new DecimalFormatSymbols();
      symbols.setDecimalSeparator('.');
      NUM_FORMAT.setDecimalFormatSymbols(symbols);
   }

   private final String[] tokens;
   private int pos;
   private int lastParameterIndex;

   public Tokens(String ql)
   {
      tokens = tokens(ql);
      pos = -1;
      lastParameterIndex = -1;
   }

   private static String[] tokens(String ql)
   {
      StreamTokenizer tokenizer = new StreamTokenizer(new StringReader(ql));
      tokenizer.ordinaryChar('/');
      List<String> tokens = new ArrayList<String>();

      try {
         while (tokenizer.nextToken() != StreamTokenizer.TT_EOF) {
            int ttype = tokenizer.ttype;
            char firstChar = (char) ttype;

            if (firstChar == '<' || firstChar == '>' || firstChar == '!' || firstChar == '|') {
               char secondChar = (char) tokenizer.nextToken();

               if (
                  firstChar == '|' && secondChar == '|' ||
                  firstChar == '!' && secondChar == '=' ||
                  firstChar == '>' && secondChar == '=' ||
                  firstChar == '<' && secondChar == '=' || 
                  firstChar == '<' && secondChar == '>'
               ) {
                  tokens.add(String.valueOf(firstChar) + secondChar);
                  continue;
               }
               else {
                  tokenizer.pushBack();
               }
            }

            String token;

            if (ttype == StreamTokenizer.TT_WORD) {
               token = tokenizer.sval;
            }
            else if (ttype == '\'') {
               token = '\'' + tokenizer.sval + '\'';
            }
            else if (ttype == StreamTokenizer.TT_NUMBER) {
               token = NUM_FORMAT.format(tokenizer.nval);

               // TODO: not needed any more?
//               if (token.endsWith(".0")) {
//                  token = token.substring(0, token.lastIndexOf('.'));
//               }
            }
            else {
               token = String.valueOf((char) ttype);
            }

            tokens.add(token);
         }
      }
      catch (IOException ignore) { /* Won't ever happen */ }

      return tokens.toArray(new String[tokens.size()]);
   }

   public boolean hasNext()
   {
      return pos + 1 < tokens.length;
   }

   public char nextChar()
   {
      return next().charAt(0);
   }

   public String next() throws QuerySyntaxException
   {
      pos++;
      return token();
   }

   private String token()
   {
      if (pos >= tokens.length) {
         throw new QuerySyntaxException(this);
      }

      return tokens[pos];
   }

   public void next(String expected) throws QuerySyntaxException
   {
      String token = next();

      if (!expected.equalsIgnoreCase(token)) {
         throw new QuerySyntaxException(this);
      }
   }

   public void pushback()
   {
      pos--;
   }

   public int getPosition()
   {
      return pos;
   }

   public void setPosition(int position)
   {
      pos = position;
   }

   public int nextParameterIndex()
   {
      lastParameterIndex++;
      return lastParameterIndex;
   }

   @Override
   public String toString()
   {
      StringBuilder result = new StringBuilder();

      for (int i = 0; i < tokens.length; i++) {
         String token = tokens[i];

         if (i == pos + 1) {
            result.append('_');
         }

         result.append(token).append(' ');
      }

      return result.toString();
   }

   public static boolean isIdentifier(String token)
   {
      char firstChar = token.charAt(0);
      return Character.isJavaIdentifierStart(firstChar) && !KEYWORDS.contains(token);
   }
}
