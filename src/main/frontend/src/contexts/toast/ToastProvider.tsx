import React, { useState, useCallback } from 'react';
import type { ReactNode } from 'react';
import { ToastContext, type Toast, type ToastContextType } from './ToastContext';

interface ToastProviderProps {
  children: ReactNode;
  maxToasts?: number;
  defaultDuration?: number;
}

export const ToastProvider: React.FC<ToastProviderProps> = ({
  children,
  maxToasts = 5,
  defaultDuration = 5000,
}) => {
  const [toasts, setToasts] = useState<Toast[]>([]);

  const generateId = useCallback(() => {
    return `toast-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`;
  }, []);

  const removeToast = useCallback((id: string) => {
    setToasts(prev => prev.filter(toast => toast.id !== id));
  }, []);

  const addToast = useCallback(
    (toast: Omit<Toast, 'id'>) => {
      const id = generateId();
      const newToast: Toast = {
        ...toast,
        id,
        duration: toast.duration ?? defaultDuration,
      };

      setToasts(prev => {
        const updated = [newToast, ...prev];
        // Limit the number of toasts
        return updated.slice(0, maxToasts);
      });

      // Auto-remove toast after duration
      if (newToast.duration && newToast.duration > 0) {
        setTimeout(() => {
          removeToast(id);
        }, newToast.duration);
      }

      return id;
    },
    [generateId, defaultDuration, maxToasts, removeToast]
  );

  const clearAllToasts = useCallback(() => {
    setToasts([]);
  }, []);

  const success = useCallback(
    (message: string, options?: Partial<Omit<Toast, 'id' | 'type' | 'message'>>) => {
      return addToast({
        message,
        type: 'success',
        ...options,
      });
    },
    [addToast]
  );

  const error = useCallback(
    (message: string, options?: Partial<Omit<Toast, 'id' | 'type' | 'message'>>) => {
      return addToast({
        message,
        type: 'error',
        duration: 0, // Error toasts don't auto-dismiss by default
        ...options,
      });
    },
    [addToast]
  );

  const warning = useCallback(
    (message: string, options?: Partial<Omit<Toast, 'id' | 'type' | 'message'>>) => {
      return addToast({
        message,
        type: 'warning',
        ...options,
      });
    },
    [addToast]
  );

  const info = useCallback(
    (message: string, options?: Partial<Omit<Toast, 'id' | 'type' | 'message'>>) => {
      return addToast({
        message,
        type: 'info',
        ...options,
      });
    },
    [addToast]
  );

  const value: ToastContextType = {
    toasts,
    addToast,
    removeToast,
    clearAllToasts,
    success,
    error,
    warning,
    info,
  };

  return <ToastContext.Provider value={value}>{children}</ToastContext.Provider>;
};
