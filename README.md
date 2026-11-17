# Gestion de salles — API REST

[![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.1-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-26-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-4169E1?style=for-the-badge&logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![JWT](https://img.shields.io/badge/JWT-Authentication-000000?style=for-the-badge&logo=JSON%20web%20tokens)](https://jwt.io/)

API REST de réservation de salles de réunion en entreprise. Les employés réservent des salles équipées, y invitent leurs collègues, et peuvent planifier des réunions récurrentes. Le front associé est [gestion-salle-frontend](https://github.com/LouisonFvrs/gestion-salle-frontend) (Angular).

## Sommaire

- [Fonctionnalités](#fonctionnalités)
- [Stack technique](#stack-technique)
- [Démarrage avec Docker](#démarrage-avec-docker)
- [Démarrage en local](#démarrage-en-local)
- [Configuration](#configuration)
- [Base de données](#base-de-données)
- [Rôles](#rôles)
- [Endpoints](#endpoints)
- [Règles de gestion](#règles-de-gestion)
- [Tests](#tests)
- [Structure du projet](#structure-du-projet)

## Fonctionnalités

- **Authentification** par JWT, mots de passe hachés en BCrypt.
- **Rôles** `ROLE_ADMIN`, `ROLE_MANAGER` et `ROLE_EMPLOYEE`, contrôlés sur chaque endpoint.
- **Référentiel** : départements, employés, équipements et salles (une salle porte une capacité et une liste d'équipements).
- **Réservations** : création, modification, annulation et suppression, avec refus des chevauchements sur une même salle.
- **Récurrence** : une réservation quotidienne, hebdomadaire ou mensuelle génère toute sa série d'occurrences.
- **Invitations** : chaque invité accepte ou décline, et retrouve la liste de ses invitations.
- **Erreurs homogènes** : toutes les erreurs suivent le même format JSON.

## Stack technique

| Élément | Choix |
|---|---|
| Langage | Java 26 |
| Framework | Spring Boot 4.1 (Web MVC, Data JPA, Security, Validation) |
| Base de données | PostgreSQL 16 |
| Migrations | Liquibase (changelogs YAML) |
| Authentification | JWT (jjwt 0.12) |
| Documentation d'API | springdoc-openapi (Swagger UI) |
| Build | Gradle (wrapper fourni) |

## Démarrage avec Docker

Prérequis : Docker et Docker Compose.

1. Cloner le dépôt :

   ```bash
   git clone https://github.com/LouisonFvrs/gestionSalleSpringApi.git
   cd gestionSalleSpringApi
   ```

2. Créer le fichier d'environnement, puis y renseigner le mot de passe PostgreSQL et la clé JWT (voir [Configuration](#configuration)) :

   ```bash
   cp .env.example .env
   ```

3. Construire et lancer l'API et sa base :

   ```bash
   docker compose up --build
   ```

L'API répond sur `http://localhost:8080` et Swagger UI sur `http://localhost:8080/swagger-ui.html`. Le schéma est créé par Liquibase au premier démarrage.

| Commande | Effet |
|---|---|
| `docker compose up --build -d` | Lance en arrière-plan |
| `docker compose logs -f` | Suit les logs |
| `docker compose down` | Arrête les conteneurs |
| `docker compose down -v` | Arrête et supprime les données PostgreSQL |

## Démarrage en local

Prérequis : un JDK 26 et une base PostgreSQL accessible.

1. Créer le `.env` comme ci-dessus. L'application le charge automatiquement au démarrage.

2. Lancer uniquement la base avec Docker :

   ```bash
   docker compose up -d db
   ```

3. Lancer l'application. Dans le `.env`, l'URL de la base vise l'hôte `db` du réseau Docker : hors Docker, il faut viser `localhost`. Une variable d'environnement prend le pas sur le `.env` :

   ```bash
   SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/gestionsalle ./gradlew bootRun
   ```

## Configuration

Toute la configuration passe par des variables d'environnement, lues par `src/main/resources/application.properties`. Aucune valeur n'est écrite dans le code. Le fichier `.env` n'est jamais commité ; `.env.example` liste les variables attendues.

| Variable | Rôle |
|---|---|
| `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD` | Base créée par le conteneur PostgreSQL |
| `SPRING_DATASOURCE_URL` | URL JDBC de la base |
| `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD` | Identifiants de connexion de l'application |
| `SPRING_JPA_HIBERNATE_DDL_AUTO` | Doit rester à `validate` : le schéma appartient à Liquibase |
| `JWT_SECRET` | Clé de signature des tokens, 256 bits minimum |
| `JWT_EXPIRATION_MS` | Durée de vie d'un token, en millisecondes |

Générer une clé JWT :

```bash
openssl rand -base64 32
```

## Base de données

Le schéma est géré par Liquibase. Les changelogs sont dans `src/main/resources/db/changelog` et s'appliquent au démarrage ; Hibernate se contente de vérifier que les entités correspondent au schéma.

- Pour faire évoluer le schéma, ajouter un fichier dans `changes/` et l'inclure dans `db.changelog-master.yaml`.
- Ne jamais modifier un changeset déjà appliqué.
- Une base créée avant l'arrivée de Liquibase est reprise telle quelle : le changelog initial ignore les tables qui existent déjà.

Aucun compte n'est créé automatiquement. Sur une base neuve, il faut insérer un premier administrateur dans la table `employee` (mot de passe haché en BCrypt) avant de pouvoir se connecter.

## Rôles

| Rôle | Peut |
|---|---|
| `ROLE_EMPLOYEE` | Consulter le référentiel et les réservations, voir ses invitations et y répondre |
| `ROLE_MANAGER` | En plus : créer des réservations et des récurrences, gérer les employés et créer des départements |
| `ROLE_ADMIN` | En plus : gérer les salles et les équipements, supprimer des départements, agir sur les réservations de tous |

Une requête sans token valide reçoit `401`, une requête sans le rôle requis reçoit `403`.

## Endpoints

Tous les endpoints sont préfixés par `/api` et attendent l'en-tête `Authorization: Bearer <token>`, sauf la connexion. Le détail des corps de requête et de réponse est dans Swagger UI.

### Authentification

| Méthode | Chemin | Accès | Description |
|---|---|---|---|
| POST | `/auth/login` | Public | Renvoie un token à partir d'un email et d'un mot de passe |

### Réservations

| Méthode | Chemin | Accès | Description |
|---|---|---|---|
| GET | `/bookings/{id}` | Tous | Détail d'une réservation |
| GET | `/bookings/me` | Tous | Réservations organisées par l'utilisateur connecté |
| GET | `/bookings/organisateur/{id}` | Admin, manager | Réservations d'un organisateur |
| GET | `/bookings/recurrence/{idRecurrence}` | Tous | Occurrences d'une série |
| POST | `/bookings` | Admin, manager | Crée une réservation, ou toute une série si `idRecurrence` est fourni |
| PUT | `/bookings/{id}` | Admin, manager | Modifie une réservation |
| PATCH | `/bookings/{id}/cancel` | Admin, manager | Annule une réservation |
| PATCH | `/bookings/recurrence/{idRecurrence}/cancel` | Admin, manager | Annule les occurrences à venir d'une série |
| DELETE | `/bookings/delete/{id}` | Admin, manager | Supprime une réservation |

### Invitations

| Méthode | Chemin | Accès | Description |
|---|---|---|---|
| GET | `/invitation/me` | Tous | Invitations de l'utilisateur connecté (paginé) |
| GET | `/invitation/booking/{bookingId}` | Admin, manager | Invitations d'une réservation (paginé) |
| PUT | `/invitation/{id}` | Tous | Répond à une invitation (`ACCEPTED` ou `DECLINED`) |

### Récurrences

| Méthode | Chemin | Accès | Description |
|---|---|---|---|
| GET | `/recurrence` | Tous | Liste des récurrences |
| POST | `/recurrence` | Admin, manager | Crée une récurrence (`DAILY`, `WEEKLY` ou `MONTHLY`, intervalle, date de fin) |

### Salles

| Méthode | Chemin | Accès | Description |
|---|---|---|---|
| GET | `/rooms` | Tous | Liste des salles (paginé) |
| GET | `/rooms/available` | Tous | Salles marquées disponibles (paginé) |
| GET | `/rooms/{id}` | Tous | Détail d'une salle |
| POST | `/rooms` | Admin | Crée une salle |
| DELETE | `/rooms/{id}` | Admin | Supprime une salle |
| PUT | `/rooms/{idRoom}/equipments/{idEquipment}` | Admin | Ajoute un équipement à une salle |
| DELETE | `/rooms/{idRoom}/equipments/{idEquipment}` | Admin | Retire un équipement d'une salle |

### Équipements

| Méthode | Chemin | Accès | Description |
|---|---|---|---|
| GET | `/equipments` | Tous | Liste des équipements |
| GET | `/equipments/{id}` | Tous | Détail d'un équipement |
| POST | `/equipments` | Admin | Crée un équipement |
| DELETE | `/equipments/{id}` | Admin | Supprime un équipement |

### Employés

| Méthode | Chemin | Accès | Description |
|---|---|---|---|
| GET | `/employees` | Tous | Liste des employés (paginé) |
| GET | `/employees/{email}` | Tous | Employé par email |
| POST | `/employees` | Admin, manager | Crée un employé |
| DELETE | `/employees/{id}` | Admin, manager | Supprime un employé |

### Départements

| Méthode | Chemin | Accès | Description |
|---|---|---|---|
| GET | `/departments` | Tous | Liste des départements |
| POST | `/departments` | Admin, manager | Crée un département |
| DELETE | `/departments/{id}` | Admin | Supprime un département |

## Règles de gestion

### Réservations

- La date de fin est strictement postérieure à la date de début, et une réservation ne débute pas dans le passé.
- Deux réservations non annulées ne peuvent pas se chevaucher sur une même salle (`409`).
- L'organisateur est l'utilisateur connecté : il n'est jamais fourni dans la requête.
- Seul l'organisateur ou un administrateur peut modifier, annuler ou supprimer une réservation (`403` sinon).

### Annulation

- Annuler passe la réservation au statut `CANCELLED` : elle reste consultable et son créneau redevient libre.
- Une réservation annulée ne peut plus être modifiée ni annulée à nouveau (`409`).

### Récurrence

- Une réservation créée avec une récurrence génère une réservation par occurrence, de la date de début jusqu'à la date de fin de la récurrence, avec les mêmes invités.
- La série est créée en entier ou pas du tout : un seul créneau occupé rejette la demande, et l'erreur liste les dates en conflit.
- Une récurrence ne sert qu'à une seule série, limitée à 366 occurrences.
- Une occurrence se modifie ou s'annule individuellement ; sa récurrence ne peut plus être changée.
- Annuler une série n'annule que les occurrences qui n'ont pas encore commencé.

### Invitations

- Les invitations sont créées avec la réservation, au statut `PENDING`.
- Seul l'invité peut répondre à son invitation, par `ACCEPTED` ou `DECLINED`.
- Une invitation à une réunion annulée n'accepte plus de réponse (`409`).

### Format des erreurs

```json
{
  "timestamp": "2026-11-17T14:34:29.85",
  "status": 409,
  "error": "Conflict",
  "message": "La salle Everest est déjà réservée sur : 24/11/2026 10:00.",
  "path": "/api/bookings"
}
```

## Tests

Les tests unitaires des services tournent sans base de données :

```bash
./gradlew test --tests "*ServiceTest"
```

La suite complète contient aussi un test de chargement du contexte Spring, qui a besoin d'une base PostgreSQL et des variables d'environnement de l'application :

```bash
./gradlew test
```

## Structure du projet

Le code est organisé par fonctionnalité, sous `src/main/java/fr/louison/gestionsallespringapi` :

```
auth/           connexion, filtre et utilitaires JWT
booking/        réservations
invitation/     invitations
recurrence/     récurrences
room/           salles
equipment/      équipements
employee/       employés
department/     départements
configuration/  sécurité et OpenAPI
exception/      exceptions métier et gestion centralisée des erreurs
```

Chaque fonctionnalité contient ses propres dossiers `controller`, `dto`, `model`, `repository` et `service`.
