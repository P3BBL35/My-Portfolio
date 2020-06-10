package com.google.sps.servlets;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gson.Gson;
import java.util.List;
import java.util.ArrayList;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("text/html");
    UserService userService = UserServiceFactory.getUserService();

    List<String> list = new ArrayList<>();

    list.add(String.valueOf(userService.isUserLoggedIn()));

    if (userService.isUserLoggedIn()) {
      String userEmail = userService.getCurrentUser().getEmail();
      String urlToRedirectToAfterUserLogsOut = "/comments.html";
      String logoutUrl = userService.createLogoutURL(urlToRedirectToAfterUserLogsOut);
        
      list.add(logoutUrl);
      list.add(getUserNickname(userService.getCurrentUser().getUserId(),
          userService.getCurrentUser().getEmail()));
    } else {
      String urlToRedirectToAfterUserLogsIn = "/comments.html";
      String loginUrl = userService.createLoginURL(urlToRedirectToAfterUserLogsIn);

      list.add(loginUrl);
    }

    Gson gson = new Gson();
    String json = gson.toJson(list);
    response.getWriter().println(json);
  }

  /**
   * Gets the nickname of the user with the given ID. If the user does not have a
   * nickname set, then return the user's email as their display name.
   */
  private String getUserNickname(String id, String email) {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Query query = new Query("User").setFilter(new Query.FilterPredicate("id",
        Query.FilterOperator.EQUAL, id));
    PreparedQuery results = datastore.prepare(query);
    Entity entity = results.asSingleEntity();
    if (entity == null) {
      return email;
    }
    return (String) (entity.getProperty("name"));
  }
}
