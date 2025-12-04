package io.shiftleft.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;


/**
 * Search login
 */
@Controller
public class SearchController {

@RequestMapping(value = "/search/user", method = RequestMethod.GET)
public String doGetSearch(@RequestParam String foo, HttpServletResponse response, HttpServletRequest request) {
  java.lang.Object message = new Object();
  
  // Define a whitelist of allowed search terms
  Map<String, String> allowedSearchTerms = new HashMap<>();
  allowedSearchTerms.put("username", "user.name");
  allowedSearchTerms.put("email", "user.email");
  allowedSearchTerms.put("id", "user.id");
  
  try {
    // Check if the input is in our allowed list
    if (allowedSearchTerms.containsKey(foo)) {
      ExpressionParser parser = new SpelExpressionParser();
      StandardEvaluationContext context = new StandardEvaluationContext();
      
      // Create a dummy user object with safe data
      Map<String, Object> user = new HashMap<>();
      user.put("name", "John Doe");
      user.put("email", "john@example.com");
      user.put("id", "12345");
      
      // Add the user to the evaluation context
      context.setVariable("user", user);
      
      // Use the pre-defined expression from our whitelist
      Expression exp = parser.parseExpression(allowedSearchTerms.get(foo));
      message = exp.getValue(context);
    } else {
      message = "Invalid search term. Allowed terms: " + String.join(", ", allowedSearchTerms.keySet());
    }
  } catch (Exception ex) {
    message = "Search error occurred";
    // Log the error securely - don't expose exception details to users
    System.out.println("Search error: " + ex.getMessage());
  }
  return message.toString();
}

    return message.toString();
  }
}
