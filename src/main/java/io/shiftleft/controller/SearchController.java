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
    // Initialize logger for secure error handling
    private static final Logger logger = LoggerFactory.getLogger(SearchController.class);
    
    // Set Content-Type header with charset
    response.setContentType("text/plain; charset=UTF-8");
    
    // Set security headers to prevent XSS
    response.setHeader("X-Content-Type-Options", "nosniff");
    response.setHeader("X-XSS-Protection", "1; mode=block");
    response.setHeader("Content-Security-Policy", "default-src 'self'");
    
    // Validate input against a whitelist pattern
    Pattern safeInputPattern = Pattern.compile("^[a-zA-Z0-9\\s.,?!]+$");
    if (foo == null || !safeInputPattern.matcher(foo).matches()) {
        // Return sanitized error message
        return "Invalid input format";
    }
    
    // Process the validated input safely (avoiding SpEL for user input)
    String result;
    try {
        // Instead of using SpEL, handle the input directly
        result = "Search results for: " + foo;
    } catch (Exception ex) {
        // Secure error logging - avoid exposing details to user
        logger.error("Error processing search: null", ex.getMessage());
        result = "An error occurred while processing your request";
    }
    
    // HTML encode the result before returning to prevent XSS
    return Encode.forHtml(result);
}

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
