import { createContext, useContext, useEffect, useState } from 'react';
import { AuthUser } from '../types/auth';
import { authService, AuthResponse } from '../services/authService';
import { apiService } from '../services/api';

interface AuthContextType {
  user: { id: string; email: string } | null;
  authUser: AuthUser | null;
  loading: boolean;
  signIn: (email: string, password: string) => Promise<void>;
  signOut: () => Promise<void>;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

const AUTH_STORAGE_KEY = 'auth_user';

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<{ id: string; email: string } | null>(null);
  const [authUser, setAuthUser] = useState<AuthUser | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const storedUser = localStorage.getItem(AUTH_STORAGE_KEY);
    const token = apiService.getToken();

    if (storedUser && token) {
      try {
        const userData = JSON.parse(storedUser);
        setUser({ id: userData.id.toString(), email: userData.email });
        setAuthUser({
          profile: {
            firstName: userData.firstName,
            lastName: userData.lastName,
            email: userData.email,
            avatar: `https://ui-avatars.com/api/?name=${userData.firstName}+${userData.lastName}&background=random`,
          },
          role: userData.role,
        });
      } catch (error) {
        console.error('Erreur lors du chargement des données utilisateur:', error);
        localStorage.removeItem(AUTH_STORAGE_KEY);
        apiService.setToken(null);
      }
    }
    setLoading(false);
  }, []);

  const signIn = async (email: string, password: string) => {
    try {
      const response: AuthResponse = await authService.login({ email, password });

      const userAuth = {
        id: response.user.id.toString(),
        email: response.user.email
      };

      setUser(userAuth);
      setAuthUser({
        profile: {
          firstName: response.user.firstName,
          lastName: response.user.lastName,
          email: response.user.email,
          avatar: `https://ui-avatars.com/api/?name=${response.user.firstName}+${response.user.lastName}&background=random`,
        },
        role: response.user.role,
      });

      localStorage.setItem(AUTH_STORAGE_KEY, JSON.stringify(response.user));
    } catch (error: any) {
      throw new Error(error.message || 'Email ou mot de passe incorrect');
    }
  };

  const signOut = async () => {
    setUser(null);
    setAuthUser(null);
    authService.logout();
    localStorage.removeItem(AUTH_STORAGE_KEY);
  };

  return (
    <AuthContext.Provider value={{ user, authUser, loading, signIn, signOut }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}
