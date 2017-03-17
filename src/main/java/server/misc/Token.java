package server.misc;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTDecodeException;

import java.io.UnsupportedEncodingException;

/**
 * Created by Psyycker on 01/02/2017.
 * For project SmartlistServer
 */
public class Token {

  String username;
  String password;

  public Token(String username, String password) {
    this.username = username;
    this.password = password;
  }


  public static JWT decodeToken(String token){
    try {
      JWT jwt = JWT.decode(token);
      return jwt;
    } catch (JWTDecodeException exception){
      //Invalid token
    }
    return null;
  }

  public String generate(){
    String token = null;
    try {
      token = JWT.create()
        .withIssuer(username)
        .sign(Algorithm.HMAC256(password));
    } catch (JWTCreationException exception){
      //Invalid Signing configuration / Couldn't convert Claims.
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
    return token;
  }

  public static void main(String[] args) {
    Token tok = new Token("user", "password");
    String token = tok.generate();
    if (token.equals("eyJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJ1c2VyIn0.uBLH__lm2wjADnXkzVffmcPIg2tQisD1Q5Fvr57IPm0"))
      System.out.println("C'est pareil");
      System.out.println(token);
    System.out.println(Token.decodeToken(token).getIssuer());
  }

}
