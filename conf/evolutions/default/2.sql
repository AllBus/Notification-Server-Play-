# DC schema

# --- !Ups
ALTER TABLE "EVENTS_TABLE" ADD (
	"author" integer NOT NULL DEFAULT 0,
	"dropDate" BIGINT NOT NULL DEFAULT CAST(0x7FFFFFFFFFFFFFFF AS bigint),
	"parent" integer NOT NULL DEFAULT 0,
	"accessGroup" integer NOT NULL DEFAULT 0
	);

# --- !Downs

ALTER TABLE EVENTS_TABLE DROP "author", DROP "dropDate", DROP "parent", DROP "accessGroup";