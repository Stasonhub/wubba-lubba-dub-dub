CREATE TABLE user (
  id    BIGINT PRIMARY KEY AUTO_INCREMENT,
  phone BIGINT  NOT NULL,
  name  VARCHAR NOT NULL
);

CREATE TABLE advert (
  id              BIGINT PRIMARY KEY AUTO_INCREMENT,
  userId          BIGINT     NOT NULL,
  publicationDate BIGINT     NOT NULL,
  district        VARCHAR(3) NOT NULL,
  address         VARCHAR    NOT NULL,
  floor           INT        NOT NULL,
  maxFloor        INT        NOT NULL,
  rooms           INT        NOT NULL,
  sq              INT        NOT NULL,
  price           INT        NOT NULL,
  conditions      INT        NOT NULL,
  description     VARCHAR    NOT NULL,
  mainPhotoUrl    VARCHAR    NOT NULL,
  FOREIGN KEY (userId) REFERENCES user (id)
);
