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

# this is supposed to give a better cli than psql
sudo apt install pgcli


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
CREATE DATABASE inventorydb;
CREATE USER inventoryuser WITH PASSWORD 'pass';
GRANT ALL PRIVILEGES ON DATABASE inventorydb TO inventoryuser;

\l  -- List databases
\q
\du -- show users
\c  -- Change database
\dt -- show tables inside database
\e  -- Select editor
\d table -- show colums, keys and indexs

-- Delete database
DROP DATABASE inventorydb;
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
ng generate component productos
ng generate service producto

ng generate component productos/producto-list
ng generate component productos/producto-form
ng generate service productos/producto


ng generate component ordenes/orden-list
ng generate component ordenes/orden-form
ng generate service ordenes/orden

ng generate component ordenes-detalle/orden-detalle-list
ng generate component ordenes-detalle/orden-detalle-form
ng generate service ordenes-detalle/orden-detalle

ng generate component ordenes-calendario/orden-calendario-list
ng generate component ordenes-calendario/orden-calendario-form
ng generate service ordenes-calendario/orden-calendario

ng generate component ordenes-seguimiento/orden-seguimiento-list

ng generate component ordenes-seguimiento/orden-seguimiento-impresion-list
ng generate component ordenes-seguimiento/orden-seguimiento-impresion-detalle-list

ng generate component ordenes-seguimiento/orden-seguimiento-preparacion-list
ng generate component ordenes-seguimiento/orden-seguimiento-preparacion-detalle-list

ng generate component ordenes-seguimiento/orden-seguimiento-entrega-list
ng generate component ordenes-seguimiento/orden-seguimiento-entrega-detalle-list

ng generate component ordenes-seguimiento/orden-seguimiento-repartir-list
ng generate component ordenes-seguimiento/orden-seguimiento-repartir-detalle-list

#ng generate component ordenes-seguimiento/orden-seguimiento-general-list
#ng generate component ordenes-seguimiento/orden-seguimiento-general-detalle-list

ng generate service ordenes-seguimiento/orden-seguimiento

ng generate component clientes/cliente-list
ng generate component clientes/cliente-form
ng generate service clientes/cliente

ng generate component productos-tipos/producto-tipo-list
ng generate component productos-tipos/producto-tipo-form
ng generate service productos-tipos/producto-tipo

ng generate component productos-sub-tipos/producto-sub-tipo-list
ng generate component productos-sub-tipos/producto-sub-tipo-form
ng generate service productos-sub-tipos/producto-sub-tipo

#ng generate component productos-tipo-estados/producto-tipo-estado-list
#ng generate component productos-tipo-estados/producto-tipo-estado-form
ng generate service productos-tipo-estados/producto-tipo-estado

ng generate component productos-precios/producto-precio-list
ng generate component productos-precios/producto-precio-form
ng generate service productos-precios/producto-precio

ng generate component home/home
ng generate service home/home
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

Install STOMP
```bash
# Instala Tailwind y dependencias
npm install @stomp/stompjs sockjs-client
# check
npm list sockjs-client
# needed because it needs it for typescript or something
npm install --save-dev @types/sockjs-client
```

Install nginx
```bash
sudo apt install nginx
sudo systemctl start nginx
sudo systemctl enable nginx

# config file
sudo nano /etc/nginx/sites-available/thegrind

# activate it
sudo ln -s /etc/nginx/sites-available/thegrind /etc/nginx/sites-enabled/
sudo nginx -t  # Test config
sudo systemctl restart nginx # start nginx proc

# access front-end at http://localhost
```

DB backup
```bash
pg_dump -U postgres -d inventorydb --schema=public > backup_2026-03-31.sql
```

The real thing
```bash
# Clone your repo
git clone https://github.com/kraudy/thegrind.git
cd thegrind

# install sht
sudo apt update
sudo apt upgrade -y
sudo apt install ca-certificates curl gnupg lsb-release -y

# 2. Add Docker's official GPG key
sudo install -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
sudo chmod a+r /etc/apt/keyrings/docker.gpg

# 3. Add the Docker repository
echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu \
  $(. /etc/os-release && echo "$VERSION_CODENAME") stable" | \
  sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

"

# 4. Update apt again and install Docker + Compose plugin
sudo apt update
sudo apt install docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin -y

# 5 Start and enable Docker
sudo systemctl enable --now docker

# Add your user to the docker group (so you don't need sudo every time): 
sudo usermod -aG docker $USER

# Verify everything
docker --version
docker compose version

# Build and run sht
docker compose up -d --build

# FE sht
cd InventoryFrontend
npm ci --omit=dev          # This install dependencies
npm ci                     # This install dependencies
ng build --configuration production
ng build --proxy-config proxy.conf.json
ng build
```

