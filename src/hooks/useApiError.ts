import { useState, useCallback } from 'react';

interface ApiError {
  message: string;
  status?: number;
}

export const useApiError = () => {
  const [error, setError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);

  const handleApiCall = useCallback(async <T>(
    apiCall: () => Promise<T>,
    onSuccess?: (data: T) => void,
    onError?: (error: ApiError) => void
  ): Promise<T | null> => {
    setError(null);
    setIsLoading(true);

    try {
      const result = await apiCall();
      setIsLoading(false);
      if (onSuccess) {
        onSuccess(result);
      }
      return result;
    } catch (err: any) {
      const apiError: ApiError = {
        message: err.message || 'Une erreur est survenue',
        status: err.status
      };

      setError(apiError.message);
      setIsLoading(false);

      if (onError) {
        onError(apiError);
      }

      return null;
    }
  }, []);

  const clearError = useCallback(() => {
    setError(null);
  }, []);

  return {
    error,
    isLoading,
    handleApiCall,
    clearError
  };
};
