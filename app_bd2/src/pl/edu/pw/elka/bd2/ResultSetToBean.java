package pl.edu.pw.elka.bd2;

import java.sql.ResultSet;

public interface ResultSetToBean<BeanType> {
	BeanType convert(ResultSet rs) throws Exception;
}
