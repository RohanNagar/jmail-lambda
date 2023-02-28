package com.sanctionco.jmail.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.sanctionco.jmail.JMail;

public class Handler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

  @Override
  public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent event, Context context) {
    LambdaLogger logger = context.getLogger();
    APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();

    String email = event.getQueryStringParameters().get("address");
    logger.log("Attempting to validate email address: " + email);

    if (JMail.isValid(email)) {
      logger.log("Valid email address");
      response.setStatusCode(200);
    } else {
      logger.log("Invalid email address");
      response.setStatusCode(400);
    }

    return response;
  }
}
