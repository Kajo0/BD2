ALTER TABLE orders MODIFY order_date DEFAULT SYSDATE;



DROP TRIGGER vehicles_sequence_trigger;
DROP SEQUENCE vehicles_sequence;


CREATE SEQUENCE vehicles_sequence
  START WITH 1
  INCREMENT BY 1
  CACHE 100;

CREATE OR REPLACE TRIGGER vehicles_sequence_trigger
  BEFORE INSERT ON vehicles
  FOR EACH ROW
BEGIN
  :new.vehicle_id := vehicles_sequence.nextval;
END;



DROP TRIGGER clients_sequence_trigger;
DROP SEQUENCE clients_sequence;


CREATE SEQUENCE clients_sequence
  START WITH 1
  INCREMENT BY 1
  CACHE 100;

CREATE OR REPLACE TRIGGER clients_sequence_trigger
  BEFORE INSERT ON clients
  FOR EACH ROW
BEGIN
  :new.client_id := clients_sequence.nextval;
END;



DROP TRIGGER orders_sequence_trigger;
DROP SEQUENCE orders_sequence;


CREATE SEQUENCE orders_sequence
  START WITH 1
  INCREMENT BY 1
  CACHE 100;

CREATE OR REPLACE TRIGGER orders_sequence_trigger
  BEFORE INSERT ON orders
  FOR EACH ROW
BEGIN
  :new.order_id := orders_sequence.nextval;
END;