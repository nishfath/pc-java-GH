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
// Secret key for JWT token signing - in a real application, this should be stored securely
private final SecretKey jwtKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);
  
private boolean isAdmin(String authToken) {
    try {
        // Parse and validate the JWT token
        Claims claims = Jwts.parserBuilder()
            .setSigningKey(jwtKey)
            .build()
            .parseClaimsJws(authToken)
            .getBody();
        
        // Check if the role is ADMIN
        return "ADMIN".equals(claims.get("role", String.class));
    } catch (Exception ex) {
        System.out.println("Invalid auth token: " + ex.getMessage());
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
public String doPostLogin(@CookieValue(value = "auth", defaultValue = "notset") String auth, @RequestBody String password, HttpServletResponse response, HttpServletRequest request) throws Exception {
    String succ = "redirect:/admin/printSecrets";

    try {
        // Check existing auth token if present
        if (!auth.equals("notset")) {
            if (isAdmin(auth)) {
                request.getSession().setAttribute("auth", auth);
                return succ;
            }
        }

        // Parse and validate the password
        String[] pass = password.split("=");
        if (pass.length != 2) {
            return fail;
        }
        
        // Validate password (consider using password hashing in production)
        if (pass[1] != null && pass[1].length() > 0 && pass[1].equals("shiftleftsecret")) {
            // Create a JWT token with role ADMIN
            String jwtToken = Jwts.builder()
                .setSubject("admin")
                .claim("role", "ADMIN")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 3600000)) // 1 hour expiration
                .signWith(jwtKey)
                .compact();
            
            // Set secure cookie with the JWT token
            Cookie cookie = new Cookie("auth", jwtToken);
            cookie.setHttpOnly(true); // Prevent JavaScript access
            cookie.setSecure(true);   // Send only over HTTPS
            cookie.setPath("/");      // Available across the application
            response.addCookie(cookie);
            
            // Store in session for redirection handling
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
