package com.company;


import org.h2.tools.Server;
import spark.ModelAndView;
import spark.Session;
import spark.Spark;
import spark.template.mustache.MustacheTemplateEngine;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;


public class Main {

    public static void main(String[] args)  throws SQLException{
        Server.createWebServer().start();
        Connection conn = DriverManager.getConnection("jdbc:h2:./main");
        createTables(conn);

        Spark.init();

        Spark.get(
                "/",
                ((request, response) -> {
                    HashMap craftBeers = new HashMap();
                    Session session = request.session();
                    String userName = session.attribute("loginName");
                    String userPassword = session.attribute("loginPassword");
                    User user = selectUser(conn, userName);
                    if(user == null){
                        return new ModelAndView(craftBeers, "login.html");

                    }else {
                        ArrayList<Beers> beer = selectBeers(conn, user.id);

                        craftBeers.put("loginName", userName);
                        craftBeers.put("userPassword", userPassword);
                        craftBeers.put("beers", beer);

                        return new ModelAndView(craftBeers, "home.html");
                    }
                }),
                new MustacheTemplateEngine()
        );


        Spark.post(
                "/login",
                ((request, response) ->{
                    Session session = request.session();
                    String enterName = request.queryParams("loginName");
                    String enterPassword = request.queryParams("loginPassword");
                    String userId = request.queryParams("userId");
                    User user = selectUser(conn, enterName);

                    if (enterName == null || enterPassword == null) {
                        throw new Exception("Enter name and password");
                    }

                    if (user == null) {
                        insertUser(conn, enterName, enterPassword);
                    } else if (!user.password.equals(enterPassword)) {
                        throw new Exception("Enter valid password");
                    }
                    session.attribute("loginName", enterName);
                    session.attribute("loginPassword", enterPassword);
                    session.attribute("userId", userId);
                    response.redirect("/");
                    return "";

                })
        );

        Spark.post(
                "/logout",
                ((request, response) -> {
                    Session session = request.session();
                    session.invalidate();
                    response.redirect("/");
                    return "";
                })
        );

        Spark.post(
                "create-beer-input",
                ((request, response) -> {
                    Session session = request.session();
                    String enterName = session.attribute("loginName");
                    User user = selectUser(conn, enterName);
                    if (user == null) {
                        throw new Exception("login please ");

                    }
                    int userId = user.id;
                    String beerName = request.queryParams("enterBeerName");
                    String brewery = request.queryParams("enterBrewery");

                    String state = request.queryParams("enterState");
                    String year = request.queryParams("enterYear");

                    String tastedString = request.queryParams("enterTasted");
                    if (beerName == null) {
                        throw new Exception("Enter a beer name");
                    }
                    boolean tasted = Boolean.parseBoolean(tastedString);
                    insertBeer(conn, userId, beerName, brewery, year, state, tasted);


                    response.redirect("/");
                    return " ";

                })
        );

        Spark.get(
                "/edit-beer-input",
                ((request, response) -> {
                    HashMap beers = new HashMap();
                    String b = request.queryParams("beerId");
                    int beerId = Integer.parseInt(b);
                    Beers beer = selectBeers(conn, beerId);
                    beers.put("beer", beer);
                    return  new ModelAndView(beers, "edit.html");
                })
        );

        Spark.post(
                "/edit",
                ((request, response) -> {
                    Session session = request.session();
                    String enterName = session.attribute("loginName");
                    int id = session.attribute("beerId");
                    User user = selectUser(conn, enterName);
                    if (user == null) {
                        throw new Exception("Please log in");
                    }
                    String beerName = request.queryParams("enterBeerName");
                    String brewery = request.queryParams("enterBrewery");
                    String year = request.queryParams("enterYear");
                    String state = request.queryParams("enterState");
                    String tastedString = request.queryParams("enterTasted");
                    if (beerName == null) {
                        throw new Exception("Enter a beer");
                    }
                    boolean seen = Boolean.parseBoolean(tastedString);
                    editBeerInput(conn, id, beerName, brewery, year, state, seen);
                    response.redirect("/");
                    return "";


                })
        );

        Spark.post(
                "/delete-film",
                ((request, response) -> {

                    String beerIdString = request.queryParams("deleteBeerId");
                    int beerId = Integer.parseInt(beerIdString);
                    deleteBeers (conn, beerId);
                    response.redirect("/");
                    return "";
                })
        );

    }

