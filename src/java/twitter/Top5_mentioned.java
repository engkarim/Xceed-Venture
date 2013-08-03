/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package twitter;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlPasswordInput;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
import com.mongodb.BasicDBList;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.util.JSON;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.scribe.builder.*;
import org.scribe.builder.api.*;
import org.scribe.model.*;
import org.scribe.oauth.*;

/**
 *
 * @author eams
 */
public class Top5_mentioned extends HttpServlet {

    /**
     * Processes requests for both HTTP
     * <code>GET</code> and
     * <code>POST</code> methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    private static final String PROTECTED_RESOURCE_URL = "https://api.twitter.com/1.1/statuses/mentions_timeline.json?count=5";

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String name = request.getParameter("name");
        String password = request.getParameter("password");
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        try {

            /***************** twitter user authentication ***************/
            
            OAuthService service = new ServiceBuilder().provider(TwitterApi.class).
                    apiKey("tCO91j77C7QLw8ONXBKvgg").
                    apiSecret("wlyFTrutrRmxUDn5DTl0AoVsW2MgMcz4LMq2lsfwk").build();
            Scanner in = new Scanner(System.in);

            System.out.println("=== Twitter's OAuth Workflow ===");
            System.out.println();

            // Obtain the Request Token
            System.out.println("Fetching the Request Token...");
            Token requestToken = service.getRequestToken();
            System.out.println("Got the Request Token!");
            System.out.println();
            System.out.println("Open the following URL and grant access to your account:");
            String url = null;
            url = service.getAuthorizationUrl(requestToken);
            System.out.println(url);
            WebClient webClient = new WebClient();
            HtmlPage page1 = webClient.getPage(url);
            System.out.println("after url ..............");
            HtmlForm form = (HtmlForm) page1.getElementById("oauth_form");

            HtmlSubmitInput button = form.getInputByValue("Authorize app");

            HtmlTextInput txtUser = form.getInputByName("session[username_or_email]");
            HtmlPasswordInput txtPass = form.getInputByName("session[password]");

            // Change the value of the text field
            txtUser.setValueAttribute(name);
            txtPass.setValueAttribute(password);

            // Now submit the form by clicking the button and get back the second page.
            HtmlPage page2 = button.click();


            String pin = page2.getElementsByTagName("code").get(0).getTextContent();
            System.out.println("===================================================");
            System.out.println(pin);


            System.out.println("Now go and authorize Scribe here:");
            System.out.println(service.getAuthorizationUrl(requestToken));

            System.out.println("And paste the verifier here");
            System.out.print(">>");
            System.out.println(pin);
            Verifier verifier = new Verifier(page2.getElementsByTagName("code").get(0).getTextContent());
            System.out.println(verifier);
            // Trade the Request Token and Verfier for the Access Token
            System.out.println("Trading the Request Token for an Access Token...");
            Token accessToken = service.getAccessToken(requestToken, verifier);
            System.out.println("Got the Access Token!");
            System.out.println("(if your curious it looks like this: " + accessToken + " )");
            //System.out.println();
            // Now let's go and ask for a protected resource!
            System.out.println("Now we're going to access a protected resource...");
            OAuthRequest trequest = new OAuthRequest(Verb.GET, PROTECTED_RESOURCE_URL);
            service.signRequest(accessToken, trequest);
            System.out.println(trequest.getBodyContents());
            Response tresponse = trequest.send();
            System.out.println("Got it! Lets see what we found...");

            System.out.println(tresponse.getBody());
            out.println(tresponse.getBody());
            System.out.println("Thats it man! Go and build something awesome with Scribe! :)");
            
               
            
            
            
            /****************** Store top 5 mentioned tweets in mongo database *******************/

            //connecting to mongo DB
            
            Mongo m = new Mongo();
            DB db = m.getDB("twitter");
            DBCollection coll = db.getCollection("top5MentionTweets");

            //Inserting tweets to database
            
            BasicDBList res = new BasicDBList();
            res = (BasicDBList) JSON.parse(tresponse.getBody().toString());

            for (Object obj : res) {
                coll.insert((DBObject) obj);
            }
            System.out.println("store tweets done.....");
            
             
            //close the connection
            m.close();
       
        } finally {
            out.close();
        }


    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP
     * <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP
     * <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
}
