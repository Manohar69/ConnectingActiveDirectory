/**
 * ADConnection - A Java class that encapsulates a JNDI connection to an Active Directory
 * Originally from Jeremy E. Mortis  mortis@ucalgary.ca  2002-07-03 
 * 
 * Note that password changes require an SSL connection to the Active Directory, but other types of calls do not.
 * 
 * To set up the SSL connection, check out:
 * http://java.sun.com/j2se/1.3/docs/tooldocs/win32/keytool.html
 * http://www.microsoft.com/windows2000/techinfo/planning/security/casetupsteps.asp
 * 
 * NOTE: Before running this sample, you must get the CA Certificate and store the certificate in a keystore that then needs to be referenced in the code.
 * 
 */
package com.willeke.samples.ldap.jndi;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.ModificationItem;

public class ADConnection
{
    static String BASE_NAME = ",cn=users,DC=activedirectory,DC=myorg,DC=ca";
    static String SERVER_ADDRESS = "activedirectory.yourdoamin.com";
    static String TEMPLATE_USER = "template";
    static String DOMAIN_NAME="yourdoamin.com";
    static String SECURITY_PRINCIPAL = "cn=ldapadmin";
    static String SECURITY_CREDENTIALS="adminPassword";
    static String TRUST_STORE="e:\\ldap\\keystore";
    DirContext ldapContext;
    
    /**
     * Create connection and set ldapContext
     */
    public ADConnection()
    {
	try
	{
	    // the keystore that holds trusted root certificates
	    System.setProperty("javax.net.ssl.trustStore", TRUST_STORE);
	    Hashtable<String, String> ldapEnv = new Hashtable<String, String>(11);
	    ldapEnv.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
	    ldapEnv.put(Context.PROVIDER_URL, "ldaps://" + SERVER_ADDRESS + ":636");
	    ldapEnv.put(Context.SECURITY_AUTHENTICATION, "simple");
	    ldapEnv.put(Context.SECURITY_PRINCIPAL, SECURITY_PRINCIPAL + BASE_NAME);
	    ldapEnv.put(Context.SECURITY_CREDENTIALS, SECURITY_CREDENTIALS);
	    ldapEnv.put(Context.SECURITY_PROTOCOL, "ssl");
	    ldapContext = new InitialDirContext(ldapEnv);
	}
	catch (Exception e)
	{
	    System.out.println(" bind error: " + e);
	    e.printStackTrace();
	    System.exit(-1);
	}
    }

    /**
     * Create a New User (Note no password is assigned)
     * @param username
     * @param surname
     * @param givenName
     */
    public void createNewUser(String username, String surname, String givenName)
    {
	try
	{
	    String distinguishedName = "cn=" + username + BASE_NAME;
	    Attributes newAttributes = new BasicAttributes(true);
	    Attribute oc = new BasicAttribute("objectclass");
	    oc.add("top");
	    oc.add("person");
	    oc.add("organizationalperson");
	    oc.add("user");
	    newAttributes.put(oc);
	    newAttributes.put(new BasicAttribute("sAMAccountName", username));
	    newAttributes.put(new BasicAttribute("userPrincipalName", username + "@" + SERVER_ADDRESS));
	    newAttributes.put(new BasicAttribute("cn", username));
	    newAttributes.put(new BasicAttribute("sn", surname));
	    newAttributes.put(new BasicAttribute("givenName", givenName));
	    newAttributes.put(new BasicAttribute("displayName", givenName + " " + surname));
	    System.out.println("Creating: " + username);
	    ldapContext.createSubcontext(distinguishedName, newAttributes);
	}
	catch (Exception e)
	{
	    System.out.println("create error: " + e);
	    e.printStackTrace();
	    System.exit(-1);
	}
    }

