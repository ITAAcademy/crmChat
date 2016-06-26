/* Single line comment */
CREATE TABLE test_migration_1 (
  name VARCHAR(25) NOT NULL,
  PRIMARY KEY(name)
);

/*
Multi-line
comment
*/

-- Placeholder
INSERT INTO `test_migration_1` (name) VALUES ('Mr. T');