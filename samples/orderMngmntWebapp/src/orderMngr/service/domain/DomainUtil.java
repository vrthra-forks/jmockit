package orderMngr.service.domain;

public final class DomainUtil
{
   public static void validateRequiredData(Object... dataItems) throws MissingRequiredData
   {
      for (Object dataItem : dataItems) {
         if (dataItem == null) {
            throw new MissingRequiredData();
         }
         else if (dataItem instanceof String) {
            String itemText = (String) dataItem;

            if (itemText.trim().length() == 0) {
               throw new MissingRequiredData();
            }
         }
      }
   }
}
