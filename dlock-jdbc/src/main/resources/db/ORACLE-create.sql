CREATE TABLE "@@tableName@@" (
  "LCK_KEY" VARCHAR2(1000) PRIMARY KEY,
  "LCK_HNDL_ID" VARCHAR2(100) not null,
  "CREATED_TIME" TIMESTAMP not null,
  "EXPIRE_SEC" INTEGER not null
)