package com.sanctionco.jmail.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HandlerTest {

  @Test
  void testValidEmailAddress() {
    Map<String, String> queryParameters = Collections.singletonMap("address", "test@test.com");

    runTest(queryParameters, 200);
  }

  @Test
  void testInvalidEmailAddress() {
    Map<String, String> queryParameters = Collections.singletonMap("address", "test@-test.com");

    runTest(queryParameters, 400);
  }

  @Test
  void testInvalidEmailAddressWhenRequiringTLD() {
    Map<String, String> queryParameters = new HashMap<>();
    queryParameters.put("address", "test@hello");
    queryParameters.put("tld", "true");

    runTest(queryParameters, 400);
  }

  @Test
  void testInvalidEmailAddressWhenRejectingIPDomains() {
    Map<String, String> queryParameters = new HashMap<>();
    queryParameters.put("address", "test@[1.2.3.4]");
    queryParameters.put("rejectIp", "true");

    runTest(queryParameters, 400);
  }

  @Test
  void testInvalidEmailAddressWhenRejectingSourceRouting() {
    Map<String, String> queryParameters = new HashMap<>();
    queryParameters.put("address", "@1st.relay,@2nd.relay:user@final.domain");
    queryParameters.put("rejectSourceRoutes", "true");

    runTest(queryParameters, 400);
  }

  @Test
  void testInvalidEmailAddressWhenRejectingReservedDomains() {
    Map<String, String> queryParameters = new HashMap<>();
    queryParameters.put("address", "test@example.com");
    queryParameters.put("rejectReserved", "true");

    runTest(queryParameters, 400);
  }

  @Test
  void testInvalidEmailAddressWhenRejectingQuotedIdentifiers() {
    Map<String, String> queryParameters = new HashMap<>();
    queryParameters.put("address", "John Smith <test@te.ex>");
    queryParameters.put("rejectQuotedIds", "true");

    runTest(queryParameters, 400);
  }

  @Test
  void testInvalidEmailAddressWhenRequiringMXRecord() {
    Map<String, String> queryParameters = new HashMap<>();
    queryParameters.put("address", "hello@one.two");
    queryParameters.put("mx", "true");

    runTest(queryParameters, 400);
  }

  @Test
  void testValidEmailAddressWhenRequiringAscii() {
    Map<String, String> queryParameters = new HashMap<>();
    queryParameters.put("address", "hello@one.two");
    queryParameters.put("ascii", "true");

    runTest(queryParameters, 200);
  }

  @Test
  void testInvalidEmailAddressWhenRequiringAscii() {
    Map<String, String> queryParameters = new HashMap<>();
    queryParameters.put("address", "he¡¡o@one.two");
    queryParameters.put("ascii", "true");

    runTest(queryParameters, 400);
  }

  @Test
  void testValidEmailAddressWhenAllowingNonstandardDots() {
    Map<String, String> queryParameters = new HashMap<>();
    queryParameters.put("address", ".hello@one.two");
    queryParameters.put("nonstandardDots", "true");

    runTest(queryParameters, 200);
  }

  @Test
  void testInvalidEmailAddressWhenDisallowingNonstandardDots() {
    Map<String, String> queryParameters = new HashMap<>();
    queryParameters.put("address", ".hello@one.two");

    runTest(queryParameters, 400);
  }

  void runTest(Map<String, String> queryParameters, int expectedStatus) {
    APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent();
    event.setQueryStringParameters(queryParameters);

    Context context = mock(Context.class);
    when(context.getLogger()).thenReturn(mock(LambdaLogger.class));

    Handler handler = new Handler();

    APIGatewayProxyResponseEvent response = handler.handleRequest(event, context);

    assertEquals(expectedStatus, response.getStatusCode());
  }
}