    public static void createTables(Connection conn) throws SQLException{
        Statement stmt = conn.createStatement();
        stmt.execute("CREATE TABLE IF NOT EXISTS users(id IDENTITY, name VARCHAR, password VARCHAR)");
        stmt.execute("CREATE TABLE IF NOT EXISTS beer(beer_id IDENTITY, user_id INT, beer_name VARCHAR, brewery VARCHAR," +
                " year VARCHAR, state VARCHAR, tasted BOOLEAN)");

    }


    public static void insertUser(Connection conn, String name, String password) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("INSERT INTO users VALUES (NULL, ?, ?)");
        stmt.setString(1, name);
        stmt.setString(2, password);
        stmt.execute();
    }


    public static User selectUser(Connection conn, String name) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM users WHERE name = ?");
        stmt.setString(1, name);
        ResultSet queryOut = stmt.executeQuery();
        if (queryOut.next()) {
            int id = queryOut.getInt("id");
            String password = queryOut.getString("password");
            return new User(id, name, password);
        }
        return null;
    }


    public static ArrayList<User> selectUsers(Connection conn) throws SQLException {
        ArrayList<User> users = new ArrayList<>();
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM users");
        ResultSet queryOut = stmt.executeQuery();
        while (queryOut.next()) {
            int id = queryOut.getInt("users.id");
            String userName = queryOut.getString("users.name");
            String userPassword = queryOut.getString("users.password");
            User user = new User(id, userName, userPassword);
            users.add(user);
        }
        return users;
    }

    public static void insertBeer(Connection conn, int beerId, String beerName, String brewery, String year, String state, boolean tasted) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("INSERT INTO beer VALUES (NULL, ?, ?, ?, ?, ?, ?)");
        stmt.setInt(1, beerId);
        stmt.setString(2, beerName);
        stmt.setString(3, brewery);
        stmt.setString(4, year);
        stmt.setString(5, state);
        stmt.setBoolean(6, tasted);

    }

    public static Beers selectBeers(Connection conn, int id) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM beer INNER JOIN users ON beer.user_Id = user_id WHERE beer_id = ?");
        stmt.setInt(1, id);
        ResultSet queryOut = stmt.executeQuery();

        if (queryOut.next()) {
            int beerId = queryOut.getInt("beer_id");
            int userId = queryOut.getInt("user_id");
            String beerName = queryOut.getString("beerName");
            String brewery = queryOut.getString("brewery");
            boolean tasted = queryOut.getBoolean("tasted");
            String state = queryOut.getString("state");
            String year = queryOut.getString("year");

            return new Beers(beerId, userId, beerName, brewery, tasted, state, year);
        }
        return null;

    }

    public static ArrayList<Beers> selectBeer(Connection conn, int id) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM beer INNER JOIN users ON beer.userId = users.id WHERE beer.id = ?");
        stmt.setInt(1, id);
        ResultSet queryOut = stmt.executeQuery();
        ArrayList<Beers> selectBeers = new ArrayList<>();
        while (queryOut.next()) {
            int beerId = queryOut.getInt("beer.id");
            int userId = queryOut.getInt("user.id");
            String beerName = queryOut.getString("beer.beerName");
            String brewery = queryOut.getString("beer.brewery");
            boolean tasted = queryOut.getBoolean("beer.tasted");
            String state = queryOut.getString("beer.state");
            String year = queryOut.getString("beer.year");

            Beers beer = new Beers(beerId, userId, beerName, brewery, tasted, state, year);
            selectBeers.add(beer);
        }
        return selectBeers;

    }

    public static void editBeerInput(Connection conn, int id, String beerName, String brewery, String year,
                                     String state, boolean tasted) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("UPDATE beers SET beerName =? SET brewery =? SET year? SET state? SET tasted=? ");
        stmt.setInt(1, id);
        stmt.setString(2, beerName);
        stmt.setString(3, brewery);
        stmt.setString(4, year);
        stmt.setString(5, state);
        stmt.setBoolean(6, tasted);
        stmt.execute();
    }

    public static void deleteBeers(Connection conn, int id) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("DELETE FROM beers WHERE id = ?");
        stmt.setInt(1, id);
        System.out.println(id);
        stmt.execute();
    }


}
