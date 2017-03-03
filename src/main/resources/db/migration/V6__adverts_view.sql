CREATE VIEW rt_adverts AS
  SELECT adv.*
  FROM advert adv
    LEFT JOIN advert_author aut ON adv.id = aut.advertid
    LEFT JOIN sys_user usr ON aut.userid = usr.id
  WHERE usr.trustrate > 3000