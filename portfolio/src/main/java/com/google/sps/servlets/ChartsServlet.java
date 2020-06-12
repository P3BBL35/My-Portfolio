package com.google.sps.servlets;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.Scanner;
import com.google.gson.Gson;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/charts")
public class ChartsServlet extends HttpServlet {

  private LinkedHashMap<String, ArrayList<Integer>> genderPopularity = new LinkedHashMap<>();

  @Override
  public void init() {
    Scanner scanner = new Scanner(getServletContext().getResourceAsStream(
        "/WEB-INF/anime-movie-popularity-by-gender.csv"));

    scanner.nextLine();  // First line is column titles.
    while (scanner.hasNextLine()) {
      String line = scanner.nextLine();
      String[] data = line.split(",");

      String gender = data[0];
      ArrayList<Integer> percentages = new ArrayList<>();
      for (int i = 1; i < data.length; i++) {
        percentages.add(Integer.parseInt(data[i]));
      }

      genderPopularity.put(gender, percentages);
    }
    scanner.close();
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("application/json");
    Gson gson = new Gson();
    String json = gson.toJson(genderPopularity);
    response.getWriter().println(json); 
  }
}
