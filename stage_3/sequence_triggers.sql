DROP TRIGGER vehicles_sequence_trigger;
DROP SEQUENCE vehicles_sequence;

DROP TRIGGER clients_sequence_trigger;
DROP SEQUENCE clients_sequence;

DROP TRIGGER orders_sequence_trigger;
DROP SEQUENCE orders_sequence;

DROP TRIGGER vversion_sequence_trigger;
DROP SEQUENCE vversion_sequence;




ALTER TABLE order_t MODIFY order_date DEFAULT SYSDATE;
ALTER TABLE service MODIFY begin_date DEFAULT SYSDATE;
ALTER TABLE service MODIFY cost DEFAULT 0;






CREATE SEQUENCE vehicles_sequence
  START WITH 1
  INCREMENT BY 1
  CACHE 100;

CREATE OR REPLACE TRIGGER vehicles_sequence_trigger
  BEFORE INSERT ON vehicle
  FOR EACH ROW
BEGIN
  :new.vehicle_id := vehicles_sequence.nextval;
END;



CREATE SEQUENCE clients_sequence
  START WITH 1
  INCREMENT BY 1
  CACHE 100;

CREATE OR REPLACE TRIGGER clients_sequence_trigger
  BEFORE INSERT ON client
  FOR EACH ROW
BEGIN
  :new.client_id := clients_sequence.nextval;
END;



CREATE SEQUENCE orders_sequence
  START WITH 1
  INCREMENT BY 1
  CACHE 100;

CREATE OR REPLACE TRIGGER orders_sequence_trigger
  BEFORE INSERT ON order_t
  FOR EACH ROW
BEGIN
  :new.order_id := orders_sequence.nextval;
END;



CREATE SEQUENCE vversion_sequence
  START WITH 1
  INCREMENT BY 1
  CACHE 100;

CREATE OR REPLACE TRIGGER vversion_sequence_trigger
  BEFORE INSERT ON vehicle_version
  FOR EACH ROW
BEGIN
  :new.vversion_id := vversion_sequence.nextval;
END;



/* Zmiana kosztu realizacji us³ugi, zwiêksza/zmniejsza koszt zamówienia */

CREATE OR REPLACE TRIGGER update_order_cost_by_service
before UPDATE OF cost ON service
FOR EACH ROW
BEGIN
  if (:new.cost <> :old.cost) then
    update order_t set value = value + :new.cost - :old.cost where order_id = :old.order_id;
  end if;
END;
/


/* Zliczanie sumy kosztów zamówieñ dla klienta o podanym id */

create or replace function get_sum_of_client_orders (i_client_id in client.client_id%type) return order_t.value%type is
  val order_t.value%type;
begin
  select sum(value) into val from order_t where client_id = i_client_id;

  if val is null then
    val := 0;
  end if;
 
  return val;
end;
/

set serveroutput on;
begin
  dbms_output.put_line('Sum of orders: ' || get_sum_of_client_orders(0));
end;
/


/* Procedura licz¹ca i wypisuj¹ca klientów którzy nie z³o¿yli zamówienia */

create or replace procedure get_clients_without_orders is
  amount NUMBER(5,0);
  cursor cur is select * from client where client_id not in (select distinct client_id from order_t);
  os client%rowtype;
begin
  amount := 0;
  open cur;
  
  loop
    fetch cur into os;
    exit when cur%notfound;
    dbms_output.put_line(os.client_id || ': ' || os.first_name || ' ' || os.last_name || ' ' || os.name);
    amount := amount + 1;
  end loop;
  
  close cur;

  dbms_output.put_line('  Total amount of clients without orders: ' || amount);
end;
/

set serveroutput on;
begin
  get_clients_without_orders();
end;
/