Docker stuff
```bash
# check sht

# 1. See status of both containers
docker compose ps

# 2. Check backend logs (watch it start)
# Este me sirve para los logs del backend
docker compose logs -f backend --tail 100

# 3. Check DB logs (optional)
docker compose logs db

# ====================
# stop sht

# Stop old containers
docker compose down

# Build with the new advanced Dockerfile (this will take 2-4 minutes the first time)
docker compose up -d --build

# ========================
# postgress sht

sudo lsof -i :5432
sudo lsof -i :8080
sudo kill xxxx

# ========================
# fix some network sht

# show logs btch
docker compose logs backend --tail 100

# =====================

# reset and rebuild

# Clean everything (volumes + network + orphans)
docker compose down -v --remove-orphans #REMOVES DATABASE

# Rebuild and start fresh
# docker compose up -d --build

docker compose build --no-cache
docker compose up -d

```

Faster redeploy
```bash
git pull
docker compose up -d --build
# usually, no need for docker compose down

# in case of major changes
git pull
docker compose build --no-cache backend   # only rebuild backend, not db
docker compose up -d

# in case of layer corruption (rare)
docker compose build --no-cache
docker compose up -d
```

Shootdown
```bash
# 1. Gracefully stop everything (recommended)
docker compose down

# 2. Then shut down or reboot the server
sudo shutdown -h now
# or
sudo reboot
```

tmux on this btch
```bash
tmux new -s main

# split vertical 
Ctrl+b %
# split horizontal
Ctrl+b "                       #"
# move between terminals
Ctrl+b 1-9 
# exit pane and terminal
exit
# detach
Ctrl+b d
# attach
tmux attach -t main
# show sessions
tmux ls
# kill main
tmux kill-session -t main
```



## Set up unbuntu server 

```bash
# OS
Ubuntu Server 24.04 LTS (Noble Numbat)

# Rufus
Device → Select your USB drive (double-check the size/name!)
Boot selection → Click SELECT → choose the file ubuntu-24.04.4-live-server-amd64.iso
Partition scheme → GPT (best for modern servers)
Target system → UEFI (non CSM) ← very important
Leave File system and all other options at default

# Update the system
sudo apt update && sudo apt upgrade -y
sudo apt install curl git -y

# Install Docker + Docker Compose 
# 1. Add Docker's official GPG key
sudo install -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
sudo chmod a+r /etc/apt/keyrings/docker.gpg

# 2. Add the Docker repository
echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] \
  https://download.docker.com/linux/ubuntu \
  $(. /etc/os-release && echo "$VERSION_CODENAME") stable" | \
  sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

# 3. Install Docker Engine + Compose plugin
sudo apt update
sudo apt install docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin -y

# 4. Start and enable Docker
sudo systemctl enable --now docker

# 5. Add your user to the docker group (so you don't need sudo every time)
sudo usermod -aG docker $USER

# Log out and log back in (or run `newgrp docker`) so the group change takes effect

# verify
docker --version
docker compose version


# firewall
sudo apt install ufw -y
sudo ufw default deny incoming
sudo ufw default allow outgoing
sudo ufw allow ssh          # or your SSH port if you changed it
sudo ufw allow 8080/tcp     # your app
sudo ufw --force enable
sudo ufw status

# clone repo
git clone https://github.com/kraudy/thegrind.git
cd thegrind

# First run (will take a few minutes the very first time)
docker compose up -d --build

# After that, normal start is just:
# docker compose up -d

# check
docker compose ps
docker compose logs backend --tail 50



# Check container health
watch -n 5 'docker compose ps'

# db backups
cat > ~/backup-db.sh << 'EOF'
#!/bin/bash
docker compose exec -T db pg_dump -U inventoryuser inventorydb > /backups/inventorydb_$(date +%F).sql
echo "Backup done: /backups/inventorydb_$(date +%F).sql"
EOF
chmod +x ~/backup-db.sh
mkdir -p ~/backups

```

Set alias 
```bash
# add alises to this file, it is already used ty bashrc
vim ~/.bash_aliases

# See logs easily
alias logs='docker compose logs -f backend'

# get into database
alias invdb='docker exec -it thegrind-db psql -U inventoryuser -d inventorydb'

# reload
source ~/.bashrc

```

