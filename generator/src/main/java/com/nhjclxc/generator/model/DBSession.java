package com.nhjclxc.generator.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Connection;
import java.sql.Statement;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DBSession {
    private Connection connct;
    private Statement statement;
    private String sessionUuid;
}
