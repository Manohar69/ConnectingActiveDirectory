package com.willeke.samples.ldap.jndi;

/**
 * A simple Example of Using JNDI As a Service with an administrative account.
 * 
 * We have been asked several times on how to perform this type of operation.
 */

import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

public class BasicJndiSearchsvc
{

    public static String ldapHostUrl = null;
    public static String ldapBindDN = null;
    public static String ldapBindPwd = null;

    public BasicJndiSearchsvc()
    {

    }

    /**
     * Generic method to obtain a reference to a DirContext
     */
    public DirContext getDirContext() throws Exception
    {
	Hashtable<String, String> env = new Hashtable<String, String>(11);
	env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
	env.put(Context.PROVIDER_URL, getLdapHostUrl());
	env.put(Context.SECURITY_AUTHENTICATION, "simple");
	env.put(Context.SECURITY_PRINCIPAL, ldapBindDN);
	env.put(Context.SECURITY_CREDENTIALS, ldapBindPwd);
	DirContext ctx = new InitialDirContext(env);
	return ctx;
    }

    /**
     * Generic method to obtain a reference to the user ENV
     */
    public static Hashtable<String, String> getUserEnv()
    {
	Hashtable<String, String> env = new Hashtable<String, String>(11);
	env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
	env.put(Context.PROVIDER_URL, getLdapHostUrl());
	env.put(Context.SECURITY_AUTHENTICATION, "simple");
	return env;
    }

    /**
     * 
     * @param attributeName
     *            String - Standard ID (userID)
     * @throws Exception
     *             -
     */
    public void doFindEntries(String attributeValue, String attributeName, String searchBase) throws Exception
    {
	DirContext xctx = getDirContext();
	SearchControls searchCons = getSimpleSearchControls();
	String searchFilter = "(" + attributeName + "=" + attributeValue + ")";
	// Search for objects with those matching attributes
	NamingEnumeration<?> answer = xctx.search(searchBase, searchFilter, searchCons);
	formatResults(answer);
	xctx.close();
    }

    public String getDN(String name, String ldapUserIDAttribute, String ldapBaseForusers) throws NamingException, Exception
    {
	String dn = null;
	String searchFilter = "(" + ldapUserIDAttribute + "=" + name + ")";
	SearchControls searchCons = getSimpleSearchControls();
	searchCons.setReturningAttributes(new String[0]);
	NamingEnumeration<?> results = getDirContext().search(ldapBaseForusers, searchFilter, searchCons);
	int thisCount = 0;
	if (results != null && results.hasMore())
	{
	    if (thisCount > 0)
	    {
		throw new NamingException("More than one Result was found!");
	    }
	    SearchResult si = (SearchResult) results.next();
	    dn = si.getNameInNamespace();
	    thisCount++;
	}
	return dn;
    }

    /**
     * I am just lazy and hate to type.
     * @return
     */
    public static SearchControls getSimpleSearchControls() {
	    SearchControls searchControls = new SearchControls();
	    searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
	    searchControls.setTimeLimit(30000);
	    return searchControls;
	}
    
    
    public static void testUserCredentials(String userFdn, String userPassword) throws NamingException
    {
	Hashtable<String, String> userEnv = getUserEnv();
	userEnv.put(Context.SECURITY_PRINCIPAL, userFdn);
	userEnv.put(Context.SECURITY_CREDENTIALS, userPassword);
    }

    
    /*
     * Generic method to format the NamingEnumeration returned from a search.
     */
    public static void formatResults(NamingEnumeration<?> enumer) throws Exception
    {
	int count = 0;
	try
	{
	    while (enumer.hasMore())
	    {
		SearchResult sr = (SearchResult) enumer.next();
		System.out.println("SEARCH RESULT:" + sr.getName());
		formatAttributes(sr.getAttributes());
		System.out.println("====================================================");
		count++;
	    }
	    System.out.println("Search returned " + count + " results");
	}
	catch (NamingException e)
	{
	    e.printStackTrace();
	}
    }
    
    /*
     * Generic method to format the Attributes .Displays all the multiple values of each Attribute in the Attributes
     */
    public static void formatAttributes(Attributes attrs) throws Exception
    {
	if (attrs == null)
	{
	    System.out.println("This result has no attributes");
	}
	else
	{
	    try
	    {
		for (NamingEnumeration<?> enumer = attrs.getAll(); enumer.hasMore();)
		{
		    Attribute attrib = (Attribute) enumer.next();

		    System.out.println("ATTRIBUTE :" + attrib.getID());
		    for (NamingEnumeration<?> e = attrib.getAll(); e.hasMore();)
		    {
			Object value = e.next();
			boolean canPrint = isAsciiPrintable(value);
			if (canPrint)
			{
			    System.out.println("\t\t        = " + value);
			}
			else
			{
			    System.out.println("\t\t        = <-value is not printable->");
			}
		    }
		}
	    }
	    catch (NamingException e)
	    {
		e.printStackTrace();
	    }
	}
    }
    
