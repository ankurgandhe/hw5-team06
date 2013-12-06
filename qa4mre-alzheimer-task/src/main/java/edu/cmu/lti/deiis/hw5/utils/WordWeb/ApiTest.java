package edu.cmu.lti.deiis.hw5.utils.WordWeb;
/*
 * Copyright (c) 2012, IDM
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted
 * provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright notice, this list of
 *       conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright notice, this list
 *       of conditions and the following disclaimer in the documentation and/or other materials
 *       provided with the distribution.
 *     * Neither the name of the IDM nor the names of its contributors may be used to endorse or
 *       promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
//import org.json.JSONArray;
//import org.json.JSONObject;

//import fr.idm.sk.publish.api.client.light.Main;
//import fr.idm.sk.publish.api.client.light.SkPublishAPI;

public class ApiTest {
	private static final Pattern TAG_REGEX = Pattern.compile("<dt>(.+?)</dt>");
public static	 String 	baseURL="http://www.dictionaryapi.com";
public static	   String key1="references/medical/xml/";//test?key=8ce9289b-ee43-4d94-9aa2-f95f9a618b68";
public static	   String key2="?key=8ce9289b-ee43-4d94-9aa2-f95f9a618b68";

//public static	   String key="references/medical/xml/test?key=8ce9289b-ee43-4d94-9aa2-f95f9a618b68";
public static HashMap<String, String> wordDirectory=new HashMap<String, String>();

public static boolean GetElements(String word,String cat) {
	  
	
	  String dicts = "";
	  boolean lookup=false;
	try {cat=cat.toLowerCase();
	dicts=wordDirectory.get(cat);
	if(dicts==null)
		lookup=true;
	
	if(lookup){
	String key=key1+word+key2;
      //  if (args.length != 2) {
        //    System.err.println("need baseurl and accesskey in parameters");
          //  return;
        //}

        DefaultHttpClient httpClient = new DefaultHttpClient(new ThreadSafeClientConnManager());
      //  SkPublishAPI api = new SkPublishAPI(args[0] + "/api/v1", args[1], httpClient);
        SkPublishAPI api = new SkPublishAPI(baseURL+ "/api/v1", key, httpClient);
        api.setRequestHandler(new SkPublishAPI.RequestHandler() {
            public void prepareGetRequest(HttpGet request) {
                System.out.println(request.getURI());
                request.setHeader("Accept", "application/xml");
            }
        });

      
            System.out.println("*** Dictionaries");
             dicts = api.getDictionaries();
             wordDirectory.put(word, dicts);
             //System.out.println(dicts);
	}
            final Matcher matcher = TAG_REGEX.matcher(dicts);
            while (matcher.find()) {
              //  tagValues.add(matcher.group(1));
            	String texgGrp=matcher.group(1);
            	texgGrp=texgGrp.toLowerCase();
         if(texgGrp.indexOf(cat)>0)
        	 return true;
            }
            
         //   System.out.println("################parsed");
            String dictsArr2[]=dicts.split("</entry_list");
dicts=dictsArr2[0].trim();          
String ver="<entry_list version=\"1.0\">";
dicts=dicts.split(ver)[1].trim();
//System.out.println(dicts);
dicts=Main.XML2JSON(dicts);
int ind1=dicts.indexOf("[");
int ind2=dicts.lastIndexOf("]");
dicts=dicts.substring(ind1-1,ind2+1);
//System.out.println(dicts);
//dicts="[]";
            if(false){
            String splitter="\"entry\":";
            System.out.println(splitter);
            String dictsArr[]=dicts.split(splitter);
            dicts=dictsArr[1];
            int lastindex=dicts.lastIndexOf("\"version\":");
            dicts=dicts.substring(0,lastindex).trim();
            dicts=dicts.substring(0,dicts.length()-1).trim();
            }
            
            if(false){
          /* System.out.println(dicts);
            
            JSONArray dictionaries = new JSONArray(dicts);
            System.out.println(dictionaries);

            JSONObject dict = dictionaries.getJSONObject(0);
            System.out.println(dict);
            String dictCode = dict.getString("dictionaryCode /n");

            System.out.println("*** Search");
            System.out.println("*** Result list");
            JSONObject results = new JSONObject(api.search(dictCode, "ca", 1, 1));
            System.out.println(results);
            System.out.println("*** Spell checking");
            JSONObject spellResults = new JSONObject(api.didYouMean(dictCode, "dorg", 3));
            System.out.println(spellResults);
            System.out.println("*** Best matching");
            JSONObject bestMatch = new JSONObject(api.searchFirst(dictCode, "ca", "html"));
            System.out.println(bestMatch);

            System.out.println("*** Nearby Entries");
            JSONObject nearbyEntries = new JSONObject(api.getNearbyEntries(dictCode,
                    bestMatch.getString("entryId"), 3));
            System.out.println(nearbyEntries);*/
            	}
        } catch (Throwable t) {
            t.printStackTrace();
        }
        
        return false;
    }


public static	   void main(String args[])
{
	boolean b=GetElements("spider", "arachnid");
System.out.println(b);

}

}
