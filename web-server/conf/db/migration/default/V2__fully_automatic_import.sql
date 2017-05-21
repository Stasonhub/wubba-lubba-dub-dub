ALTER TABLE advert
  DROP COLUMN trustRate;

ALTER TABLE advert
  DROP COLUMN raw;

ALTER TABLE advert_author
  DROP COLUMN isMain;

INSERT INTO importState (typeName, lastImportDate) VALUES ('TTK', 0);