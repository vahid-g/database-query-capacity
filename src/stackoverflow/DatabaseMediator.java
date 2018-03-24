package stackoverflow;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import database.DatabaseConnection;
import database.DatabaseType;

public class DatabaseMediator {

	private static final Logger LOGGER = Logger.getLogger(DatabaseMediator.class.getName());

	private Connection conn;

	private DatabaseConnection dc;

	public DatabaseMediator() throws IOException, SQLException {
		dc = new DatabaseConnection(DatabaseType.STACKOVERFLOW);
		conn = dc.getConnection();
	}

	public List<QuestionDAO> loadQuestions() throws SQLException {
		String query = "select ID, Title, AcceptedAnswerId from table questions;";
		List<QuestionDAO> result = new ArrayList<QuestionDAO>();
		try (Statement stmt = conn.createStatement()) {
			ResultSet rs = stmt.executeQuery(query);
			while (rs.next()) {
				String id = rs.getString("Id");
				String question = rs.getString("Title");
				String answer = rs.getString("AcceptedAnswerId");
				QuestionDAO dao = new QuestionDAO(id, question, answer);
				result.add(dao);
			}
		} catch (SQLException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		}
		return result;
	}

	public List<AnswerDAO> loadAnswers() throws SQLException {
		List<AnswerDAO> result = new ArrayList<AnswerDAO>();
		String query = "select Id, Body from table questions;";
		try (Statement stmt = conn.createStatement()) {
			ResultSet rs = stmt.executeQuery(query);
			while (rs.next()) {
				String id = rs.getString("Id");
				String answer = rs.getString("Body");
				AnswerDAO dao = new AnswerDAO(id, answer);
				result.add(dao);
			}
		} catch (SQLException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		}
		return result;
	}

}
