# UnChicks 🍗
Bewerte und checke Grillhähnchen ein – wie Untappd, nur mit Hendl.

    docker compose up --build   # dann http://localhost:8080

Nur das Image: `docker build -t unchicks .` und Env `DB_URL`, `DB_USER`, `DB_PASSWORD` setzen.
Entwicklung: Backend `mvn spring-boot:run`, Frontend `cd frontend && npm i && npm start` (Proxy auf :8080).
