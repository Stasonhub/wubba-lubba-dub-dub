CREATE TYPE districts_enum AS ENUM ('AV', 'CV', 'KR', 'MS', 'NS', 'PV', 'VH');

DROP VIEW rt_adverts;

ALTER TABLE advert ALTER district TYPE districts_enum USING district::districts_enum;

ALTER TABLE photo ALTER COLUMN advertid TYPE integer;

ALTER TABLE advert_author
  ALTER COLUMN advertid TYPE integer,
  ALTER COLUMN userid TYPE integer;

CREATE VIEW rt_adverts AS
  SELECT adv.*
  FROM advert adv
    LEFT JOIN advert_author aut ON adv.id = aut.advertid
    LEFT JOIN sys_user usr ON aut.userid = usr.id
  WHERE
    usr.trustrate > 3000 AND TO_TIMESTAMP((adv.publicationdate + 1209600000) / 1000) > CURRENT_TIMESTAMP;
