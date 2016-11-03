CREATE TABLE user (
  id       BIGINT PRIMARY KEY AUTO_INCREMENT,
  phone    BIGINT      NOT NULL,
  name     VARCHAR(30) NOT NULL,
  password VARCHAR     NOT NULL,
);

CREATE TABLE advert (
  id                 BIGINT PRIMARY KEY AUTO_INCREMENT,
  userId             BIGINT       NOT NULL,
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
  FOREIGN KEY (userId) REFERENCES user (id)
);

CREATE TABLE photo (
  id       BIGINT PRIMARY KEY AUTO_INCREMENT,
  advertId BIGINT  NOT NULL,
  path     VARCHAR NOT NULL,
  main     BOOLEAN NOT NULL,
  FOREIGN KEY (advertId) REFERENCES advert (id)
);