package io.shiftleft.controller;

import io.shiftleft.model.AuthToken;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;


/**
 * Admin checks login
 */
@Controller
public class AdminController {
  private String fail = "redirect:/";

  // helper
// Secure signing key - in production, this should be stored securely and not in code
private static final SecretKey JWT_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);

private boolean isAdmin(String authToken) {
    try {
        // Validate and parse the JWT token
        Claims claims = Jwts.parserBuilder()
            .setSigningKey(JWT_KEY)
            .build()
            .parseClaimsJws(authToken)
            .getBody();
        
        // Check if the user has admin role
        return claims.get("role", String.class).equals("ADMIN");
    } catch (Exception ex) {
        System.out.println("Invalid authentication token: " + ex.getMessage());
        return false;
    }
}


  //
  @RequestMapping(value = "/admin/printSecrets", method = RequestMethod.POST)
  public String doPostPrintSecrets(HttpServletResponse response, HttpServletRequest request) {
    return fail;
  }


  @RequestMapping(value = "/admin/printSecrets", method = RequestMethod.GET)
  public String doGetPrintSecrets(@CookieValue(value = "auth", defaultValue = "notset") String auth, HttpServletResponse response, HttpServletRequest request) throws Exception {

    if (request.getSession().getAttribute("auth") == null) {
      return fail;
    }

    String authToken = request.getSession().getAttribute("auth").toString();
    if(!isAdmin(authToken)) {
      return fail;
    }

    ClassPathResource cpr = new ClassPathResource("static/calculations.csv");
    try {
      byte[] bdata = FileCopyUtils.copyToByteArray(cpr.getInputStream());
      response.getOutputStream().println(new String(bdata, StandardCharsets.UTF_8));
      return null;
    } catch (IOException ex) {
      ex.printStackTrace();
      // redirect to /
      return fail;
    }
  }

  /**
   * Handle login attempt
   * @param auth cookie value base64 encoded
   * @param password hardcoded value
   * @param response -
   * @param request -
   * @return redirect to company numbers
   * @throws Exception
   */
@RequestMapping(value = "/admin/login", method = RequestMethod.POST)
public String doPostLogin(@CookieValue(value = "auth", defaultValue = "notset") String auth,
                          @RequestBody String password,
                          HttpServletResponse response,
                          HttpServletRequest request) throws Exception {
    String succ = "redirect:/admin/printSecrets";
    String fail = "redirect:/admin/login";

    try {
        // Check if auth cookie exists and is valid
        if (!auth.equals("notset")) {
            if (isAdmin(auth)) {
                request.getSession().setAttribute("auth", auth);
                return succ;
            }
        }

        // Split password=value
        String[] pass = password.split("=");
        if (pass.length != 2) {
            return fail;
        }
        
        // Compare password - consider using a more secure password verification method
        if (pass[1] != null && pass[1].length() > 0 && pass[1].equals("shiftleftsecret")) {
            // Generate JWT token with admin role
            String jwtToken = Jwts.builder()
                .claim("role", "ADMIN")
                .setSubject("admin-user")
                .signWith(JWT_KEY)
                .compact();
            
            // Set the JWT token as a cookie
            Cookie authCookie = new Cookie("auth", jwtToken);
            authCookie.setHttpOnly(true);  // Prevent JavaScript access
            authCookie.setSecure(true);    // Send only over HTTPS
            authCookie.setPath("/");       // Set cookie path
            response.addCookie(authCookie);

            // Store in session for use after redirection
            request.getSession().setAttribute("auth", jwtToken);

            return succ;
        }
        return fail;
    } catch (Exception ex) {
        ex.printStackTrace();
        return fail;
    }
}

  }

  /**
   * Same as POST but just a redirect
   * @param response
   * @param request
   * @return redirect
   */
  @RequestMapping(value = "/admin/login", method = RequestMethod.GET)
  public String doGetLogin(HttpServletResponse response, HttpServletRequest request) {
    return "redirect:/";
  }
}
