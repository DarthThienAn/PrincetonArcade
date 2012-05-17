package com.Tabletop;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

//adapted from code at http://www.mkyong.com/regular-expressions/how-to-validate-email-address-with-regular-expression/
public class PasswordChecker {

  
 
  
  private Pattern pattern;
  private Matcher matcher;
  
  private static final String PASSWORD_PATTERN = 
   "^[_A-Za-z0-9-]+$";
  
 public PasswordChecker(){
    pattern = Pattern.compile(PASSWORD_PATTERN);
 }
  
    /**
     * Validate hex with regular expression
     * @param hex hex for validation
     * @return true valid hex, false invalid hex
     */
 public boolean check(final String hex){
  
  matcher = pattern.matcher(hex);
  return matcher.matches();
  
 }
}
