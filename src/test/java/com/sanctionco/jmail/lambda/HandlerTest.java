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
    APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent();
    event.setQueryStringParameters(Collections.singletonMap("address", "test@test.com"));

    Context context = mock(Context.class);
    when(context.getLogger()).thenReturn(mock(LambdaLogger.class));

    Handler handler = new Handler();

    APIGatewayProxyResponseEvent response = handler.handleRequest(event, context);

    assertEquals(200, response.getStatusCode());
  }

  @Test
  void testInvalidEmailAddress() {
    APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent();
    event.setQueryStringParameters(Collections.singletonMap("address", "test@-test.com"));

    Context context = mock(Context.class);
    when(context.getLogger()).thenReturn(mock(LambdaLogger.class));

    Handler handler = new Handler();

    APIGatewayProxyResponseEvent response = handler.handleRequest(event, context);

    assertEquals(400, response.getStatusCode());
  }

  @Test
  void testInvalidEmailAddressWhenRequiringTLD() {
    Map<String, String> queryParameters = new HashMap<>();
    queryParameters.put("address", "test@hello");
    queryParameters.put("tld", "true");

    APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent();
    event.setQueryStringParameters(queryParameters);

    Context context = mock(Context.class);
    when(context.getLogger()).thenReturn(mock(LambdaLogger.class));

    Handler handler = new Handler();

    APIGatewayProxyResponseEvent response = handler.handleRequest(event, context);

    assertEquals(400, response.getStatusCode());
  }

  @Test
  void testInvalidEmailAddressWhenRejectingIPDomains() {
    Map<String, String> queryParameters = new HashMap<>();
    queryParameters.put("address", "test@[1.2.3.4]");
    queryParameters.put("rejectIp", "true");

    APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent();
    event.setQueryStringParameters(queryParameters);

    Context context = mock(Context.class);
    when(context.getLogger()).thenReturn(mock(LambdaLogger.class));

    Handler handler = new Handler();

    APIGatewayProxyResponseEvent response = handler.handleRequest(event, context);

    assertEquals(400, response.getStatusCode());
  }

  @Test
  void testInvalidEmailAddressWhenRejectingSourceRouting() {
    Map<String, String> queryParameters = new HashMap<>();
    queryParameters.put("address", "@1st.relay,@2nd.relay:user@final.domain");
    queryParameters.put("rejectSourceRoutes", "true");

    APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent();
    event.setQueryStringParameters(queryParameters);

    Context context = mock(Context.class);
    when(context.getLogger()).thenReturn(mock(LambdaLogger.class));

    Handler handler = new Handler();

    APIGatewayProxyResponseEvent response = handler.handleRequest(event, context);

    assertEquals(400, response.getStatusCode());
  }

  @Test
  void testInvalidEmailAddressWhenRejectingReservedDomains() {
    Map<String, String> queryParameters = new HashMap<>();
    queryParameters.put("address", "test@example.com");
    queryParameters.put("rejectReserved", "true");

    APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent();
    event.setQueryStringParameters(queryParameters);

    Context context = mock(Context.class);
    when(context.getLogger()).thenReturn(mock(LambdaLogger.class));

    Handler handler = new Handler();

    APIGatewayProxyResponseEvent response = handler.handleRequest(event, context);

    assertEquals(400, response.getStatusCode());
  }

  @Test
  void testInvalidEmailAddressWhenRejectingQuotedIdentifiers() {
    Map<String, String> queryParameters = new HashMap<>();
    queryParameters.put("address", "John Smith <test@te.ex>");
    queryParameters.put("rejectQuotedIds", "true");

    APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent();
    event.setQueryStringParameters(queryParameters);

    Context context = mock(Context.class);
    when(context.getLogger()).thenReturn(mock(LambdaLogger.class));

    Handler handler = new Handler();

    APIGatewayProxyResponseEvent response = handler.handleRequest(event, context);

    assertEquals(400, response.getStatusCode());
  }

  @Test
  void testInvalidEmailAddressWhenRequiringMXRecord() {
    Map<String, String> queryParameters = new HashMap<>();
    queryParameters.put("address", "hello@one.two");
    queryParameters.put("mx", "true");

    APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent();
    event.setQueryStringParameters(queryParameters);

    Context context = mock(Context.class);
    when(context.getLogger()).thenReturn(mock(LambdaLogger.class));

    Handler handler = new Handler();

    APIGatewayProxyResponseEvent response = handler.handleRequest(event, context);

    assertEquals(400, response.getStatusCode());
  }
}
