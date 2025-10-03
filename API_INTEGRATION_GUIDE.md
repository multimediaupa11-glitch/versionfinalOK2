# Guide d'Intégration API Frontend-Backend

## Configuration

### 1. Variables d'environnement

Créez un fichier `.env.local` à la racine du projet:

```env
VITE_API_URL=http://localhost:8080/api
```

### 2. Démarrage du backend

```bash
cd spring-boot-backend
mvn spring-boot:run
```

Le backend démarre sur `http://localhost:8080`

### 3. Démarrage du frontend

```bash
npm run dev
```

Le frontend démarre sur `http://localhost:5173`

## Architecture des Services

### Service API de Base (`src/services/api.ts`)

Service central qui gère:
- Configuration de l'URL de base
- Gestion du token JWT
- Headers automatiques
- Gestion des erreurs
- Déconnexion automatique en cas d'erreur 401

### Services Métier

#### AuthService (`src/services/authService.ts`)
- `checkEmail(email)`: Vérifie si un email existe et a un mot de passe
- `login(credentials)`: Connexion avec email/password
- `createPassword(request)`: Création du premier mot de passe
- `createAdmin(request)`: Création d'un admin
- `createEncadreur(request)`: Création d'un encadreur
- `createStagiaire(request)`: Création d'un stagiaire
- `logout()`: Déconnexion

#### DashboardService (`src/services/dashboardService.ts`)
- `getDashboardData()`: Récupère les métriques du tableau de bord

#### InternService (`src/services/internService.ts`)
- `getAllInterns()`: Liste tous les stagiaires
- `getInternById(id)`: Détails d'un stagiaire
- `getInternsByEncadreur(encadreurId)`: Stagiaires par encadreur
- `createIntern(request)`: Créer un stagiaire
- `updateIntern(id, request)`: Modifier un stagiaire
- `deleteIntern(id)`: Supprimer un stagiaire

#### ProjectService (`src/services/projectService.ts`)
- `getAllProjects()`: Liste tous les projets
- `getProjectById(id)`: Détails d'un projet
- `getProjectsByIntern(internId)`: Projets d'un stagiaire
- `getProjectsByEncadreur(encadreurId)`: Projets d'un encadreur
- `createProject(request)`: Créer un projet
- `updateProject(id, request)`: Modifier un projet
- `deleteProject(id)`: Supprimer un projet

#### TaskService (`src/services/taskService.ts`)
- `getAllTasks()`: Liste toutes les tâches
- `getTaskById(id)`: Détails d'une tâche
- `getTasksByProject(projectId)`: Tâches d'un projet
- `getTasksByIntern(internId)`: Tâches d'un stagiaire
- `createTask(request)`: Créer une tâche
- `updateTask(id, request)`: Modifier une tâche
- `deleteTask(id)`: Supprimer une tâche

#### EncadreurService (`src/services/encadreurService.ts`)
- `getAllEncadreurs()`: Liste tous les encadreurs
- `getEncadreurById(id)`: Détails d'un encadreur
- `updateEncadreur(id, request)`: Modifier un encadreur
- `deleteEncadreur(id)`: Supprimer un encadreur

## Scénarios d'Utilisation

### 1. Connexion (Flux Standard)

```typescript
import { authService } from '../services/authService';

// Vérifier si l'email existe
const emailCheck = await authService.checkEmail(email);

if (emailCheck.exists && emailCheck.hasPassword) {
  // L'utilisateur a déjà un mot de passe
  const response = await authService.login({ email, password });
  // Le token est automatiquement stocké
} else if (emailCheck.exists && !emailCheck.hasPassword) {
  // Premier login, créer le mot de passe
  const response = await authService.createPassword({ email, password });
  // Le token est automatiquement stocké
} else {
  // Email n'existe pas
  throw new Error('Compte introuvable');
}
```

### 2. Déconnexion

```typescript
import { authService } from '../services/authService';
import { useAuth } from '../contexts/AuthContext';

const { signOut } = useAuth();

// Méthode 1: Via le contexte (recommandé)
await signOut();

// Méthode 2: Directement via le service
authService.logout();
```

### 3. Récupération des données avec gestion d'erreur

