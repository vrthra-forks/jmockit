/*
 * Copyright (c) 2006-2012 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package powermock.examples.dom4j;

import org.junit.*;

import mockit.*;

import org.dom4j.*;
import org.dom4j.tree.*;
import static org.junit.Assert.*;

/**
 * <a href="http://code.google.com/p/powermock/source/browse/trunk/examples/dom4j/src/test/java/org/powermock/examples/dom4j/AbstractXMLRequestCreatorBaseTest.java">PowerMock version</a>
 */
@UsingMocksAndStubs(AbstractNode.class)
public final class AbstractXMLRequestCreatorBase_JMockit_Test
{
   @Tested AbstractXMLRequestCreatorBase tested;

   @Test
   public void testConvertDocumentToByteArray() throws Exception
   {
      // Create a fake document.
      Document document = DocumentHelper.createDocument();
      Element root = document.addElement("ListExecutionContexts");
      root.addAttribute("id", "2");

      // Perform the test.
      byte[] array = tested.convertDocumentToByteArray(document);

      assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<ListExecutionContexts id=\"2\"/>", new String(array));
   }

   @Test // just to demonstrate API; too much mocking for a real test
   public void testCreateRequest() throws Exception
   {
      final String[] params = {"String1", "String2"};
      final byte[] expected = {42};

      new Expectations(tested) {
         @Mocked final DocumentHelper unused = null;
         @Mocked Document documentMock;
         @Mocked Element rootElementMock;
         @Mocked Element headerElementMock;
         @Mocked Element bodyElementMock;

         {
            DocumentHelper.createDocument(); result = documentMock;
            documentMock.addElement(XMLProtocol.ENCODE_ELEMENT); result = rootElementMock;
            rootElementMock.addElement(XMLProtocol.HEADER_ELEMENT); result = headerElementMock;
            headerElementMock.addAttribute(XMLProtocol.HEADER_MSG_ID_ATTRIBUTE, anyString);
            rootElementMock.addElement(XMLProtocol.BODY_ELEMENT); result = bodyElementMock;

            tested.createBody(bodyElementMock, params);
            tested.convertDocumentToByteArray(documentMock); result = expected;
         }
      };

      byte[] actual = tested.createRequest(params);

      assertSame(expected, actual);
   }

   @Test
   public void sanerTestForCreateRequest() throws Exception
   {
      new NonStrictExpectations(tested) {{
         // Avoids re-exercising this method (covered by another test) while verifying it gets called:
         tested.convertDocumentToByteArray((Document) any); times = 1;
         result = new Delegate<Document>() {
            byte[] delegate(Document doc) { return doc.asXML().getBytes(); }
         };
      }};

      String actualXML = new String(tested.createRequest("String1", "String2"));

      assertTrue(actualXML.startsWith(
         "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<EncodeElement><MyHeader MyMsgIdAttribute="));
      assertTrue(actualXML.endsWith("/><BodyElement/></EncodeElement>"));

      new Verifications() {{ tested.createBody((Element) withNotNull(), "String1", "String2"); }};
   }
}
