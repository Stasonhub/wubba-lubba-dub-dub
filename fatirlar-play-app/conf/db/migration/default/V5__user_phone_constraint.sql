ALTER TABLE sys_user
  ADD
  CONSTRAINT phone_number_constraint
  CHECK (sys_user.phone <= 9999999999 AND sys_user.phone >= 1000000000)