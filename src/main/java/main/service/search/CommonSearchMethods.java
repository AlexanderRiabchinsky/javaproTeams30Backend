package main.service.search;

import lombok.RequiredArgsConstructor;
import main.model.entities.Person;
import main.repository.PersonsRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class CommonSearchMethods {

    @Value("${spring.datasource.url}")
    private String url;
    @Value("${spring.datasource.username}")
    private String user;
    @Value("${spring.datasource.password}")
    private String pass;
    private Connection connection;
    private Statement statement;
    private final PersonsRepository personsRepository;

    public Statement getStatement() throws SQLException {
        connection = DriverManager.getConnection(url, user, pass);
        statement = connection.createStatement();
        return statement;
    }

    public LocalDateTime longToLocalDateTime(Long time) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(time), ZoneId.systemDefault());
    }

    public List<Person> findPersonByNameOrLastNameContains(String name) {
        List<Person> persons = null;
        String[] splitName = name.split("\\s+");
        if (splitName.length > 1) {
            persons = personsRepository.findPersonByFirstNameContainsIgnoreCaseOrLastNameContainsIgnoreCase(splitName[0], splitName[1]);
            persons.forEach(person -> System.out.println(person.getLastName() + " -- " + person.getFirstName()));
        }
        if (splitName.length < 2) {
            persons = personsRepository.findPersonByFirstNameContainsIgnoreCaseOrLastNameContainsIgnoreCase(name, name);
        }
        assert persons != null;
        if (persons.size() == 0 && splitName.length > 1) {
            persons = personsRepository.findPersonByFirstNameContainsIgnoreCaseOrLastNameContainsIgnoreCase(splitName[1], splitName[0]);
        }
        return persons;
    }

    public List getPageFromList(List list, int offset, int perPage) {
        Pageable pageable = PageRequest.of(offset, perPage);
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), list.size());
        return list.subList(start, end);
    }

    public List<Long> getIdsFromResultSet(ResultSet resultSet) throws SQLException {
        List<Long> Ids = new ArrayList<>();
        while (resultSet.next()) {
            Ids.add(resultSet.getLong("id"));
        }
        return Ids;
    }

    public void closeStatementAndConnection() throws SQLException {
        connection.close();
        statement.close();
    }

}
