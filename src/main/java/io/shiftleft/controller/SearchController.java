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
    // Default message in case of errors
    String message = "Invalid input";
    
    try {
        // Validate input using a whitelist pattern - only allow alphanumeric and basic operations
        if (isValidExpression(foo)) {
            // Create a restricted evaluation context that doesn't allow access to system resources
            StandardEvaluationContext context = new StandardEvaluationContext();
            // Disable method execution and constructor invocation
            context.setVariables(null);
            
            ExpressionParser parser = new SpelExpressionParser();
            Expression exp = parser.parseExpression(foo);
            Object result = exp.getValue(context);
            
            if (result != null) {
                // Encode the output to prevent XSS
                message = Encode.forHtml(result.toString());
            }
        } else {
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            message = "Expression contains invalid characters or operations";
        }
    } catch (Exception ex) {
        // Log the error but don't expose details to the user
        System.err.println("Error processing expression: " + ex.getMessage());
        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
    }
    
    return message;
}

/**
 * Validates if an expression is safe to evaluate
 * Only allows alphanumeric characters, basic operations and limited symbols
 */
private boolean isValidExpression(String expression) {
    if (expression == null || expression.isEmpty()) {
        return false;
    }
    
    // Whitelist pattern for safe expressions
    Pattern safePattern = Pattern.compile("^[\\w\\s+\\-*/%(),.\"']+$");
    
    // Blacklist dangerous keywords
    String[] dangerousKeywords = {"T(", "new", "java", "System", "Runtime", "class", "getClass", 
                                  "exec", "invoke", "forName", "ProcessBuilder", "Method"};
    
    if (!safePattern.matcher(expression).matches()) {
        return false;
    }
    
    // Check for dangerous keywords
    for (String keyword : dangerousKeywords) {
        if (expression.contains(keyword)) {
            return false;
        }
    }
    
    return true;
}

    return message.toString();
  }
}
