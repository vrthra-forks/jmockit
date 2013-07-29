/*
 * Copyright (c) 2006-2013 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.multicore;

import java.io.*;

public final class StreamIO
{
   final InputStream input;
   private final OutputStream output;
   private byte[] buf;
   private int bytesRead;

   StreamIO(InputStream input, OutputStream output)
   {
      this.input = input;
      this.output = output;
      buf = new byte[1024];
   }

   void writeLine(String line)
   {
      try {
         output.write(line.getBytes());
         output.write('\n');
         output.flush();
      }
      catch (IOException e) {
         throw new RuntimeException(e);
      }
   }

   public String readNextLine() { return readNextLine(false); }
   String readNextLineIncludingTerminator() { return readNextLine(true); }

   private String readNextLine(boolean includingTerminator)
   {
      bytesRead = 0;

      while (true) {
         int nextByte = readNextByte();
         if (nextByte < 0) return null;

         if (nextByte == '\n') {
            if (includingTerminator) storeByteJustRead('\n');
            break;
         }

         storeByteJustRead(nextByte);
         recreateBufferWithTwiceTheSizeIfNeeded();
      }

      return new String(buf, 0, bytesRead);
   }

   private int readNextByte()
   {
      try { return input.read(); } catch (IOException e) { throw new RuntimeException(e); }
   }

   private void storeByteJustRead(int byteRead)
   {
      buf[bytesRead] = (byte) byteRead;
      bytesRead++;
   }

   private void recreateBufferWithTwiceTheSizeIfNeeded()
   {
      if (bytesRead == buf.length) {
         byte[] buf2 = new byte[2 * bytesRead];
         System.arraycopy(buf, 0, buf2, 0, bytesRead);
         buf = buf2;
      }
   }

   public <T> T readObject()
   {
      try {
         //noinspection unchecked
         return (T) new ObjectInputStream(input).readObject();
      }
      catch (ClassNotFoundException e) { throw new RuntimeException(e); }
      catch (IOException e) { throw new RuntimeException(e); }
   }
}
