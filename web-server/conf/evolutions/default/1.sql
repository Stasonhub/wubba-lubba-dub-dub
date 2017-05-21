# Base structures

# --- !Ups

CREATE TYPE DISTRICTS_ENUM AS ENUM ('AV', 'CV', 'KR', 'MS', 'NS', 'PV', 'VH');

CREATE TABLE sys_user (
  id         SERIAL PRIMARY KEY,
  phone      BIGINT       NOT NULL,
  name       VARCHAR(120) NOT NULL,
  trustRate  BIGINT       NOT NULL,
  password   VARCHAR,
  registered BOOLEAN      NOT NULL,
  CONSTRAINT phone_number_constraint CHECK (sys_user.phone <= 9999999999 AND sys_user.phone >= 1000000000),
  CONSTRAINT phone_uniq UNIQUE (phone)
);

CREATE TABLE advert (
  id                 SERIAL PRIMARY KEY,
  publicationDate    BIGINT           NOT NULL,
  district           DISTRICTS_ENUM   NOT NULL,
  address            VARCHAR(250)     NOT NULL,
  floor              INT              NOT NULL,
  maxFloor           INT              NOT NULL,
  rooms              INT              NOT NULL,
  sq                 INT              NOT NULL,
  price              INT              NOT NULL,
  withPublicServices BOOLEAN          NOT NULL,
  conditions         INT              NOT NULL,
  description        VARCHAR(15000)   NOT NULL,
  latitude           DOUBLE PRECISION NOT NULL,
  longitude          DOUBLE PRECISION NOT NULL,
  bedrooms           INT              NOT NULL,
  beds               INT              NOT NULL,
  originType         VARCHAR(60)      NOT NULL,
  originId           INTEGER          NOT NULL,
  CONSTRAINT origin_uniq UNIQUE (originType, originId)
);

CREATE TABLE advert_author (
  advertId INTEGER NOT NULL,
  userId   INTEGER NOT NULL
);

CREATE TABLE photo (
  id       SERIAL PRIMARY KEY,
  advertId INTEGER NOT NULL,
  path     VARCHAR NOT NULL,
  main     BOOLEAN NOT NULL,
  hash     BIGINT  NOT NULL,
  FOREIGN KEY (advertId) REFERENCES advert (id)
);

CREATE TABLE importState (
  typeName       VARCHAR(100) PRIMARY KEY,
  lastImportDate BIGINT NOT NULL
);

INSERT INTO importState (typeName, lastImportDate) VALUES ('AVT', 0);
INSERT INTO importState (typeName, lastImportDate) VALUES ('TTK', 0);

# --- !Downs

DROP TABLE importState;
DROP TABLE photo;
DROP TABLE advert_author;
DROP TABLE advert;
DROP TABLE sys_user;
DROP TYPE DISTRICTS_ENUM;