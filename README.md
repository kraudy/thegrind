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

# PostgreSQL
sudo apt install postgresql postgresql-contrib
sudo systemctl start postgresql
sudo systemctl enable postgresql
```


Start postgress
```bash
sudo -u postgres psql
```

Simple database
```sql
CREATE DATABASE inventorydb
CREATE USER xxxuser WITH PASSWORD 'xxxpass'
GRANT ALL PRIVILEGES ON DATABASE inventorydb TO xxxuser
```

To get the maven project command
```js
https://start.spring.io/
```