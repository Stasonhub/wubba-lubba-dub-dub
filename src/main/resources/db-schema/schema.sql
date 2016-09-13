CREATE TABLE user (
  id    BIGINT PRIMARY KEY AUTO_INCREMENT,
  phone BIGINT,
  name  VARCHAR
);

CREATE TABLE advert (
  id              BIGINT PRIMARY KEY AUTO_INCREMENT,
  user            BIGINT,
  publicationDate BIGINT,
  district        CHAR(3),
  address         VARCHAR,
  floor           INT,
  maxFloor        INT,
  rooms           INT,
  sq              INT,
  price           INT,
  conditions      INT,
  description     VARCHAR,
  FOREIGN KEY (user) REFERENCES user (id)
);
