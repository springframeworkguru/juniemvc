/**
 * Environment variable utility functions
 *
 * This module provides type-safe access to environment variables.
 * All environment variables should be accessed through these functions
 * to ensure consistent handling and type safety.
 */

/**
 * Get the current environment (development, test, production)
 */
export const getEnvironment = (): string => {
  return import.meta.env.VITE_APP_ENV || 'development';
};

/**
 * Check if the current environment is development
 */
export const isDevelopment = (): boolean => {
  return getEnvironment() === 'development';
};

/**
 * Check if the current environment is test
 */
export const isTest = (): boolean => {
  return getEnvironment() === 'test';
};

/**
 * Check if the current environment is production
 */
export const isProduction = (): boolean => {
  return getEnvironment() === 'production';
};

/**
 * Get the application name
 */
export const getAppName = (): string => {
  return import.meta.env.VITE_APP_NAME || 'Beer Service';
};

/**
 * Get the application version
 */
export const getAppVersion = (): string => {
  return import.meta.env.VITE_APP_VERSION || '0.1.0';
};

/**
 * Get the API base URL
 */
export const getApiBaseUrl = (): string => {
  // Service paths already include `/api/v1/...`. Base must not also be `/api`
  // or requests become `/api/api/v1/...`.
  const base = import.meta.env.VITE_API_BASE_URL;
  if (base === undefined || base === null || base === '') {
    return '';
  }
  return base;
};

/**
 * Get the API timeout in milliseconds
 */
export const getApiTimeout = (): number => {
  return parseInt(import.meta.env.VITE_API_TIMEOUT || '10000', 10);
};

/**
 * Check if mock API is enabled
 */
export const isMockApiEnabled = (): boolean => {
  return import.meta.env.VITE_ENABLE_MOCK_API === 'true';
};

/**
 * Check if debug mode is enabled
 */
export const isDebugEnabled = (): boolean => {
  return import.meta.env.VITE_ENABLE_DEBUG === 'true';
};
