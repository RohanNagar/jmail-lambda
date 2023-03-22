package com.sanctionco.jmail.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.sanctionco.jmail.EmailValidator;
import com.sanctionco.jmail.JMail;

import java.util.Map;

public class Handler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

  @Override
  public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent event, Context context) {
    LambdaLogger logger = context.getLogger();
    APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();

    String email = event.getQueryStringParameters().get("address");
    logger.log("Attempting to validate email address: " + email);

    EmailValidator validator = buildValidator(logger, event.getQueryStringParameters());

    if (validator.isValid(email)) {
      logger.log("Valid email address");
      response.setStatusCode(200);
    } else {
      logger.log("Invalid email address");
      response.setStatusCode(400);
    }

    return response;
  }

  private EmailValidator buildValidator(LambdaLogger logger, Map<String, String> options) {
    EmailValidator validator = JMail.validator();

    if (options.get("tld") != null) {
      logger.log("Adding rule requireTopLevelDomain");
      validator = validator.requireTopLevelDomain();
    }

    return validator;
  }
}
