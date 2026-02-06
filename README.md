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
psql -U postgres -d inventorydb -W
psql -h localhost -U postgres -d inventorydb -W # tcp
```

Simple database
```sql
CREATE DATABASE inventorydb
CREATE USER inventoryuser WITH PASSWORD 'pass'
GRANT ALL PRIVILEGES ON DATABASE inventorydb TO inventoryuser

\l -- List databases
\q
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
ng new InventoryFrontend --routing=false --style=css
cd InventoryFrontend
npm install

```

```bash
ng generate component products
ng generate service product
```

```bash
kraudy@Ubuntu:~/grind/thegrind/InventoryFrontend$ ng generate component products
CREATE src/app/products/products.css (0 bytes)
CREATE src/app/products/products.spec.ts (545 bytes)
CREATE src/app/products/products.ts (194 bytes)
CREATE src/app/products/products.html (23 bytes)
kraudy@Ubuntu:~/grind/thegrind/InventoryFrontend$ ng generate service product
CREATE src/app/product.spec.ts (326 bytes)
CREATE src/app/product.ts (112 bytes)
```
