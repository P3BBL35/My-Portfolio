// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.servlets;

import java.io.IOException;
import java.io.BufferedReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import com.google.gson.Gson;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that returns comments data */
@WebServlet("/data")
public class DataServlet extends HttpServlet {

  private static final int ERROR = -1;
  private static final int TIME_DESCENDING = 0;
  private static final int TIME_ASCENDING = 1;

  private int numDisplay = 10;  // Default value.
  private int sortOrder;

  /**
   * Handles a GET request, and fetches all the comments data from the Datastore.
   */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    boolean toChange = getToChange(request);
    if (toChange) {
      numDisplay = Integer.parseInt(request.getHeader("numComments"));
      sortOrder = getSortOrder(request);
    }

    Query query = new Query("Comment").addSort("time", SortDirection.DESCENDING);
    if (sortOrder == TIME_ASCENDING) {
      query = new Query("Comment").addSort("time", SortDirection.ASCENDING);
    }

    UserService userService = UserServiceFactory.getUserService();
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);

    List<String> comments = new ArrayList<>();
    int index = 0;
    for (Entity entity : results.asIterable()) {
      String name = getDisplayName((String) (entity.getProperty("name")));
      String comment = (String) (entity.getProperty("comment"));

      final String result = name + ": " + comment;
      comments.add(result);
      
      index++;
      if (index >= numDisplay) {
        break;
      }
    }

    comments.add(numDisplay + ";" + sortOrder);

    Gson gson = new Gson();
    String json = gson.toJson(comments);
    response.setContentType("text/html;");
    response.getWriter().println(json);
  }

  /**
   * Sends a POST request to the server. In this case, the POST request simply adds a comment to
   * the list of comments, and redirects the user back to the same page so that the comment appears
   * on that page.
   */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    UserService userService = UserServiceFactory.getUserService();

    String comment = request.getParameter("comment");
    String time = DateTimeFormatter.ISO_DATE_TIME.format(ZonedDateTime.now(ZoneId.of("UTC")));

    Entity commentEntity = new Entity("Comment");

    commentEntity.setProperty("name", userService.getCurrentUser().getUserId());
    commentEntity.setProperty("comment", comment);
    commentEntity.setProperty("time", time);

    datastore.put(commentEntity);

    String url = "/comments.html";
    try {
      url = appendQuery(url, "commentSort=" + sortOrder).toString();
      url = appendQuery(url, "numComments=" + numDisplay).toString();
    } catch (URISyntaxException e) {}

    response.sendRedirect(url);
  }

  /**
   * Gets the sort order from the request header, and parses it accordingly.
   * @return an integer indicating the sort order.
   */
  private int getSortOrder(HttpServletRequest request) {
    String data = request.getHeader("commentSort");
    if (data.equals("timeAscending")) {
      return TIME_ASCENDING;
    } else if (data.equals("timeDescending")) {
      return TIME_DESCENDING;
    } else {
      return ERROR;
    }
  }

  /**
   * @return true if the fields need to be updated, false otherwise.
   */
  private boolean getToChange(HttpServletRequest request) {
    String data = request.getHeader("change");
    return data != null && data.equals("true");
  }

  /**
   * @return the display name of the currently logged-in user. 
   */
  private String getDisplayName(String id) {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Query query = new Query("User").setFilter(
        new Query.FilterPredicate("id", Query.FilterOperator.EQUAL, id));  
    PreparedQuery results = datastore.prepare(query);
    Entity entity = results.asSingleEntity();
    if (entity == null) {
      return "";
    }
    return (String) (entity.getProperty("name"));
  }

  /**
   * @return a new URI with the given query appended.
   */
  private URI appendQuery(String uri, String query) throws URISyntaxException {
    URI oldURL = new URI(uri);

    String newQuery = oldURL.getQuery();
    if (newQuery == null) {
      newQuery = query;
    } else {
      newQuery += "&" + query;
    }

    return new URI(oldURL.getScheme(), oldURL.getAuthority(), oldURL.getPath(), newQuery,
        oldURL.getFragment());
  }
}
