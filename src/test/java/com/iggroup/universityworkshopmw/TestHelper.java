package com.iggroup.universityworkshopmw;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.nio.charset.Charset;

public class TestHelper {

   public static final MediaType APPLICATION_JSON_UTF8 = new MediaType(
         MediaType.APPLICATION_JSON.getType(),
         MediaType.APPLICATION_JSON.getSubtype(),
         Charset.forName("utf8")
   );

   public static byte[] convertObjectToJsonBytes(Object object) throws IOException {
      ObjectMapper mapper = new ObjectMapper();
      mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
      return mapper.writeValueAsBytes(object);
   }

   public static class ResultCaptor<T> implements Answer {
      private T result = null;

      public T getResult() {
         return result;
      }

      @Override
      public T answer(InvocationOnMock invocationOnMock) throws Throwable {
         result = (T) invocationOnMock.callRealMethod();
         return result;
      }
   }
}
