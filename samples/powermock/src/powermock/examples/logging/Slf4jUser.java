package powermock.examples.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Slf4jUser
{
   private static final Logger log = LoggerFactory.getLogger(Slf4jUser.class);

   public final String getMessage()
   {
      log.debug("getMessage!");
      return "sl4j user";
   }
}
