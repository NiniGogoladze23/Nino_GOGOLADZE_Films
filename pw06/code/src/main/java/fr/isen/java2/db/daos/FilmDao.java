package fr.isen.java2.db.daos;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import fr.isen.java2.db.entities.Film;
import fr.isen.java2.db.entities.Genre;

public class FilmDao {

	public List<Film> listFilms() {
		List<Film> listOfFilms = new ArrayList<>();
		try (Connection connection = DataSourceFactory.getDataSource().getConnection()) {
			try (Statement statement = connection.createStatement()) {
				try (ResultSet results = statement
						.executeQuery("SELECT * FROM film JOIN genre ON film.genre_id = genre.idgenre")) {
					while (results.next()) {
						Film film = new Film(results.getInt("idfilm"),
								results.getString("title"),
								results.getDate("release_date").toLocalDate(),
								new Genre(results.getInt("idgenre"), results.getString("name")),
								results.getInt("duration"),
								results.getString("director"),
								results.getString("summary"));
						listOfFilms.add(film);
					}

				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return listOfFilms;
	}

	public List<Film> listFilmsByGenre(String genreName) {
		List<Film> listOfFilmsByGenre = new ArrayList<>();
		try (Connection connection = DataSourceFactory.getDataSource().getConnection()) {
			try (PreparedStatement statement = connection.prepareStatement(
					"SELECT * FROM film JOIN genre ON film.genre_id = genre.idgenre WHERE genre.name =?")) {
				statement.setString(1, genreName);
				try (ResultSet results = statement.executeQuery()) {
					while (results.next()) {
						Film film = new Film(results.getInt("idfilm"),
								results.getString("title"),
								results.getDate("release_date").toLocalDate(),
								new Genre(results.getInt("idgenre"), results.getString("name")),
								results.getInt("duration"),
								results.getString("director"),
								results.getString("summary"));
						listOfFilmsByGenre.add(film);

					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return listOfFilmsByGenre;
	}

	public Film addFilm(Film film) {
		try (Connection connection = DataSourceFactory.getDataSource().getConnection()) {
			String title = film.getTitle();
			LocalDate release_date = film.getReleaseDate();
			Integer genre_id = film.getGenre().getId();
			Integer duration = film.getDuration();
			String director = film.getDirector();
			String summary = film.getSummary();
			String sqlQuery = "INSERT INTO film(title,release_date,genre_id,duration,director,summary) VALUES(?,?,?,?,?,?)";
			try (PreparedStatement statement = connection.prepareStatement(sqlQuery, Statement.RETURN_GENERATED_KEYS)) {
				statement.setString(1, title);
				statement.setDate(2, Date.valueOf(release_date));
				statement.setInt(3, genre_id);
				statement.setInt(4, duration);
				statement.setString(5, director);
				statement.setString(6, summary);
				statement.executeUpdate();
				ResultSet ids = statement.getGeneratedKeys();
				if (ids.next()) {
					return new Film(ids.getInt(1), title, release_date, film.getGenre(), duration, director, summary);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
}
