package com.sanctionco.jmail.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.sanctionco.jmail.EmailValidationResult;
import com.sanctionco.jmail.EmailValidator;
import com.sanctionco.jmail.JMail;

import java.util.HashMap;
import java.util.Map;

public class Handler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

  @Override
  public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent event, Context context) {
    LambdaLogger logger = context.getLogger();
    APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();

    String email = event.getQueryStringParameters().get("address");
    logger.log("Attempting to validate email address: " + email);

    EmailValidator validator = buildValidator(logger, event.getQueryStringParameters());

    EmailValidationResult result = validator.validate(email);

    Map<String, String> headers = new HashMap<>();
    headers.put("Access-Control-Allow-Origin", "*");

    if (result.isSuccess()) {
      logger.log("Valid email address");
      response.setStatusCode(200);
    } else {
      logger.log("Invalid email address");
      response.setStatusCode(400);

      headers.put("Validation-Failure-Reason", result.getFailureReason().toString());
      headers.put("Access-Control-Expose-Headers", "Validation-Failure-Reason");
    }

    response.setHeaders(headers);

    return response;
  }

  private EmailValidator buildValidator(LambdaLogger logger, Map<String, String> options) {
    EmailValidator validator = JMail.validator();

    if ("true".equals(options.get("tld"))) {
      logger.log("Adding rule requireTopLevelDomain");
      validator = validator.requireTopLevelDomain();
    }

    if ("true".equals(options.get("rejectIp"))) {
      logger.log("Adding rule disallowIpDomain");
      validator = validator.disallowIpDomain();
    }

    if ("true".equals(options.get("rejectSourceRoutes"))) {
      logger.log("Adding rule disallowExplicitSourceRouting");
      validator = validator.disallowExplicitSourceRouting();
    }

    if ("true".equals(options.get("rejectReserved"))) {
      logger.log("Adding rule disallowReservedDomains");
      validator = validator.disallowReservedDomains();
    }

    if ("true".equals(options.get("rejectQuotedIds"))) {
      logger.log("Adding rule disallowQuotedIdentifiers");
      validator = validator.disallowQuotedIdentifiers();
    }

    if ("true".equals(options.get("mx"))) {
      logger.log("Adding rule requireValidMXRecord");
      validator = validator.requireValidMXRecord();
    }

    if ("true".equals(options.get("ascii"))) {
      logger.log("Adding rule requireAscii");
      validator = validator.requireAscii();
    }

    if ("true".equals(options.get("nonstandardDots"))) {
      logger.log("Adding rule allowNonstandardDots");
      validator = validator.allowNonstandardDots();
    }

    return validator;
  }
}
