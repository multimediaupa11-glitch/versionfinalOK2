# Exemples d'Implémentation

Ce document présente des exemples concrets de remplacement des données mockées par des appels API réels.

## Exemple 1: Dashboard avec API

### Avant (avec données mockées)

```typescript
import { mockDashboardData } from '../../data/mockData';

function Dashboard() {
  const [data, setData] = useState(mockDashboardData);

  // Utilisation directe des données mockées
  return <div>{data.totalInterns}</div>;
}
```

### Après (avec API)

```typescript
import { useState, useEffect } from 'react';
import { dashboardService } from '../../services/dashboardService';
import { useApiError } from '../../hooks/useApiError';

function Dashboard() {
  const [data, setData] = useState(null);
  const { error, isLoading, handleApiCall } = useApiError();

  useEffect(() => {
    loadDashboardData();
  }, []);

  const loadDashboardData = async () => {
    await handleApiCall(
      () => dashboardService.getDashboardData(),
      (result) => setData(result)
    );
  };

  if (isLoading) {
    return <div>Chargement...</div>;
  }

  if (error) {
    return <div>Erreur: {error}</div>;
  }

  if (!data) {
    return null;
  }

  return (
    <div>
      <h1>Total Stagiaires: {data.metrics.totalInterns}</h1>
      <h2>Projets Actifs: {data.metrics.activeProjects}</h2>
    </div>
  );
}
```

## Exemple 2: Liste des Stagiaires

### Avant (avec données mockées)

```typescript
import { mockInterns } from '../../data/mockData';

function Interns() {
  const [interns, setInterns] = useState(mockInterns);

  return (
    <div>
      {interns.map(intern => (
        <div key={intern.id}>{intern.name}</div>
      ))}
    </div>
  );
}
```

### Après (avec API)

```typescript
import { useState, useEffect } from 'react';
import { internService, InternDTO } from '../../services/internService';
import { useApiError } from '../../hooks/useApiError';
import { useAuth } from '../../contexts/AuthContext';

function Interns() {
  const [interns, setInterns] = useState<InternDTO[]>([]);
  const { error, isLoading, handleApiCall } = useApiError();
  const { authUser } = useAuth();

  useEffect(() => {
    loadInterns();
  }, []);

  const loadInterns = async () => {
    if (authUser?.role === 'ROLE_ADMIN') {
      // Admin voit tous les stagiaires
      await handleApiCall(
        () => internService.getAllInterns(),
        (data) => setInterns(data)
      );
    } else if (authUser?.role === 'ROLE_ENCADREUR') {
      // Encadreur voit ses stagiaires
      const encadreurId = parseInt(authUser.profile.id || '0');
      await handleApiCall(
        () => internService.getInternsByEncadreur(encadreurId),
        (data) => setInterns(data)
      );
    }
  };

  const handleDelete = async (id: number) => {
    if (!confirm('Voulez-vous vraiment supprimer ce stagiaire?')) {
      return;
    }

    await handleApiCall(
      () => internService.deleteIntern(id),
      () => {
        // Recharger la liste après suppression
        loadInterns();
        alert('Stagiaire supprimé avec succès');
      }
    );
  };

  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-orange-500"></div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="bg-red-50 border border-red-200 rounded-lg p-4">
        <p className="text-red-600">Erreur: {error}</p>
        <button
          onClick={loadInterns}
          className="mt-2 text-red-600 underline"
        >
          Réessayer
        </button>
      </div>
    );
  }

  return (
    <div>
      <h2>Stagiaires ({interns.length})</h2>
      {interns.map(intern => (
        <div key={intern.id} className="border p-4 rounded-lg">
          <h3>{intern.firstName} {intern.lastName}</h3>
          <p>Email: {intern.email}</p>
          <p>Encadreur: {intern.encadreurName}</p>
          <p>Département: {intern.department}</p>
          <p>Statut: {intern.status}</p>
          <button onClick={() => handleDelete(intern.id)}>
            Supprimer
          </button>
        </div>
      ))}
    </div>
  );
}
```

