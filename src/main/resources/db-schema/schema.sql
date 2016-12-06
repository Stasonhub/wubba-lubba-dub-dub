CREATE TABLE user (
  id         BIGINT PRIMARY KEY AUTO_INCREMENT,
  phone      BIGINT      NOT NULL,
  name       VARCHAR(30) NOT NULL,
  trustRate  BIGINT      NOT NULL,
  password   VARCHAR     NOT NULL,
  registered BOOLEAN     NOT NULL
);

CREATE TABLE advert (
  id                 BIGINT PRIMARY KEY AUTO_INCREMENT,
  publicationDate    BIGINT       NOT NULL,
  district           VARCHAR(3)   NOT NULL,
  address            VARCHAR(100) NOT NULL,
  floor              INT          NOT NULL,
  maxFloor           INT          NOT NULL,
  rooms              INT          NOT NULL,
  sq                 INT          NOT NULL,
  price              INT          NOT NULL,
  withPublicServices BOOLEAN      NOT NULL,
  conditions         INT          NOT NULL,
  description        VARCHAR(250) NOT NULL,
  raw                BOOLEAN      NOT NULL
);

CREATE TABLE advert_author (
  isMain   BOOLEAN NOT NULL,
  advertId BIGINT  NOT NULL,
  userId   BIGINT  NOT NULL
);

CREATE TABLE photo (
  id       BIGINT PRIMARY KEY AUTO_INCREMENT,
  advertId BIGINT  NOT NULL,
  path     VARCHAR NOT NULL,
  main     BOOLEAN NOT NULL,
  FOREIGN KEY (advertId) REFERENCES advert (id)
);

CREATE TABLE importState (
  typeName       VARCHAR(100) PRIMARY KEY,
  lastImportDate BIGINT NOT NULL
);