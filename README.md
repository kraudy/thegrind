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
```

```bash
kraudy@Ubuntu:~/grind/thegrind/InventoryFrontend$ ng generate component products/product-list
CREATE src/app/products/product-list/product-list.css (0 bytes)
CREATE src/app/products/product-list/product-list.spec.ts (567 bytes)
CREATE src/app/products/product-list/product-list.ts (209 bytes)
CREATE src/app/products/product-list/product-list.html (27 bytes)
kraudy@Ubuntu:~/grind/thegrind/InventoryFrontend$ ng generate component products/product-form
CREATE src/app/products/product-form/product-form.css (0 bytes)
CREATE src/app/products/product-form/product-form.spec.ts (567 bytes)
CREATE src/app/products/product-form/product-form.ts (209 bytes)
CREATE src/app/products/product-form/product-form.html (27 bytes)
kraudy@Ubuntu:~/grind/thegrind/InventoryFrontend$ ng generate service products/product
CREATE src/app/products/product.spec.ts (326 bytes)
CREATE src/app/products/product.ts (112 bytes)
```

Start both
```bash
# Backend
./mvnw clean spring-boot:run
# Frontend
ng serve --proxy-config proxy.conf.json
```