## Exemple 3: Création d'un Projet

### Avant (avec données mockées)

```typescript
function ProjectForm() {
  const handleSubmit = (e) => {
    e.preventDefault();
    const newProject = {
      id: Math.random().toString(),
      title,
      description,
      // ...
    };
    console.log('Projet créé localement:', newProject);
  };
}
```

### Après (avec API)

```typescript
import { useState } from 'react';
import { projectService } from '../../services/projectService';
import { useApiError } from '../../hooks/useApiError';

function ProjectForm({ onSuccess }: { onSuccess: () => void }) {
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [internId, setInternId] = useState('');
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');

  const { error, isLoading, handleApiCall, clearError } = useApiError();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    clearError();

    await handleApiCall(
      () => projectService.createProject({
        title,
        description,
        internId: parseInt(internId),
        startDate,
        endDate
      }),
      (newProject) => {
        alert('Projet créé avec succès!');
        // Réinitialiser le formulaire
        setTitle('');
        setDescription('');
        setInternId('');
        setStartDate('');
        setEndDate('');
        // Notifier le parent
        onSuccess();
      }
    );
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      <div>
        <label>Titre</label>
        <input
          type="text"
          value={title}
          onChange={(e) => setTitle(e.target.value)}
          required
        />
      </div>

      <div>
        <label>Description</label>
        <textarea
          value={description}
          onChange={(e) => setDescription(e.target.value)}
          required
        />
      </div>

      <div>
        <label>Stagiaire ID</label>
        <input
          type="number"
          value={internId}
          onChange={(e) => setInternId(e.target.value)}
          required
        />
      </div>

      <div>
        <label>Date de début</label>
        <input
          type="date"
          value={startDate}
          onChange={(e) => setStartDate(e.target.value)}
          required
        />
      </div>

      <div>
        <label>Date de fin</label>
        <input
          type="date"
          value={endDate}
          onChange={(e) => setEndDate(e.target.value)}
          required
        />
      </div>

      {error && (
        <div className="bg-red-50 border border-red-200 p-3 rounded">
          <p className="text-red-600">{error}</p>
        </div>
      )}

      <button
        type="submit"
        disabled={isLoading}
        className="bg-orange-500 text-white px-6 py-2 rounded-lg disabled:opacity-50"
      >
        {isLoading ? 'Création...' : 'Créer le projet'}
      </button>
    </form>
  );
}
```

## Exemple 4: Mise à jour d'une Tâche (Kanban)

### Avant (avec données mockées)

```typescript
function Kanban() {
  const updateTaskStatus = (taskId: string, newStatus: string) => {
    setTasks(tasks.map(task =>
      task.id === taskId ? { ...task, status: newStatus } : task
    ));
  };
}
```

### Après (avec API)

```typescript
import { taskService } from '../../services/taskService';
import { useApiError } from '../../hooks/useApiError';

function Kanban() {
  const [tasks, setTasks] = useState<TaskDTO[]>([]);
  const { handleApiCall } = useApiError();

  const updateTaskStatus = async (taskId: number, newStatus: string) => {
    // Optimistic update (mise à jour immédiate de l'UI)
    setTasks(prevTasks =>
      prevTasks.map(task =>
        task.id === taskId ? { ...task, status: newStatus } : task
      )
    );

    // Mise à jour côté serveur
    await handleApiCall(
      () => taskService.updateTask(taskId, { status: newStatus }),
      (updatedTask) => {
        // Confirmer avec les données du serveur
        setTasks(prevTasks =>
          prevTasks.map(task =>
            task.id === updatedTask.id ? updatedTask : task
          )
        );
      },
      (error) => {
        // En cas d'erreur, annuler l'optimistic update
        alert('Erreur lors de la mise à jour: ' + error.message);
        loadTasks(); // Recharger les données
      }
    );
  };

  const loadTasks = async () => {
    await handleApiCall(
      () => taskService.getAllTasks(),
      (data) => setTasks(data)
    );
  };

  useEffect(() => {
    loadTasks();
  }, []);

  return (
    <div>
      {/* Interface Kanban avec drag & drop */}
    </div>
  );
}
```

