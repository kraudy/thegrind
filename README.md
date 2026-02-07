# The Grind

Back in the grind

Spring boot and Angular on Ubuntu. Here we go.

## Installs

This could alse be set in a docker container for IT tests
``` bash
# Java 17 or 21 (Spring Boot 3.x recommends 17+)
sudo apt update
sudo apt install openjdk-21-jdk maven

# Node.js + npm (for Angular)
curl -fsSL https://deb.nodesource.com/setup_20.x | sudo -E bash -
sudo apt install -y nodejs
sudo npm install -g @angular/cli

node -v
npm -v
ng version

# PostgreSQL
sudo apt install postgresql postgresql-contrib
sudo systemctl start postgresql
sudo systemctl enable postgresql
```

## Backend

Start postgress
```bash
sudo -u postgres psql

# configuration route
/etc/postgresql/14/main/pg_hba.conf

#test
psql -U postgres -d inventorydb -W        # Estos dan error, revisar la codificacion
psql -h localhost -U postgres -d inventorydb -W # tcp

psql -U inventoryuser -d inventorydb -W   # Estos dan error, revisar la codificacion
psql -h localhost -U inventoryuser -d inventorydb -W # tcp
```

Simple database
```sql
CREATE DATABASE inventorydb
CREATE USER inventoryuser WITH PASSWORD 'pass'
GRANT ALL PRIVILEGES ON DATABASE inventorydb TO inventoryuser

\l  -- List databases
\q
\du -- show users
\c  -- Change database
\dt -- show tables inside database
```

Creat db user
```sql
DROP OWNED BY inventoryuser;
DROP ROLE inventoryuser;
CREATE USER inventoryuser WITH PASSWORD 'pass';
ALTER ROLE inventoryuser SET client_encoding TO 'utf8';
--ALTER ROLE inventoryuser SET default_transaction_isolation TO 'read committed';
ALTER ROLE inventoryuser SET timezone TO 'UTC';
GRANT ALL PRIVILEGES ON DATABASE inventorydb TO inventoryuser;
```

To get the maven project
```js
https://start.spring.io/


// init server
cd InventoryBackend
./mvnw clean spring-boot:run
```

## Front End

install
```bash
#   ng new InventoryFrontend --routing=false --style=css
ng new InventoryFrontend --routing=true --style=css
cd InventoryFrontend
npm install

```

```bash
ng generate component products
ng generate service product

ng generate component products/product-list
ng generate component products/product-form
ng generate service products/product
# After creating these add rename product.ts => to product.service.ts
# and product-list.ts => product-list.component.ts
# and create product.model.ts for the interfaces
```

Install Tailwind
```bash
# Instala Tailwind y dependencias
npm install tailwindcss @tailwindcss/postcss postcss
# Genera los archivos de configuración
npx tailwindcss init -p

Create postcss.config.js file
Update InventoryFrontend/src/styles.css
```

Start both
```bash
# Backend
./mvnw clean spring-boot:run
# Frontend
ng serve --proxy-config proxy.conf.json
```