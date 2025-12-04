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
    // Security fix: Do not evaluate user input with SpEL parser
    // Return the safely encoded user input instead
    
    // Set content type to explicitly handle the response as HTML
    response.setContentType("text/html; charset=UTF-8");
    
    // Use OWASP Encoder to properly escape the user input for HTML context
    return Encode.forHtml(foo);
    
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
