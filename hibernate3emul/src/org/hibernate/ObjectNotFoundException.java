package org.hibernate;

import java.io.*;

public final class ObjectNotFoundException extends RuntimeException
{
   public ObjectNotFoundException(Serializable id, String name) {}
}
