# Scénarios de Gestion des Erreurs

Ce document décrit tous les scénarios d'erreur possibles et comment ils sont gérés dans l'application.

## 1. Erreurs d'Authentification

### Scénario 1.1: Email Inexistant

**Situation**: L'utilisateur entre un email qui n'existe pas dans la base de données.

**Flux**:
```
User entre email → checkEmail() → exists: false
                                 ↓
                    Afficher: "Aucun compte trouvé avec cet email"
```

**Code**:
```typescript
const response = await authService.checkEmail(email);
if (!response.exists) {
  setError('Aucun compte trouvé avec cet email');
}
```

### Scénario 1.2: Mot de Passe Incorrect

**Situation**: L'utilisateur entre un mot de passe incorrect.

**Flux**:
```
User entre password → login() → Backend retourne 401
                                ↓
                    Afficher: "Email ou mot de passe incorrect"
```

**Code**:
```typescript
try {
  await authService.login({ email, password });
} catch (error) {
  setError('Email ou mot de passe incorrect');
}
```

### Scénario 1.3: Token Expiré

**Situation**: Le token JWT est expiré (après 24h).

**Flux**:
```
API call → Backend retourne 401
         ↓
    ApiService détecte 401
         ↓
    Supprime le token
         ↓
    Redirige vers login
         ↓
    Afficher: "Session expirée, veuillez vous reconnecter"
```

**Code** (dans `api.ts`):
```typescript
if (response.status === 401) {
  this.setToken(null);
  window.location.href = '/';
}
```

### Scénario 1.4: Compte Sans Mot de Passe

**Situation**: Un admin/encadreur a créé un compte pour un utilisateur, mais il n'a pas encore défini de mot de passe.

**Flux**:
```
User entre email → checkEmail() → exists: true, hasPassword: false
                                 ↓
                    Rediriger vers création de mot de passe
```

**Code**:
```typescript
if (response.exists && !response.hasPassword) {
  setStep('create-password');
}
```

## 2. Erreurs de Réseau

### Scénario 2.1: Backend Inaccessible

**Situation**: Le backend n'est pas démarré ou inaccessible.

**Flux**:
```
API call → fetch() throw Error
         ↓
    Catch dans handleResponse()
         ↓
    Afficher: "Impossible de se connecter au serveur"
```

**Code**:
```typescript
try {
  const response = await fetch(url);
} catch (error) {
  throw {
    message: 'Impossible de se connecter au serveur',
    status: 0
  };
}
```

### Scénario 2.2: Timeout

**Situation**: La requête prend trop de temps.

**Flux**:
```
API call → timeout (30s)
         ↓
    Afficher: "La requête a expiré, veuillez réessayer"
```

**Implementation**:
```typescript
const controller = new AbortController();
const timeout = setTimeout(() => controller.abort(), 30000);

try {
  const response = await fetch(url, { signal: controller.signal });
} catch (error) {
  if (error.name === 'AbortError') {
    throw { message: 'La requête a expiré' };
  }
}
```

## 3. Erreurs de Permission

### Scénario 3.1: Accès Refusé

**Situation**: L'utilisateur tente d'accéder à une ressource sans permission.

**Flux**:
```
User clique → API call → Backend retourne 403
                        ↓
            Afficher: "Vous n'avez pas les permissions nécessaires"
```

**Code**:
```typescript
if (error.status === 403) {
  alert('Vous n\'avez pas les permissions nécessaires');
}
```

### Scénario 3.2: Rôle Insuffisant

**Situation**: Un stagiaire tente de créer un projet.

**Flux**:
```
Stagiaire clique "Créer projet" → Vérification côté client
                                  ↓
                        Bouton désactivé ou caché
```

**Code**:
```typescript
const canCreateProject = authUser?.role === 'ROLE_ADMIN' ||
                        authUser?.role === 'ROLE_ENCADREUR';

{canCreateProject && (
  <button onClick={handleCreate}>Créer</button>
)}
```

## 4. Erreurs de Validation

### Scénario 4.1: Champs Requis Manquants

**Situation**: L'utilisateur soumet un formulaire incomplet.

**Flux**:
```
User soumet formulaire → Validation frontend
                        ↓
            HTML5 validation (required)
                        ↓
            Afficher messages natifs du navigateur
```

**Code**:
```typescript
<input
  type="email"
  required
  minLength={3}
/>
```

### Scénario 4.2: Format Invalide

**Situation**: L'email n'est pas au bon format.

**Flux**:
```
User entre email → onChange validation
                  ↓
    Afficher erreur en temps réel
```

**Code**:
```typescript
const validateEmail = (email: string) => {
  const regex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  if (!regex.test(email)) {
    setError('Format d\'email invalide');
  }
};
```

### Scénario 4.3: Mot de Passe Faible

**Situation**: Le mot de passe ne respecte pas les règles.

**Flux**:
```
User entre password → Validation
                     ↓
    < 8 caractères → Afficher: "Au moins 8 caractères requis"
```

**Code**:
```typescript
if (password.length < 8) {
  setError('Le mot de passe doit contenir au moins 8 caractères');
}
```

### Scénario 4.4: Mots de Passe Non-Identiques

**Situation**: Confirmation de mot de passe différente.

**Flux**:
```
User entre confirmPassword → Comparaison
                            ↓
    password !== confirmPassword
                            ↓
    Afficher: "Les mots de passe ne correspondent pas"
```

**Code**:
```typescript
if (password !== confirmPassword) {
  setError('Les mots de passe ne correspondent pas');
}
```

## 5. Erreurs de Données

### Scénario 5.1: Email Déjà Utilisé

**Situation**: L'admin tente de créer un compte avec un email existant.

