package fr.isen.java2.db.daos;

import java.sql.Connection;
import java.sql.Statement;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.sql.ResultSet;
import java.util.List;
import fr.isen.java2.db.entities.Film;
import fr.isen.java2.db.entities.Genre;
import java.time.LocalDate;

public class FilmDaoTestCase {

	private FilmDao filmDao = new FilmDao();

	@Before
	public void initDb() throws Exception {
		Connection connection = DataSourceFactory.getDataSource().getConnection();
		Statement stmt = connection.createStatement();
		stmt.executeUpdate(
				"CREATE TABLE IF NOT EXISTS genre (idgenre INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT , name VARCHAR(50) NOT NULL);");
		stmt.executeUpdate(
				"CREATE TABLE IF NOT EXISTS film (\r\n"
						+ "  idfilm INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,\r\n"
						+ "  title VARCHAR(100) NOT NULL,\r\n"
						+ "  release_date DATETIME NULL,\r\n" + "  genre_id INT NOT NULL,\r\n"
						+ "  duration INT NULL,\r\n"
						+ "  director VARCHAR(100) NOT NULL,\r\n" + "  summary MEDIUMTEXT NULL,\r\n"
						+ "  CONSTRAINT genre_fk FOREIGN KEY (genre_id) REFERENCES genre (idgenre));");
		stmt.executeUpdate("DELETE FROM film");
		stmt.executeUpdate("DELETE FROM genre");
		stmt.executeUpdate("INSERT INTO genre(idgenre,name) VALUES (1,'Drama')");
		stmt.executeUpdate("INSERT INTO genre(idgenre,name) VALUES (2,'Comedy')");
		stmt.executeUpdate("INSERT INTO film(idfilm,title, release_date, genre_id, duration, director, summary) "
				+ "VALUES (1, 'Title 1', '2015-11-26 12:00:00.000', 1, 120, 'director 1', 'summary of the first film')");
		stmt.executeUpdate("INSERT INTO film(idfilm,title, release_date, genre_id, duration, director, summary) "
				+ "VALUES (2, 'My Title 2', '2015-11-14 12:00:00.000', 2, 114, 'director 2', 'summary of the second film')");
		stmt.executeUpdate("INSERT INTO film(idfilm,title, release_date, genre_id, duration, director, summary) "
				+ "VALUES (3, 'Third title', '2015-12-12 12:00:00.000', 2, 176, 'director 3', 'summary of the third film')");
		stmt.close();
		connection.close();
	}

	@Test
	public void shouldListFilms() {
		// WHEN
		List<Film> films = filmDao.listFilms();
		// THEN
		assertThat(films).hasSize(3);
		assertThat(films).extracting("id", "title", "duration", "director", "summary").containsOnly(
				tuple(1, "Title 1", 120, "director 1", "summary of the first film"),
				tuple(2, "My Title 2", 114, "director 2", "summary of the second film"),
				tuple(3, "Third title", 176, "director 3", "summary of the third film"));
		assertThat(films.stream().map(film -> film.getReleaseDate().toString()).toList()).containsOnly("2015-11-26",
				"2015-11-14", "2015-12-12");
		assertThat(films.stream().map(film -> film.getGenre()).toList()).extracting("id", "name")
				.containsOnly(tuple(1, "Drama"), tuple(2, "Comedy"));

	}

	@Test
	public void shouldListFilmsByGenre() {
		// WHEN
		List<Film> filmsByGenre = filmDao.listFilmsByGenre("Comedy");
		// THEN
		assertThat(filmsByGenre).hasSize(2);
		assertThat(filmsByGenre.stream().map(film -> film.getGenre()).toList()).extracting("id", "name")
				.containsOnly(tuple(2, "Comedy"));
	}

	@Test
	public void shouldAddFilm() throws Exception {
		// WHEN
		Film film = new Film(null, "Title of new film", LocalDate.of(1999, 1, 23), new Genre(2, "Comedy"), 123, "me",
				"Glad that it's the last test :D");
		filmDao.addFilm(film);
		// THEN
		Connection connection = DataSourceFactory.getDataSource().getConnection();
		Statement statement = connection.createStatement();
		ResultSet resultSet = statement.executeQuery(
				"SELECT * FROM film JOIN genre ON film.genre_id = genre.idgenre WHERE film.director = 'me'");
		assertThat(resultSet.next()).isTrue();
		assertThat(resultSet.getInt("idfilm")).isNotNull();
		assertThat(resultSet.getString("title")).isEqualTo("Title of new film");
		assertThat(resultSet.getDate("release_date").toLocalDate()).isEqualTo(LocalDate.of(1999, 1, 23));
		assertThat(resultSet.getString("director")).isEqualTo("me");
		assertThat(resultSet.getString("summary")).isEqualTo("Glad that it's the last test :D");
		assertThat(resultSet.getInt("idgenre")).isEqualTo(2);
		assertThat(resultSet.getString("name")).isEqualTo("Comedy");
		assertThat(resultSet.next()).isFalse();
		resultSet.close();
		statement.close();
		connection.close();
	}
}
