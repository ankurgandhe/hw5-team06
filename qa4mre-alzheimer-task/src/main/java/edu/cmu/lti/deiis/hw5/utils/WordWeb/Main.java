package edu.cmu.lti.deiis.hw5.utils.WordWeb;

/*import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
*/
public class Main {

    public static int PRETTY_PRINT_INDENT_FACTOR = 4;
    public static String TEST_XML_STRING =
        "<?xml version=\"1.0\" ?><test attrib=\"moretest\">Turn this to JSON</test>";

    public static String XML2JSON(String string) {
    	  String jsonPrettyPrintString ="";
    /*	try {
            JSONObject xmlJSONObj = XML.toJSONObject(string);
            jsonPrettyPrintString=    xmlJSONObj.toString(PRETTY_PRINT_INDENT_FACTOR);
           // System.out.println(jsonPrettyPrintString);
        } catch (JSONException je) {
            System.out.println(je.toString());
        }*/
    	return jsonPrettyPrintString;
    }
}