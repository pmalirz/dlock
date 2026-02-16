CREATE TABLE IF NOT EXISTS "@@tableName@@" (
  "LCK_KEY" varchar(1000) PRIMARY KEY,
  "LCK_HNDL_ID" varchar(100) not null,
  "CREATED_TIME" TIMESTAMP not null,
  "EXPIRE_SEC" integer not null
);
CREATE UNIQUE INDEX IF NOT EXISTS "@@tableName@@_HNDL_UX" ON "@@tableName@@" ("LCK_HNDL_ID");