Docker postgress
```bash
# Get in there you weasel!
docker exec -it thegrind-db psql -U inventoryuser -d inventorydb

```

Set auto start
```bash
sudo nano /etc/systemd/system/thegrind.service

============================================================
[Unit]
Description=TheGrind App (Spring Boot + Angular + PostgreSQL)
Requires=docker.service
After=docker.service network-online.target
Wants=network-online.target

# ← These 3 lines ensure ExecStop runs and systemd WAITS for it during full shutdown/reboot/power-off
DefaultDependencies=no
Conflicts=shutdown.target
Before=shutdown.target

[Service]
Type=oneshot
RemainAfterExit=yes
WorkingDirectory=/home/kraudy/thegrind

# Start
ExecStart=/usr/bin/docker compose up -d --build

# Stop (this runs on systemctl stop AND during shutdown/power button)
ExecStop=/usr/bin/docker compose down --timeout 60

# Give more time for graceful shutdown (PostgreSQL and Spring Boot need time)
TimeoutStartSec=300
TimeoutStopSec=180     # <--- Important addition (3 minutes)

# Optional but useful: restart the whole stack if it fails to start
Restart=on-failure
RestartSec=10

[Install]
WantedBy=multi-user.target
============================================================

# Step 2: Reload 
sudo systemctl daemon-reload
sudo systemctl restart thegrind.service   # or just reboot to test

# systemd and enable the service
sudo systemctl enable thegrind.service

sudo systemctl start thegrind.service
sudo systemctl stop thegrind.service

sudo systemctl status thegrind.service

sudo journalctl -u thegrind.service -f

# Spring Boot graceful shutdown
docker compose stop

sudo docker logs thegrind-backend | tail -50 | grep -E "shutdown|SHUTDOWN|graceful|Closing|Stopped"

# Postgres clean shutdown
sudo docker logs thegrind-db | tail -30 | grep -E "checkpoint|shut down|terminated|LOG:  shutting down"

# seguimiento en linea
sudo journalctl -u thegrind.service -f
sudo journalctl -u thegrind.service -xe
# this shows logs after shutdown
sudo journalctl -u thegrind.service -b -1 --no-pager | tail -200

docker compose logs backend
docker compose logs db
```

Images
```bash
# check images
docker exec -it thegrind-backend ls -l /app/images/
```

Set timezone
```bash
# 1. Check current timezone and NTP status
timedatectl

# 2. Confirm the exact timezone name exists
timedatectl list-timezones | grep -i managua
# (should return America/Managua)

# 3. Set it
sudo timedatectl set-timezone America/Managua

# 4. Verify
timedatectl

# extra: Make sure the system clock stays accurate
sudo apt update && sudo apt install -y chrony  # or just use the default systemd-timesyncd
timedatectl set-ntp true

# check shit
# Inside Postgres container
docker exec -it thegrind-db psql -U inventoryuser -d inventorydb -c "SHOW timezone;"

# Inside Spring Boot container
docker exec -it thegrind-backend date
docker exec -it thegrind-backend sh -c 'echo "Current Java timezone:" && java -XshowSettings:properties -version 2>&1 | grep -i timezone'
```

Start both
```bash
# do this sht fast
docker compose down
docker compose up -d --build

# Backend
# for prod
cd InventoryBackend
./mvnw clean package
java -jar target/*.jar 
# for dev
./mvnw clean spring-boot:run

# Frontend
# prod
ng build --configuration production

ng serve --proxy-config proxy.conf.json

# show process listeingn
sudo lsof -i :4200
sudo kill -9 xxxx
```



Backup
```bash
services:
  db:
    ...
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./backups:/backups   # ← add this

  # Optional: dedicated backup service (recommended for automation)
  backup:
    image: postgres:16-alpine
    container_name: thegrind-backup
    restart: unless-stopped
    depends_on:
      db:
        condition: service_healthy
    volumes:
      - ./backups:/backups
    entrypoint: /bin/sh
    command: >
      -c '
        while true; do
          echo "=== Starting backup at $$(date) ==="
          pg_dump -h db -U inventoryuser -d inventorydb -Fc -Z 9 > /backups/inventorydb_$$(date +%Y-%m-%d_%H-%M).dump
          echo "Backup completed. Size: $$(du -sh /backups/inventorydb_*.dump | tail -n1)"
          # Optional: keep only last 7 daily + last 4 weekly
          find /backups -name "inventorydb_*.dump" -mtime +7 -delete
          sleep 86400   # 24 hours
        done
      '
    environment:
      PGPASSWORD: pass
```