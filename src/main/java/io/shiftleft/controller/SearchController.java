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
    // Don't use SpEL to evaluate user input - it's a security risk
    // Instead, treat the input as plain text and encode it for safe HTML output
    String message;
    
    if (foo != null && !foo.isEmpty()) {
        message = "Search query: " + foo;
    } else {
        message = "No search query provided";
    }
    
    // Use OWASP Encoder to prevent XSS attacks
    return Encode.forHtml(message);
}

    return message.toString();
  }
}
