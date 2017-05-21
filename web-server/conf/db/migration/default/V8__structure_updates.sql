CREATE TYPE districts_enum AS ENUM ('AV', 'CV', 'KR', 'MS', 'NS', 'PV', 'VH');

DROP VIEW rt_adverts;

ALTER TABLE advert ALTER district TYPE districts_enum USING district::districts_enum;
ALTER TABLE advert ADD COLUMN originType VARCHAR(60) NOT NULL;
ALTER TABLE advert ADD COLUMN originId INTEGER NOT NULL;
ALTER TABLE advert ADD CONSTRAINT origin_uniq UNIQUE (originType, originId);

ALTER TABLE photo ALTER COLUMN advertid TYPE integer;

ALTER TABLE advert_author
  ALTER COLUMN advertid TYPE integer,
  ALTER COLUMN userid TYPE integer;

ALTER TABLE sys_user ADD CONSTRAINT phone_uniq UNIQUE (phone);
