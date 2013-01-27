package pl.edu.pw.elka.bd2;

import java.sql.PreparedStatement;

public interface Query {
	void prepareQuery(PreparedStatement ps) throws Exception;
}
