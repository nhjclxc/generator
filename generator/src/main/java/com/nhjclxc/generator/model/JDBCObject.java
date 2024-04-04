package com.nhjclxc.generator.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JDBCObject {
    private String driverClassName = "com.mysql.cj.jdbc.Driver";
    private String jdbcUrl;
    private String username;
    private String password;
}
