/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.emulation.hibernate3.ast;

public final class QuerySyntaxException extends RuntimeException
{
   public QuerySyntaxException(Tokens query)
   {
      super("Syntax error in query:\n" + query);
   }
}