    /**
     * Check to see if this Object can be printed.
     * 
     * @param obj
     * @return
     */
    public static boolean isAsciiPrintable(Object obj)
    {
	String str = null;
	try
	{
	    str = (String) obj;
	}
	catch (Exception e)
	{
	    return false;
	    // TODO Auto-generated catch block e.printStackTrace();
	}
	if (str == null)
	{
	    return false;
	}
	int sz = str.length();
	for (int i = 0; i < sz; i++)
	{
	    if (isAsciiPrintable(str.charAt(i)) == false)
	    {
		return false;
	    }
	}
	return true;
    }

    
    /**
     * Reads from console and works in Eclipse
     * @param prompt
     * @return
     */
    private static String readLine(String prompt)
    {
	String line = null;
	Console c = System.console();
	if (c != null)
	{
	    line = c.readLine(prompt);
	}
	else
	{
	    System.out.print(prompt);
	    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
	    try
	    {
		line = bufferedReader.readLine();
	    }
	    catch (IOException e)
	    {
		// Ignore
	    }
	}
	return line;
    }

    static String getLdapHostUrl()
    {
	return ldapHostUrl;
    }

    static void setLdapHostUrl(String ldapHostUrl)
    {
	BasicJndiSearchsvc.ldapHostUrl = ldapHostUrl;
    }


    static String getLdapBindDN()
    {
        return ldapBindDN;
    }

    static void setLdapBindDN(String ldapBindDN)
    {
        BasicJndiSearchsvc.ldapBindDN = ldapBindDN;
    }

    static String getLdapBindPwd()
    {
        return ldapBindPwd;
    }

    static void setLdapBindPwd(String ldapBindPwd)
    {
        BasicJndiSearchsvc.ldapBindPwd = ldapBindPwd;
    }
    
    
    /**
     * @param args
     */
    public static void main(String[] args)
    {
	String ldapHostUrl = "ldap://" + args[0] + ":" + args[1];
	System.out.println("Performing LDAP Operations with the Following:");
	System.out.println("	ldapHostName = " + args[0]);
	System.out.println("	    ldapPort = " + args[1]);
	System.out.println("	 ldapHostUrl = " + ldapHostUrl);
	System.out.println("	      bindDn = " + args[2]);
	System.out.println("	   bindDnPwd = " + "**********"); //args[3]);
	System.out.println("	  searchBase = " + args[4]);
	System.out.println("	      filter = (" + args[5] + "=" + args[6] + ")");
	System.out.println("	      Scope: = SUBTREE_SCOPE");

	BasicJndiSearchsvc ss = new BasicJndiSearchsvc();
	//String ldapHostName = args[0];
	//String ldapPort = args[1];
	BasicJndiSearchsvc.setLdapHostUrl(ldapHostUrl);
	BasicJndiSearchsvc.setLdapBindDN(args[2]);
	BasicJndiSearchsvc.setLdapBindPwd(args[3]);
	String ldapBaseForusers = args[4];
	String ldapUserIDAttribute = args[5];

	while (true)
	{
	    String userFindValue = null;
	    String userFdn = null;
	    String userPassword = null;
	    // /Find a user
	    System.out.println("====================================================");
	    userFindValue = BasicJndiSearchsvc.readLine("Enter your loginID: (Q to Quit) ");
	    if(userFindValue.equalsIgnoreCase("Q"))
	    {
		System.exit(0);
	    }
	    userPassword = BasicJndiSearchsvc.readLine("Enter your Password: ");
	    System.out.println();
	    System.out.println("Return All Attributes for ALL users by: " + ldapUserIDAttribute + " = "+ userFindValue);
	    
	    try
	    {
		ss.doFindEntries(userFindValue, ldapUserIDAttribute, ldapBaseForusers);
	    }
	    catch (javax.naming.PartialResultException e)
	    {
		System.out.println("Find user DN by Name: Returned a PartialResultException\n" + e.getMessage());
	    }
	    catch (Exception e)
	    {
		System.out.println("Find user DN by Name: FAILED\n" + e.getMessage());
	    }
	    
	    System.out.println();
	    System.out.println("Find user DN by: " + ldapUserIDAttribute + " = "+ userFindValue);
	    System.out.println("====================================================");
	    try
	    {
		userFdn = ss.getDN(userFindValue, ldapUserIDAttribute, ldapBaseForusers);
		System.out.println("DN: " + userFdn);
	    }
	    catch (NamingException e)
	    {
		if (e instanceof javax.naming.PartialResultException)
		{
		    // ignore
		}
		else
		{
		    System.out.println("Find user DN by Name: Returned a PartialResultException\n" + e.getMessage());
		}
	    }
	    catch (Exception e)
	    {
		System.out.println("Find user DN by Name: FAILED\n" + e.getMessage());
	    }
	    System.out.println();
	    System.out.println("Test User's Credentials: ");
	    System.out.println("====================================================");
	    try
	    {
		testUserCredentials(userFdn, userPassword);
		System.out.println("Test User's Credentials: SUCCESS!");
	    }
	    catch (NamingException e)
	    {
		System.err.println("Test User's Credentials: FAILED\n" + e.getMessage());
	    }
	}// end while
    }
}