```typescript
import { useApiError } from '../hooks/useApiError';
import { internService } from '../services/internService';

const { error, isLoading, handleApiCall } = useApiError();

const loadInterns = async () => {
  const interns = await handleApiCall(
    () => internService.getAllInterns(),
    (data) => {
      console.log('Stagiaires chargés:', data);
    },
    (error) => {
      console.error('Erreur:', error.message);
    }
  );
};
```

### 4. Création d'un stagiaire

```typescript
import { internService } from '../services/internService';

const createIntern = async () => {
  try {
    const newIntern = await internService.createIntern({
      email: 'stagiaire@example.com',
      firstName: 'Jean',
      lastName: 'Dupont',
      encadreurId: 1,
      startDate: '2025-01-01',
      endDate: '2025-06-30',
      university: 'Université Paris',
      department: 'Informatique'
    });
    console.log('Stagiaire créé:', newIntern);
  } catch (error) {
    console.error('Erreur:', error.message);
  }
};
```

### 5. Mise à jour d'un projet

```typescript
import { projectService } from '../services/projectService';

const updateProject = async (projectId: number) => {
  try {
    const updated = await projectService.updateProject(projectId, {
      status: 'EN_COURS',
      progress: 50
    });
    console.log('Projet mis à jour:', updated);
  } catch (error) {
    console.error('Erreur:', error.message);
  }
};
```

## Gestion des Erreurs

### Erreurs Automatiques

Le service API gère automatiquement:
- **401 Unauthorized**: Déconnexion automatique et redirection vers login
- **404 Not Found**: Message d'erreur spécifique
- **500 Server Error**: Message d'erreur générique

### Gestion Personnalisée

```typescript
try {
  const data = await apiService.get('/endpoint');
} catch (error: any) {
  if (error.status === 403) {
    // Accès refusé
    alert('Vous n\'avez pas les permissions nécessaires');
  } else if (error.status === 409) {
    // Conflit
    alert('Cet élément existe déjà');
  } else {
    // Erreur générique
    alert(error.message);
  }
}
```

## Hook useApiError

Hook personnalisé pour simplifier la gestion des appels API:

```typescript
import { useApiError } from '../hooks/useApiError';

function MyComponent() {
  const { error, isLoading, handleApiCall, clearError } = useApiError();

  const fetchData = async () => {
    await handleApiCall(
      () => myService.getData(),
      (data) => {
        // Succès
        setMyData(data);
      },
      (error) => {
        // Erreur personnalisée
        console.error(error);
      }
    );
  };

  return (
    <div>
      {isLoading && <p>Chargement...</p>}
      {error && <p>Erreur: {error}</p>}
    </div>
  );
}
```

## Authentification Persistante

Le système maintient l'authentification via:
1. **Token JWT** stocké dans localStorage (clé: `auth_token`)
2. **Données utilisateur** stockées dans localStorage (clé: `auth_user`)
3. **Vérification au chargement** dans AuthContext
4. **Ajout automatique** du token dans les headers de requêtes

## Test de l'API

### Avec Postman

Importez la collection: `spring-boot-backend/POSTMAN_COLLECTION.json`

### Endpoints Principaux

```
POST   /api/auth/check-email
POST   /api/auth/login
POST   /api/auth/create-password
GET    /api/dashboard
GET    /api/interns
POST   /api/interns
GET    /api/projects
POST   /api/projects
GET    /api/tasks
POST   /api/tasks
GET    /api/encadreurs
```

## Dépannage

### Erreur CORS

Assurez-vous que le backend autorise les requêtes depuis `http://localhost:5173`

Vérifiez dans `SecurityConfig.java`:
```java
@CrossOrigin(origins = "*")
```

### Token Expiré

Le token expire après 24h. L'utilisateur sera automatiquement déconnecté.

### Backend Non Disponible

Vérifiez que:
1. Le backend est démarré (`mvn spring-boot:run`)
2. MySQL est en cours d'exécution
3. L'URL dans `.env.local` est correcte

## Prochaines Étapes

1. Remplacer les données mockées dans les composants par les appels API
2. Implémenter le rafraîchissement automatique du token
3. Ajouter un intercepteur pour logger les requêtes
4. Implémenter la mise en cache côté client
5. Ajouter des websockets pour les notifications en temps réel