## Exemple 5: Gestion des Permissions par Rôle

```typescript
import { useAuth } from '../../contexts/AuthContext';

function Component() {
  const { authUser } = useAuth();

  const canCreateIntern = () => {
    return authUser?.role === 'ROLE_ADMIN' ||
           authUser?.role === 'ROLE_ENCADREUR';
  };

  const canDeleteIntern = () => {
    return authUser?.role === 'ROLE_ADMIN';
  };

  const canViewAllProjects = () => {
    return authUser?.role === 'ROLE_ADMIN';
  };

  return (
    <div>
      {canCreateIntern() && (
        <button onClick={handleCreateIntern}>
          Créer un stagiaire
        </button>
      )}

      {canDeleteIntern() && (
        <button onClick={handleDeleteIntern}>
          Supprimer
        </button>
      )}

      {canViewAllProjects() ? (
        <AllProjectsList />
      ) : (
        <MyProjectsList />
      )}
    </div>
  );
}
```

## Exemple 6: Actualisation Automatique

```typescript
import { useState, useEffect } from 'react';
import { dashboardService } from '../../services/dashboardService';

function Dashboard() {
  const [data, setData] = useState(null);

  const loadData = async () => {
    const result = await dashboardService.getDashboardData();
    setData(result);
  };

  useEffect(() => {
    // Charger au montage
    loadData();

    // Actualiser toutes les 30 secondes
    const interval = setInterval(loadData, 30000);

    // Nettoyer l'interval au démontage
    return () => clearInterval(interval);
  }, []);

  return <div>{/* Interface */}</div>;
}
```

## Exemple 7: Recherche avec Debounce

```typescript
import { useState, useEffect } from 'react';
import { internService } from '../../services/internService';

function SearchInterns() {
  const [search, setSearch] = useState('');
  const [results, setResults] = useState([]);

  useEffect(() => {
    // Debounce: attendre 500ms après la dernière saisie
    const timer = setTimeout(async () => {
      if (search.length >= 3) {
        const allInterns = await internService.getAllInterns();
        const filtered = allInterns.filter(intern =>
          intern.firstName.toLowerCase().includes(search.toLowerCase()) ||
          intern.lastName.toLowerCase().includes(search.toLowerCase()) ||
          intern.email.toLowerCase().includes(search.toLowerCase())
        );
        setResults(filtered);
      } else {
        setResults([]);
      }
    }, 500);

    return () => clearTimeout(timer);
  }, [search]);

  return (
    <div>
      <input
        type="text"
        placeholder="Rechercher..."
        value={search}
        onChange={(e) => setSearch(e.target.value)}
      />
      <div>
        {results.map(intern => (
          <div key={intern.id}>{intern.firstName} {intern.lastName}</div>
        ))}
      </div>
    </div>
  );
}
```

## Bonnes Pratiques

1. **Toujours gérer les états de chargement**
   - Afficher un spinner pendant les requêtes
   - Désactiver les boutons pendant les soumissions

2. **Gérer les erreurs de manière conviviale**
   - Afficher des messages d'erreur clairs
   - Proposer des actions de récupération (réessayer)

3. **Valider les données avant l'envoi**
   - Validation côté client pour l'UX
   - Toujours valider côté serveur

4. **Optimistic Updates**
   - Mettre à jour l'UI immédiatement
   - Confirmer avec la réponse du serveur
   - Annuler en cas d'erreur

5. **Permissions et sécurité**
   - Vérifier les permissions côté client (UX)
   - Toujours vérifier côté serveur (sécurité)

6. **Feedback utilisateur**
   - Toasts pour les succès
   - Alertes pour les erreurs
   - Confirmations pour les actions destructives