    /**
     * 
     * @param username - The new User Name
     * @param surname - The new users Surname
     * @param givenName - The new Users givenName
     */
    public void createCloneUser(String username, String surname, String givenName)
    {
	try
	{
	    Attributes modelAttributes = fetchUserAttributes(TEMPLATE_USER);
	    String distinguishedName = "cn=" + username + BASE_NAME;
	    Attributes newAttributes = new BasicAttributes(true);
	    newAttributes.put(modelAttributes.get("objectclass"));
	    newAttributes.put(modelAttributes.get("userAccountControl"));
	    newAttributes.put(new BasicAttribute("sAMAccountName", username));
	    newAttributes.put(new BasicAttribute("userPrincipalName", username + "@" + DOMAIN_NAME));
	    newAttributes.put(new BasicAttribute("cn", username));
	    newAttributes.put(new BasicAttribute("sn", surname));
	    newAttributes.put(new BasicAttribute("givenName", givenName));
	    newAttributes.put(new BasicAttribute("displayName", givenName + " " + surname));
	    System.out.println("distinguishedName: " + distinguishedName + " Attributes: " + newAttributes);
	    ldapContext.createSubcontext(distinguishedName, newAttributes);
	}
	catch (Exception e)
	{
	    System.out.println("create clone error: " + e);
	    e.printStackTrace();
	    System.exit(-1);
	}
    }

    /**
     * Simple utility to update String Attribute Values
     * @param username
     * @param attributeName
     * @param attributeValue
     */
    public void updateUser(String username, String attributeName, String attributeValue)
    {
	try
	{
	    System.out.println("updating " + username +"\n");
	    ModificationItem[] mods = new ModificationItem[1];
	    mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute(attributeName, attributeValue));
	    ldapContext.modifyAttributes("cn=" + username + BASE_NAME, mods);
	}
	catch (Exception e)
	{
	    System.out.println(" update error: " + e);
	    System.exit(-1);
	}
    }

    /**
     * Update User Password in Microsoft Active Directory
     * @param username
     * @param password
     */
    public void updateUserPassword(String username, String password)
    {
	try
	{
	    System.out.println("updating password...\n");
	    String quotedPassword = "\"" + password + "\"";
	    char unicodePwd[] = quotedPassword.toCharArray();
	    byte pwdArray[] = new byte[unicodePwd.length * 2];
	    for (int i = 0; i < unicodePwd.length; i++)
	    {
		pwdArray[i * 2 + 1] = (byte) (unicodePwd[i] >>> 8);
		pwdArray[i * 2 + 0] = (byte) (unicodePwd[i] & 0xff);
	    }
	    System.out.print("encoded password: ");
	    for (int i = 0; i < pwdArray.length; i++)
	    {
		System.out.print(pwdArray[i] + " ");
	    }
	    System.out.println();
	    ModificationItem[] mods = new ModificationItem[1];
	    mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute("UnicodePwd", pwdArray));
	    ldapContext.modifyAttributes("cn=" + username + BASE_NAME, mods);
	}
	catch (Exception e)
	{
	    System.out.println("update password error: " + e);
	}
    }

    /**
     * 
     * @param username
     * @return
     */
    public Attributes fetchUserAttributes(String username)
    {
	Attributes attributes = null;
	try
	{
	    System.out.println("fetching: " + username);
	    DirContext o = (DirContext) ldapContext.lookup("cn=" + username + BASE_NAME);
	    System.out.println("search done\n");
	    attributes = o.getAttributes("");
	    for (NamingEnumeration<?> ae = attributes.getAll(); ae.hasMoreElements();)
	    {
		Attribute attr = (Attribute) ae.next();
		String attrId = attr.getID();
		for (NamingEnumeration<?> vals = attr.getAll(); vals.hasMore();)
		{
		    String thing = vals.next().toString();
		    System.out.println(attrId + ": " + thing);
		}
	    }
	}
	catch (Exception e)
	{
	    System.out.println(" fetch error: " + e);
	}
	return attributes;
    }

    /**
     * Used for demonstration and testing only.
     * @param args
     */
    public static void main(String[] args)
    {
	System.setProperty("javax.net.debug", "all");
	ADConnection adc = new ADConnection();
	adc.createCloneUser("clone1", "Clone", "Clarissa");
	adc.updateUserPassword("clone1", "xxxx");
	adc.createNewUser("user1", "User", "Joe");
	Attributes a = adc.fetchUserAttributes("clone1");
    }
}