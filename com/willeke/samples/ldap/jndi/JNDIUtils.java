/**
 * A simple collection of static utility methods we have found useful for working with JNDI
 */
package com.willeke.samples.ldap.jndi;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchResult;

/**
 * @author jim@willeke.com
 * A simple collection of static utility methods we have found useful for working with JNDI
 */
public class JNDIUtils
{
    
    /**
     * Generic method to obtain a reference to a LDAPS DirContext
     * @param ldapHostName
     * @param ldapPost
     * @param bindDn
     * @param bindDnPwd
     * @param path_to_truststore
     * @return
     * @throws Exception
     */
    public static DirContext getLDAPSDirContext(String ldapHostName, String ldapPort, String bindDn, String bindDnPwd, String path_to_truststore) throws Exception
    {
	Hashtable<String, String> ldapEnv = new Hashtable<String, String>(11);
	System.setProperty("javax.net.ssl.trustStore", path_to_truststore);
	ldapEnv.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
	ldapEnv.put(Context.PROVIDER_URL, "ldaps://" + ldapHostName + ":" + ldapPort);
	ldapEnv.put(Context.SECURITY_AUTHENTICATION, "simple");
	ldapEnv.put(Context.SECURITY_PRINCIPAL, bindDn);
	ldapEnv.put(Context.SECURITY_CREDENTIALS, bindDnPwd);
	// Create the initial context
	DirContext ldapContext = new InitialDirContext(ldapEnv);
	return ldapContext;
    }

    /**
     * Generic method to obtain a reference to a DirContext
     * 
     * @param ldapHostName
     * @param ldapPost
     * @param bindDn
     * @param bindDnPwd
     */
    public static DirContext getDirContext(String ldapHostName, String ldapPort, String bindDn, String bindDnPwd) throws Exception
    {
	Hashtable<String, String> ldapEnv = new Hashtable<String, String>(11);
	ldapEnv.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
	ldapEnv.put(Context.PROVIDER_URL, "ldap://" + ldapHostName + ":" + ldapPort);
	ldapEnv.put(Context.SECURITY_PRINCIPAL, bindDn);
	ldapEnv.put(Context.SECURITY_CREDENTIALS, bindDnPwd);
	// Create the initial context
	DirContext ldapContext = new InitialDirContext(ldapEnv);
	return ldapContext;
    }
    
    
    /**
     * Generic method to format the NamingEnumeration returned from a search.
     * @param enumer
     * @throws Exception
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
    }//formatResults 
    
    /**
     * Generic method to format the Attributes .Displays all the multiple values of each Attribute in the Attributes
     * @param attrs
     * @throws Exception
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
    }// end formatAttributes
    
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
    }// end isAsciiPrintable
}
