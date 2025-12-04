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
    // Default message in case of invalid input or error
    java.lang.Object message = "Invalid input";
    
    try {
        // Validate input against strict pattern - only allow alphanumeric and basic operations
        if (isValidExpression(foo)) {
            // Create a restricted evaluation context that only allows safe operations
            EvaluationContext context = SimpleEvaluationContext.forReadOnlyDataBinding().build();
            
            // Use restricted parser configuration
            SpelParserConfiguration config = new SpelParserConfiguration(false, false);
            ExpressionParser parser = new SpelExpressionParser(config);
            
            // Parse and evaluate with restricted context
            Expression exp = parser.parseExpression(foo);
            message = exp.getValue(context);
        } else {
            message = "Expression contains potentially unsafe operations";
        }
    } catch (Exception ex) {
        // Log the error properly instead of printing to console
        // logger.error("Error processing expression: " + ex.getMessage(), ex);
        message = "Error processing expression";
    }
    
    return message != null ? message.toString() : "null";
}

/**
 * Validates if the expression contains only safe operations
 * This is a basic whitelist approach and should be refined for production
 */
private boolean isValidExpression(String expression) {
    if (expression == null || expression.isEmpty()) {
        return false;
    }
    
    // Only allow simple expressions with literals, basic operators, and method calls on safe objects
    Pattern safePattern = Pattern.compile("^[\\w\\s+\\-*/%().'\"]+$");
    
    // Check for dangerous functions and methods
    Pattern dangerousPattern = Pattern.compile("(T\\(|new|\\bclass\\b|\\bClassLoader\\b|\\bRuntime\\b|\\bSystem\\b|\\bProcess\\b)");
    
    return safePattern.matcher(expression).matches() && !dangerousPattern.matcher(expression).find();
}

    /* Removed vulnerable code:
    java.lang.Object message = new Object();
    try {
        ExpressionParser parser = new SpelExpressionParser();
        Expression exp = parser.parseExpression(foo);
        message = (Object) exp.getValue();
    } catch (Exception ex) {
        System.out.println(ex.getMessage());
    }
    return message.toString();
    */
}

    return message.toString();
  }
}
