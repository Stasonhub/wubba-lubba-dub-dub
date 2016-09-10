CREATE TABLE address (
  id     BIGINT PRIMARY KEY AUTO_INCREMENT,
  street VARCHAR,
  house  VARCHAR,
  flat   VARCHAR
);

CREATE TABLE user (
  id    BIGINT PRIMARY KEY AUTO_INCREMENT,
  phone BIGINT,
  name  VARCHAR
);

CREATE TABLE advert (
  id              BIGINT PRIMARY KEY AUTO_INCREMENT,
--   userId          BIGINT,
--   addressId       BIGINT,
  publicationDate BIGINT,
  district        CHAR(3),
  price           INT,
  conditions      INT,
  description     VARCHAR
);
