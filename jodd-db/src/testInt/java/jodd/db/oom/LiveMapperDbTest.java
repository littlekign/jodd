// Copyright (c) 2003-present, Jodd Team (http://jodd.org)
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are met:
//
// 1. Redistributions of source code must retain the above copyright notice,
// this list of conditions and the following disclaimer.
//
// 2. Redistributions in binary form must reproduce the above copyright
// notice, this list of conditions and the following disclaimer in the
// documentation and/or other materials provided with the distribution.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
// AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
// IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
// ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
// LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
// CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
// SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
// INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
// CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
// ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
// POSSIBILITY OF SUCH DAMAGE.

package jodd.db.oom;

import jodd.datetime.JDateTime;
import jodd.db.DbSession;
import jodd.db.oom.fixtures.Tester2;
import jodd.db.oom.sqlgen.DbEntitySql;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class LiveMapperDbTest extends DbBaseTest {

	/**
	 * DATABASES TO TEST!
	 */
	DbAccess[] databases = new DbAccess[]{
			new MySql(),
			new PostgreSql(),
			new HsqlDb(),
	};

	/**
	 * MySql.
	 */
	class MySql extends MySqlDbAccess {

		@Override
		public String getCreateTableSql() {
			return "create table TESTER2 (" +
						"ID			INT UNSIGNED NOT NULL AUTO_INCREMENT," +
						"NAME		VARCHAR(20)	not null," +
						"VALUE		INT NULL," +
						"TIME		TIMESTAMP," +
						"TIME2		TIMESTAMP," +
						"primary key (ID)" +
						')';
		}

		@Override
		public String getTableName() {
			return "TESTER2";
		}
	}

	/**
	 * PostgreSql.
	 */
	class PostgreSql extends PostgreSqlDbAccess {

		@Override
		public void initDb() {
			super.initDb();
			dboom.getTableNames().setLowercase(true);
			dboom.getColumnNames().setLowercase(true);
		}

		@Override
		public String getCreateTableSql() {
			return "create table TESTER2 (" +
						"ID			SERIAL," +
						"NAME		varchar(20)	NOT NULL," +
						"VALUE		integer NULL," +
						"TIME		TIMESTAMP," +
						"TIME2		TIMESTAMP," +
						"primary key (ID)" +
						')';
		}

		@Override
		public String getTableName() {
			return "TESTER2";
		}
	}

	/**
	 * HsqlDB.
	 */
	class HsqlDb extends HsqlDbAccess {

		@Override
		public String getCreateTableSql() {
			return "create table TESTER2 (" +
						"ID			integer GENERATED BY DEFAULT AS IDENTITY(START WITH 1) PRIMARY KEY," +
						"NAME		varchar(20)	NOT NULL," +
						"VALUE		integer NULL," +
						"TIME		TIMESTAMP," +
						"TIME2		TIMESTAMP" +
						')';
		}

		@Override
		public String getTableName() {
			return "TESTER2";
		}
	}

	// ---------------------------------------------------------------- test

	@Test
	void testLiveMapperDb() throws Exception {
		for (DbAccess db : databases) {
			System.out.println("\t" + db.getClass().getSimpleName());
			init();
			db.initDb();
			connect();

			dboom.registerEntity(Tester2.class);

			db.createTables();

			try {
				Tester2 tester2 = insertEntry();
				loadEntry(tester2);
			} finally {
				db.close();
			}
		}
	}


	protected Tester2 insertEntry() {
		DbSession session = new DbSession();

		Tester2 tester2 = new Tester2();
		tester2.id = 1;
		tester2.name = "Hello";
		tester2.value = Integer.valueOf(123);
		tester2.time = new JDateTime(2014, 1, 30, 10, 42, 34, 0).convertToSqlTimestamp();
		tester2.time2 = new JDateTime(2014, 1, 31, 11, 41, 32, 0);

		DbOomQuery dbOomQuery = DbOomQuery.query(session, DbEntitySql.insert(tester2));
		dbOomQuery.setGeneratedKey();
		int result = dbOomQuery.executeUpdate();

		assertEquals(1, result);
		session.closeSession();

		return tester2;
	}

	protected void loadEntry(Tester2 tester2) {
		DbSession session = new DbSession();

		DbOomQuery dbOomQuery = DbOomQuery.query(session, DbEntitySql.findById(Tester2.class, tester2.id));
		Tester2 tester21 = dbOomQuery.find(Tester2.class);

		assertNotNull(tester21);

		assertEquals(tester2.id, tester21.id);
		assertEquals(tester2.value, tester21.value);
		assertEquals(tester2.name, tester21.name);
		assertEquals(tester2.time, tester21.time);
		assertEquals(tester2.time2, tester21.time2);

		session.closeSession();
	}

}
