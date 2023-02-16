package fr.isen.java2.db.daos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import fr.isen.java2.db.entities.Genre;

public class GenreDao {

	public List<Genre> listGenres() {
		List<Genre> listOfGenres = new ArrayList<>();
		try (Connection connection = DataSourceFactory.getDataSource().getConnection()) {
			try (Statement statement = connection.createStatement()) {
				try (ResultSet results = statement.executeQuery("SELECT * FROM genre")) {
					while (results.next()) {
						Genre genre = new Genre(results.getInt("idgenre"), results.getString("name"));
						listOfGenres.add(genre);
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return listOfGenres;
	}

	public Genre getGenre(String genreName) {
		try (Connection connection = DataSourceFactory.getDataSource().getConnection()) {
			try (PreparedStatement statement = connection.prepareStatement(
					"SELECT * FROM genre WHERE name=?")) {
				statement.setString(1, genreName);
				try (ResultSet results = statement.executeQuery()) {
					if (results.next()) {
						return new Genre(results.getInt("idgenre"),
								results.getString("name"));
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void addGenre(String genreName) {
		try (Connection connection = DataSourceFactory.getDataSource().getConnection()) {
			String sqlQuery = "INSERT INTO genre(name) " + "VALUES(?)";
			try (PreparedStatement statement = connection.prepareStatement(sqlQuery, Statement.RETURN_GENERATED_KEYS)) {
				statement.setString(1, genreName);
				statement.executeUpdate();
				ResultSet ids = statement.getGeneratedKeys();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
