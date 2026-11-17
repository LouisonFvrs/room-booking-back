# 🏛️ Rest API — Gestion de Réservation de Salles Municipales

[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-17+-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![Security](https://img.shields.io/badge/JWT-Authentication-000000?style=for-the-badge&logo=JSON%20web%20tokens)](https://jwt.io/)
[![GitHub](https://img.shields.io/badge/GitHub-Repository-181717?style=for-the-badge&logo=github)](https://github.com/LouisonFvrs/gestionSalleSpringApi)

API REST Backend développée avec **Spring Boot** dédiée à la gestion des réservations et plannings de salles communales pour les mairies et collectivités localisées. Le service offre une architecture sécurisée via authentification par Token (JWT), une gestion centralisée des erreurs et un découpage métier prêt à être consommé par un client Frontend (Angular).

---

## 🚀 Fonctionnalités Principales

- **Authentification & Sécurité (Token-based)** :
  - Inscription, connexion et génération de tokens JWT sécurisés.
  - Gestion des rôles et des autorisations (ex: *Utilisateur / Administrateur Mairie*).
  - Protection des endpoints sensibles via Spring Security.

- **Gestion des Salles** :
  - CRUD complet sur le parc de salles (capacité, équipements, statut).
  - Consultation des disponibilités et des caractéristiques des espaces.

- **Gestion des Réservations** :
  - Création, modification et annulation de demandes de réservation.
  - Vérification automatisée des chevauchements de créneaux (anti-double booking).
  - Suivi des statuts de réservation (*En attente, Validée, Refusée*).

- **Gestion Centralisée des Erreurs (Global Exception Handling)** :
  - Capture propre des exceptions via `@ControllerAdvice` / `@ExceptionHandler`.
  - Envoi de réponses HTTP structurées et homogènes (Code statut HTTP, Horodatage, Message d'erreur explicite).

---

## 🛠️ Stack Technique & Architecture

- **Langage** : Java 17+
- **Framework Principal** : Spring Boot
- **Sécurité** : Spring Security & JWT (JSON Web Tokens)
- **Accès aux Données** : Spring Data JPA / Hibernate
- **Base de données** : PostgreSQL / MySQL
- **Gestionnaire de dépendances** : Maven / Gradle

---

## 🐳 Installation et Démarrage avec Docker (recommandé)

### Prérequis
- **Docker** et **Docker Compose** installés ([Docker Desktop](https://www.docker.com/products/docker-desktop/) inclut les deux)

### Étapes

1. **Cloner le dépôt** :
```bash
   git clone https://github.com/LouisonFvrs/gestionSalleSpringApi.git
   cd gestionSalleSpringApi
```

2. **Configurer les variables d'environnement** :
```bash
   cp .env.example .env
```
Puis éditer `.env` pour renseigner un mot de passe PostgreSQL et une clé secrète JWT (voir section [Génération de la clé JWT](#génération-de-la-clé-jwt) ci-dessous).

3. **Lancer l'application** :
```bash
   docker compose up --build
```
Cette commande construit l'image de l'API, démarre un conteneur PostgreSQL, puis démarre l'application. Le schéma est créé et migré par Liquibase au démarrage (`src/main/resources/db/changelog`).

4. **Accéder à l'API** :
   L'API est disponible sur `http://localhost:8080`.

### Commandes utiles

| Commande | Description |
|---|---|
| `docker compose up --build -d` | Lance en arrière-plan |
| `docker compose logs -f` | Suit les logs en temps réel |
| `docker compose down` | Arrête les conteneurs |
| `docker compose down -v` | Arrête et supprime les données PostgreSQL |

### Génération de la clé JWT

La variable `JWT_SECRET` doit faire au moins 256 bits pour un algorithme HS256. Génère-en une avec :
```bash
openssl rand -base64 32
```
Colle le résultat dans `.env` sans la commiter (le fichier `.env` est ignoré par git).

---

## 💻 Installation et Démarrage Local (sans Docker)

### Prérequis
- **Java JDK 17** ou supérieur
- **Maven**
- **Base de données** SQL (PostgreSQL / MySQL)

### Étapes

1. **Cloner le dépôt** :
```bash
   git clone https://github.com/LouisonFvrs/gestionSalleSpringApi.git
   cd gestionSalleSpringApi
```