package com.zhaoyanpeng.subzlib.mapper.typehandler;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicLong;

/**
 * AtomicLongTypeHandler
 *
 * @author zhaoyanpeng
 * @date 2022/12/4 10:10
 */
public class AtomicLongTypeHandler implements TypeHandler<AtomicLong> {
    @Override
    public void setParameter(PreparedStatement ps, int i, AtomicLong parameter, JdbcType jdbcType) throws SQLException {
        ps.setLong(i, parameter.get());
    }

    @Override
    public AtomicLong getResult(ResultSet rs, String columnName) throws SQLException {
        return new AtomicLong(rs.getLong(columnName));
    }

    @Override
    public AtomicLong getResult(ResultSet rs, int columnIndex) throws SQLException {
        return new AtomicLong(rs.getLong(columnIndex));
    }

    @Override
    public AtomicLong getResult(CallableStatement cs, int columnIndex) throws SQLException {
        return new AtomicLong(cs.getLong(columnIndex));
    }
}