**Flux**:
```
Admin crée compte → Backend retourne 409 Conflict
                   ↓
    Afficher: "Cet email est déjà utilisé"
```

**Code**:
```typescript
if (error.status === 409) {
  setError('Cet email est déjà utilisé');
}
```

### Scénario 5.2: Ressource Non Trouvée

**Situation**: L'utilisateur tente d'accéder à un stagiaire qui n'existe pas.

**Flux**:
```
User clique stagiaire → getInternById(999) → Backend retourne 404
                                             ↓
                        Afficher: "Stagiaire introuvable"
```

**Code**:
```typescript
if (error.status === 404) {
  setError('Stagiaire introuvable');
}
```

### Scénario 5.3: Conflit de Données

**Situation**: Modification simultanée de la même ressource.

**Flux**:
```
User A modifie projet → Save
User B modifie projet → Save → Backend retourne 409
                               ↓
                    Afficher: "Données modifiées par un autre utilisateur"
                               ↓
                    Proposer de recharger
```

## 6. Erreurs Métier

### Scénario 6.1: Encadreur N'Existe Plus

**Situation**: Création d'un stagiaire avec un encadreur supprimé.

**Flux**:
```
User crée stagiaire avec encadreurId: 5 → Backend vérifie
                                         ↓
                            Encadreur n'existe pas
                                         ↓
                            Retourne 400 Bad Request
                                         ↓
                        Afficher: "L'encadreur sélectionné n'existe plus"
```

### Scénario 6.2: Dates Invalides

**Situation**: Date de fin avant date de début.

**Flux**:
```
User entre dates → Validation frontend
                  ↓
    endDate < startDate
                  ↓
    Afficher: "La date de fin doit être après la date de début"
```

**Code**:
```typescript
if (new Date(endDate) < new Date(startDate)) {
  setError('La date de fin doit être après la date de début');
}
```

### Scénario 6.3: Suppression avec Dépendances

**Situation**: Suppression d'un encadreur qui a des stagiaires.

**Flux**:
```
User supprime encadreur → Backend vérifie dépendances
                         ↓
                Has active interns
                         ↓
            Retourne 400 Bad Request
                         ↓
    Afficher: "Impossible de supprimer: stagiaires actifs"
```

## 7. Gestion des Erreurs dans les Composants

### Pattern Recommandé

```typescript
import { useState } from 'react';
import { useApiError } from '../hooks/useApiError';

function MyComponent() {
  const { error, isLoading, handleApiCall, clearError } = useApiError();

  const loadData = async () => {
    await handleApiCall(
      // API call
      () => myService.getData(),

      // Success callback
      (data) => {
        setData(data);
      },

      // Error callback (optional)
      (error) => {
        if (error.status === 404) {
          // Gestion spécifique pour 404
          navigate('/not-found');
        }
      }
    );
  };

  return (
    <div>
      {isLoading && <LoadingSpinner />}

      {error && (
        <ErrorMessage
          message={error}
          onRetry={loadData}
          onDismiss={clearError}
        />
      )}

      {/* Content */}
    </div>
  );
}
```

## 8. Optimistic Updates avec Rollback

### Pattern pour les Modifications

```typescript
const updateTask = async (taskId: number, newStatus: string) => {
  // Sauvegarder l'état actuel
  const previousTasks = [...tasks];

  // Mise à jour optimiste
  setTasks(tasks.map(task =>
    task.id === taskId ? { ...task, status: newStatus } : task
  ));

  try {
    // Appel API
    const updated = await taskService.updateTask(taskId, { status: newStatus });

    // Confirmer avec les données du serveur
    setTasks(tasks.map(task =>
      task.id === updated.id ? updated : task
    ));
  } catch (error) {
    // Rollback en cas d'erreur
    setTasks(previousTasks);
    alert('Erreur lors de la mise à jour: ' + error.message);
  }
};
```

## 9. Messages d'Erreur Conviviaux

### Mapping des Erreurs Techniques

```typescript
const getErrorMessage = (error: any): string => {
  const errorMap: { [key: number]: string } = {
    400: 'Données invalides',
    401: 'Session expirée',
    403: 'Accès refusé',
    404: 'Ressource introuvable',
    409: 'Conflit de données',
    500: 'Erreur serveur',
    503: 'Service temporairement indisponible'
  };

  return errorMap[error.status] || error.message || 'Une erreur est survenue';
};
```

## 10. Tests des Scénarios d'Erreur

### Checklist de Test

- [ ] Login avec email inexistant
- [ ] Login avec mot de passe incorrect
- [ ] Token expiré (attendre 24h ou modifier manuellement)
- [ ] Backend arrêté
- [ ] Formulaire incomplet
- [ ] Email invalide
- [ ] Mot de passe trop court
- [ ] Mots de passe différents
- [ ] Email déjà utilisé
- [ ] Ressource non trouvée
- [ ] Dates invalides
- [ ] Suppression avec dépendances
- [ ] Accès sans permission
- [ ] Modification simultanée

### Comment Simuler

```typescript
// Simuler une erreur 500
mockService.getData = () => {
  throw { message: 'Erreur serveur', status: 500 };
};

// Simuler un timeout
mockService.getData = () => {
  return new Promise((resolve) => {
    setTimeout(() => resolve([]), 31000);
  });
};

// Simuler un token expiré
localStorage.setItem('auth_token', 'expired_token');
```

## Résumé

L'application gère de manière robuste:
✅ Erreurs d'authentification
✅ Erreurs de réseau
✅ Erreurs de permission
✅ Erreurs de validation
✅ Erreurs de données
✅ Erreurs métier
✅ Optimistic updates avec rollback
✅ Messages conviviaux
✅ Retry automatique si possible
